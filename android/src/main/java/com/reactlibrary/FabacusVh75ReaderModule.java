
package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.BaseAdapter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.vanch.vhxdemo.Epc;
import com.vanch.vhxdemo.Status;
import com.vanch.vhxdemo.TimeoutException;
import com.vanch.vhxdemo.VH73Device;
import com.vanch.vhxdemo.helper.Utility;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;


public class FabacusVh75ReaderModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    BluetoothAdapter mBluetoothAdapter;
    boolean hasRequetBt = false;
    public static final int REQUEST_ENABLE_BT = 1;
    private String lastDeviceMac = "";
    private String connectToDevice;



    private  boolean isScanning = false;

    public static final String action_scan = "scan_click";
    public static final String action_disconnect = "disconnect_click";
    protected static final int CONNECTING = 1;
    protected static final int CONNECTING_OK = 2;
    protected static final int CONNECTING_FAILE = 3;
    protected static final int DISCONNECT = 4;

    public VH73Device vh73Device;
    public static class FreshList {

    }

    /**
     * device found event
     *
     * @author liugang
     */
    public static class BTDeviceFoundEvent {
        BluetoothDevice device;

        public BTDeviceFoundEvent(BluetoothDevice device) {
            this.device = device;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public void setDevice(BluetoothDevice device) {
            this.device = device;
        }

    }



    List<BluetoothDevice> foundDevices = new ArrayList<BluetoothDevice>();
    static VH73Device currentDevice;

    public FabacusVh75ReaderModule(ReactApplicationContext reactContext) {
        super(reactContext);



        EventBus.getDefault().register(this);
        this.reactContext = reactContext;
        initBluetooth();


    }


    /**
     * init bluetooth adaptor
     */
    private void initBluetooth() {
        //check support
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //bluetoothNotSupport();
            return;
        }

        //check enable
        if (!mBluetoothAdapter.isEnabled() && !hasRequetBt) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            hasRequetBt = true;
            return;
        }
    }
    /**
     * Event sender from React Native
     * @param reactContext
     * @param eventName
     * @param params
     */
    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }



    /**
     * RN bridge mnethod to connect to selected device
     * @param deviceName
     */
    @ReactMethod
    public void connectToDevice(String deviceName) {
        Log.e("CDCDC", "connectToDevice :; attemp to connect to "+deviceName);
        connectToDevice = deviceName;
        queryPairedDevices();

    }





    /**
     * RN bridge method to search devices already paired via Bluetooth
     */
    @ReactMethod
    public void searchDevices() {
        foundDevices.clear();
        //Gets a collection of paired remote Bluetooth devices
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext(); ) {
                BluetoothDevice device2 = (BluetoothDevice) it.next();
                //Prints the physical address of the remote Bluetooth device
                System.out.println("Connected Bluetooth device:" + device2.getAddress());

                WritableMap map = Arguments.createMap();
                map.putString("DeviceName",device2.getName());
                sendEvent(reactContext, "FabacusOnDeviceDetection",map);
            }
        } else {
            System.out.println("There is no paired remote Bluetooth device yet！");
        }


        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.startDiscovery();
        }
    }

    private Handler handle = new Handler(getReactApplicationContext().getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            WritableMap map;
            switch(msg.what) {
                case CONNECTING:
                    //progressDialog.setMessage(Strings.getString(R.string.msg_connecting));
                    // progressDialog.show();
                    Log.e("OGTAGDEBUG::", "handleMessage: CONNECTING" );

                    map = Arguments.createMap();


                    map.putString("status","CONNECTING");

                    sendEvent(reactContext, "FabacusOnDeviceConnectionChanghed",map);

                    break;
                case CONNECTING_OK:
                    //  progressDialog.setMessage(Strings.getString(R.string.msg_connected));
                    // progressDialog.show();
                    //  progressDialog.dismiss();
                    // refreshList();
                    Log.e("OGTAGDEBUG::", "handleMessage: CONNECTING_OK" );
                    map = Arguments.createMap();


                    map.putString("status","CONNECTING_OK");

                    sendEvent(reactContext, "FabacusOnDeviceConnectionChanghed",map);

                    break;
                case CONNECTING_FAILE:
                    //progressDialog.setMessage("Connecting failed...");
                    //progressDialog.show();
                    //   progressDialog.dismiss();
                    //  Utility.WarningAlertDialg(getActivity(), "", Strings.getString(R.string.msg_connect_fail)).show();
                    Log.e("OGTAGDEBUG::", "handleMessage: CONNECTING_FAILE" );
                    map = Arguments.createMap();


                    map.putString("status","CONNECTING_FAILED");

                    sendEvent(reactContext, "FabacusOnDeviceConnectionChanghed",map);

                    break;
                case DISCONNECT:
                    Log.e("OGTAGDEBUG::", "handleMessage: DISCONNECT" );

                    map = Arguments.createMap();


                    map.putString("status","DISCONNECT");

                    sendEvent(reactContext, "FabacusOnDeviceConnectionChanghed",map);
                    break;
            }
        }
    };





    public static class DoReadParam {

    }

    private void connect(final BluetoothDevice device) {
        handle.sendEmptyMessage(CONNECTING);
        new Thread() {
            public void run() {
                VH73Device vh75Device = new VH73Device(getCurrentActivity(), device);
                boolean succ = vh75Device.connect();
                if (succ) {
                    handle.sendEmptyMessage(CONNECTING_OK);
                    currentDevice = vh75Device;
                    EventBus.getDefault().post(new DoReadParam());
                    //ConfigUI.setConfigLastConnect(getActivity(), currentDevice.getAddress());
                } else {
                    handle.sendEmptyMessage(CONNECTING_FAILE);
                }
            }
        }.start();
    }


    //Querying paired devices
    private void queryPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                foundDevices.add(device);
                EventBus.getDefault().post(new BTDeviceFoundEvent(device));
                //last connected device
                if(device.getName().equals(connectToDevice) && currentDevice==null) {
                    currentDevice = new VH73Device(getCurrentActivity(), device);
                    connect(device);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "FabacusVh75Reader";
    }


    /**
     * INVENTORY
     */

    @ReactMethod
    public void activateScan() {
        Log.e("OGTAGDEBUG::", "activateScan: " + inventoring);

        if (currentDevice != null) {



            final Handler handler = new Handler() ;
            final Runnable inventoryRunnable = new Runnable() {
                public void run() {
                    if(inventoring) {
                        doInventory();
                        handler.postDelayed(this, 1000);
                    }else{
                        Log.e("OGTAGDEBUG::", "run: Removing CALLBACK"  );
                        handler.removeCallbacks(this);
                    }


                }
            };

            if (!inventoring) {
                inventoring = true;
                readCount = 0;


                WritableMap map = Arguments.createMap();
                map.putString("scanningStatus","ON");
                sendEvent(reactContext, "onDeviceScanningStatusChanged",map);

                Thread thread = new Thread() {
                    public void run() {
                        Looper.prepare();
                        handler.postDelayed(inventoryRunnable, 1000);
                        Looper.loop();
                    }
                };
                thread.start();


                try {
                    currentDevice.SetReaderMode((byte) 1);
                    byte[] res = currentDevice.getCmdResult();
                    if (!VH73Device.checkSucc(res)) {
                        inventoring = false;

                        return;

                    }else{
                        Log.e("OGTAGDEBUG::", "onEventBackgroundThread: OK" + res );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    currentDevice.SetReaderMode((byte) 1);
                    byte[] ret = currentDevice.getCmdResult();
                    if (!VH73Device.checkSucc(ret)) {

                        Log.e("OGTAGDEBUG::", "SetReaderMode Fail!: " );
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } else {
                inventoring = false;
                handler.removeCallbacks(inventoryRunnable);

                WritableMap map = Arguments.createMap();
                map.putString("scanningStatus","OFF");
                sendEvent(reactContext, "onDeviceScanningStatusChanged",map);
            }

        } else {
            Log.e("OGTAGDEBUG::", "activateScan: warning ");
        }
    }
    private void freshStatus() {
        Log.e("OGTAGDEBUG::", "freshStatus: " );
    }

    public static class StatusChangeEvent {

    }
    public void onEventMainThread(StatusChangeEvent e) {
        freshStatus();
    }

    public Status getOn() {
        return on;
    }

    public void setOn(Status on) {
        this.on = on;
        EventBus.getDefault().post(new StatusChangeEvent());
    }


    public void setRx(Status rx) {
        this.rx = rx;
        EventBus.getDefault().post(new StatusChangeEvent());
    }

    MediaPlayer findEpcSound;
    AudioManager audioManager;
    private Vibrator vibrator;
    long[] pattern = { 100, 400, 100, 400 };
    List<Epc> epcs = new ArrayList<Epc>();
    Map<String, Integer> epc2num = new ConcurrentHashMap<String, Integer>();

    Status on = Status.ON, tx = Status.BAD, rx = Status.BAD;
    boolean stoped = false;
    int readCount = 0;

    boolean inventoring = false;
    /**
     * clear id list
     */
    private void clearList() {
        if (epc2num != null && epc2num.size() > 0) {
            epc2num.clear();
            //refreshList();
        }
    }




    class InventoryThread extends Thread {
        int len, addr, mem;
        Strings mask;

        public InventoryThread(int len, int addr, int mem, Strings mask) {
            this.len = len;
            this.addr = addr;
            this.mem = mem;
            this.mask = mask;
        }

        public void run() {
            try {
                currentDevice.listTagID(1, 0, 0);

                Log.e("OGTAGDEBUG::", "run: start read!!"  );
                currentDevice.getCmdResult();
                Log.e("OGTAGDEBUG::", "run:  read ok!!"  );

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * inventory terminal event
     * @author liugang
     */
    public static class InventoryTerminal {
    }

    private static final String TAG = "inventory";

    public static class TimeoutEvent {
    }

    /**
     * an epc discovered
     * @author liugang
     */
    public static class EpcInventoryEvent {
    }

    /**
     * inventory button clicked
     * @author liugang
     */
    public static class InventoryEvent {
    }

    /**
     * check data length ......
     * @return
     */
    static Epc epcToBeAccess;

    private boolean checkAccessReadEnable() {
        if (epcToBeAccess == null || epcToBeAccess.getId().length() <= 0) {
            return false;
        }



        return true;
    }

    //ReadWordBlock
    private void readData() {
        if (!checkAccessReadEnable()) {
            return;
        }
        int mem,addr,lenData;
        String passwd = "00000000";

        if (epcToBeAccess==null || epcToBeAccess.getId().length() <= 0)
            return;
        mem = 0;


        addr = 0;
        // addr

        lenData = 0;



        try {
            currentDevice.ReadWordBlock(epcToBeAccess.getId(), mem, addr, lenData, passwd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //read
        try {
            byte[] ret = currentDevice.getCmdResultWithTimeout(300000);

            StringBuilder builder = new StringBuilder(Utility.bytes2HexString(ret));
            builder.delete(0, 6);
            builder.delete(builder.length()-2, builder.length());

            // Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_ReadWordBlock_title) + builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
//			e.printStackTrace();
            //  Utility.showTostInNonUIThread(getActivity(), Strings.getString(R.string.msg_ReadWordBlock_timeout));
        }
    }

    public void onEventBackgroundThread(InventoryEvent e) {


        // 1. To be consistent with the new VH75, so to add 0B command
        // Set the phone into the reader mode, that is, the module power to open, 1 - open, 0 - off


        try {
            currentDevice.SetReaderMode((byte) 1);
            byte[] res = currentDevice.getCmdResult();
            if (!VH73Device.checkSucc(res)) {
                // TODO show error message
                Log.e("OGTAGDEBUG::", "onEventBackgroundThread: ERROR" );
                // if (i > ) {
                inventoring = false;
                EventBus.getDefault().post(new InventoryTerminal());
                return;

            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // }

        while (inventoring) {
            // if (inventoring) {//this is a test!!!
            long lnow = android.os.SystemClock.uptimeMillis(); // 起始时间
            doInventory();
            while (true) {
                long lnew = android.os.SystemClock.uptimeMillis(); // 结束时间
                if (lnew - lnow > 500) {
                    break;
                }
            }
        }
        EventBus.getDefault().post(new InventoryTerminal());

        // 片断code开始 try { // 1.因为要跟新的VH75的一致，所以要加0B命令 //
        // 设置手机进入读写器模式，即模块电源打开，1--打开，0--关闭
        try {
            currentDevice.SetReaderMode((byte) 1);
            byte[] ret = currentDevice.getCmdResultWithTimeout(3000);
            if (!VH73Device.checkSucc(ret)) { // TODO show error message //
                Log.i(TAG, "SetReaderMode Fail!"); // return;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (TimeoutException e1) { // timeout
            Log.i(TAG, "Timeout!!@");
        }
        // 片断code结束
    }

    private void doInventory() {
        try {

            currentDevice.listTagID(1, 0, 0);
            byte[] ret = currentDevice.getCmdResult();
            if (!VH73Device.checkSucc(ret)) {
                //    // TODO show error message
                return;
            }


            Log.e("OGTAGDEBUG::", "doInventory: " );
            VH73Device.ListTagIDResult listTagIDResult = VH73Device.parseListTagIDResult(ret);
            addEpc(listTagIDResult);


            // read the left id
            int left = listTagIDResult.totalSize - 8;
            while (left > 0) {
                if (left >= 8) {
                    currentDevice.getListTagID(8, 8);
                    left -= 8;
                } else {
                    currentDevice.getListTagID(8, left);
                    left = 0;
                }
                byte[] retLeft = currentDevice.getCmdResult();
                if (!VH73Device.checkSucc(retLeft)) {
                    Log.e("OGTAGDEBUG::", "doInventory: command fail ");
                    continue;
                }
                VH73Device.ListTagIDResult listTagIDResultLeft = VH73Device
                        .parseGetListTagIDResult(retLeft);
                addEpc(listTagIDResultLeft);

            }


        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public void onEventMainThread(TimeoutEvent e) {
        inventoring = false;
        setRx(Status.BAD);
        EventBus.getDefault().post(new InventoryTerminal());
    }

    /**
     * inventory 结束
     * @param e
     */
    public void onEventMainThread(InventoryTerminal e) {

        inventoring = false;
        findEpcSound.stop(); //When the card is stopped, the ringing tone is paused and the ringtone is recalled when the card is reread
        setRx(Status.BAD);

    }

    private void addEpc(VH73Device.ListTagIDResult list) {
        ArrayList<byte[]> epcs = list.epcs;
        for (byte[] bs : epcs) {
            String string = Utility.bytes2HexString(bs);
            epc2num.put(string, 1);

            //To the following table how many lines, then the number of lines show, add by martrin 20131114


             WritableMap map = Arguments.createMap();
            map.putString(string,string);
             sendEvent(reactContext, "FabacusOnTagReceived",map);


            readCount = epc2num.size();
        }


    }

    private void addEpcTest(String strEpc) {
        if (epc2num.containsKey(strEpc)) {
            epc2num.put(strEpc, epc2num.get(strEpc) + 1);
        } else {
            epc2num.put(strEpc, 1);
        }
        readCount = epc2num.size();
    }

    /**
     * when inventory an epc, refresh the list
     *
     * @param e
     */
    public void onEventMainThread(EpcInventoryEvent e) {
        shock();
        playFindEpcSound();
    }

    private void playFindEpcSound() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        try {
            findEpcSound.setDataSource(getCurrentActivity(), alert);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            findEpcSound.setAudioStreamType(AudioManager.STREAM_ALARM);
            findEpcSound.setLooping(false);
            try {
                findEpcSound.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            findEpcSound.start();
        }
    }

    private void shock() {
        vibrator.vibrate(pattern, -1);
    }




    public static class Strings {
        public static final String LANGUAGE_ENGLISH = "english";
        public static final String LANGUAGE_CHINESE = "chinese";
        /** 一个Integer对应一个string或者string[]，即&lt;Integer, Object&gt;。 */
        public static HashMap<Integer, Object> stringCustom;
        /** 标识当前显示的语系；默认值为英文。 */
        public static String language = LANGUAGE_ENGLISH;

        public static String getLanguage() {
            return language;
        }



        private static String[] readStringArray(XmlResourceParser xmlParser)
                throws XmlPullParserException, IOException {
            String[] arr = null;
            LinkedList<String> list = new LinkedList<String>();
            String tagName, tagValue;
            while (true) {
                xmlParser.next();
                tagName = xmlParser.getName();
                if ("string-array".equals(tagName)) {
                    arr = new String[list.size()];
                    // 这个函数设计得好奇怪，传参和返参都一样。
                    // list.toArray(arr);作用同下：
                    arr = list.toArray(arr);
                    break;
                }
                tagName = xmlParser.getName();
                if ((xmlParser.getEventType() == XmlResourceParser.START_TAG) && tagName.equals("item")) {
                    xmlParser.next();
                    tagValue = xmlParser.getText();
                    list.add(tagValue);
                    // Log.d("ANDROID_LAB", tagName + "=" + tagValue);
                }
            }
            return arr;
        }

        private static HashMap<Integer, Object> readStringsXML(Context context, int xmlId) {
            HashMap<Integer, Object> hashMap = new HashMap<Integer, Object>();
            Resources res = context.getResources();
            String pkg = context.getPackageName();
            XmlResourceParser xmlParser = context.getResources().getXml(xmlId);
            try {
                String tagName, attName, attValue, tagValue;
                int identifier = -1;
                int eventType = xmlParser.next();
                while (eventType != XmlResourceParser.END_DOCUMENT) {
                    if (eventType == XmlResourceParser.START_DOCUMENT) {
                        // Log.d("ANDROID_LAB", "[Start document]");
                    } else if (eventType == XmlResourceParser.END_DOCUMENT) {
                        // Log.d("ANDROID_LAB", "[End document]");
                    } else if (eventType == XmlResourceParser.START_TAG) {
                        tagName = xmlParser.getName();
                        if ("string".equals(tagName)) {
                            attName = xmlParser.getAttributeName(0);
                            attValue = xmlParser.getAttributeValue(0);
                            eventType = xmlParser.next();
                            if (eventType == XmlResourceParser.TEXT) {
                                tagValue = xmlParser.getText();
                                // Log.d("ANDROID_LAB", "[Start tag]" + tagName +
                                // " " + attName + "="
                                // + attValue + " tagValue=" + tagValue);
                                identifier = res.getIdentifier(attValue, "string", pkg);
                                hashMap.put(identifier, tagValue);
                                // Log.d("ANDROID_LAB",
                                // Integer.toHexString(identifier) + " " + attValue
                                // + "=" + tagValue);
                            }
                        } else if ("string-array".equals(tagName)) {
                            attName = xmlParser.getAttributeName(0);
                            attValue = xmlParser.getAttributeValue(0);
                            identifier = res.getIdentifier(attValue, "array", pkg);
                            String[] arr = readStringArray(xmlParser);
                            hashMap.put(identifier, arr);
                            // Log.d("ANDROID_LAB", "[Start tag]" + tagName + " " +
                            // attName + "="
                            // + attValue);
                        }
                    } else if (eventType == XmlResourceParser.END_TAG) {
                        // Log.d("ANDROID_LAB", "[End tag]" + xmlParser.getName());
                    } else if (eventType == XmlResourceParser.TEXT) {
                        // Log.d("ANDROID_LAB", "[Text]" + xmlParser.getText());
                    }
                    eventType = xmlParser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hashMap;
        }



    }


}