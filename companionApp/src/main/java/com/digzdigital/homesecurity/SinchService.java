package com.digzdigital.homesecurity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoController;

/**
 * Created by Digz on 09/02/2017.
 */

public class SinchService extends Service {

    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();
    private static final String APP_KEY = "2ee63f3d-328b-40dd-9e72-0a7ad6af10a7";
    private static final String APP_SECRET = "rC7EBpnMi0CUlYj7CENlAA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private SinchServiceInterface sinchServiceInterface = new SinchServiceInterface();
    private SinchClient sinchClient;
    private String userId;

    private StartFailedListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (sinchClient != null && sinchClient.isStarted()) {
            sinchClient.terminate();
        }
        super.onDestroy();
    }

    private void start(String userName) {
        if (sinchClient == null) {
            userId = userName;
            sinchClient = Sinch.getSinchClientBuilder()
                    .context(getApplicationContext())
                    .applicationKey(APP_KEY)
                    .userId(userId)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT)
                    .build();

            sinchClient.setSupportCalling(true);
            sinchClient.startListeningOnActiveConnection();
            sinchClient.addSinchClientListener(new MySinchClientListener());
            sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
            sinchClient.start();
        }
    }

    private void stop() {
        if (sinchClient != null) {
            sinchClient.terminate();
            sinchClient = null;
        }
    }

    private boolean isStarted() {
        return (sinchClient != null && sinchClient.isStarted());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sinchServiceInterface;
    }

    public interface StartFailedListener {
        void onStartFailed(SinchError error);

        void onStarted();
    }

    public class SinchServiceInterface extends Binder {

        public Call callUserVideo(String userId) {
            return sinchClient.getCallClient().callUserVideo(userId);
        }

        public String getUserName() {
            return userId;
        }

        public boolean isStarted() {
            return SinchService.this.isStarted();
        }

        public void startClient(String userName) {
            start(userName);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            SinchService.this.listener = listener;
        }

        public Call getCall(String callId) {
            return sinchClient.getCallClient().getCall(callId);
        }

        public VideoController getVideoController() {
            if (!isStarted())
                return null;

            return sinchClient.getVideoController();
        }

        public AudioController getAudioController() {
            if (!isStarted()) return null;
            return sinchClient.getAudioController();
        }
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientStarted(SinchClient sinchClient) {
            Log.d(TAG, "SinchClient started");
            if (listener !=null){
                listener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient sinchClient) {
            Log.d(TAG, "SinchClient stopped");
        }

        @Override
        public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
            if (listener != null) {
                listener.onStartFailed(sinchError);
            }
            sinchClient.terminate();
            sinchClient = null;
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

    }

    private class SinchCallClientListener implements CallClientListener{

        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            Log.d(TAG, "Incoming call");
            Intent intent = new Intent(SinchService.this, IncomingCallScreenActivity.class);
            intent.putExtra(CALL_ID, call.getCallId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SinchService.this.startActivity(intent);
        }
    }
}
