package utils;


import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cpdevice.cpcomm.boards.CPDEVICE;
import com.cpdevice.cpcomm.common.SocketCanFrameRxListener;
import com.cpdevice.cpcomm.common.VoltageChangedListener;
import com.cpdevice.cpcomm.datalink.CPV3DataLink;
import com.cpdevice.cpcomm.exception.CPBusException;
import com.cpdevice.cpcomm.frame.ICPCanFrame;
import com.cpdevice.cpcomm.frame.ISocketCanFrame;
import com.cpdevice.cpcomm.port.SpiPort;
import com.cpdevice.cpcomm.proto.CPV3Protocol;
import com.cpdevice.cpcomm.proto.CPVxProtocolProxy;
import com.cpdevice.cpcomm.proto.Protocol;
import com.cpdevice.cpcomm.proto.SocketCanProxy;

import packexcalib.exca.DataSaved;

/**
 * CPCanHelper is a singleton class that provides a unified interface for CAN (Controller Area Network) communication
 * across different devices. It supports devices like SRT8PROS, SRT7PROS, and APOLLO2 with specific configurations and
 * communication protocols.
 */
public class CPCanHelper {
    static final String APOLLO2_SPI_DATA_IND = "/sys/class/gpio/gpio104/value";
    static final String APOLLO2_SPI_PORT = "/dev/spidev2.0";
    static final String APOLLO2_UART_PORT = "/dev/ttyS9";
    public static String voltApollo2 = "0.0";


    /**
     * The singleton instance of the CPCanHelper class.
     */
    public static volatile CPCanHelper INSTANCE = null;
    static SocketCanProxy mSocketCanProxy;
    static SocketCanProxy mSocketCanProxy_2;
    /**
     * The device brand name.
     */
    static String device;

    /**
     * The proxy for CPVx protocol communication.
     */
    static CPVxProtocolProxy mProxy;

    /**
     * The SPI port for communication on APOLLO2 devices.
     */
    static SpiPort mSpi;

    /**
     * The data link for CPV3 protocol communication.
     */
    static CPV3DataLink mCPVxDataLink;


    /**
     * The protocol handler for CPV3 protocol.
     */
    static CPV3Protocol mCPVxProtocol;


