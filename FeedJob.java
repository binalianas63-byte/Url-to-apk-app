package com.webtoapk.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FeedJob extends JobService {
    @Override
    public boolean onStartJob(final JobParameters p) {
        new Thread(() -> {
            try { check(getApplicationContext()); } catch (Exception ignored) { }
            jobFinished(p, false);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters p) { return true; }

    static String read(InputStream in, int max) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] b = new byte[2048];
        int r, total = 0;
        while ((r = in.read(b)) > 0 && total < max) { o.write(b, 0, r); total += r; }
        in.close();
        return o.toString("UTF-8");
    }

    static void check(Context c) throws Exception {
        String cfgTxt = read(c.getAssets().open("config.json"), 20000);
        String feed = new JSONObject(cfgTxt).optString("feed", "");
        if (!feed.startsWith("http")) return;
        HttpURLConnection h = (HttpURLConnection) new URL(feed).openConnection();
        h.setConnectTimeout(15000);
        h.setReadTimeout(15000);
        JSONObject j = new JSONObject(read(h.getInputStream(), 20000));
        String id = j.optString("id", "");
        if (id.isEmpty()) return;
        SharedPreferences sp = c.getSharedPreferences("feed", Context.MODE_PRIVATE);
        String last = sp.getString("last", null);
        sp.edit().putString("last", id).apply();
        if (last != null && !last.equals(id)) show(c, j.optString("title", ""), j.optString("body", ""), j.optString("url", ""));
    }

    static void show(Context c, String title, String body, String url) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            nm.createNotificationChannel(new NotificationChannel("updates", "Updates", NotificationManager.IMPORTANCE_DEFAULT));
        Intent i = new Intent(c, MainActivity.class);
        i.putExtra("url", url == null ? "" : url);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int fl = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        int nid = (int) (System.currentTimeMillis() % 100000);
        PendingIntent pi = PendingIntent.getActivity(c, nid, i, fl);
        String t = (title == null || title.isEmpty()) ? c.getApplicationInfo().loadLabel(c.getPackageManager()).toString() : title;
        String b = body == null ? "" : body;
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, "updates") : new Notification.Builder(c);
        nb.setSmallIcon(c.getApplicationInfo().icon).setContentTitle(t).setContentText(b)
          .setStyle(new Notification.BigTextStyle().bigText(b)).setContentIntent(pi).setAutoCancel(true);
        nm.notify(nid, nb.build());
    }
}
