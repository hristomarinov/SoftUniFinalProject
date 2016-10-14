package marinov.hristo.softuniproject.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import marinov.hristo.softuniproject.R;
import marinov.hristo.softuniproject.main.ItemNews;

/**
 * @author HristoMarinov (christo_marinov@abv.bg).
 */
public class MyService extends Service {

    IBinder binder = new ServiceBinder();
    private NotificationManager mNM;
    private int NOTIFICATION = R.string.favourites;

    public class ServiceBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel the notification.
        mNM.cancel(NOTIFICATION);
    }

    /**
     * Show a notification while this service is running.
     */
    public void showNotification(String articleId, String title, String contentText) {

        // The PendingIntent to launch our activity if the user selects this notification
        Intent resultIntent = new Intent(this, ItemNews.class);
        resultIntent.putExtra("getID", articleId);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_heart)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(contentText)
                .setContentIntent(resultPendingIntent)
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}
