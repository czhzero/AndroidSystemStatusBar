package org.chen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import org.chen.statusbar.SystemStatusView;


public class MainActivity extends AppCompatActivity {

    private SystemStatusView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusView = (SystemStatusView) findViewById(R.id.status_bar);
        statusView.registerStatusBarReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusView.unregisterStatusBarReceiver();
    }








}
