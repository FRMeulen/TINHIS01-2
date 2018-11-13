//  Project: TINHIS.
//  BluetoothConnectionService.java --  Source file for handling Bluetooth connections.
//  Revisions:
//  2018-11-11  --  F.R. van der Meulen --  Created.

//  Package.
package com.example.falco.tinhis;

//  Import statements.
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

//  Class definition.
public class BluetoothConnectionService {
    //  Parent activity & context.
    private final LoggingActivity parent;
    private final Context m_context;

    //  Log tag, appName & UUID.
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "TINHIS";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    //  Threads.
    private AcceptThread m_acceptThread;
    private ConnectThread m_connectThread;
    private ConnectedThread m_connectedThread;
    private ParseThread m_parseThread;

    //  Progress dialog.
    private ProgressDialog m_progressDialog;

    //  Bluetooth objects.
    private BluetoothDevice m_device;
    private final BluetoothAdapter m_bluetoothAdapter;
    private String strDataRead = "";

    //  Constructor.
    //  Parameters:
    //      context     --  Class context.
    //      activity    --  Activity class will serve.
    public BluetoothConnectionService (Context context, LoggingActivity activity) {
        parent = activity;
        m_context = context;
        m_parseThread = new ParseThread(context, parent);
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    //  AcceptThread    --  Creates Bluetooth socket and awaits acceptance.
    private class AcceptThread extends Thread {
        //  Server socket.
        private final BluetoothServerSocket m_serverSocket;

        //  Constructor.
        //  Parameters: none.
        public AcceptThread(){
            //  Start with null socket.
            BluetoothServerSocket tmp = null;

            //  Try listening for RFComms
            try {
                tmp = m_bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                //  Log.
                Log.i(TAG, "AcceptThread: Setting up server using " + MY_UUID_INSECURE);
            } catch(IOException e) {
                //  Log.
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());

                //  Restart app.
                parent.restartApp();
            }

            //  Server socket saved if successful.
            m_serverSocket = tmp;
        }

        //  run --  Runs the AcceptThread.
        //  Parameters: none.
        //  Returns:    void.
        public void run(){
            //  Log
            Log.i(TAG, "AcceptThread: started");

            //  Try to accept.
            try {
                //  Log.
                Log.d(TAG, "run: Rfcomm server socket start");

                m_serverSocket.accept();

                //  Log.
                Log.d(TAG, "run: Rfcomm server socket accepted connection");
                Log.i(TAG, "Killing AcceptThread!");

                //  Kill thread.
                return;
            } catch(IOException e) {
                //  Log.
                Log.e(TAG, "run: IOException: " + e.getMessage());

                //  Restart app.
                parent.restartApp();
            }
        }
    }

    //  ConnectThread   --  Connects to device in socket.
    private class ConnectThread extends Thread {
        //  Socket for connection.
        private BluetoothSocket m_socket;

        //  Constructor.
        //  Parameters:
        //      device  --  Device to connect to.
        public ConnectThread(BluetoothDevice device){
            //  Log.
            Log.i(TAG, "ConnectThread: started");

            //  Set device.
            m_device = device;
        }

        //  run --  Runs the ConnectThread.
        //  Parameters: none.
        //  Returns:    void.
        public void run(){
            //  Start with null socket.
            BluetoothSocket tmp = null;

            //  Try to create RFCommSocket.
            try {
                //  Log.
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID " + MY_UUID_INSECURE);

                tmp = m_device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch(IOException e) {
                //  Log.
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());

                //  Restart app.
                parent.restartApp();
            }

            //  Store socket.
            m_socket = tmp;

            //  Cancel discovery to save phone resources.
            m_bluetoothAdapter.cancelDiscovery();

            //  Try to connect.
            try {
                m_socket.connect();

                //  Log.
                Log.d(TAG, "ConnectThread: Connection successful!");
            } catch (IOException e) {
                //  Try to close socket.
                try {
                    m_socket.close();
                } catch (IOException e1) {
                    //  Log.
                    Log.e(TAG, "ConnectThread: Unable to close connection in socket");

                    //  Restart app.
                    parent.restartApp();
                }

                //  Log.
                Log.e(TAG, "ConnectThread: Could not connect to RfcommSocket " + e.getMessage());

                //  Restart app.
                parent.restartApp();
            }

            //  Start ConnectedThread.
            connected(m_socket);

            //  Log.
            Log.i(TAG, "Killing ConnectThread!");

            //  Kill ConnectThread.
            return;
        }

