package services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import event_bus.CMD_Event;
import event_bus.SerialEvent;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;
import utils.MyDeviceManager;


public class Bluetooth_GNSS_Service extends Service {

    public static boolean GNSSServiceState = false;
    private final IBinder mBinder = new BluetoothGNSSBinder();
    private BufferedInputStream mmInputStream_Gnss = new BufferedInputStream(null);
    //public static boolean gpsIsConnected;
    final int handlerState = 0;
    //used to identify handler message
    Handler bluetoothIn;
    private BluetoothAdapter btAdapter = null;

    private ConnectingThread mConnectingThread;
    private static ConnectedThread mConnectedThread;

    private boolean stopThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static final String MAC_ADDRESS = DataSaved.macaddress;

    private StringBuilder recDataString = new StringBuilder();

    @Override
    public void onCreate() {
        super.onCreate();


        stopThread = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("BT SERVICE", "SERVICE STARTED");
        bluetoothIn = new Handler() {

            public void handleMessage(android.os.Message msg) {
                bluetoothIn = new Handler() {
                    public void handleMessage(android.os.Message msg) {
                        if (msg.what == handlerState) {
                            String readMessage = (String) msg.obj;
                            recDataString.append(readMessage);

                            // Verifica se ci sono messaggi completi terminati con il delimitatore
                            while (recDataString.indexOf("\n") != -1) {
                                // Estrai un messaggio completo
                                String completeMessage = recDataString.substring(0, recDataString.indexOf("\n"));

                                // Do stuff here con il tuo messaggio completo

                                if (MyDeviceManager.serialCom(DataSaved.my_comPort).equals("BT")) {
                                    EventBus.getDefault().post(new CMD_Event(completeMessage));
                                    EventBus.getDefault().post(new SerialEvent(completeMessage));
                                    new NmeaListener().NmeaStandard(completeMessage);
                                }
                                if (!MyDeviceManager.serialCom(DataSaved.my_comPort).equals("BT")) {
                                    stopSelf();
                                }

                                // Rimuovi il messaggio completo dal buffer
                                recDataString.delete(0, recDataString.indexOf("\n") + 1);
                            }
                        }
                    }
                };


            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            bluetoothIn.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        stopThread = true;
        if (mConnectedThread != null) {
            mConnectedThread.closeStreams();
        }
        if (mConnectingThread != null) {
            mConnectingThread.closeSocket();
        }
        GNSSServiceState = false;
        //new CustomToast(MyApp.visibleActivity,"BT DISCONNECTED").show();


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    @SuppressLint("MissingPermission")
    private void checkBTState() {
        if (btAdapter == null) {
            // new CustomToast(MyApp.visibleActivity,"BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE").show();

            stopSelf();
        } else {
            if (btAdapter.isEnabled()) {


                try {
                    BluetoothDevice device = btAdapter.getRemoteDevice(DataSaved.macaddress);
                    // new CustomToast(MyApp.visibleActivity,"CONNECTING TO: " + MAC_ADDRESS).show();

                    mConnectingThread = new ConnectingThread(device);
                    mConnectingThread.start();
                } catch (IllegalArgumentException e) {
                    // new CustomToast(MyApp.visibleActivity,"ILLEGAL MAC ADDRESS, STOPPING SERVICE").show();

                    stopSelf();
                }
            } else {
                //new CustomToast(MyApp.visibleActivity,"BLUETOOTH NOT ON, STOPPING SERVICE").show();


                stopSelf();
            }
        }
    }

    // New Class for Connecting Thread
    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        @SuppressLint("MissingPermission")
        ConnectingThread(BluetoothDevice device) {
            Log.d("DEBUG BT", "IN CONNECTING THREAD");
            mmDevice = device;
            BluetoothSocket temp = null;
            Log.d("DEBUG BT", "MAC ADDRESS : " + MAC_ADDRESS);
            Log.d("DEBUG BT", "BT UUID : " + BTMODULEUUID);
            try {
                temp = mmDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
                Log.d("DEBUG BT", "SOCKET CREATED : " + temp.toString());

            } catch (IOException e) {
                Log.d("DEBUG BT", "SOCKET CREATION FAILED :" + e.toString());
                Log.d("BT SERVICE", "SOCKET CREATION FAILED, STOPPING SERVICE");

                stopSelf();
            }
            mmSocket = temp;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            super.run();
            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection

            //btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called
                mConnectedThread.write("GETALL\n\r");
            } catch (IOException e) {
                try {
                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");

                    mmSocket.close();

                    stopSelf();
                } catch (IOException e2) {
                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");

                    stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");

                stopSelf();
            }
        }

        void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT 187", e2.toString());
                Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");

                stopSelf();
            }
        }
    }

    // New Class for Connected Thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        ConnectedThread(BluetoothSocket socket) {
            Log.d("DEBUG BT", "IN CONNECTED THREAD");
            GNSSServiceState = true;
            //new CustomToast(MyApp.visibleActivity,"BT CONNECTED").show();
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("DEBUG BT 218", e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");

                stopSelf();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d("DEBUG BT", "IN CONNECTED THREAD RUN");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep looping to listen for received messages
            while (true && !stopThread) {
                try {
                    bytes = mmInStream.read(buffer); //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d("DEBUG BT PART", "CONNECTED THREAD " + readMessage);//dati di input qui

                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.d("DEBUG BT 244", e.toString());
                    Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");

                    stopSelf();
                    break;
                }
            }
        }

        //write method
        void write(String input) {
            byte[] msgBuffer = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer); //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Log.d("DEBUG BT", "UNABLE TO READ/WRITE " + e.toString());
                Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");

                stopSelf();
            }
        }

        void closeStreams() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT 263", e2.toString());
                Log.d("BT SERVICE", "STREAM CLOSING FAILED, STOPPING SERVICE");

                stopSelf();
            }
        }
    }

    public class BluetoothGNSSBinder extends Binder {
        Bluetooth_GNSS_Service getService() {
            return Bluetooth_GNSS_Service.this;
        }
    }

    public static void sendGNSSata(String s) {
        if (mConnectedThread != null) {
            mConnectedThread.write(s);
        }
    }
}