//  Project: TINHIS.
//  MainActivity.java   --  Source file for Main Activity.
//  Revisions:
//  2018-11-11  --  F.R. van der Meulen --  Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//  Class definition.
public class MainActivity extends AppCompatActivity {
    //  Debug tag.
    String TAG = "MainActivity";

    //  Buttons.
    Button m_selectButton;
    Button m_toLogButton;

    //  TextViews.
    TextView m_selectedDeviceTextView;

    //  Devices.
    BluetoothDevice m_device;

    //  Bluetooth adapter.
    BluetoothAdapter m_bluetoothAdapter;

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
        setContentView(R.layout.activity_main);

        //  Get bluetooth adapter.
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //  Find views.
        m_selectedDeviceTextView = findViewById(R.id.actMainBTDevice);
        m_selectButton = findViewById(R.id.actMainSelectDeviceButton);
        m_toLogButton = findViewById(R.id.actMainDoneButton);

        //  Restore extras if intent contains them.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //  Log.
            Log.i(TAG, "extras retrieved.");

            //  If extras exist, retrieve device & device name.
            m_device = extras.getParcelable("device");
            m_selectedDeviceTextView.setText(extras.getString("deviceName"));
        }

        //  Check if preparations for logging are done.
        if (!m_selectedDeviceTextView.getText().equals("NONE")) {
            //  Log.
            Log.i(TAG, "ready to log.");

            //  If a device is selected, show "to Log" button.
            m_toLogButton.setVisibility(View.VISIBLE);
        }

        //  On-click listeners.
        m_selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toSelectScreen();
            }
        });

        m_toLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogScreen();
            }
        });
    }

    //  toSelectScreen  --  Switches to selection activity.
    //  Parameters: none.
    //  Returns:    void.
    public void toSelectScreen() {
        //  Log.
        Log.i(TAG, "switching to SelectionActivity.");

        //  Create intent for screen switch.
        Intent selectIntent = new Intent(this, SelectionActivity.class);

        //  Add extras.
        selectIntent.putExtra("deviceName", m_selectedDeviceTextView.getText());
        selectIntent.putExtra("device", m_device);

        //  Switch activity.
        startActivity(selectIntent);
    }

    //  toLogScreen --  Switches to logging activity.
    //  Parameters: none.
    //  Returns:    void.
    public void toLogScreen() {
        //  Log.
        Log.i(TAG, "switching to LoggingActivity.");

        //  Create intent for screen switch.
        Intent logIntent = new Intent(this, LoggingActivity.class);

        //  Add extras.
        logIntent.putExtra("deviceName", m_selectedDeviceTextView.getText());
        logIntent.putExtra("device", m_device);

        //  Switch activity.
        startActivity(logIntent);
    }

    //  Overridden onRestoreInstanceState method.
    //  Parameters:
    //      savedInstanceState  --  Bundle containing instance state.
    //  Returns:    void.
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //  Log.
        Log.i(TAG, "restoring instance state.");

        //  Call super onRestoreInstanceState.
        super.onRestoreInstanceState(savedInstanceState);

        //  Retrieve data.
        m_selectedDeviceTextView.setText(savedInstanceState.getString("deviceName"));
        m_device = savedInstanceState.getParcelable("device");
    }

    //  Overridden onSaveInstanceState method.
    //  Parameters:
    //      outState    --  Bundle for storing instance state.
    //  Returns:    void.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //  Log.
        Log.i(TAG, "saving instance state.");

        //  Call super onSaveInstanceState.
        super.onSaveInstanceState(outState);

        //  Store data.
        outState.putString("deviceName", (String)m_selectedDeviceTextView.getText());
        outState.putParcelable("device", m_device);
    }

    //  showBTDisabled  --  Notifies user of disabled bluetooth.
    //  Parameters: none.
    //  Returns:    void.
    private void showBTDisabled() {
        Toast.makeText(this, "Please re-enable Bleutooth before continuing!", Toast.LENGTH_LONG).show();
    }
}
