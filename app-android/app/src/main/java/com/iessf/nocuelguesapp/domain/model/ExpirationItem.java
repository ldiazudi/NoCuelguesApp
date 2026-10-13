package com.iessf.nocuelguesapp.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;

/**
 * A document or obligation with an expiration date.
 */
public class ExpirationItem {

    /**
     * List order: closest expiration first; on the same date, higher urgency first;
     * then alphabetical by name.
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

    /** Creates a new item with a freshly generated id. */
    public ExpirationItem(String name, LocalDate expirationDate, Category category,
                          Urgency urgency, String note) {
        this(UUID.randomUUID().toString(), name, expirationDate, category, urgency, note);
    }

    /** Rebuilds an existing item, keeping its id. */
    public ExpirationItem(String id, String name, LocalDate expirationDate, Category category,
                          Urgency urgency, String note) {
        this.id = id;
        this.name = name;
        this.expirationDate = expirationDate;
        this.category = category;
        this.urgency = urgency;
        this.note = note == null ? "" : note;
    }

    /** Days from {@code today} to the expiration date; negative when already expired. */
    public int getDaysUntilExpiration(LocalDate today) {
        return (int) ChronoUnit.DAYS.between(today, expirationDate);
    }

    public boolean isOverdue(LocalDate today) {
        return expirationDate.isBefore(today);
    }

    public boolean hasNote() {
        return !note.isEmpty();
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