    /**
     * Gets the singleton instance of CPCanHelper.
     * Uses double-checked locking to ensure that the instance is created only once.
     *
     * @return The singleton instance of CPCanHelper.
     */
    public static CPCanHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (CPCanHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CPCanHelper();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * Action interface to define the execution of CAN frame reception.
     */
    public interface Action {
        /**
         * Executes the action with the provided CAN frame data.
         *
         * @param channel The CAN channel number.
         * @param id      The CAN frame ID.
         * @param data    The CAN frame data.
         */
        void execute(int channel, int id, byte[] data);
    }


    /**
     * Private constructor to initialize the CPCanHelper instance with the device brand.
     */
    private CPCanHelper() {
        this.device = Build.BRAND;
    }

    /**
     * Starts the CAN communication by setting up the necessary configurations
     * based on the device type and assigns a listener for receiving CAN frames.
     *
     * @param action The action to be executed when a CAN frame is received.
     */
    public synchronized void start(Action action) {
        try {
            switch (device) {
                case "SRT8PROS":
                case "SRT7PROS":
                case "APOLLO2_10":
                case "APOLLO2_7":

                    try {

                        mProxy = new CPVxProtocolProxy();
                        mProxy.setCANBaudrate(DataSaved.boudrateCAN1, DataSaved.boudrateCAN2);
                        mProxy.setCanFrameRxListener((i, i1, b, b1, i2, bytes) -> {
                            action.execute(i, i1, bytes);
                        });
                        mProxy.connect(null, null);

                        if (mProxy != null && mProxy.isConnected()) {
                            mProxy.setOnVoltageChanged(new VoltageChangedListener() {
                                @Override
                                public void onVoltageChanged(float v) {
                                    voltApollo2 = String.valueOf(v);
                                }
                            });
                        }

                    } catch (CPBusException e) {
                        Log.e("CanError", e.toString() + "  mProxy");
                    }

                    break;

                case "TANK2_7_10":

                    try {

                        mSpi = new SpiPort(CPDEVICE.APOLLO2.SPI2_0, CPDEVICE.APOLLO2.SPI_DATA_IND, true);
                        mCPVxDataLink = new CPV3DataLink();
                        mCPVxProtocol = new CPV3Protocol();
                        ((CPV3Protocol) mCPVxProtocol).config(DataSaved.boudrateCAN1, DataSaved.boudrateCAN2);
                        ((CPV3Protocol) mCPVxProtocol).clearFilters(ICPCanFrame.Channel.CHN_1);
                        ((CPV3Protocol) mCPVxProtocol).clearFilters(ICPCanFrame.Channel.CHN_2);

                        ((ICPCanFrame) mCPVxProtocol).setCanFrameRxListener((channel, id, idType, remote, dlc, canpack) -> {
                            action.execute(channel, id, canpack);


                        });
                        mCPVxProtocol.connect(mCPVxDataLink, mSpi);
                        getVoultage();
                    } catch (CPBusException e) {
                        Log.e("CanError", Log.getStackTraceString(e));
                    }
                    break;
                case "APOLLO2_12_PRO":
                case "APOLLO2_12_PLUS":

                    try {
                        if (DataSaved.boudrateCAN1 == 0) {
                            DataSaved.boudrateCAN1 = 250000;
                        }
                        if (DataSaved.boudrateCAN2 == 0) {
                            DataSaved.boudrateCAN2 = 250000;
                        }
                        mProxy = new CPVxProtocolProxy();
                        mProxy.connect(null, null);
                        if (mProxy != null && mProxy.isConnected()) {
                            mProxy.setOnVoltageChanged(new VoltageChangedListener() {
                                @Override
                                public void onVoltageChanged(float v) {
                                    voltApollo2 = String.valueOf(v);
                                }
                            });
                        }
                        mSocketCanProxy = new SocketCanProxy(1);
                        mSocketCanProxy.setCANBaudrate(DataSaved.boudrateCAN1, Protocol.CAN_BAUD_2M);
                        mSocketCanProxy.clearFilters();
                        mSocketCanProxy.setCanFrameRxCallback(new SocketCanFrameRxListener() {
                            @Override
                            public void onCanReceive(int i, int i1, byte[] bytes) {
                                action.execute(1, i & 0x1FFFFFFF, bytes);
                            }

                            @Override
                            public void onCanFdReceive(int i, int i1, int i2, byte[] bytes) {
                                //Log.d("MainActivity" ,"CANFD i:" + i + " i1:" + i1);
                            }
                        });
                        mSocketCanProxy.connect(null, null);

                        //can2
                        mSocketCanProxy_2 = new SocketCanProxy(2);
                        mSocketCanProxy_2.setCANBaudrate(DataSaved.boudrateCAN2, Protocol.CAN_BAUD_2M);
                        mSocketCanProxy_2.setCanFrameRxCallback(new SocketCanFrameRxListener() {
                            @Override
                            public void onCanReceive(int i, int i1, byte[] bytes) {
                                action.execute(2, i & 0x1FFFFFFF, bytes);

                            }

                            @Override
                            public void onCanFdReceive(int i, int i1, int i2, byte[] bytes) {
                                //
                            }
                        });
                        mSocketCanProxy_2.connect(null, null);


                    } catch (CPBusException e) {
                        Log.e("MainActivity", e.toString() + "  APOLLO2_12_PRO");
                    }
                    break;


            }
        } catch (Exception e) {
            //new CustomToast(MyApp.visibleActivity,e.toString()).show_error();
        }
    }

    public void disconnectAll() {
        try {

            switch (device) {
                case "SRT8PROS":
                case "SRT7PROS":
                case "APOLLO2_10":
                case "APOLLO2_7":

                    if (mProxy.isConnected()) {
                        mProxy.disconnect();
                        mProxy.release();


                    }
                    break;


                case "TANK2_7_10":

                    //CPV3 release
                    if (mCPVxProtocol != null) mCPVxProtocol.disconnect();
                    if (mCPVxProtocol != null) mCPVxProtocol.release();
                    if (mCPVxDataLink != null) mCPVxDataLink.release();
                    if (mSpi != null) mSpi.release();

                    break;


                case "APOLLO2_12_PRO":
                case "APOLLO2_12_PLUS":
                    if (mSocketCanProxy != null) {
                        mSocketCanProxy.release();
                    }
                    if (mSocketCanProxy_2 != null) {
                        mSocketCanProxy_2.release();
                    }
                    break;


            }

        } catch (Exception e) {
            Log.e("proxyC", e.toString());
            e.printStackTrace();
        }
    }

    public void getVoultage() {
        if (mCPVxProtocol != null) {
            if (mCPVxProtocol.isConnected()) {
                mCPVxProtocol.setOnVoltageChanged(new VoltageChangedListener() {
                    @Override
                    public void onVoltageChanged(float v) {
                        voltApollo2 = String.valueOf(v);

                    }
                });
            }
        }
    }


    /**
     * Sends a CAN frame to the specified channel and ID with the provided data.
     *
     * @param channel The CAN channel to send the frame to.
     * @param id      The CAN frame ID.
     * @param data    The data to be sent in the CAN frame.
     */
    public synchronized void canWrite(ICPCanFrame.Channel channel, int id, @NonNull byte[] data) {

        try {
            switch (device) {
                case "SRT8PROS":
                case "SRT7PROS":
                case "APOLLO2_10":
                case "APOLLO2_7":
                    if (mProxy != null) {
                        if (mProxy.isConnected()) {
                            boolean t = id > 2047;
                            mProxy.sendCanFrame(channel.ordinal(), id, t, false, data.length, data);
                        }
                    }
                    break;

                case "TANK2_7_10":
                    try {
                        if (mCPVxProtocol != null) {
                            if (mCPVxProtocol.isConnected()) {
                                boolean b = id > 2047;
                                ((ICPCanFrame) mCPVxProtocol).sendCanFrame(channel.ordinal(), id, b, false, data.length, data);
                            }
                        }
                    } catch (Exception ignored) {

                    }
                    break;


                case "APOLLO2_12_PRO":
                case "APOLLO2_12_PLUS":
                    switch (channel.ordinal()) {
                        case 1:
                            if (mSocketCanProxy != null) {

                                if (id > 2047) {
                                    mSocketCanProxy.sendCanFrame(id | ISocketCanFrame.CAN_EFF_FLAG, data.length, data);
                                } else {
                                    mSocketCanProxy.sendCanFrame(id, data.length, data);
                                }


                            }
                            break;
                        case 2:
                            if (mSocketCanProxy_2 != null) {

                                if (id > 2047) {
                                    mSocketCanProxy_2.sendCanFrame(id | ISocketCanFrame.CAN_EFF_FLAG, data.length, data);
                                } else {
                                    mSocketCanProxy_2.sendCanFrame(id, data.length, data);
                                }


                            }
                            break;

                    }


                    break;


            }
        } catch (Exception e) {
            //new CustomToast(MyApp.visibleActivity,e.toString()).show_error();
        }
    }
}