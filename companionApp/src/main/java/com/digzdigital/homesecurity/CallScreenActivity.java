package com.digzdigital.homesecurity;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class CallScreenActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String CALL_START_TIME = "callStartTime";
    static final String ADDED_LISTENER = "addedListener";

    private AudioPlayer audioPlayer;
    private Timer timer;
    private UpdateCallDurationTask durationTask;

    private String callId;
    private long callStart = 0;
    private boolean addedListener = false;
    private boolean videoViewsAdded = false;

    private TextView callDuration;
    private TextView callState;
    private TextView callerName;

    @Override
    protected void onSaveInstanceState(Bundle savedInsanceState) {
        savedInsanceState.putLong(CALL_START_TIME, callStart);
        savedInsanceState.putBoolean(ADDED_LISTENER, addedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        callStart = savedInstanceState.getLong(CALL_START_TIME);
        addedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callscreen);

        audioPlayer = new AudioPlayer(this);
        callDuration = (TextView) findViewById(R.id.callDuration);
        callerName = (TextView) findViewById(R.id.remoteUser);
        callState = (TextView) findViewById(R.id.callState);

        Button endCallButton = (Button) findViewById(R.id.hangupButton);
        endCallButton.setOnClickListener(this);
        callId = getIntent().getStringExtra(SinchService.CALL_ID);
        if (savedInstanceState == null) {
            callStart = System.currentTimeMillis();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.hangupButton:
                endCall();
                break;
        }

    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(callId);
        if (call != null) {
            if (!addedListener) {
                call.addCallListener(new SinchCallListener());
                addedListener = true;
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
        updateUI();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) return;
        Call call = getSinchServiceInterface().getCall(callId);
        if (call != null) {
            callerName.setText(call.getRemoteUserId());
            callState.setText(call.getState().toString());
            if (call.getState() == CallState.ESTABLISHED) addVideoViews();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        durationTask.cancel();
        timer.cancel();
        removeVideoViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        timer = new Timer();
        durationTask = new UpdateCallDurationTask();
        timer.schedule(durationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed() {

    }

    private void endCall() {
        audioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(callId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(long timespan) {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        if (callStart > 0) {
            callDuration.setText(formatTimespan(System.currentTimeMillis() - callStart));
        }
    }

    private void addVideoViews() {
        if (videoViewsAdded || getSinchServiceInterface() == null) return;

        final VideoController videoController = getSinchServiceInterface().getVideoController();
        if (videoController != null) {
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.addView(videoController.getLocalView());
            localView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoController.toggleCaptureDevicePosition();
                }
            });

            LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
            view.addView(videoController.getRemoteView());
            videoViewsAdded = true;
        }
    }

    private void removeVideoViews(){
        if (getSinchServiceInterface() == null)return;

        VideoController videoController = getSinchServiceInterface().getVideoController();
        if (videoController != null){
            LinearLayout view = (LinearLayout) findViewById(R.id.remoteVideo);
            view.removeView(videoController.getRemoteView());

            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.removeView(videoController.getLocalView());
            videoViewsAdded = false;
        }
    }

    private class SinchCallListener implements VideoCallListener{

        @Override
        public void onVideoTrackAdded(Call call) {
            Log.d(TAG, "Video track added");
            addVideoViews();
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            audioPlayer.playProgressTone();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            audioPlayer.stopProgressTone();
            callState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            callStart = System.currentTimeMillis();
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            audioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            endCall();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }
    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

}