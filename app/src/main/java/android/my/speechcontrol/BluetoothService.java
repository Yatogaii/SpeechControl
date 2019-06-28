package android.my.speechcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Thread{
    private BluetoothServerSocket mServerSocket;
    private BluetoothAdapter BA;
    private Handler mHandler;
    private OutputStream mOut;
    public static final int WHAT_CONNECTED = 1;
    public static final int WHAT_SENSOR = 0;
    private static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothSocket mSocket;         //m
    public BluetoothService(BluetoothAdapter BA, Handler mHandler){
        this.mHandler = mHandler;
        mSocket = null;
        this.BA = BA;
    }

    private class ReciveThread extends Thread {
        byte[] bytes ;
        @Override
        public void run(){
            bytes = new byte[]{'s','e','r','v','e','r'};
            try{
                InputStream is = mSocket.getInputStream();
                OutputStream os = mSocket.getOutputStream();
                os.write(bytes);
                while (true){
                    is.read(bytes);
                    Log.d("Recived:", bytes.toString());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void sendMessage(byte[] status){
//        new SendThread(mSocket,status);
//        Log.w("sendMessage", "sendMessage: 调用发送");
        try{
        mOut.write(status);}catch (Exception e){
            Log.w("sendMessage",e.toString()  );
        }
    }
    public void connect(BluetoothDevice device){
        String TAG = "BT connect:";
        int status = device.getBondState();
        /* 是否配对检查 */
        if (status==BluetoothDevice.BOND_NONE)
            device.createBond();

        Log.d(TAG, "Start");
        new ConnectThread(device).start();
    }
    private class ConnectThread extends Thread{
        BluetoothDevice mDevice;
        BluetoothSocket tmpSoc;
        ConnectThread(BluetoothDevice mDevices){

            this.mDevice = mDevices;
            try{
                tmpSoc = mDevice.createRfcommSocketToServiceRecord(mUUID);}catch (Exception e){e.printStackTrace();
            }
        }

        @Override
        public void run(){
            try{
                setName("Connect Thread");
                tmpSoc.connect();
                if (tmpSoc!=null){
                    mSocket = tmpSoc;
                    Log.w("BlueToothConnect:", mDevice.getName());
                }
                else Log.d("BlueTooth:", "error");
                new ConnectedThread(tmpSoc,mDevice).start();
                Message mes = new Message();
                mes.what = WHAT_CONNECTED;
                mHandler.sendMessage(mes);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class SendThread extends Thread{
        BluetoothDevice targetDevices;
        OutputStream mOutput;
        BluetoothSocket mSocket;
        int status;
        public SendThread(BluetoothSocket mSocket,int status){
            this.mSocket = mSocket;
            this.status = status;
        }
        @Override
        public void run() {
            try {
                mOutput = mSocket.getOutputStream();
                assert (mOutput != null);
                byte tmp = (byte)status;
                mOutput.write(tmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedList<BluetoothDevice> getBondedDeivices(){
        LinkedList<BluetoothDevice> devices = new LinkedList<>();
        devices.addAll(BA.getBondedDevices());
            return devices;
    }
    private class ConnectedThread extends Thread{
        BluetoothSocket mSocket;
        BluetoothDevice mDevice;
        InputStream mIunput;
        OutputStream mOutut;
        byte[] validMes;
        SmsManager mSmsManager;

        ConnectedThread(BluetoothSocket mSocket,BluetoothDevice mDevice){
            this.mDevice = mDevice;
            this.mSocket = mSocket;

            mSmsManager = SmsManager.getDefault();

            validMes = new byte[10];
            try{
                mIunput = mSocket.getInputStream();
                mOutut = mSocket.getOutputStream();
            }catch (Exception e){e.printStackTrace();}
            mOut = mOutut;
        }
        @Override
        public void run(){
            byte[] buffer = new byte[10];
            byte[]  bytes = new byte[10];
            try{
            mOutut.write(new byte[]{'L','I','N','K'});}catch (Exception e){e.printStackTrace();
            }
        }

    }
}