//  Project: TINHIS.
//  ParseThread.java    --  Source file for ParseThread class.
//  Revisions:
//  2018-11-11  --  F.R.  van der Meulen    --  Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.content.Context;
import android.util.Log;

//  Class definition.
public class ParseThread extends Thread {
    //  Fields.
    private LoggingActivity parent;
    private String dataRead;
    private final Context mContext;

    //  Log tag.
    private static final String TAG = "ParseThread";

    //  Constructor.
    //  Parameters:
    //      context     --  Class context.
    //      activity    --  Parent activity.
    public ParseThread(Context context, LoggingActivity activity){
        //  Assign fields.
        parent = activity;
        mContext = context;
    }

    //  run --  Runs the ParseThread.
    //  Parameters: none.
    //  Returns:    void.
    public void run() {
        //  Log.
        Log.i(TAG, "Created ParseThread");

        //  Infinite loop.
        while(true){

        }
    }

    //  parse   --  Parses given message to rebuild split messages.
    //  Parameters:
    //      messageIn   --  String of incoming message.
    //  Returns:    void.
    public void parse(String messageIn){
        //  If messageIn has desired length.
        if (messageIn.length() == 7) {
            //  Cut off end data.
            dataRead = messageIn.substring(0, 5);

            //  Log.
            Log.d(TAG, "addTimeStamp called with " + dataRead);

            //  Add timestamp.
            logTimestamp(dataRead);

            //  Reset string variable.
            dataRead = null;
        }

        //  If messageIn is too short.
        else if (messageIn.length() < 7) {
            //  If no data is currently held.
            if (dataRead == null){
                //  Store messageIn.
                dataRead = messageIn;

                //  Log.
                Log.d(TAG, "New length: "+dataRead.length());
            } else {
                //  If data exists, concatenate messageIn to it.
                dataRead += messageIn;

                //  Log.
                Log.d(TAG, "Parsed message: "+dataRead+" with length "+dataRead.length());

                //  Parse new message.
                parse(dataRead);
            }
        } else {
            //  Ignore if message is too long.
            dataRead = null;

            //  Log.
            Log.d(TAG, "Message too long, ignoring...");
        }
    }

    //  logTimestamp    --  Sends timestamp to parent for logging.
    //  Parameters:
    //      timestamp   --  String of timestamp.
    //  Returns:    void.
    private void logTimestamp(String timestamp){
        final String loggedTimestamp = timestamp;
        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parent.addTimestamp(loggedTimestamp);
            }
        });
    }
}