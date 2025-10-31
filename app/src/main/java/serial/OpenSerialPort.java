package serial;

import static gui.MyApp.GEN1;
import static gui.MyApp.GEN2;

import android.content.Context;
import android.os.Build;
import android.serialport.SerialPortFinder;
import android.util.Log;
import android.widget.Toast;

import packexcalib.exca.DataSaved;
import utils.MyDeviceManager;


public class OpenSerialPort {
    public static boolean mOpened;
    String mBaudrate;
    Device mDevice;

    public OpenSerialPort(Context ctx) {
        String[] mDevices;
        int aPosition = -1;

        try {
                SerialPortFinder serialPortFinder = new SerialPortFinder();
                mBaudrate = "115200";
                mDevices = serialPortFinder.getAllDevicesPath();
               String stringa="";
               if(DataSaved.my_comPort==0){
                   if (GEN1) {
                       stringa = "/dev/ttyWK0";
                   } else if (GEN2) {
                       stringa = "/dev/ttyS0";
                   }
               }else {
                   stringa=MyDeviceManager.serialCom(DataSaved.my_comPort);
               }

                String[] item = mDevices;
                for (int i = 0; i < item.length; i++)
                    if (item[i].contains(stringa))
                        aPosition = i;
                mDevice = new Device(mDevices[aPosition], mBaudrate);
                mOpened = SerialPortManager.instance().open(mDevice) != null;
                if (mOpened) {
                    Toast toast = Toast.makeText(ctx.getApplicationContext(), stringa, Toast.LENGTH_LONG);
                    toast.setGravity(0, 0, 0);
                    toast.show();

                    if (DataSaved.my_comPort == 3||DataSaved.my_comPort==0) {
                        try {
                            if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
                                SerialPortManager.instance().sendCommand("CONFIG UNDULATION AUTO\r\n");
                                SerialPortManager.instance().sendCommand("GNHDT 0.05\r\n");
                                SerialPortManager.instance().sendCommand("GPHDT 0.05\r\n");
                                SerialPortManager.instance().sendCommand("SAVECONFIG\r\n");
                            } else {
                                SerialPortManager.instance().sendCommand("CONFIG UNDULATION AUTO\r\n");
                                SerialPortManager.instance().sendCommand("GNGGA COM1 10\r\n");
                                SerialPortManager.instance().sendCommand("GPGGA COM1 10\r\n");
                                SerialPortManager.instance().sendCommand("GPHDT COM1 0.05\r\n");
                                SerialPortManager.instance().sendCommand("SAVECONFIG\r\n");
                            }

                        } catch (Exception e) {
                        }
                    }
                    {
                        if (DataSaved.gpsType == 1) {
                            //Log.d("Programmo","Programmo");
                            SerialPortManager.instance().sendCommand("SET,DEVICE.LOGLIST,GST:2000|GGA:100|HDT:100|GPLLQ:100|\r\n");
                           Thread.sleep(500);
                            SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");

                        }

                    }

                } else {

                    Toast.makeText(ctx.getApplicationContext(), "Serial COM Fail", Toast.LENGTH_LONG).show();
                    try {
                        mOpened = false;
                        SerialPortManager.instance().close();
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {

                Toast.makeText(ctx.getApplicationContext(), "Exception Serial COM Fail", Toast.LENGTH_LONG).show();
                try {
                    mOpened = false;
                    SerialPortManager.instance().close();

                } catch (Exception ex) {

                }
            }

    }

}
