package com.digzdigital.homesecurity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Digz on 09/02/2017.
 */

public class BaseActivity extends AppCompatActivity implements ServiceConnection{

    private SinchService.SinchServiceInterface sinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, SinchService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())){
            sinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())){
            sinchServiceInterface = null;
            onServiceDisconnected();
        }
    }

    protected void onServiceConnected(){

    }

    protected void onServiceDisconnected(){

    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface(){
        return sinchServiceInterface;
    }
}
