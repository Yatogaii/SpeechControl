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
import java.util.UUID;

public class BluetoothService extends Thread{
    private BluetoothServerSocket mServerSocket;
    private BluetoothAdapter BA;
    private Handler mHandler;
    private static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothSocket mSocket;
    private MainActivity activity;
    public BluetoothService(BluetoothAdapter BA, Handler mHandler, MainActivity activity){
        this.mHandler = mHandler;
        mSocket = null;
        this.activity = activity;
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
                    Log.d("BlueToothConnect:", mDevice.getName());
                    mSocket = tmpSoc;
                    Message mes = new Message();
                    mHandler.sendMessage(mes);
                }
                else Log.d("BlueTooth:", "error");
                new ConnectedThread(mSocket,mDevice).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private class ConnectedThread extends Thread{
        BluetoothSocket mSocket;
        BluetoothDevice mDevice;
        InputStream mIunput;
        OutputStream mOutut;
        byte[] validMes;

        SmsManager mSmsManager;

        int cntValid = 0;
        ConnectedThread(BluetoothSocket mSocket,BluetoothDevice mDevice){
            this.mDevice = mDevice;
            this.mSocket = mSocket;

            mSmsManager = SmsManager.getDefault();

            validMes = new byte[10];
            try{
                mIunput = mSocket.getInputStream();
                mOutut = mSocket.getOutputStream();
            }catch (Exception e){e.printStackTrace();}
        }
        @Override
        public void run(){
            byte[] buffer = new byte[10];
            byte[]  bytes = new byte[10];
            try{
            mOutut.write(new byte[]{'L','I','N','K'});}catch (Exception e){e.printStackTrace();}
            while(true){
                try{
                    mIunput.read(bytes);
                    String tmp = new String(bytes);
                    Log.d("Blue Rec", tmp);
                    if(bytes[0] == '1' | bytes[0]==1){
                        Log.d("收到标志位", "Start");
                        Message mes = new Message();
                        mHandler.sendMessage(mes);
                    }
//                    this.sleep(200);
                }catch (Exception e){

                }
            }
        }
        String setPriority(byte[] bytes){
            boolean isValid = false;
            String tmp = null;
            for (int i=0;i<bytes.length;i++){
                if (bytes[i]=='$'){
                    isValid = false;
                    cntValid = 0;
                    tmp = new String(validMes);
                }
                if (isValid){
                    validMes[cntValid++] = bytes[i];
                }
                /* 帧头为# */
                if (bytes[i]=='#'){
                    isValid = true;
                }
            }
            return tmp;
        }
    }
}
