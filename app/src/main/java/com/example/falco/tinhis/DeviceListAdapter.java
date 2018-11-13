//  Project: TINHIS.
//  DeviceListAdapter.java  --  Source file for DeviceListAdapter class.
//  Revisions:
//  2018-11-11  --  F.R. van der Meulen --   Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

//  Class definition.
public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    //  Fields.
    private LayoutInflater m_layoutInflater;
    private ArrayList<BluetoothDevice> m_devices;
    private int  m_viewResourceId;

    //  Constructor.
    //  Parameters:
    //      context         --  Class context.
    //      tvResourceId    --  ID of target TextView.
    //      devices         --  ArrayList of devices to show.
    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        //  Call super constructor.
        super(context, tvResourceId,devices);

        //  Assign parameters.
        this.m_devices = devices;
        m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_viewResourceId = tvResourceId;
    }

    //  getView --  Returns device at given position.
    //  Parameters:
    //      position    --  Position in ArrayList.
    //      convertView --  Inflated view of device at position.
    //      parent      --  ViewGroup containing parent.
    public View getView(int position, View convertView, ViewGroup parent) {
        //  Inflate view at id.
        convertView = m_layoutInflater.inflate(m_viewResourceId, null);

        //  Get device from ArrayList.
        BluetoothDevice device = m_devices.get(position);

        //  If it exists.
        if (device != null) {
            //  Get device name & address.
            TextView deviceName = convertView.findViewById(R.id.devAdapterDeviceName);
            TextView deviceAddress = convertView.findViewById(R.id.devAdapterDeviceAddress);

            //  If it has a name.
            if (deviceName != null) {
                //  Show name on screen.
                deviceName.setText(device.getName());
            }

            //  If it has an address.
            if (deviceAddress != null) {
                //  Show address on screen.
                deviceAddress.setText(device.getAddress());
            }
        }

        //  Return view.
        return convertView;
    }

}