package com.example.p2p;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothManager {
    private String messageHolder = "";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private ChatController mChatController;
    private BluetoothDevice mDevice;
    private Context context;
    private P2PCallback callback;
    private String address="";
    Handler handler = new Handler();

    public BluetoothManager(Context context, P2PCallback callback){
        this.context = context;
        this.callback = callback;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatController = new ChatController(context, mHandler);
    }
    public ArrayList<BluetoothDevice> bluetoothSearch() {
        ArrayList<BluetoothDevice> list = new ArrayList<>();
        try {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!pairedDevices.isEmpty())
                list.addAll(pairedDevices);
        }catch (Exception e){}
        return list;
    }

    public void connectToDevice(String deviceAddress) {
        stop();
        address = deviceAddress;
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        mChatController.connect(mBluetoothDevice);
    }

    private void jsonMessage(String jsonData, boolean write) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;  //you can also calculate your inSampleSize
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];
            JSONObject messageJSON = new JSONObject(jsonData);
            ChatMessage message = new ChatMessage(messageJSON.get("message").toString(), messageJSON.get("from").toString());
            Log.i("checkConnection",message.getMessageText());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (mChatController.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(context, "Connection was lost!", Toast.LENGTH_SHORT).show();
            callback.disconnected();
            return;
        }
        if (message.length() > 0) {
            byte[] send = makeJSON(message).getBytes();
            if (send.length<=1024){
                mChatController.write(send);
            }else {
                List<byte[]> list = stringToList(makeJSON(message));
                myFor(list,0);
            }

        }
    }

    private String makeJSON(String message) {
        JSONObject json = new JSONObject();
        try {
            json.put("message", message);
            json.put("from", mBluetoothAdapter.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void myFor(List<byte[]> list, int i){
        int finalI = i;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("MyError", finalI +"data "+new String(list.get(finalI)));
                mChatController.write(list.get(finalI));
                int v = finalI +1;

                if (list.size()>v)
                    myFor(list,v);
            }
        },200);

    }

    public List<byte[]> stringToList(String data){

        List<byte[]> list = new ArrayList<>();
        int buffer = 800;
        int size = data.length();
        int max = size/buffer;
        if (size%buffer > 0){
            max++;
        }
        Log.i("sljifes",buffer +"   "+size +"   "+max);

        for (int i = 0; i < max; i++) {
            int start= i * buffer;
            int end= (i+1) * buffer;
            if (end>size)
                end = start +(size % buffer);
            Log.i("sljifes",start+"   "+end);
            String substring = data.substring(start, end);

            list.add(substring.getBytes());
        }
        return list;
    }

    public void stop(){
        if (mChatController != null) {
            mChatController.stop();
        }
    }

    public void onResume() {

        if (mChatController != null) {
            if (mChatController.getState() == Constants.STATE_NONE) mChatController.start();
        }
    }

    //--------------------------------------------------------------
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            //setTitle(mDevice.getName());
                            Log.i("jhjhjhj","Connected    up");
                            callback.connected();
                            break;
                        case Constants.STATE_CONNECTING:
                            //setTitle("Connecting...");
                            break;
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            //setTitle("Not connected");
                            callback.disconnected();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    jsonMessage(writeMessage, true);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i("checkConnection",readMessage);
                    Log.i("checkConnection","1");
                    jsonMessage(readMessage, false);

                    try {
                        JSONObject messageJSON = new JSONObject(readMessage);
                        String message2 = messageJSON.get("message").toString();
                        callback.receivedMessage(message2);
                        messageHolder = "";
                    } catch (Exception e) {
                        messageHolder = messageHolder +readMessage;
                        try {
                            JSONObject messageJSON = new JSONObject(messageHolder);
                            String message2 = messageJSON.get("message").toString();
                            callback.receivedMessage(messageJSON.toString());
                            messageHolder = "";//TODO remove
                        }catch (Exception e1){}
                        e.printStackTrace();
                    }


                    break;
                case Constants.MESSAGE_DEVICE_OBJECT:
                    mDevice = msg.getData().getParcelable(Constants.DEVICE_OBJECT);
                    Toast.makeText(context, "Connected to " + mDevice.getName(), Toast.LENGTH_SHORT).show();
                    Log.i("jhjhjhj","Connected    down");
                    Log.i("jhjhjhj",Thread.currentThread().getName());
                    callback.connected();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(context, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_LOST:
                    ChatController.sleep(200);
                    Toast.makeText(context, "Reconnected", Toast.LENGTH_SHORT).show();

                    //chatController.connect(mDevice);
                    break;
            }
            return false;
        }
    });




    private void checkConnection(){
        try {
            JSONObject obj = new JSONObject();
            try {
                obj.put("status","connect");
            } catch (JSONException e1) {}
            mChatController.write(obj.toString().getBytes());

        }catch (Exception e){

        }
    }

    public void checkStatus(){

    }
}
