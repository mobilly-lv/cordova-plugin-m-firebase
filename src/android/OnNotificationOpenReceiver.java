package mobilly;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class OnNotificationOpenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("mobilly", "onReceive");
        PackageManager pm = context.getPackageManager();

        Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle data = intent.getExtras();
        data.putBoolean("tap", true);

        MobillyFirebase.sendNotification(data, context);

        launchIntent.putExtras(data);
        context.startActivity(launchIntent);
    }
}
