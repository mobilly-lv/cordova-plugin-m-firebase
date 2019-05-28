package mobilly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Set;

public class MobillyFirebase extends CordovaPlugin {

    private static MobillyFirebase currentInstance = null;
    private static CallbackContext firebaseCallback = null;
//    private CallbackContext callbackContext;


    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.v("mobilly","onNewIntent");
        final Bundle data = intent.getExtras();
        if (data != null && data.containsKey("google.message_id")) {
            data.putBoolean("tap", true);
            MobillyFirebase.sendNotification(data, this.cordova.getActivity().getApplicationContext());
        }
    }

    public static void sendNotification(Bundle data, Context context) {
        if (data != null) {



            /*for (String key : data.keySet()) {
                Object value = data.get(key);
                Log.d("mobilly", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }*/



            MobillyFirebase.currentInstance.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    JSONObject json = new JSONObject();
                    Set<String> keys = data.keySet();



                    try {
                        for (String key : keys)
                        json.put(key, JSONObject.wrap(data.get(key)));
                        Log.v("mobilly","data: "+json.toString());
                        if(MobillyFirebase.firebaseCallback!=null){
                            PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, json);
                            pluginresult.setKeepCallback(true);
                            MobillyFirebase.firebaseCallback.sendPluginResult(pluginresult);
                        }
                    } catch (Exception e) {
                        PluginResult pluginresult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                        pluginresult.setKeepCallback(true);
                        MobillyFirebase.firebaseCallback.sendPluginResult(pluginresult);
                    }
                }
            });

        }

    }

    @Override
    public void initialize (CordovaInterface cordova, CordovaWebView webView) {
        MobillyFirebase.currentInstance = this;
    }

    @Override
    public boolean execute(String _action, JSONArray _args, CallbackContext _callbackContext) throws JSONException {
        Log.v("mobilly execute _action",_action);
        if (_action.equals("initialize")) {
            Log.v("mobilly","initialize");
            FirebaseApp.initializeApp(webView.getContext());
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("mobilly", "getInstanceId failed", task.getException());
                                _callbackContext.error(String.valueOf(task.getException()));

                                return;
                            }
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            Log.v("mobilly", MessageFormat.format("my token {0}", token));
                            _callbackContext.success(token);
                        }
                    });

            return true;
        }else if(_action.equals("onNotification")){
            Log.v("mobilly","onNotification");
            MobillyFirebase.firebaseCallback = _callbackContext;
            return true;
        }
        return false;
    }

    public static void sendResult(String _result) {
        if(MobillyFirebase.currentInstance!=null && MobillyFirebase.currentInstance.firebaseCallback !=null)
            MobillyFirebase.currentInstance.firebaseCallback.success(_result);
    }
}
