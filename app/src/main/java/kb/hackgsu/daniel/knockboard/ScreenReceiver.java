package kb.hackgsu.daniel.knockboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Daniel on 3/26/2016.
 */
public class ScreenReceiver extends BroadcastReceiver {

    private boolean screenOff;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
        }
        Intent i = new Intent(context, KnockLockBackground.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);
    }
}
