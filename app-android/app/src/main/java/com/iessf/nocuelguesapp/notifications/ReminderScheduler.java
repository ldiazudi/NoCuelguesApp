package com.iessf.nocuelguesapp.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.core.app.NotificationManagerCompat;
import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.application.ExpirationService;
import com.iessf.nocuelguesapp.application.ReminderPolicy;
import com.iessf.nocuelguesapp.data.ExpirationItemRepository;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Programa un único aviso por vencimiento usando las alarmas de Android. */
public final class ReminderScheduler {
    public static final String CHANNEL_ID = "expiration_reminders";
    public static final String EXTRA_ITEM_ID = "reminder_item_id";
    private final Context context;
    private final AlarmManager alarms;
    private final SharedPreferences delivered;

    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        alarms = this.context.getSystemService(AlarmManager.class);
        delivered = this.context.getSharedPreferences("delivered_reminders", Context.MODE_PRIVATE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT);
        this.context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    public boolean canNotify() {
        NotificationChannel channel = context.getSystemService(NotificationManager.class)
                .getNotificationChannel(CHANNEL_ID);
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
                && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    public String getDeliveredTime(String id) {
        return delivered.getString(id, "");
    }

    public void markDelivered(ExpirationItem item) {
        delivered.edit().putString(item.getId(), ReminderPolicy.getReminderTime(item).toString()).apply();
    }

    public void schedule(ExpirationItem item) {
        PendingIntent alarm = alarmIntent(item.getId());
        alarms.cancel(alarm);
        if (!canNotify() || !ReminderPolicy.isPending(item, LocalDateTime.now(),
                getDeliveredTime(item.getId()))) {
            return;
        }
        long trigger = ReminderPolicy.getReminderTime(item).atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        // Si el teléfono estaba apagado, recuperar el aviso mientras el ítem no haya vencido.
        trigger = Math.max(trigger, System.currentTimeMillis());
        // No requiere permiso de alarmas exactas; Android puede demorar el aviso por batería.
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, alarm);
    }

    public void rescheduleAll() {
        ExpirationService service = new ExpirationService(new ExpirationItemRepository(context));
        for (ExpirationItem item : service.getAll()) {
            schedule(item);
        }
    }

    public void cancel(String id) {
        PendingIntent alarm = alarmIntent(id);
        alarms.cancel(alarm);
        alarm.cancel();
        context.getSystemService(NotificationManager.class).cancel(id, 0);
    }

    public void delete(String id) {
        cancel(id);
        delivered.edit().remove(id).apply();
    }

    private PendingIntent alarmIntent(String id) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        // La URI usa el ID completo para que dos ítems nunca compartan la misma alarma.
        intent.setData(Uri.parse("nocuelgues://reminder/" + Uri.encode(id)));
        intent.putExtra(EXTRA_ITEM_ID, id);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
