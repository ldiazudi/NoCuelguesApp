package com.iessf.nocuelguesapp.presentation;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.application.ExpirationService;
import com.iessf.nocuelguesapp.data.ExpirationItemRepository;
import com.iessf.nocuelguesapp.domain.model.Category;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.domain.model.Urgency;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Formulario existente, conectado al modelo y servicio del grupo. */
public class FormularioActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "expiration_item_id";
    private static final String FECHA = "fecha_seleccionada";
    private ExpirationService service;
    private String itemId;
    private LocalDate fechaSeleccionada;
    private Button btnFecha;
    private TextView txtDiasRestantes;
    private EditText edtNombre;
    private EditText edtNota;
    private Spinner categoria;
    private RadioGroup urgencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        service = new ExpirationService(new ExpirationItemRepository(this));
        itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        edtNombre = findViewById(R.id.edtNombre);
        edtNota = findViewById(R.id.edtNota);
        categoria = findViewById(R.id.spCategoria);
        urgencia = findViewById(R.id.rgUrgencia);
        btnFecha = findViewById(R.id.btnFecha);
        txtDiasRestantes = findViewById(R.id.txtDiasRestantes);
        findViewById(R.id.btnVolver).setOnClickListener(view -> finish());
        btnFecha.setOnClickListener(view -> abrirCalendario());
        findViewById(R.id.btnGuardar).setOnClickListener(view -> guardarVencimiento());

        if (itemId != null) {
            ExpirationItem item = service.findById(itemId);
            if (item == null) {
                Toast.makeText(this, R.string.form_item_missing, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            ((TextView) findViewById(R.id.txtTitulo)).setText(R.string.form_title_edit);
            ((Button) findViewById(R.id.btnGuardar)).setText(R.string.action_save_changes);
            if (savedInstanceState == null) {
                edtNombre.setText(item.getName());
                edtNota.setText(item.getNote());
                fechaSeleccionada = item.getExpirationDate();
                categoria.setSelection(item.getCategory().ordinal());
                urgencia.check(item.getUrgency() == Urgency.HIGH ? R.id.rbAlta
                        : item.getUrgency() == Urgency.LOW ? R.id.rbBaja : R.id.rbMedia);
            }
        }
        if (savedInstanceState != null) {
            String fecha = savedInstanceState.getString(FECHA);
            if (fecha != null) fechaSeleccionada = LocalDate.parse(fecha);
        }
        mostrarFecha();
    }

    private void abrirCalendario() {
        LocalDate inicial = fechaSeleccionada != null ? fechaSeleccionada : LocalDate.now();
        new DatePickerDialog(this, (selector, anio, mes, dia) -> {
            // Android numera los meses desde cero; LocalDate los numera desde uno.
            fechaSeleccionada = LocalDate.of(anio, mes + 1, dia);
            mostrarFecha();
        }, inicial.getYear(), inicial.getMonthValue() - 1, inicial.getDayOfMonth()).show();
    }

    private void guardarVencimiento() {
        String nombre = edtNombre.getText().toString().trim();
        edtNombre.setError(null);
        btnFecha.setError(null);
        boolean valido = true;
        if (nombre.isEmpty()) {
            edtNombre.setError(getString(R.string.error_name_required));
            edtNombre.requestFocus();
            valido = false;
        }
        if (fechaSeleccionada == null) {
            btnFecha.setError(getString(R.string.error_date_required));
            Toast.makeText(this, R.string.error_date_required, Toast.LENGTH_SHORT).show();
            valido = false;
        }
        if (!valido) return;
        if (itemId != null && service.findById(itemId) == null) {
            Toast.makeText(this, R.string.form_item_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Category category = Category.values()[categoria.getSelectedItemPosition()];
        int opcion = urgencia.getCheckedRadioButtonId();
        Urgency nivel = opcion == R.id.rbAlta ? Urgency.HIGH
                : opcion == R.id.rbBaja ? Urgency.LOW : Urgency.MEDIUM;
        String nota = edtNota.getText().toString().trim();
        ExpirationItem item = itemId == null
                ? new ExpirationItem(nombre, fechaSeleccionada, category, nivel, nota)
                : new ExpirationItem(itemId, nombre, fechaSeleccionada, category, nivel, nota);
        service.save(item);
        findViewById(R.id.btnGuardar).setEnabled(false);
        Toast.makeText(this, R.string.form_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarFecha() {
        if (fechaSeleccionada == null || btnFecha == null) return;
        btnFecha.setText(fechaSeleccionada.format(
                DateTimeFormatter.ofPattern(getString(R.string.date_pattern))));
        btnFecha.setError(null);
        int dias = (int) ChronoUnit.DAYS.between(LocalDate.now(), fechaSeleccionada);
        if (dias > 0) {
            txtDiasRestantes.setText(getResources().getQuantityString(R.plurals.form_days_left, dias, dias));
        } else if (dias == 0) {
            txtDiasRestantes.setText(R.string.form_due_today);
        } else {
            txtDiasRestantes.setText(getResources().getQuantityString(R.plurals.form_days_overdue, -dias, -dias));
        }
        txtDiasRestantes.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarFecha();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fechaSeleccionada != null) outState.putString(FECHA, fechaSeleccionada.toString());
    }
}
