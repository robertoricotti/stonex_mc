package gui;

import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;

import android.os.Handler;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;

import gui.gps.NmeaGenerator;
import packexcalib.exca.DataSaved;
import utils.MyData;
import utils.MyDeviceManager;

public class BaseClass extends AppCompatActivity {
    boolean isPressed,isTouched;
    static int countSalva;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
            // F1
            NmeaGenerator.LATITUDE += 0.3;
            DataSaved.demoNORD =NmeaGenerator.LATITUDE;

            if(!isTouched) {
                isTouched=true;
                if (hAlarm && isPressed) {
                    isPressed = false;
                }
                if (hAlarm && isApollo && !isPressed) {
                    MyDeviceManager.OUT1(MyApp.visibleActivity,0);

                    isPressed = true;
                    (new Handler()).postDelayed(this::SafetyDelay, 10000);
                }
            }
            return true;
        }

        if (MyDeviceManager.serialCom(DataSaved.my_comPort).equals("DEMO") ) {

            if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
                // F1
                NmeaGenerator.LATITUDE += 0.3;
                DataSaved.demoNORD =NmeaGenerator.LATITUDE;


                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F2) {
                // F2
                NmeaGenerator.LATITUDE -= 0.3;
                DataSaved.demoNORD =NmeaGenerator.LATITUDE;
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F3) {
                // F3
                NmeaGenerator.LONGITUDE -= 0.3;
                DataSaved.demoEAST =NmeaGenerator.LONGITUDE;
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F4) {
                // F4
                NmeaGenerator.LONGITUDE += 0.3;
                DataSaved.demoEAST =NmeaGenerator.LONGITUDE;
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F5) {
                // F5
                NmeaGenerator.ALTITUDE += 0.01;
                DataSaved.demoZ=NmeaGenerator.ALTITUDE;
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F6) {
                // F6
                NmeaGenerator.ALTITUDE -= 0.01;
                DataSaved.demoZ=NmeaGenerator.ALTITUDE;
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F7) {
                // F7
                NmeaGenerator.HEADING -= 0.5;
                if (NmeaGenerator.HEADING >= 360) {
                    NmeaGenerator.HEADING = 0;
                } else if (NmeaGenerator.HEADING <= 0) {
                    NmeaGenerator.HEADING = 360;
                }
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_F8) {
                // F8
                NmeaGenerator.HEADING += 0.5;
                if (NmeaGenerator.HEADING >= 360) {
                    NmeaGenerator.HEADING = 0;
                } else if (NmeaGenerator.HEADING <= 0) {
                    NmeaGenerator.HEADING = 360;
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {

                case KeyEvent.KEYCODE_VOLUME_UP:
                    NmeaGenerator.LATITUDE += 0.3;
                    DataSaved.demoNORD =NmeaGenerator.LATITUDE;
                    if(!isTouched) {
                        isTouched=true;
                        if (hAlarm && isPressed) {
                            isPressed = false;
                        }
                        if (hAlarm && isApollo && !isPressed) {
                            MyDeviceManager.OUT1(MyApp.visibleActivity,0);

                            isPressed = true;
                            (new Handler()).postDelayed(this::SafetyDelay, 10000);
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    // Blocca l'azione predefinita del pulsante Volume Down
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
            isTouched=false;
        }
        if(event.getKeyCode()==KeyEvent.KEYCODE_F2){
            MyData.push("demoNORD",String.valueOf(DataSaved.demoNORD));
            MyData.push("demoEAST",String.valueOf(DataSaved.demoEAST));
            MyData.push("demoZ",String.valueOf(DataSaved.demoZ));
        }

        return super.onKeyUp(keyCode, event);
    }

    private void SafetyDelay() {
        if(hAlarm){
            if (isApollo) {
                MyDeviceManager.OUT1(MyApp.visibleActivity,1);
                isPressed=false;
            }
        }
    }

}
