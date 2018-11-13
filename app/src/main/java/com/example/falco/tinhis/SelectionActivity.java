//  Project: TINHIS
//  SelectionActivity.java  --  Source file for Selection Activity.
//  Revisions:
//  2018-11-11  --  F.R. van der Meulen --  Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//Imports
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

//  Class definition.
public class SelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    //  Buttons.
    Button m_onOffButton;
    Button m_discoverButton;
    Button m_selectButton;

    //  TextViews.
    TextView m_btSelectedText;
    TextView m_btSelected;

    //  Bluetooth fields.
    BluetoothDevice m_bTDevice;
    BluetoothAdapter m_bluetoothAdapter;
    String strDeviceName;

    //  Receiver booleans.
    boolean state_changed_receiver = false;
    boolean found_receiver = false;
    boolean bond_state_changed_receiver = false;

    //  Log tag.
    private static final String TAG = "SelectionActivity";

    //  Device list.
    public ArrayList<BluetoothDevice> m_bTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> m_ignoredDevices = new ArrayList<>();
    public DeviceListAdapter m_deviceListAdapter;
    ListView m_deviceList;

    //  BroadcastReceiver for ACTION_STATE_CHANGED.
    private final BroadcastReceiver m_broadcastReceiverBTState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(m_bluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, m_bluetoothAdapter.ERROR);

                //  Log state.
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "bluetoothAdapter: State Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "bluetoothAdapter: State Turning Off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "bluetoothAdapter: State On");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "bluetoothAdapter: State Turning On");
                        break;
                }
            }
        }
    };

    //  Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver m_broadcastReceiverDeviceFound = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

                //  Ignore duplicates.
                if(!m_ignoredDevices.contains(device)){
                    if(!m_bTDevices.contains(device)){
                        if(device.getName() != null){
                            //  Add to connectable devices.
                            m_bTDevices.add(device);

                            //  Ignore when found again.
                            m_ignoredDevices.add(device);

                            //  Log.
                            Log.d(TAG, "onReceive: Listed " + device.getName() + " at " + device.getAddress() + ".");
                        }
                    }
                }

                //  Create adapter for listed devices.
                m_deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, m_bTDevices);
                m_deviceList.setAdapter(m_deviceListAdapter);
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_BOND_STATE_CHANGED.
    private final BroadcastReceiver m_broadcastReceiverBondState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice m_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //  If already bonded.
                if(m_device.getBondState() == BluetoothDevice.BOND_BONDED){
                    //  Log.
                    Log.d(TAG, "bondStateReceiver: BOND_BONDED");

                    //  Connect to device.
                    m_bTDevice = m_device;
                }

                //  If in the process of bonding.
                if(m_device.getBondState() == BluetoothDevice.BOND_BONDING){
                    //  Log.
                    Log.d(TAG, "bondStateReceiver: BOND_BONDING");
                }

                //  If no bond exists.
                if(m_device.getBondState() == BluetoothDevice.BOND_NONE){
                    //  Log.
                    Log.d(TAG, "bondStateReceiver: BOND_NONE");
                }
            }
        }
    };

    //  Overridden onDestroy method.
    //  Parameters: none.
    //  Returns:    void.
    @Override
    protected void onDestroy(){
        //  Log.
        Log.d(TAG, "onDestroy: called.");

        //  Call super onDestroy.
        super.onDestroy();

        //  Unregister broadcast receivers.
        if (state_changed_receiver) {
            unregisterReceiver(m_broadcastReceiverBTState);
        }

        if (found_receiver) {
            unregisterReceiver(m_broadcastReceiverDeviceFound);
        }

        if (bond_state_changed_receiver) {
            unregisterReceiver(m_broadcastReceiverBondState);
        }
    }

    //  Overridden onCreate method.
    //  Parameters:
    //      savedInstanceState  --  Bundle  containing instance state.
    //  Returns:    void.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  Log.
        Log.i(TAG, "created.");

        //  Call super onCreate.
        super.onCreate(savedInstanceState);

        //  Apply layout.
        setContentView(R.layout.activity_selection);

        //  If extras exist.
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            //  Retrieve extras.
            m_bTDevice = extras.getParcelable("device");
            strDeviceName = extras.getString("deviceName");

            //  Log.
            Log.d(TAG, "Connection name: "+ strDeviceName);
        }

        //  Adapters.
        m_bluetoothAdapter = m_bluetoothAdapter.getDefaultAdapter();

        //  Find Buttons.
        m_onOffButton = findViewById(R.id.actSelectOnOffButton);
        m_discoverButton = findViewById(R.id.actSelectListButton);
        m_selectButton = findViewById(R.id.actSelectSelectButton);

        //  Device list.
        m_deviceList = findViewById(R.id.actSelectDeviceList);
        m_bTDevices = new ArrayList<>();

        //  Find TextViews.
        m_btSelectedText = findViewById(R.id.actSelectBt_selected_text);
        m_btSelected = findViewById(R.id.actSelectBt_selected);

        //  Filter for bond state changed.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(m_broadcastReceiverBondState, filter);
        bond_state_changed_receiver = true;

        //  onItemClickListener for device list.
        m_deviceList.setOnItemClickListener(SelectionActivity.this);

        if(m_bluetoothAdapter.isEnabled()){
            m_discoverButton.setVisibility(View.VISIBLE);
        }

        //  Set onClickListeners.
        m_onOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Log.
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");

                //  Show / hide objects.
                if (!m_bluetoothAdapter.isEnabled()) {
                    m_discoverButton.setVisibility(View.VISIBLE);
                }
                else {
                    m_discoverButton.setVisibility(View.INVISIBLE);
                }

                //  Switch adapter state.
                enableDisableBluetooth();
            }
        });

        m_discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Log.
                Log.d(TAG, "onClick: Discovering devices...");  //Log

                //  Clear devicelist to avoid duplicates.
                m_bTDevices.clear();

                //  Show / hide objects.
                m_selectButton.setVisibility(View.VISIBLE);
                m_btSelectedText.setVisibility(View.VISIBLE);
                m_btSelected.setVisibility(View.VISIBLE);
                m_deviceList.setVisibility(View.VISIBLE);

                //  Discover bluetooth devices.
                discoverDevices();
            }
        });

        m_selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_bTDevice != null) {
                    //  Return connection to device.
                    returnConnection();
                }
                else{
                    //  Notify user that no device is selected.
                    Toast.makeText(getApplicationContext(), "No device selected!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //  returnConnection    --  Passes connection device & name back to Main Activity.
    public void returnConnection() {
        //  Create return intent.
        Intent returnIntent = new Intent(this, MainActivity.class);

        //  Put extras.
        returnIntent.putExtra("device", m_bTDevice);
        returnIntent.putExtra("deviceName", strDeviceName);

        //  Start activity with returnIntent.
        startActivity(returnIntent);
    }

    //  discoverDevices --  Scans for available Bluetooth devices.
    public void discoverDevices() {
        //  Log.
        Log.d(TAG, "discoverButton: Looking for unpaired devices...");

        //  Restart discovery if already discovering.
        if (m_bluetoothAdapter.isDiscovering()) {
            //  Log.
            Log.d(TAG, "discoverButton: Canceling discovery.");

            //  Cancel discovery.
            m_bluetoothAdapter.cancelDiscovery();

            //  Check Bluetooth permissions in manifest - Required.
            checkBTPermissions();

            //  Log.
            Log.d(TAG, "discoverButton: Enabling discovery.");

            //  Start discovery.
            m_bluetoothAdapter.startDiscovery();

            //  Register broadcastReceiver.
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(m_broadcastReceiverDeviceFound, discoverDevicesIntent);
            found_receiver = true;
        }

        //  Start discovery if not already discovering.
        if (!m_bluetoothAdapter.isDiscovering()) {
            //  Check Bluetooth permissions in manifest - Required.
            checkBTPermissions();

            //  Log.
            Log.d(TAG, "discoverButton: Enabling discovery.");

            //  Start discovery.
            m_bluetoothAdapter.startDiscovery();

            //  Register broadcastReceiver.
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(m_broadcastReceiverDeviceFound, discoverDevicesIntent);
            found_receiver = true;
        }
    }

    //  checkBTPermissions  --  Checks manifest for Bluetooth permissions.
    //  Parameters: none.
    //  Returns:    void.
    //  Suppresses NewApi Lint.
    @SuppressLint("NewApi")
    private void checkBTPermissions() {
        //  If phone uses Android Lollipop or up.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            //  Check permissions.
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            //  If permissions are granted.
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            //  Log.
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    //  enableDisableBluetooth  --  Switches Bluetooth adapter state.
    //  Parameters: none.
    //  Returns:    void.
    public void enableDisableBluetooth(){
        //  If phone has no Bluetooth adapter.
        if (m_bluetoothAdapter == null) {
            //  Log.
            Log.d(TAG,"onOffButton: No bluetooth adapter.");
        }

        //  If Bluetooth adapter is disabled.
        if (!m_bluetoothAdapter.isEnabled()) {
            //  Log.
            Log.d(TAG, "onOffButton: enabling bluetooth."); //Enabling bluetooth

            //  Create intent to enable Bluetooth.
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBluetoothIntent);

            //  Register broadcastReceiver.
            IntentFilter bluetoothIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(m_broadcastReceiverBTState, bluetoothIntent);
            state_changed_receiver = true;
        }

        //  If Bluetooth adapter is enabled.
        if (m_bluetoothAdapter.isEnabled()) {
            //  Log.
            Log.d(TAG, "onOffButton: disabling bluetooth.");    //Disabling bluetooth

            //  Disable Bluetooth adapter.
            m_bluetoothAdapter.disable();

            //  Register broadcastReceiver.
            IntentFilter bluetoothIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(m_broadcastReceiverBTState, bluetoothIntent);
            state_changed_receiver = true;
        }
    }

    //  Overridden onItemClick method.
    //  Parameters:
    //      parent      --  parent AdapterView
    //      view        --  View to register to.
    //      position    --  Position of item clicked.
    //      id          --  ID of item.
    //  Returns:    void.
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //  Cancel discovery to save phone resources.
        m_bluetoothAdapter.cancelDiscovery();

        //  Log.
        Log.d(TAG, "onItemClick: Item Clicked!");

        //  Get device information.
        String deviceName = m_bTDevices.get(position).getName();
        String deviceAddress = m_bTDevices.get(position).getAddress();

        //  Show device name.
        m_btSelected.setText(deviceName);

        //  Log.
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //  Create the bond, granted phone uses Android Jellybean MR2 or up.
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //  Log.
            Log.d(TAG, "Trying to pair with " + deviceName);

            //  Create bond.
            m_bTDevices.get(position).createBond();

            //  Register device information.
            m_bTDevice = m_bTDevices.get(position);
            strDeviceName = m_bTDevice.getName();

            //  Show selectButton.
            m_selectButton.setVisibility(View.VISIBLE);

            //  -----------------------------------------------------   //
            //  Starting a connection service starts an AcceptThread.   //
            //  -----------------------------------------------------   //
        }
    }
}