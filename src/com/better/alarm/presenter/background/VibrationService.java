package com.better.alarm.presenter.background;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.better.alarm.model.interfaces.Intents;
import com.github.androidutils.logger.Logger;
import com.github.androidutils.wakelock.WakeLockManager;

public class VibrationService extends Service {
    private static final long[] sVibratePattern = new long[] { 500, 500 };
    private Vibrator mVibrator;
    private Logger log;
    private Intent mIntent;

    /**
     * Dispatches intents to the KlaxonService
     */
    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            intent.setClass(context, VibrationService.class);
            WakeLockManager.getWakeLockManager().acquirePartialWakeLock(intent, "VibrationService");
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        log = Logger.getDefaultLogger();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onDestroy() {
        stopVibration();
        WakeLockManager.getWakeLockManager().releasePartialWakeLock(mIntent);
        log.d("Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIntent = intent;
        String action = intent.getAction();
        try {
            if (action.equals(Intents.ALARM_ALERT_ACTION)) {
                startVibrationIfShould();
                return START_STICKY;

            } else if (action.equals(Intents.ALARM_SNOOZE_ACTION)) {
                stopSelf();
                return START_NOT_STICKY;

            } else if (action.equals(Intents.ALARM_DISMISS_ACTION)) {
                stopSelf();
                return START_NOT_STICKY;

            } else {
                log.e("unexpected intent " + intent.getAction());
                stopSelf();
                return START_NOT_STICKY;
            }
        } catch (Exception e) {
            log.e("Something went wrong" + e.getMessage());
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void startVibrationIfShould() {
        boolean shouldVibrate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("vibrate", true);
        if (shouldVibrate) {
            mVibrator.vibrate(sVibratePattern, 0);
        }
    }

    private void stopVibration() {
        mVibrator.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
