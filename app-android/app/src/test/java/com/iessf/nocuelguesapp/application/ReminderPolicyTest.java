package com.iessf.nocuelguesapp.application;

import com.iessf.nocuelguesapp.domain.model.Category;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.domain.model.Urgency;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class ReminderPolicyTest {
    private ExpirationItem item() {
        ExpirationItem item = new ExpirationItem("Seguro", LocalDate.of(2026, 11, 1),
                Category.INSURANCE, Urgency.MEDIUM, "");
        item.setReminder(true, 3, 9, 30);
        return item;
    }

    @Test
    public void subtractsDaysAcrossMonthBoundaryAndKeepsChosenTime() {
        assertEquals(LocalDateTime.of(2026, 10, 29, 9, 30), ReminderPolicy.getReminderTime(item()));
    }

    @Test
    public void waitsUntilChosenMinute() {
        ExpirationItem item = item();
        LocalDateTime trigger = ReminderPolicy.getReminderTime(item);
        assertFalse(ReminderPolicy.isDue(item, trigger.minusSeconds(1), ""));
        assertTrue(ReminderPolicy.isDue(item, trigger, ""));
    }

    @Test
    public void recoversMissedReminderButNeverNotifiesAnExpiredItem() {
        ExpirationItem item = item();
        assertTrue(ReminderPolicy.isDue(item, LocalDateTime.of(2026, 11, 1, 23, 59), ""));
        assertFalse(ReminderPolicy.isPending(item, LocalDateTime.of(2026, 11, 2, 0, 0), ""));
    }

    @Test
    public void deliveredReminderDoesNotRepeatAfterReopeningOrRebooting() {
        ExpirationItem item = item();
        String delivered = ReminderPolicy.getReminderTime(item).toString();
        assertFalse(ReminderPolicy.isPending(item, LocalDateTime.of(2026, 10, 30, 12, 0), delivered));
    }

    @Test
    public void changingTheTimeSchedulesANewReminder() {
        ExpirationItem item = item();
        String delivered = ReminderPolicy.getReminderTime(item).toString();
        item.setReminder(true, 1, 14, 15);
        assertTrue(ReminderPolicy.isPending(item, LocalDateTime.of(2026, 10, 30, 12, 0), delivered));
        assertFalse(ReminderPolicy.isDue(item, LocalDateTime.of(2026, 10, 30, 12, 0), delivered));
    }

    @Test
    public void disablingReminderPreventsDelivery() {
        ExpirationItem item = item();
        item.setReminder(false, 3, 9, 30);
        assertFalse(ReminderPolicy.isPending(item, LocalDateTime.of(2026, 10, 30, 12, 0), ""));
    }

    @Test
    public void zeroDaysMeansExpirationDay() {
        ExpirationItem item = item();
        item.setReminder(true, 0, 18, 5);
        assertEquals(LocalDateTime.of(2026, 11, 1, 18, 5), ReminderPolicy.getReminderTime(item));
    }

    @Test
    public void oldItemsHaveRemindersDisabled() {
        ExpirationItem old = new ExpirationItem("Viejo", LocalDate.of(2026, 11, 1),
                Category.OTHER, Urgency.LOW, "");
        assertFalse(old.isReminderEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeLeadTime() {
        item().setReminder(true, -1, 9, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidHour() {
        item().setReminder(true, 1, 24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidMinute() {
        item().setReminder(true, 1, 9, 60);
    }
}
