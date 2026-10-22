package com.iessf.nocuelguesapp.presentation;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.application.ExpirationService;
import com.iessf.nocuelguesapp.data.ExpirationItemRepository;
import com.iessf.nocuelguesapp.domain.model.Category;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.domain.model.Urgency;
import com.iessf.nocuelguesapp.notifications.ReminderScheduler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

/**
 * Da de alta un vencimiento o edita uno existente.
 * Sin {@link #EXTRA_ITEM_ID} funciona en modo alta.
 */
public class FormActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "expiration_item_id";
    private static final String STATE_SELECTED_DATE = "selected_date";
    private static final String STATE_SELECTED_CATEGORY = "selected_category";
    private static final String STATE_REMINDER_HOUR = "reminder_hour";
    private static final String STATE_REMINDER_MINUTE = "reminder_minute";
    private static final Category DEFAULT_CATEGORY = Category.OTHER;

    private ExpirationService service;
    private ReminderScheduler reminderScheduler;

    /** Se pide el permiso de notificar después de guardar; al responder, se reprograma y se cierra. */
    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                reminderScheduler.rescheduleAll();
                showSavedMessage();
                finish();
            });

    private String itemId;
    private LocalDate selectedDate;
    private int nameMaxLength;
    private int noteMaxLength;
    private int reminderHour = ExpirationItem.DEFAULT_REMINDER_HOUR;
    private int reminderMinute = ExpirationItem.DEFAULT_REMINDER_MINUTE;

    private EditText inputName;
    private TextView textNameCounter;
    private TextView textNameError;
    private TextView inputDate;
    private TextView textDaysLeft;
    private TextView textDateError;
    private ChipGroup chipGroupCategory;
    private RadioGroup radioGroupUrgency;
    private EditText inputNote;
    private TextView textNoteCounter;
    private CheckBox inputReminderEnabled;
    private EditText inputReminderDays;
    private TextView inputReminderTime;
    private TextView textReminderError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        service = new ExpirationService(new ExpirationItemRepository(this));
        reminderScheduler = new ReminderScheduler(this);
        itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        nameMaxLength = getResources().getInteger(R.integer.name_max_length);
        noteMaxLength = getResources().getInteger(R.integer.note_max_length);

        inputName = findViewById(R.id.input_name);
        textNameCounter = findViewById(R.id.text_name_counter);
        textNameError = findViewById(R.id.text_name_error);
        inputDate = findViewById(R.id.input_date);
        textDaysLeft = findViewById(R.id.text_days_left);
        textDateError = findViewById(R.id.text_date_error);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        radioGroupUrgency = findViewById(R.id.radio_group_urgency);
        inputNote = findViewById(R.id.input_note);
        textNoteCounter = findViewById(R.id.text_note_counter);

        inputReminderEnabled = findViewById(R.id.input_reminder_enabled);
        inputReminderDays = findViewById(R.id.input_reminder_days);
        inputReminderTime = findViewById(R.id.input_reminder_time);
        textReminderError = findViewById(R.id.text_reminder_error);
        inputReminderDays.setText(String.valueOf(ExpirationItem.DEFAULT_REMINDER_DAYS_BEFORE));
        inputReminderEnabled.setOnCheckedChangeListener((button, checked) -> showReminderFields());
        inputReminderTime.setOnClickListener(v -> new TimePickerDialog(this,
                (picker, hour, minute) -> {
                    reminderHour = hour;
                    reminderMinute = minute;
                    showReminderFields();
                }, reminderHour, reminderMinute, DateFormat.is24HourFormat(this)).show());
        findViewById(R.id.button_notification_settings).setOnClickListener(v -> {
            Intent settings = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
            settings.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            settings.putExtra(Settings.EXTRA_CHANNEL_ID, ReminderScheduler.CHANNEL_ID);
            startActivity(settings);
        });

        Button buttonSave = findViewById(R.id.button_save);
        centerIconWithText(buttonSave);
        centerIconWithText(findViewById(R.id.radio_urgency_high));
        centerIconWithText(findViewById(R.id.radio_urgency_medium));
        centerIconWithText(findViewById(R.id.radio_urgency_low));

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        inputDate.setOnClickListener(v -> openDatePicker());
        buttonSave.setOnClickListener(v -> saveItem());
        inputName.addTextChangedListener(new SimpleTextWatcher(() -> {
            updateNameCounter();
            showNameError(false);
        }));
        inputNote.addTextChangedListener(new SimpleTextWatcher(this::updateNoteCounter));

        addCategoryChips();
        selectCategory(DEFAULT_CATEGORY);

        if (itemId != null) {
            ExpirationItem item = service.findById(itemId);
            if (item == null) {
                Toast.makeText(this, R.string.form_item_missing, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            showEditMode(item);
            if (savedInstanceState == null) {
                fillFields(item);
            }
        }
        if (savedInstanceState != null) {
            reminderHour = savedInstanceState.getInt(STATE_REMINDER_HOUR, reminderHour);
            reminderMinute = savedInstanceState.getInt(STATE_REMINDER_MINUTE, reminderMinute);
            String savedDate = savedInstanceState.getString(STATE_SELECTED_DATE);
            if (savedDate != null) {
                selectedDate = LocalDate.parse(savedDate);
            }
            selectCategory(Category.valueOf(savedInstanceState.getString(STATE_SELECTED_CATEGORY)));
        }
        updateNameCounter();
        updateNoteCounter();
        showDate();
        showReminderFields();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDate();
        showReminderFields();
        reminderScheduler.rescheduleAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_REMINDER_HOUR, reminderHour);
        outState.putInt(STATE_REMINDER_MINUTE, reminderMinute);
        if (selectedDate != null) {
            outState.putString(STATE_SELECTED_DATE, selectedDate.toString());
        }
        // Los chips se crean por código, así que Android no puede restaurar solo cuál estaba marcado.
        outState.putString(STATE_SELECTED_CATEGORY, getSelectedCategory().name());
    }

    private void showEditMode(ExpirationItem item) {
        TextView textItemName = findViewById(R.id.text_item_name);
        textItemName.setText(item.getName());
        textItemName.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.text_title)).setText(R.string.form_title_edit);
        ((Button) findViewById(R.id.button_save)).setText(R.string.action_save_changes);

        View buttonDelete = findViewById(R.id.button_delete);
        buttonDelete.setVisibility(View.VISIBLE);
        buttonDelete.setOnClickListener(v -> confirmDelete(item.getName()));
    }

    private void confirmDelete(String itemName) {
        View content = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
        ((TextView) content.findViewById(R.id.text_dialog_title))
                .setText(getString(R.string.dialog_delete_title, itemName));

        AlertDialog dialog = new AlertDialog.Builder(this).setView(content).create();
        content.findViewById(R.id.button_cancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.button_confirm_delete).setOnClickListener(v -> {
            dialog.dismiss();
            reminderScheduler.delete(itemId);
            service.delete(itemId);
            finish();
        });
        dialog.show();
        // Solo debe verse el fondo propio del layout (borde y sombra dura), no el de la ventana.
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void fillFields(ExpirationItem item) {
        inputReminderEnabled.setChecked(item.isReminderEnabled());
        inputReminderDays.setText(String.valueOf(item.getReminderDaysBefore()));
        reminderHour = item.getReminderHour();
        reminderMinute = item.getReminderMinute();
        inputName.setText(item.getName());
        inputNote.setText(item.getNote());
        selectedDate = item.getExpirationDate();
        selectCategory(item.getCategory());
        radioGroupUrgency.check(urgencyToRadioId(item.getUrgency()));
    }

    private void openDatePicker() {
        LocalDate initial = selectedDate != null ? selectedDate : LocalDate.now();
        new DatePickerDialog(this, (picker, year, month, day) -> {
            // DatePicker numera los meses desde 0; LocalDate, desde 1.
            selectedDate = LocalDate.of(year, month + 1, day);
            showDateError(false);
            showDate();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void saveItem() {
        String name = inputName.getText().toString().trim();
        boolean nameMissing = name.isEmpty();
        boolean dateMissing = selectedDate == null;
        showNameError(nameMissing);
        showDateError(dateMissing);
        if (nameMissing) {
            inputName.requestFocus();
            return;
        }
        if (dateMissing) {
            inputDate.requestFocus();
            return;
        }
        if (itemId != null && service.findById(itemId) == null) {
            Toast.makeText(this, R.string.form_item_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Category category = getSelectedCategory();
        Urgency urgency = radioIdToUrgency(radioGroupUrgency.getCheckedRadioButtonId());
        String note = inputNote.getText().toString().trim();
        ExpirationItem item = itemId == null
                ? new ExpirationItem(name, selectedDate, category, urgency, note)
                : new ExpirationItem(itemId, name, selectedDate, category, urgency, note);
        int daysBefore;
        try {
            daysBefore = Integer.parseInt(inputReminderDays.getText().toString().trim());
        } catch (NumberFormatException ignored) {
            daysBefore = -1;
        }
        if (daysBefore < 0 || daysBefore > ExpirationItem.MAX_REMINDER_DAYS_BEFORE) {
            if (inputReminderEnabled.isChecked()) {
                showReminderError(R.string.reminder_invalid_days);
                inputReminderDays.requestFocus();
                return;
            }
            // Con el aviso apagado, el valor del campo no importa: se guarda el de por defecto.
            daysBefore = ExpirationItem.DEFAULT_REMINDER_DAYS_BEFORE;
        }
        item.setReminder(inputReminderEnabled.isChecked(), daysBefore, reminderHour, reminderMinute);
        if (item.isReminderEnabled() && item.isOverdue(LocalDate.now())) {
            showReminderError(R.string.reminder_expired);
            return;
        }
        textReminderError.setVisibility(View.GONE);
        reminderScheduler.cancel(item.getId());
        service.save(item);
        reminderScheduler.schedule(item);
        if (item.isReminderEnabled() && Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        showSavedMessage();
        finish();
    }

    private void showSavedMessage() {
        int message = inputReminderEnabled.isChecked() && !reminderScheduler.canNotify()
                ? R.string.reminder_saved_blocked : R.string.form_saved;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showReminderFields() {
        boolean enabled = inputReminderEnabled.isChecked();
        findViewById(R.id.layout_reminder_options).setVisibility(enabled ? View.VISIBLE : View.GONE);
        inputReminderTime.setText(LocalTime.of(reminderHour, reminderMinute)
                .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
        findViewById(R.id.button_notification_settings)
                .setVisibility(enabled && !reminderScheduler.canNotify() ? View.VISIBLE : View.GONE);
    }

    private void showReminderError(int message) {
        textReminderError.setText(message);
        textReminderError.setVisibility(View.VISIBLE);
    }

    private void showDate() {
        if (selectedDate == null) {
            return;
        }
        inputDate.setText(selectedDate.format(
                DateTimeFormatter.ofPattern(getString(R.string.date_pattern))));

        int days = (int) ChronoUnit.DAYS.between(LocalDate.now(), selectedDate);
        if (days > 0) {
            textDaysLeft.setText(getResources().getQuantityString(R.plurals.form_days_left, days, days));
        } else if (days == 0) {
            textDaysLeft.setText(R.string.form_due_today);
        } else {
            textDaysLeft.setText(getResources().getQuantityString(R.plurals.form_days_overdue, -days, -days));
        }

        boolean overdue = days < 0;
        int textColor = ContextCompat.getColor(this, overdue ? R.color.brand : R.color.ink);
        textDaysLeft.setBackgroundResource(overdue ? R.drawable.bg_pill_overdue : R.drawable.bg_pill);
        textDaysLeft.setTextColor(textColor);
        TextViewCompat.setCompoundDrawableTintList(textDaysLeft, ColorStateList.valueOf(textColor));
        textDaysLeft.setVisibility(View.VISIBLE);
    }

    private void addCategoryChips() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Category category : Category.values()) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_category, chipGroupCategory, false);
            chip.setId(View.generateViewId());
            chip.setTag(category);
            chip.setText(ExpirationResources.categoryLabel(category));
            chip.setChipIconResource(ExpirationResources.categoryIcon(category));
            chipGroupCategory.addView(chip);
        }
    }

    private void selectCategory(Category category) {
        chipGroupCategory.check(chipGroupCategory.findViewWithTag(category).getId());
    }

    private Category getSelectedCategory() {
        View chip = chipGroupCategory.findViewById(chipGroupCategory.getCheckedChipId());
        return (Category) chip.getTag();
    }

    private void updateNameCounter() {
        textNameCounter.setText(getString(R.string.field_counter,
                inputName.length(), nameMaxLength));
    }

    private void updateNoteCounter() {
        textNoteCounter.setText(getString(R.string.field_note_optional)
                + getString(R.string.item_date_separator)
                + getString(R.string.field_counter, inputNote.length(), noteMaxLength));
    }

    private void showNameError(boolean visible) {
        inputName.setBackgroundResource(visible ? R.drawable.bg_input_error : R.drawable.bg_input);
        textNameError.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showDateError(boolean visible) {
        inputDate.setBackgroundResource(visible ? R.drawable.bg_input_error : R.drawable.bg_input);
        textDateError.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Un TextView ancho (Button, RadioButton) dibuja el ícono inicial pegado al borde izquierdo,
     * lejos del texto centrado. Esto los mantiene juntos en el medio recalculando el padding lateral.
     */
    private static void centerIconWithText(TextView view) {
        int shadowOffset = view.getPaddingEnd() - view.getPaddingStart();
        view.addOnLayoutChangeListener((v, left, top, right, bottom,
                                        oldLeft, oldTop, oldRight, oldBottom) -> {
            Drawable icon = view.getCompoundDrawablesRelative()[0];
            int iconWidth = icon == null ? 0 : icon.getIntrinsicWidth() + view.getCompoundDrawablePadding();
            float textWidth = view.getPaint().measureText(view.getText().toString());
            int sidePadding = Math.max(0, (int) ((view.getWidth() - shadowOffset - iconWidth - textWidth) / 2));
            view.setPaddingRelative(sidePadding, view.getPaddingTop(),
                    sidePadding + shadowOffset, view.getPaddingBottom());
        });
    }

    private static int urgencyToRadioId(Urgency urgency) {
        switch (urgency) {
            case HIGH:
                return R.id.radio_urgency_high;
            case LOW:
                return R.id.radio_urgency_low;
            default:
                return R.id.radio_urgency_medium;
        }
    }

    private static Urgency radioIdToUrgency(int radioId) {
        if (radioId == R.id.radio_urgency_high) {
            return Urgency.HIGH;
        }
        if (radioId == R.id.radio_urgency_low) {
            return Urgency.LOW;
        }
        return Urgency.MEDIUM;
    }

    /** TextWatcher que solo avisa que el texto cambió. */
    private static final class SimpleTextWatcher implements TextWatcher {

        private final Runnable onChanged;

        SimpleTextWatcher(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onChanged.run();
        }
    }
}
