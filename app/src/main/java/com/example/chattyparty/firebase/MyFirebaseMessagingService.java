package com.example.chattyparty.firebase;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.chattyparty.ChatActivity;
import com.example.chattyparty.ForegroundNotificationService;
import com.example.chattyparty.MainActivity;
import com.example.chattyparty.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        // Hiển thị thông báo
        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "foreground_channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, notificationBuilder.build());
    }
//    private void showNotification(String title, String message) {
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "foreground_channel_id")
//                .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // Hiển thị thông báo trong foreground
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Handle permission request
//                return;
//            }
//            notificationManager.notify(1, notificationBuilder.build());
//
//            // Hiển thị thông báo trong background
//            NotificationCompat.Builder backgroundNotificationBuilder = new NotificationCompat.Builder(this, "background_channel_id")
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setContentTitle(title)
//                    .setContentText(message)
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    .setContentIntent(pendingIntent)
//                    .setAutoCancel(true);
//            NotificationManagerCompat.from(this).notify(2, backgroundNotificationBuilder.build());
//        } else {
//            notificationManager.notify(1, notificationBuilder.build());
//        }
//    }
}

