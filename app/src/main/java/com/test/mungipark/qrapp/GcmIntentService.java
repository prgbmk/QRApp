/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.mungipark.qrapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i = 0; i < 5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // 여기서 푸쉬 알람 메세지를 처리하고 작업한다.
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        //타이틀..여기서
        //메세지 부분에는 밑의 getTakeMedicineMessage메소드에서 처리.
        String commaTok[] = msg.split(",");
        String Title[] = commaTok[1].split("=");


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_stat_gcm)
        .setContentTitle(Title[1])
        .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(getTakeMedicineMessage(msg)))
        .setContentText(getTakeMedicineMessage(msg));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    //복약 알림 정보 해석하여 반환해주는 메소드
    private String getTakeMedicineMessage(String msg){

        String notiMessage = null;//실제로 넘겨줄 값
        //Comma대로
        String commaTok[] = msg.split(",");
        String Title[] = commaTok[1].split("=");
        String Message[] = commaTok[2].split("=");

        String verticalTok[] = Message[1].split("[|]");
        String dashTok[]=null;


        //복약 알림 메세지 해석해서 저장.
        for(int i = 0, k = 0; i<verticalTok.length; i++){
            dashTok = verticalTok[i].split("-");//(날짜 - 처방내용(알레르기약*30*7)) 분리.
            String enterTok[] = dashTok[1].split("\n");//복약 정보(알레르기약*30*7) 분리
            String starTok[] = null;
            for(int j=0; j<enterTok.length; j++){
                starTok = enterTok[j].split("[*]");//약, 수량, 타입이 분리되어 나눠짐.
                if(k == 0){
                    notiMessage = "<처방내용>\n" + (dashTok[0]  + starTok[0] + "은 "+ starTok[1] + "개 " + starTok[3] + "일치 남\n");
                }
                else{
                    notiMessage = notiMessage +  (dashTok[0]  + starTok[0] + "은 "+ starTok[1] + "개 " + starTok[3] + "일치 남음\n");
                }
                k++;
            }
        }
        return notiMessage;
    }


}
