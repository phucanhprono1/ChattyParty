package com.example.chattyparty;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.reactivex.rxjava3.annotations.Nullable;

public class ForegroundNotificationService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            // Create the notification for the foreground service
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "foreground_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setAutoCancel(true);

            // Start the service in the foreground
            startForeground(1, builder.build());
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
