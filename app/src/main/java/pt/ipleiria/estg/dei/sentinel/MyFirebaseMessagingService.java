package pt.ipleiria.estg.dei.sentinel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashSet;
import java.util.Set;

import pt.ipleiria.estg.dei.sentinel.activities.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "NOTIFICATION_EVENT";
    private SharedPreferences sharedPref;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /*GETS SHARED PREF*/
        sharedPref = getSharedPreferences(Constants.PREFERENCES_FILE_NAME,Context.MODE_PRIVATE);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        String message = remoteMessage.getNotification().getBody();
        sendNotification(message);
    }

    private void scheduleJob() {
    }

    private void handleNow() {
        Log.d(TAG, "Sentinel Alert Sent");
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo_white)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Sentinel Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
        saveNotification(messageBody);
    }


    private void saveNotification(String messageBody){
        Set<String> set = null;
        int counter = 0;
        try{
            SharedPreferences.Editor e = sharedPref.edit();

            /*GETS SAVE LIST IF EXISTS*/
            set = sharedPref.getStringSet(Constants.PREFERENCES_NOTIFICATIONS_SET,new HashSet<>());

            /*GETS UNREAD MESSAGE COUNTER*/
            counter = sharedPref.getInt(Constants.PREFERENCES_NOTIFICATIONS_UNREAD,0);

            counter++; //INCREASES COUNTER BECAUSE OF NEW MESSAGE
            set.add(messageBody+":1");

            /*SAVES LIST WITH NEW MESSAGE AND UNREAD MESSSAGES COUNTER*/

            e.putStringSet(Constants.PREFERENCES_NOTIFICATIONS_SET,set).commit();
            e.putInt(Constants.PREFERENCES_NOTIFICATIONS_UNREAD,counter).commit();





        }catch(Exception ex){
            Log.v("ERROR_SAVE_NOTIFICATION",ex.getMessage());
        }

    }
}
