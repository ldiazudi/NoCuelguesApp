package com.iessf.nocuelguesapp.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import com.iessf.nocuelguesapp.R;
import com.iessf.nocuelguesapp.application.ExpirationService;
import com.iessf.nocuelguesapp.application.ReminderPolicy;
import com.iessf.nocuelguesapp.data.ExpirationItemRepository;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.presentation.FormActivity;
import java.time.LocalDateTime;

/** Android llama a este receptor aunque la pantalla de la app esté cerrada. */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(ReminderScheduler.EXTRA_ITEM_ID);
        if (id == null) {
            return;
        }
        ExpirationService service = new ExpirationService(new ExpirationItemRepository(context));
        ExpirationItem item = service.findById(id);
        ReminderScheduler scheduler = new ReminderScheduler(context);
        if (item == null || !scheduler.canNotify()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!ReminderPolicy.isDue(item, now, scheduler.getDeliveredTime(id))) {
            scheduler.schedule(item);
            return;
        }

        Intent openItem = new Intent(context, FormActivity.class);
        openItem.setData(Uri.parse("nocuelgues://item/" + Uri.encode(id)));
        openItem.putExtra(FormActivity.EXTRA_ITEM_ID, id);
        PendingIntent open = TaskStackBuilder.create(context).addNextIntentWithParentStack(openItem)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        int days = item.getDaysUntilExpiration(now.toLocalDate());
        String message = days == 0 ? context.getString(R.string.form_due_today)
                : context.getResources().getQuantityString(R.plurals.form_days_left, days, days);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context,
                ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.getName())
                .setContentText(message)
                .setContentIntent(open)
                .setAutoCancel(true);
        try {
            context.getSystemService(NotificationManager.class).notify(id, 0, notification.build());
            scheduler.markDelivered(item);
        } catch (SecurityException ignored) {
            // El permiso puede revocarse entre la comprobación y la publicación.
        }
    }
}
