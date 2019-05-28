package mobilly;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;
import java.util.Random;



import static android.support.v4.app.NotificationCompat.BADGE_ICON_SMALL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("mobilly", "onMessageReceived");

        String title = "";
        String text = "";
        String id = "";
        String sound = "";
        String lights = "";

        Map<String, String> data = remoteMessage.getData();

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            text = remoteMessage.getNotification().getBody();
            id = remoteMessage.getMessageId();
        } else if (data != null) {
            title = data.get("title");
            text = data.get("text");
            id = data.get("id");
            sound = data.get("sound");
            lights = data.get("lights"); //String containing hex ARGB color, miliseconds on, miliseconds off, example: '#FFFF00FF,1000,3000'
            if (TextUtils.isEmpty(text))
                text = data.get("body");
        }

        if (TextUtils.isEmpty(id)) {
            Random rand = new Random();
            int n = rand.nextInt(50) + 1;
            id = Integer.toString(n);
        }

        data.put("collapse_key", remoteMessage.getCollapseKey());
        data.put("from", remoteMessage.getFrom());
        data.put("google.original_priority", remoteMessage.getOriginalPriority() == 1 ? "high" : "normal");
        data.put("google.delivered_priority", remoteMessage.getPriority() == 1 ? "high" : "normal");
        data.put("google.ttl", String.valueOf(remoteMessage.getTtl()));
        data.put("google.sent_time", String.valueOf(remoteMessage.getSentTime()));
        data.put("notification.title", title);
        data.put("notification.text", text);

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        MobillyFirebase.sendNotification(bundle, this);

        if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title) || (data != null && !data.isEmpty())) {
            sendNotification(id, title, text, data, true, sound, lights);
        }

    }

    private String getStringResource(String name) {
        return this.getString(
                this.getResources().getIdentifier(
                        name, "string", this.getPackageName()
                )
        );
    }


    private void sendNotification(String id, String title, String messageBody, Map<String, String> data, boolean showNotification, String sound, String lights) {
        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
            bundle.putString(key, data.get(key));
        }
        bundle.putString("google.message_id", id);
        bundle.putString("google.message_id", id);

        Intent intent = new Intent(this, OnNotificationOpenReceiver.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = this.getStringResource("default_notification_channel_id");
        String channelName = this.getStringResource("default_notification_channel_name");
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);



        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        notificationBuilder
                .setContentTitle(title)
                .setContentText(messageBody)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setBadgeIconType(BADGE_ICON_SMALL)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        int resID = getResources().getIdentifier("ic_notification", "drawable", getPackageName());
        if (resID != 0) {
            Log.d("mobilly", "using resID");
            notificationBuilder.setSmallIcon(resID);
        } else {
            Log.d("mobilly", "using getApplicationInfo");
            notificationBuilder.setSmallIcon(getApplicationInfo().icon);
        }
        notificationBuilder.setColorized(true);
        notificationBuilder.setColor(getResources().getColor(getResources().getIdentifier("colorAccent", "color", getPackageName())));
        if (sound != null) {
            Log.d(TAG, "sound before path is: " + sound);
            Uri soundPath = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/" + sound);
            Log.d(TAG, "Parsed sound is: " + soundPath.toString());
            notificationBuilder.setSound(soundPath);
        } else {
            Log.d(TAG, "Sound was null ");
        }

        int lightArgb = 0;
        if (lights != null) {
            try {
                String[] lightsComponents = lights.replaceAll("\\s", "").split(",");
                if (lightsComponents.length == 3) {
                    lightArgb = Color.parseColor(lightsComponents[0]);
                    int lightOnMs = Integer.parseInt(lightsComponents[1]);
                    int lightOffMs = Integer.parseInt(lightsComponents[2]);

                    notificationBuilder.setLights(lightArgb, lightOnMs, lightOffMs);
                }
            } catch (Exception e) {
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int accentID = getResources().getIdentifier("colorAccent", "color", getPackageName());
            notificationBuilder.setColor(getResources().getColor(accentID, null));
        }

        Notification notification = notificationBuilder.build();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int iconID = android.R.id.icon;
            int notiID = getResources().getIdentifier("ic_notification", "drawable", getPackageName());
            if (notification.contentView != null) {
                notification.contentView.setImageViewResource(iconID, notiID);
            }
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            List<NotificationChannel> channels = notificationManager.getNotificationChannels();

            boolean channelExists = false;
            for (int i = 0; i < channels.size(); i++) {
                if (channelId.equals(channels.get(i).getId())) {
                    channelExists = true;
                }
            }

            if (!channelExists) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                if (lights != null) {
                    channel.setLightColor(lightArgb);
                }
                notificationManager.createNotificationChannel(channel);
            }
        }

        notificationManager.notify(id.hashCode(), notification);

    }

}
