package com.eaglesakura.lib.sample.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.eaglesakura.lib.android.game.R;
import com.eaglesakura.lib.android.game.thread.UIHandler;

public class RemoteViewSample extends Activity {

    NotificationManager nManager = null;
    RemoteViews remote = null;
    Notification notifi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        {
            nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifi = new Notification(R.drawable.ic_launcher, "notifi", System.currentTimeMillis());

            remote = new RemoteViews(getPackageName(), R.layout.notification);
            notifi.contentView = remote;
            nManager.notify(0, notifi);
        }

        for (int i = 0; i < 10; ++i) {
            final int number = i;
            UIHandler.postDelayedUI(new Runnable() {

                @Override
                public void run() {
                    remote.setProgressBar(R.id.notifi_progress, 10, (number + 1), false);
                    remote.setTextViewText(R.id.notifi_text_0, "文字を変更します :: " + number);
                    nManager.notify(0, notifi);
                }
            }, 1000 * (i + 1));
        }
    }
}
