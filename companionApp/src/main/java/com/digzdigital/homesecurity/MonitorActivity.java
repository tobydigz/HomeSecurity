package com.digzdigital.homesecurity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MonitorActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
    }

    @Override
    public void onDestroy(){
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }
}
