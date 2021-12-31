package io.agora.fcm; /**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.agora.openvcall.AGApplication;
import io.agora.openvcall.R;
import io.agora.openvcall.ui.CallActivity;
import io.agora.openvcall.ui.GridViewActivity;
import io.agora.propeller.Constant;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 * <p>
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 * <p>
 * <intent-filter>
 * <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.appointed);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
        r.play();

        String clickAction = "";
        String content = "";

        clickAction = remoteMessage.getData().get("click_action");
        content = remoteMessage.getData().get("message");
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (clickAction != null) {

                switch (clickAction) {

                    case "request":

                        Intent inviteRequestIntent;
                        if (Constant.IS_CONFERENCE_GOING) {
                            inviteRequestIntent = new Intent(this, CallActivity.class);
                            inviteRequestIntent.setAction(Constant.CALL_REQUEST);
                            inviteRequestIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            inviteRequestIntent.putExtra("requestedBy", remoteMessage.getData().get("requested_by"));
                            inviteRequestIntent.putExtra("messageBody", remoteMessage.getData().get("message"));
                            inviteRequestIntent.putExtra("requestedIMEI", remoteMessage.getData().get("imei"));
                            inviteRequestIntent.putExtra("channel_name", remoteMessage.getData().get("channel_name"));
                            inviteRequestIntent.putExtra("access_token", remoteMessage.getData().get("access_token"));
                            inviteRequestIntent.putExtra("device_id", remoteMessage.getData().get("device_id"));
                            startActivity(inviteRequestIntent);

                        } else {

                            inviteRequestIntent = new Intent(Constant.CALL_REQUEST);
                            inviteRequestIntent.putExtra("message", remoteMessage.getData().get("message"));
                            sendBroadcast(inviteRequestIntent);
                            sendNotificationForRequest(content, remoteMessage);
                        }


                        break;

                    case "yes":

                        Intent yesIntent;
                        if (Constant.IS_CONFERENCE_GOING) {
                            yesIntent = new Intent(Constant.CALL_YES);
                            yesIntent.putExtra("message", remoteMessage.getData().get("message"));
                            sendBroadcast(yesIntent);
                            sendNotificationForYes(content, remoteMessage);


                        }else if (AGApplication.isAppIsInBackground(this)) {
                            wakeUpScreen();
                            System.out.println("yes app is in background on yes click");
                            yesIntent = new Intent(Constant.CALL_YES);
                            yesIntent.putExtra("message", remoteMessage.getData().get("message"));
                            sendBroadcast(yesIntent);
                            sendNotificationForYes(content, remoteMessage);
                        } else {

                            yesIntent = new Intent(this, CallActivity.class);
                            yesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (remoteMessage.getData().get("click_action").equals("yes")) {
                                yesIntent.setAction(Constant.CALL_YES);
                            }
                            yesIntent.putExtra("requestedIMEI", remoteMessage.getData().get("imei"));
                            yesIntent.putExtra("channel_name", remoteMessage.getData().get("channel_name"));
                            yesIntent.putExtra("access_token", remoteMessage.getData().get("access_token"));
                            yesIntent.putExtra("device_id", remoteMessage.getData().get("device_id"));
                            startActivity(yesIntent);
                        }

                        break;

                    case "no":

                        Intent noIntent = new Intent(Constant.CALL_REQUEST);
                        noIntent.putExtra("message", remoteMessage.getData().get("message"));
                        sendBroadcast(noIntent);
                        sendNotification(content);

                        break;

                    case "invite_sent":

                        Intent inviteSentIntent;
                        if (Constant.IS_CONFERENCE_GOING) {
                            inviteSentIntent = new Intent(this, CallActivity.class);
                            inviteSentIntent.setAction(Constant.INVITE_SENT);
                            inviteSentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            inviteSentIntent.putExtra("messageBody", remoteMessage.getData().get("message"));
                            inviteSentIntent.putExtra("requestedIMEI", remoteMessage.getData().get("imei"));
                            inviteSentIntent.putExtra("channel_name", remoteMessage.getData().get("channel_name"));
                            inviteSentIntent.putExtra("access_token", remoteMessage.getData().get("access_token"));
                            inviteSentIntent.putExtra("device_id", remoteMessage.getData().get("device_id"));
                            startActivity(inviteSentIntent);

                        } else if (AGApplication.isAppIsInBackground(this)) {
                            wakeUpScreen();
                            System.out.println("yes app is in background");
                            inviteSentIntent = new Intent(Constant.INVITE_SENT);
                            inviteSentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            inviteSentIntent.putExtra("message", remoteMessage.getData().get("message"));
                            sendBroadcast(inviteSentIntent);
                            sendNotificationForRequest(content, remoteMessage);
                        } else {

                            inviteSentIntent = new Intent(getApplicationContext(), GridViewActivity.class);
                            inviteSentIntent.setAction(Constant.INVITE_SENT);
                            inviteSentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            inviteSentIntent.putExtra("messageBody", remoteMessage.getData().get("message"));
                            inviteSentIntent.putExtra("requestedIMEI", remoteMessage.getData().get("imei"));
                            inviteSentIntent.putExtra("channel_name", remoteMessage.getData().get("channel_name"));
                            inviteSentIntent.putExtra("access_token", remoteMessage.getData().get("access_token"));
                            inviteSentIntent.putExtra("device_id", remoteMessage.getData().get("device_id"));
                            startActivity(inviteSentIntent);




                        }

                        break;

                    case "invite_accept":

                        Intent inviteAcceptIntent = new Intent(Constant.CALL_REQUEST);
                        inviteAcceptIntent.putExtra("message", remoteMessage.getData().get("message"));
                        sendBroadcast(inviteAcceptIntent);
                        sendActionNotification(content);

                        break;

                    case "invite_cancel":

                        Intent inviteCancelIntent = new Intent(Constant.CALL_REQUEST);
                        inviteCancelIntent.putExtra("message", remoteMessage.getData().get("message"));
                        sendBroadcast(inviteCancelIntent);
                        sendActionNotification(content);

                        break;

                    case "exit":


                        Intent exitIntent = new Intent(Constant.CALL_REQUEST);
                        exitIntent.putExtra("message", remoteMessage.getData().get("message"));
                        sendBroadcast(exitIntent);
                        sendActionNotification(content);


                        break;


                }

            } else {
                sendNotification(content);
            }

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotificationForRequest(String messageBody, RemoteMessage remoteMessage) {
        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.appointed);
        Intent intent;
        if (Constant.IS_CONFERENCE_GOING) {
            intent = new Intent(this, CallActivity.class);
        } else {
            intent = new Intent(this, GridViewActivity.class);
        }

        if (remoteMessage.getData().get("click_action").equals("request")) {
            intent.setAction(Constant.CALL_REQUEST);
        } else if (remoteMessage.getData().get("click_action").equals("invite_sent")) {
            intent.setAction(Constant.INVITE_SENT);
        }
        intent.putExtra("messageBody", messageBody);
        intent.putExtra("requestedIMEI", remoteMessage.getData().get("imei"));
        intent.putExtra("channel_name", remoteMessage.getData().get("channel_name"));
        intent.putExtra("access_token", remoteMessage.getData().get("access_token"));
        intent.putExtra("device_id", remoteMessage.getData().get("device_id"));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] v = {500, 1000};
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_icon))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(v)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody))
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String CHANNEL_ID = getString(R.string.default_notification_channel_id);// The id of the channel.
            String name = getString(R.string.default_notification_channel_id);// The user-visible name of the channel.
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setShowBadge(false);
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setSound(soundUri, attributes);

            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }


    private void sendNotificationForYes(String messageBody, RemoteMessage remoteMessage) {

        Intent intent = new Intent(this, CallActivity.class);
        if (remoteMessage.getData().get("click_action").equals("yes")) {
            intent.setAction(Constant.CALL_YES);
        }
        intent.putExtra("requestedIMEI", remoteMessage.getData().get("imei"));
        intent.putExtra("channel_name", remoteMessage.getData().get("channel_name"));
        intent.putExtra("access_token", remoteMessage.getData().get("access_token"));
        intent.putExtra("device_id", remoteMessage.getData().get("device_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_icon))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.appointed))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String CHANNEL_ID = getString(R.string.default_notification_channel_id);// The id of the channel.
            String name = getString(R.string.default_notification_channel_id);// The user-visible name of the channel.
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, GridViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.appointed))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }


    private void sendActionNotification(String messageBody) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.appointed))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    /* when your phone is locked screen wakeup method*/
    private void wakeUpScreen() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        Log.e("screen on......", "" + isScreenOn);
        if (isScreenOn == false) {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
            wl_cpu.acquire(10000);
        }
    }
}