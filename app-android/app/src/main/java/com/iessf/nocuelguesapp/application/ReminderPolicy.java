package com.iessf.nocuelguesapp.application;

import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import java.time.LocalDateTime;

/** Cálculo del aviso, sin depender de Android. */
public final class ReminderPolicy {
    private ReminderPolicy() {
    }

    public static LocalDateTime getReminderTime(ExpirationItem item) {
        return item.getExpirationDate().minusDays(item.getReminderDaysBefore())
                .atTime(item.getReminderHour(), item.getReminderMinute());
    }

    public static boolean isPending(ExpirationItem item, LocalDateTime now, String deliveredTime) {
        return item.isReminderEnabled() && !item.isOverdue(now.toLocalDate())
                && !getReminderTime(item).toString().equals(deliveredTime);
    }

    public static boolean isDue(ExpirationItem item, LocalDateTime now, String deliveredTime) {
        return isPending(item, now, deliveredTime) && !getReminderTime(item).isAfter(now);
    }
}
