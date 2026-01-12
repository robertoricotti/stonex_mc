package gui;

import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.MyApp.isOffgrid;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;

import gui.gps.NmeaGenerator;
import packexcalib.exca.DataSaved;
import utils.MyData;
import utils.MyDeviceManager;

public class BaseClass extends AppCompatActivity {
    boolean isPressed,isTouched;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("KeyEve", String.valueOf(event.getKeyCode()));

        if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {

            if(!isTouched) {
                isTouched=true;
                if (hAlarm && isPressed) {
                    isPressed = false;
                }
                if ((hAlarm||isOffgrid) && isApollo && !isPressed) {
                    MyDeviceManager.OUT1(MyApp.visibleActivity,0);
                    MyDeviceManager.OUT2(MyApp.visibleActivity,0);
                    isPressed = true;
                    (new Handler()).postDelayed(this::SafetyDelay, 10000);
                }
            }
            return true;
        }


        return super.onKeyDown(keyCode, event);
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {

                case KeyEvent.KEYCODE_VOLUME_UP:

                    if(!isTouched) {
                        isTouched=true;
                        if (hAlarm && isPressed) {
                            isPressed = false;
                        }
                        if ((hAlarm||isOffgrid) && isApollo && !isPressed) {
                            MyDeviceManager.OUT1(MyApp.visibleActivity,0);
                            MyDeviceManager.OUT2(MyApp.visibleActivity,0);
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


        return super.onKeyUp(keyCode, event);
    }

    private void SafetyDelay() {
        if(hAlarm){
            if (isApollo) {
                MyDeviceManager.OUT1(MyApp.visibleActivity,1);
                isPressed=false;
            }
        }
        if(isOffgrid){
            if (isApollo) {
                MyDeviceManager.OUT2(MyApp.visibleActivity,1);
                isPressed=false;
            }
        }
    }


}
