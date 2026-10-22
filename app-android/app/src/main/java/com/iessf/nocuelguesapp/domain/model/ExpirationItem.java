package com.iessf.nocuelguesapp.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;

/**
 * Documento u obligación con fecha de vencimiento.
 */
public class ExpirationItem {

    /**
     * Orden del listado: primero el vencimiento más cercano; a igual fecha, la urgencia mayor;
     * después, alfabético por nombre.
     */
    public static final Comparator<ExpirationItem> DISPLAY_ORDER =
            Comparator.comparing(ExpirationItem::getExpirationDate)
                    .thenComparing(ExpirationItem::getUrgency)
                    .thenComparing(ExpirationItem::getName, String.CASE_INSENSITIVE_ORDER);

    private final String id;
    private String name;
    private LocalDate expirationDate;
    private Category category;
    private Urgency urgency;
    private String note;
    private boolean reminderEnabled;
    private int reminderDaysBefore = 1;
    private int reminderHour = 9;
    private int reminderMinute;


    /** Crea un ítem nuevo con un identificador generado en el momento. */
    public ExpirationItem(String name, LocalDate expirationDate, Category category,
                          Urgency urgency, String note) {
        this(UUID.randomUUID().toString(), name, expirationDate, category, urgency, note);
    }

    /** Reconstruye un ítem existente, conservando su identificador. */
    public ExpirationItem(String id, String name, LocalDate expirationDate, Category category,
                          Urgency urgency, String note) {
        this.id = id;
        this.name = name;
        this.expirationDate = expirationDate;
        this.category = category;
        this.urgency = urgency;
        this.note = note == null ? "" : note;
    }

    /** Días desde {@code today} hasta el vencimiento; negativo si ya venció. */
    public int getDaysUntilExpiration(LocalDate today) {
        return (int) ChronoUnit.DAYS.between(today, expirationDate);
    }

    public boolean isOverdue(LocalDate today) {
        return expirationDate.isBefore(today);
    }

    public boolean hasNote() {
        return !note.isEmpty();
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public int getReminderDaysBefore() {
        return reminderDaysBefore;
    }

    public int getReminderHour() {
        return reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }

    public void setReminder(boolean enabled, int daysBefore, int hour, int minute) {
        if (daysBefore < 0 || daysBefore > 365 || hour < 0 || hour > 23
                || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid reminder time");
        }
        reminderEnabled = enabled;
        reminderDaysBefore = daysBefore;
        reminderHour = hour;
        reminderMinute = minute;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? "" : note;
    }
}
