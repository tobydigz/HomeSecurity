package com.digzdigital.homesecurity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import java.io.IOException;

public class PlaceCallActivity extends BaseActivity implements SinchService.StartFailedListener {

    private static final String TAG = "Pir sensor";
    private static final String GPIO_PIN_NAME = "a";

    private boolean started = false;
    private Gpio pirGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient("digzHardware");
        }
    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
        started = true;
    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
//// TODO: 10/02/2017 initialise PIR
        try {
            initialisePirSensor();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private void initialisePirSensor() throws IOException {
        PeripheralManagerService service = new PeripheralManagerService();
        pirGpio = service.openGpio(GPIO_PIN_NAME);
        pirGpio.setDirection(Gpio.DIRECTION_IN);
        pirGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
        pirGpio.registerGpioCallback(callback);
    }

    private GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            callStart();
            return true;
        }
    };


    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();

        if (pirGpio !=null){
            pirGpio.unregisterGpioCallback(callback);
            try {
                pirGpio.close();
            }catch (IOException e){
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

    }

    // TODO: 10/02/2017 activated by pir sensor
    private void callStart() {
        if (!started) return;
        String username = "tobydigz";

        Call call = getSinchServiceInterface().callUserVideo(username);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);

    }
}
