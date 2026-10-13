package com.iessf.nocuelguesapp.presentation;

import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.application.ExpirationService;
import com.iessf.nocuelguesapp.data.ExpirationItemRepository;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
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

    /** Duración del balanceo del logo; tiene que coincidir con splash_logo_animated.xml. */
    private static final long SPLASH_ANIMATION_MS = 1400;

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
        keepSplashUntilAnimationEnds(splashScreen);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
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
     * Android saca la pantalla de carga apenas la app está lista, aunque la animación no haya terminado.
     * Solo se espera desde Android 12, que es donde el logo se anima; antes se muestra quieto.
     */
    private static void keepSplashUntilAnimationEnds(SplashScreen splashScreen) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }
        long start = SystemClock.uptimeMillis();
        splashScreen.setKeepOnScreenCondition(
                () -> SystemClock.uptimeMillis() - start < SPLASH_ANIMATION_MS);
    }

    private void openForm(@Nullable String itemId) {
        Intent intent = new Intent(this, FormActivity.class);
        if (itemId != null) {
            intent.putExtra(FormActivity.EXTRA_ITEM_ID, itemId);
        }
        startActivity(intent);
    }
}
