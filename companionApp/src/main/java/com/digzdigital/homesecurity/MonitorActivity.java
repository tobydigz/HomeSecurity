package com.digzdigital.homesecurity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MonitorActivity extends BaseActivity implements View.OnClickListener {

    private ToggleButton monitorSwitch;
    private TextView monitorStateText;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        reference = FirebaseDatabase.getInstance().getReference().child("systemEnabled");
        monitorSwitch = (ToggleButton) findViewById(R.id.toggleMonitorButton);
        monitorStateText = (TextView) findViewById(R.id.monitorStateText);
        monitorSwitch.setOnClickListener(this);

    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switchMonitorOn(monitorSwitch.isChecked());
        setMonitorStateText(monitorSwitch.isChecked());
    }

    @SuppressLint("SetTextI18n")
    private void setMonitorStateText(boolean checked) {
        if (checked) {
            monitorStateText.setText("Monitoring on");
            return;
        }
        monitorStateText.setText("Monitoring off");
    }

    private void switchMonitorOn(boolean checked) {
        reference.setValue(checked);
    }
}
