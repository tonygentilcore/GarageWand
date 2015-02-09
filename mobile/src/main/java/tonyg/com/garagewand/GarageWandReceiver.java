package tonyg.com.garagewand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GarageWandReceiver extends BroadcastReceiver {

    public static final String TAG = "GarageWand";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Intent newIntent = new Intent(context, GarageWandService.class);
        // TODO(tonyg): Stop server appropriately.
        context.startService(newIntent);
    }
} 