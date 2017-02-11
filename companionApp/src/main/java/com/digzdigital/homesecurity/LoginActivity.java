package com.digzdigital.homesecurity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sinch.android.rtc.SinchError;

public class LoginActivity extends BaseActivity implements SinchService.StartFailedListener, View.OnClickListener{

    private Button login;
    private EditText loginName;
    private ProgressDialog spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginName = (EditText) findViewById(R.id.loginName);
        login = (Button) findViewById(R.id.loginButton);
        login.setEnabled(false);
        login.setOnClickListener(this);
    }

    @Override
    protected void onServiceConnected(){
        login.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    protected void onPause(){
        if (spinner !=null)spinner.dismiss();
        super.onPause();
    }
    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (spinner !=null) spinner.dismiss();
    }

    @Override
    public void onStarted() {
        openMonitorActivity();
    }

    private void openMonitorActivity() {
        Intent mainActivity = new Intent(this, MonitorActivity.class);
        startActivity(mainActivity);
    }

    @Override
    public void onClick(View view) {
        loginClicked();
    }

    private void loginClicked() {
        String userName = loginName.getText().toString();
        if (userName.isEmpty()){
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
            return;
        }
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
            showSpinner();
        } else {
            openMonitorActivity();
        }
    }

    private void showSpinner() {
        spinner = new ProgressDialog(this);
        spinner.setTitle("Logging in");
        spinner.setMessage("Please wait...");
        spinner.show();
    }
}