        //  cancel  --  Cancels ConnectThread.
        //  Parameters: none.
        //  Returns:    void.
        public void cancel(){
            //  Log.
            Log.d(TAG, "cancel: Canceling ConnectThread");   //Cancel

            //  Try to close socket.
            try {
                m_socket.close();
            } catch (IOException e) {
                //  Log.
                Log.e(TAG, "cancel: Closing of ConnectThread Socket failed " + e.getMessage());

                //  Restart app.
                parent.restartApp();
            }
        }
    }

    //  start   --  Starts Bluetooth service.
    public synchronized void start(){
        //  Log.
        Log.d(TAG, "BluetoothConnectionService: started");

        //  Cancel ConnectThread if it exists.
        if (m_connectThread != null) {
            m_connectThread.cancel();
            m_connectThread = null;
        }

        //  Create and start AcceptThread.
        if (m_acceptThread == null) {
            m_acceptThread = new AcceptThread();
            m_acceptThread.start();
        }
    }

    //  killClient  --  Kills & nullifies ConnectedThread.
    //  Parameters: none.
    //  Returns:    void.
    public void killClient() {
        //  Cancel & delete ConnectedThread if it exists.
        if (m_connectedThread != null) {
            m_connectedThread.cancel();
            m_connectedThread = null;
        }
    }

    //  startClient --  Creates & starts ConnectedThread.
    public void startClient(BluetoothDevice device) {
        //  Log.
        Log.d(TAG, "startClient: started");

        //  Show user connection is being made.
        m_progressDialog = ProgressDialog.show(m_context, "Connecting Bluetooth", "This may take a while...", true);

        //  Create & start ConnectedThread.
        m_connectThread = new ConnectThread(device);
        m_connectThread.start();
    }

    //  ConnectedThread --  Runs continually while connected.
    private class ConnectedThread extends Thread {
        //  Essential objects.
        private final BluetoothSocket m_socket;
        private final InputStream m_inStream;
        byte m_buffer[];

        //  Constructor.
        //  Parameters:
        //      socket  --  BluetoothSocket for device.
        public ConnectedThread (BluetoothSocket socket) {
            //  Log.
            Log.i(TAG, "ConnectedThread: starting");

            //  Set socket.
            m_socket = socket;

            //  Dismiss progress dialog.
            m_progressDialog.dismiss();

            //  Set inputStream.
            InputStream tmpIn = null;
            try {
                tmpIn = m_socket.getInputStream();
            } catch (IOException e) {
                //  Log.
                Log.e(TAG, "ConnectedThread: Failed to get input stream");

                //  Restart app.
                parent.restartApp();
            }
            m_inStream = tmpIn;
        }

        //  run --  Runs the ConnectedThread.
        public void run(){
            //  Byte variables.
            m_buffer = new byte[1024]; //Buffer byte array - needs more space for longer strings
            int numBytes;  //Length of read bytes

            //  Log.
            Log.d(TAG, "Starting read loop...");

            //  Loop tasks infinitely.
            while (true) {
                //  Try to read input.
                try {
                    //  Process input when found.
                    if (m_inStream.available()> 0) {
                        //  Log.
                        Log.d(TAG, "Reading...");

                        //  Set message length.
                        numBytes = m_inStream.read(m_buffer);

                        //  Read message.
                        String incomingMessage = new String(m_buffer, 0, numBytes);

                        //  Log.
                        Log.d(TAG, "InputStream: " + incomingMessage);  //Log

                        //  Store & parse read message.
                        strDataRead = incomingMessage;
                        m_parseThread.parse(incomingMessage);
                    } else {
                        //  Sleep for 10 milliseconds if no input is found.
                        SystemClock.sleep(10);
                    }
                } catch (IOException e) {
                    //  Log.
                    Log.e(TAG, "run: Error reading input stream");

                    //  Restart app.
                    parent.restartApp();

                    //  Break loop, ending thread.
                    break;
                }
            }

            //  Log.
            Log.i(TAG, "Killing ConnectedThread!");

            //  Kill ConnectedThread.
            return;
        }

        //  cancel  --  Cancels ConnectedThread.
        //  Parameters: none.
        //  Returns:    void.
        public void cancel() {
            //  Try to close socket.
            try {
                m_socket.close();
            } catch(IOException e) {}
        }
    }

    //  connected   --  Starts ConnectedThread & ParseThread.
    private void connected(BluetoothSocket mmSocket){
        //  Log
        Log.d(TAG, "connected: starting");

        //  Create & start ConnectedThread.
        m_connectedThread = new ConnectedThread(mmSocket);   //Create ConnectedThread
        m_connectedThread.start();   //Start ConnectedThread

        //  Start ParseThread.
        m_parseThread.start();
    }
}