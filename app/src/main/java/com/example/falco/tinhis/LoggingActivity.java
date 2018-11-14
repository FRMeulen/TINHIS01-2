//  Project: TINHIS.
//  LoggingActivity.java    --  Source file for Logging Activity.
//  Revisions:
//  2018-11-11  --  F.R. van der Meulen --  Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//  Class definition.
public class LoggingActivity extends AppCompatActivity {
    //  Log tag.
    String TAG = "LoggingActivity";

    //  Buttons.
    Button m_resetButton;

    //  TextViews.
    TextView m_deviceNameTextView;
    TextView m_visitorCounterTextView;

    //  Visitor count.
    int m_visitorCount;

    //  Bluetooth fields.
    BluetoothConnectionService m_btServ;
    BluetoothDevice m_btDev;
    String strDeviceName;

    //  Timestamp list.
    public ArrayList<String> m_timeStampLog = new ArrayList<>();
    public TimestampListAdapter m_timestampListAdapter;
    ListView m_listView;

    //  Overridden onCreate method.
    //  Parameters:
    //      savedInstanceState  --  Bundle containing instance state.
    //  Returns:    void.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  Log.
        Log.i(TAG, "created.");

        //  Call super onCreate.
        super.onCreate(savedInstanceState);

        //  Apply layout.
        setContentView(R.layout.activity_logging);

        //  Find views.
        m_resetButton = findViewById(R.id.actLoggingResetButton);
        m_deviceNameTextView = findViewById(R.id.actLoggingSelectedDevice);
        m_visitorCounterTextView = findViewById(R.id.actLoggingCounter);

        //  Timestamp list.
        m_listView = findViewById(R.id.actLoggingTimestampList);
        m_timeStampLog = new ArrayList<>();
        m_timestampListAdapter = new TimestampListAdapter(this, R.layout.timestamp_adapter_view, m_timeStampLog);
        m_listView.setAdapter(m_timestampListAdapter);

        //  Set onClickListeners.
        m_resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  If Bluetooth service is active.
                if (m_btServ != null) {
                    //  Kill service.
                    m_btServ.killClient();
                }

                //  Restart app.
                restartApp();
            }
        });

        //  Restore extras if intent contains them.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            m_btDev = extras.getParcelable("device");
            strDeviceName = extras.getString("deviceName");
            m_deviceNameTextView.setText(strDeviceName);
        }

        //  Start connection service.
        m_btServ = new BluetoothConnectionService(this, this);
        m_btServ.startClient(m_btDev);
    }

    //  addTimestamp    --  Adds timestamp to ArrayList.
    //  Parameters:
    //      timestamp   --  String of timestamp.
    //  Returns:    void.
    public void addTimestamp(String type) {
        boolean valid = false;

        //  Check if arrival or departure.
        if (type.equals("enter")) {
            //  Update visitor count.
            updateVisitorCount(1);

            //  Log.
            Log.i(TAG, "visitor arrived.");

            //  Approve timestamp.
            valid = true;
        } else if (type.equals("leave")) {
            //  Update visitor count.
            updateVisitorCount(-1);

            //  Log.
            Log.i(TAG, "visitor left.");

            //  Approve timestamp.
            valid = true;
        } else {
            //  Log.
            Log.e(TAG, "timestamp without type recorded.");
        }

        //  If valid.
        if (valid) {
            //  Add to ArrayList.
            m_timeStampLog.add(type);

            //  Log.
            Log.i(TAG, "timestamp logged: " + type);
        }
    }

    //  updateVisitorCount  --  Updates visitor count shown on screen.
    //  Parameters:
    //      count           --  +1 or -1.
    //  Returns:    void.
    public void updateVisitorCount(int count) {
        m_visitorCount += count;
        if (m_visitorCount < 0){
            m_visitorCount = 0;
        }
        String displayCount = ""+m_visitorCount;
        m_visitorCounterTextView.setText(displayCount);
    }

    //  restartApp  --  Restarts application.
    //  Parameters: none.
    //  Returns:    void.
    public void restartApp() {
        //  Create restartIntent.
        Intent restartIntent = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();

        //  Start activity with restartIntent.
        startActivity(restartIntent);
    }
}