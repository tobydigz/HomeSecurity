package com.digzdigital.homesecurity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import java.io.IOException;
import java.util.List;

public class PlaceCallActivity extends BaseActivity implements SinchService.StartFailedListener {

    private static final String TAG = "Digz Pir sensor";
    private static final String GPIO_PIN_NAME = "BCM4";

    private boolean started = false;
    private Gpio pirGpio;
    private int noOfTimes = 0;
    private boolean tryCall = false;
    private boolean systemEnabled = true;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        firebaseListener();
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (!portList.isEmpty()){
            Log.i(TAG, "List of available ports:" + portList);
        }else Log.i(TAG, "No available ports");

        try {
            initialisePirSensor();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private void firebaseListener(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        reference= FirebaseDatabase.getInstance().getReference().child("sytemEnabled");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                systemEnabled = (Boolean)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onServiceConnected() {
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient("digzHardware");
        }
        getSinchServiceInterface().setStartListener(this);

    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        started = false;
    }

    @Override
    public void onStarted() {
//// TODO: 10/02/2017 initialise PIR
        started = true;
        if (tryCall) callStart();

    }

    private void initialisePirSensor() throws IOException {
        PeripheralManagerService service = new PeripheralManagerService();
        pirGpio = service.openGpio(GPIO_PIN_NAME);
        pirGpio.setActiveType(Gpio.ACTIVE_HIGH);
        pirGpio.setDirection(Gpio.DIRECTION_IN);
        pirGpio.setEdgeTriggerType(Gpio.EDGE_RISING);
        pirGpio.registerGpioCallback(callback);
    }


    private GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            noOfTimes++;
            Log.d(TAG, "event triggered: " + noOfTimes + " time(s)");
            callStart();
           Toast.makeText(PlaceCallActivity.this, "Sensor triggered", Toast.LENGTH_SHORT).show();
            return true;
        }
    };


    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null && started ) {
            getSinchServiceInterface().stopClient();
        }

        if (pirGpio !=null){
            pirGpio.unregisterGpioCallback(callback);
            try {
                pirGpio.close();
            }catch (IOException e){
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
        super.onDestroy();


    }

    // TODO: 10/02/2017 activated by pir sensor
    private void callStart() {
        if (!systemEnabled)return;
        if (!started) {
            tryCall = true;
            startSinchApi();
            return;
        }
        String username = "tobydigz";

        Call call = getSinchServiceInterface().callUserVideo(username);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);

    }

    private void startSinchApi() {
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient("digzHardware");
        }
        getSinchServiceInterface().setStartListener(this);
    }
}
