package serial;

import android.os.Build;
import android.os.HandlerThread;
import android.serialport.SerialPort;

import androidx.annotation.RequiresApi;

import com.licheedev.hwutils.ByteUtil;
import com.licheedev.myutils.LogPlus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SerialPortManager {

    private static final String TAG = "SerialPortManager";

    private SerialReadThread mReadThread;
    private OutputStream mOutputStream;
    private HandlerThread mWriteThread;
    private Scheduler mSendScheduler;

    private static class InstanceHolder {
        public static SerialPortManager sManager = new SerialPortManager();
    }

    public static SerialPortManager instance() {
        return InstanceHolder.sManager;
    }

    private SerialPort mSerialPort;

    private SerialPortManager() {
    }

    public SerialPort open(Device device) {
        return open(device.getPath(), device.getBaudrate());
    }

    public SerialPort open(String devicePath, String baudrateString) {
        if (mSerialPort != null) {
            close();
        }

        try {
            File device = new File(devicePath);
            int baudrate = Integer.parseInt(baudrateString);
            mSerialPort = new SerialPort(device, baudrate);

            mReadThread = new SerialReadThread(mSerialPort.getInputStream(),devicePath);
            mReadThread.start();

            mOutputStream = mSerialPort.getOutputStream();

            mWriteThread = new HandlerThread("write-thread");
            mWriteThread.start();
            mSendScheduler = AndroidSchedulers.from(mWriteThread.getLooper());

            return mSerialPort;
        } catch (Throwable tr) {
            LogPlus.e(TAG, "Impossibile aprire la porta seriale", tr);
            close();
            return null;
        }
    }

    public void close() {
        if (mReadThread != null) {
            mReadThread.close();
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (mWriteThread != null) {
            mWriteThread.quit();
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private void sendData(byte[] datas) throws Exception {
        if (mOutputStream == null) {
            throw new IOException("OutputStream non è inizializzato.");
        }
        byte[] a = new byte[datas.length];
        for (int i = 0; i < datas.length; i++) {
            a[i] = (byte) (datas[i] & 0xff);
        }
        mOutputStream.write(a);
    }

    private Observable<Object> rxSendData(final byte[] datas) {
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                try {
                    sendData(datas);
                    emitter.onNext(new Object());
                } catch (Exception e) {
                    LogPlus.e("Invia：" + ByteUtil.bytes2HexStr(datas) + " fail", e);
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                        return;
                    }
                }
                emitter.onComplete();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendCommand(final String command) {
        LogPlus.i("" + command);
        byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
        rxSendData(bytes).subscribeOn(getSendScheduler()).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object o) {
                LogManager.instance().post(new SendMessage(command));
            }

            @Override
            public void onError(Throwable e) {
                LogPlus.e("Invio non riuscito", e);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public void sendCommand(final byte[] command) {
        rxSendData(command).subscribeOn(getSendScheduler()).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object o) {
                LogManager.instance().post(new SendMessage(ByteUtil.bytes2HexStr(command)));
                LogPlus.w("Invio riuscito", Arrays.toString(command));
            }

            @Override
            public void onError(Throwable e) {
                LogPlus.e("Invio non riuscito", e);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private Scheduler getSendScheduler() {
        if (mSendScheduler == null) {
            mWriteThread = new HandlerThread("write-thread");
            mWriteThread.start();
            mSendScheduler = AndroidSchedulers.from(mWriteThread.getLooper());
        }
        return mSendScheduler;
    }



    public OutputStream getmOutputStream() {
        return mOutputStream;
    }

    public SerialReadThread getmReadThread() {
        return mReadThread;
    }

    public SerialPort getmSerialPort() {
        return mSerialPort;
    }
}