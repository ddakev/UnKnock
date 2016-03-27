package kb.hackgsu.daniel.knockboard;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.PowerManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class KnockLockBackground extends IntentService {

    private boolean isScreenOn;

    public KnockLockBackground() {
        super("KnockLockBackground");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        isScreenOn=true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        boolean screenOff = intent.getBooleanExtra("screen_state", false);
        if (screenOff) {
            isScreenOn=false;
        } else {
            if(isScreenOn==false)
            {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Intent mainIntent = new Intent(KnockLockBackground.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                            }
                        },
                        100);
                try {Thread.sleep(100);}
                catch (InterruptedException e) {}

                this.stopSelf();
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean screenOff = intent.getBooleanExtra("screen_state", false);
        if (screenOff) {

        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }
}
