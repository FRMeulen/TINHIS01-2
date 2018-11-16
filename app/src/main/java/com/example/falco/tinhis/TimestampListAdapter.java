//  Project: TINHIS.
//  TimestampListAdapter.java   --  Source file for TimestampListAdapter class.
//  Revisions:
//  2018-11-11  --  F.R. van der Meulen --  Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//  Class definition.
public class TimestampListAdapter extends ArrayAdapter<String> {
    //  Log tag.
    String TAG = "TimestampListAdapter";

    //  Fields.
    private LayoutInflater m_layoutInflater;
    private ArrayList<String> m_timestamps;
    private int  m_viewResourceId;

    //  Constructor.
    //  Parameters:
    //      context         --  Class context.
    //      tvResourceId    --  ID of target TextView.
    //      devices         --  ArrayList of devices to show.
    public TimestampListAdapter(Context context, int tvResourceId, ArrayList<String> timestamps){
        //  Call super constructor.
        super(context, tvResourceId, timestamps);

        //  Assign parameters.
        this.m_timestamps = timestamps;
        m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_viewResourceId = tvResourceId;
    }

    //  getView --  Returns device at given position.
    //  Parameters:
    //      position    --  Position in ArrayList.
    //      convertView --  Inflated view of device at position.
    //      parent      --  ViewGroup containing parent.
    public View getView(int position, View convertView, ViewGroup parent) {
        //  Local string variables.
        String strInOut = "";
        String strDate;
        String strTime;
        String strLogTop;
        String strLogBottom;

        //  Inflate view at id.
        convertView = m_layoutInflater.inflate(m_viewResourceId, null);

        //  Get device from ArrayList.
        String timestamp = m_timestamps.get(position);

        //  Set strings.
        SimpleDateFormat m_date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        strDate = m_date.format(new Date());
        SimpleDateFormat m_time = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        strTime = m_time.format(new Date());
        if (timestamp.equals("enter")) {
            //  Set string.
            strInOut = "    >";

            //  Log.
            Log.i(TAG, "customer arrived.");
        } else if (timestamp.equals("leave")) {
            //  Set string.
            strInOut = "    <";

            //  Log.
            Log.i(TAG, "customer left.");
        } else {
            //  Log.
            Log.e(TAG, "unknown timestamp type: " + timestamp);
        }
        strLogTop = strInOut + " " + strDate;
        strLogBottom = strInOut + " " + strTime;

        //  Find views.
        TextView timestampDate = convertView.findViewById(R.id.tsAdapterTimestampType);
        TextView timestampTime = convertView.findViewById(R.id.tsAdapterTimeStampValue);

        //  Show text in app.
        timestampDate.setText(strLogTop);
        timestampTime.setText(strLogBottom);

        //  Return view.
        return convertView;
    }

}
