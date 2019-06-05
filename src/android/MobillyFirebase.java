package mobilly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
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
import java.util.Iterator;
import java.util.Set;

public class MobillyFirebase extends CordovaPlugin {

    private static MobillyFirebase currentInstance = null;
    private static CallbackContext firebaseCallback = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String TAG;




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
    protected void pluginInitialize() {
        final Context context = this.cordova.getActivity().getApplicationContext();
        final Bundle extras = this.cordova.getActivity().getIntent().getExtras();
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Log.v("mtest","init1");
                FirebaseApp.initializeApp(context);
                FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
                mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
//                FirebaseApp.initializeApp(context);

            }
        });
    }

    @Override
    public void initialize (CordovaInterface cordova, CordovaWebView webView) {
        Log.v("mtest","init2");
        MobillyFirebase.currentInstance = this;
    }

    @Override
    public boolean execute(String _action, JSONArray _args, CallbackContext _callbackContext) throws JSONException {
        Log.v("mobilly execute _action",_action);
        if (_action.equals("initialize")) {
            Log.v("mobilly","initializ");



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
        }else if(_action.equals("setUserId")){
            Log.v("mobilly","setUserId");
            this.setUserId(_callbackContext, _args.getString(0));
            return true;
        }else if (_action.equals("logEvent")) {
            this.logEvent(_callbackContext, _args.getString(0), _args.getJSONObject(1));
            return true;
        }
        return false;
    }

    public static void sendResult(String _result) {
        if(MobillyFirebase.currentInstance!=null && MobillyFirebase.currentInstance.firebaseCallback !=null)
            MobillyFirebase.currentInstance.firebaseCallback.success(_result);
    }

    private void setUserId(final CallbackContext callbackContext, final String id) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    mFirebaseAnalytics.setUserId(id);
                    callbackContext.success();
                } catch (Exception e) {
//                    Crashlytics.logException(e);
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void logEvent(final CallbackContext callbackContext, final String name, final JSONObject params)
            throws JSONException {
        final Bundle bundle = new Bundle();
        Iterator iter = params.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object value = params.get(key);

            if (value instanceof Integer || value instanceof Double) {
                bundle.putFloat(key, ((Number) value).floatValue());
            } else {
                bundle.putString(key, value.toString());
            }
        }

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    mFirebaseAnalytics.logEvent(name, bundle);
                    callbackContext.success();
                } catch (Exception e) {
//                    Crashlytics.logException(e);
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

}
