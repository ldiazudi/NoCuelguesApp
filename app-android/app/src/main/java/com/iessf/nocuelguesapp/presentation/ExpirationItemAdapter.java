package com.iessf.nocuelguesapp.presentation;

import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Muestra cada vencimiento en una tarjeta definida en item_expiration.xml.
 */
public class ExpirationItemAdapter extends RecyclerView.Adapter<ExpirationItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ExpirationItem item);
    }

    private final OnItemClickListener listener;
    private final List<ExpirationItem> items = new ArrayList<>();
    private LocalDate today = LocalDate.now();

    public ExpirationItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Reemplaza la lista completa; {@code today} es la fecha de referencia para los días restantes. */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<ExpirationItem> newItems, LocalDate today) {
        this.today = today;
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expiration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpirationItem item = items.get(position);
        holder.bind(item, today);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutDays;
        private final TextView textDaysNumber;
        private final TextView textDaysUnit;
        private final TextView textName;
        private final TextView textUrgency;
        private final TextView textCategory;
        private final TextView textDate;
        private final TextView textNote;
        private final DateTimeFormatter dateFormatter;
        private final int categoryIconSize;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDays = itemView.findViewById(R.id.layout_days);
            textDaysNumber = itemView.findViewById(R.id.text_days_number);
            textDaysUnit = itemView.findViewById(R.id.text_days_unit);
            textName = itemView.findViewById(R.id.text_name);
            textUrgency = itemView.findViewById(R.id.text_urgency);
            textCategory = itemView.findViewById(R.id.text_category);
            textDate = itemView.findViewById(R.id.text_date);
            textNote = itemView.findViewById(R.id.text_note);

            Context context = itemView.getContext();
            dateFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.date_pattern));
            categoryIconSize = context.getResources().getDimensionPixelSize(R.dimen.icon_sm);
        }

        void bind(ExpirationItem item, LocalDate today) {
            Context context = itemView.getContext();
            Resources resources = context.getResources();
            int days = item.getDaysUntilExpiration(today);
            boolean overdue = days < 0;
            String formattedDate = item.getExpirationDate().format(dateFormatter);

            String daysUnit;
            String dateLine;
            if (overdue) {
                int daysAgo = -days;
                daysUnit = context.getString(R.string.item_unit_overdue);
                dateLine = context.getString(R.string.item_expired_on, formattedDate)
                        + context.getString(R.string.item_date_separator)
                        + resources.getQuantityString(R.plurals.item_expired_ago, daysAgo, daysAgo);
            } else if (days == 0) {
                daysUnit = context.getString(R.string.item_unit_today);
                dateLine = context.getString(R.string.item_expires_today);
            } else {
                daysUnit = resources.getQuantityString(R.plurals.item_days_unit, days);
                dateLine = context.getString(R.string.item_expires_on, formattedDate);
            }

            textDaysNumber.setText(String.valueOf(Math.abs(days)));
            textDaysUnit.setText(daysUnit);
            textDate.setText(dateLine);
            textName.setText(item.getName());

            textUrgency.setText(ExpirationResources.urgencyLabel(item.getUrgency()));
            textUrgency.setBackgroundResource(ExpirationResources.urgencyBackground(item.getUrgency()));

            textCategory.setText(ExpirationResources.categoryLabel(item.getCategory()));
            Drawable categoryIcon = ContextCompat.getDrawable(context, ExpirationResources.categoryIcon(item.getCategory()));
            if (categoryIcon != null) {
                categoryIcon.setBounds(0, 0, categoryIconSize, categoryIconSize);
            }
            textCategory.setCompoundDrawablesRelative(categoryIcon, null, null, null);

            textNote.setText(item.getNote());
            textNote.setVisibility(item.hasNote() ? View.VISIBLE : View.GONE);

            itemView.setBackgroundResource(overdue ? R.drawable.bg_card_overdue : R.drawable.bg_card);
            layoutDays.setBackgroundResource(
                    overdue ? R.drawable.bg_days_block_overdue : R.drawable.bg_days_block);
            int daysColor = ContextCompat.getColor(context, overdue ? R.color.brand : R.color.ink);
            textDaysNumber.setTextColor(daysColor);
            textDaysUnit.setTextColor(daysColor);

            String daysDescription = overdue || days == 0
                    ? daysUnit
                    : days + " " + daysUnit;
            itemView.setContentDescription(context.getString(R.string.item_content_description,
                    item.getName(), daysDescription, context.getString(ExpirationResources.urgencyLabel(item.getUrgency()))));
        }
    }
}
