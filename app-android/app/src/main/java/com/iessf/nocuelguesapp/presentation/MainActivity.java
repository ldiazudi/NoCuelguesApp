package com.iessf.nocuelguesapp.presentation;

import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.application.ExpirationService;
import com.iessf.nocuelguesapp.data.ExpirationItemRepository;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.notifications.ReminderScheduler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.LocalDate;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ExpirationService service;
    private ExpirationItemAdapter adapter;

    private View layoutStats;
    private View layoutSort;
    private View layoutEmpty;
    private RecyclerView listItems;
    private TextView textStatOverdue;
    private TextView textStatWeek;
    private TextView textStatTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tiene que ir antes de super.onCreate: muestra la pantalla de carga y después aplica el tema normal.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        service = new ExpirationService(new ExpirationItemRepository(this));

        layoutStats = findViewById(R.id.layout_stats);
        layoutSort = findViewById(R.id.layout_sort);
        layoutEmpty = findViewById(R.id.layout_empty);
        listItems = findViewById(R.id.list_items);
        textStatOverdue = findViewById(R.id.text_stat_overdue);
        textStatWeek = findViewById(R.id.text_stat_week);
        textStatTotal = findViewById(R.id.text_stat_total);

        adapter = new ExpirationItemAdapter(item -> openForm(item.getId()));
        listItems.setAdapter(adapter);

        findViewById(R.id.button_new).setOnClickListener(v -> openForm(null));

        addEmptyExamples();
        splashScreen.setOnExitAnimationListener(splashView -> {
            playSplash();
            splashView.remove();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
        new ReminderScheduler(this).rescheduleAll();
    }

    private void loadItems() {
        LocalDate today = LocalDate.now();
        List<ExpirationItem> items = service.getAll();

        adapter.setItems(items, today);
        updateStats(items, today);

        boolean isEmpty = items.isEmpty();
        listItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutStats.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutSort.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateStats(List<ExpirationItem> items, LocalDate today) {
        ExpirationService.Summary summary = service.summarize(items, today);
        textStatOverdue.setText(String.valueOf(summary.getOverdue()));
        textStatWeek.setText(String.valueOf(summary.getThisWeek()));
        textStatTotal.setText(String.valueOf(summary.getTotal()));
    }

    private void addEmptyExamples() {
        ChipGroup chipGroup = findViewById(R.id.chip_group_examples);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String example : getResources().getStringArray(R.array.empty_examples)) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_example, chipGroup, false);
            chip.setText(example);
            chipGroup.addView(chip);
        }
    }

    /**
     * Muestra el logo grande balanceándose y, al terminar, apaga la capa como un televisor viejo.
     * Se llama cuando el sistema retira su pantalla de carga, que es del mismo color: así la animación
     * se ve desde el principio. Si el sistema no la mostró (por ejemplo al rotar), no se llama.
     */
    private void playSplash() {
        View layoutSplash = findViewById(R.id.layout_splash);
        View layoutSplashContent = findViewById(R.id.layout_splash_content);
        ImageView imageLogo = findViewById(R.id.image_splash_logo);
        layoutSplash.setVisibility(View.VISIBLE);
        ((Animatable) imageLogo.getDrawable()).start();

        // El contenido se recorta con un círculo que se achica hasta el centro; afuera se ve el fondo negro.
        int centerX = layoutSplashContent.getWidth() / 2;
        int centerY = layoutSplashContent.getHeight() / 2;
        float fullRadius = (float) Math.hypot(centerX, centerY);
        Animator turnOff = ViewAnimationUtils.createCircularReveal(
                layoutSplashContent, centerX, centerY, fullRadius, 0f);
        turnOff.setStartDelay(getResources().getInteger(R.integer.splash_animation_duration));
        turnOff.setDuration(getResources().getInteger(R.integer.splash_close_duration));
        turnOff.setInterpolator(new AccelerateInterpolator());
        turnOff.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layoutSplashContent.setVisibility(View.INVISIBLE);
                hideSplash(layoutSplash);
            }
        });
        turnOff.start();
    }

    /** Deja un instante la pantalla en negro y después muestra la pantalla principal. */
    private void hideSplash(View layoutSplash) {
        int shortDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        layoutSplash.animate()
                .alpha(0f)
                .setStartDelay(shortDuration)
                .setDuration(shortDuration)
                .withEndAction(() -> layoutSplash.setVisibility(View.GONE));
    }

    private void openForm(@Nullable String itemId) {
        Intent intent = new Intent(this, FormActivity.class);
        if (itemId != null) {
            intent.putExtra(FormActivity.EXTRA_ITEM_ID, itemId);
        }
        startActivity(intent);
    }
}
