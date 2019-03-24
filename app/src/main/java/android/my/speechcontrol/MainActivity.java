package android.my.speechcontrol;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptC;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements EventListener {
    final String TAG = "自分の";
    /* 要发给飞行器的数据的分片 */
    byte[] angle;
    byte userCommit = 0;
    byte asrCommand = 0;
    byte asrCommandBackup = 0;

    Button speechControl;
    EditText inputText;

    TextView angleView;
    TextView flyMode;

    Button submit;
    Switch sensorSwitch;
    Handler mHandler;
    EventManager mEventManager;
    BluetoothService mBluetoothService;
    boolean isRecording = false;
    boolean isSensoring = false;
    boolean isConnected = false;
    String appid = "14502692";
    String appKey ="io8viNBppPhTkXzNqbN7i2j7";
    String secret ="NbkUE6Mi8N2aFAQl2tP2521Nwa5c4xzC";
    SensorClient mSensorClient;
    LinkedList<BluetoothDevice> mDevicesVector;
    Toolbar toolbar;
    public static final byte TAKE_OFF = 'T';
    public static final byte ROLL = 'R';
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* 自己的代码 */
        isRecording = false;
        /* 初始化包括语音识别和陀螺仪的功能 */
        init();
        /* 初始化一些按键 */
        initInterface();
        /* 动态申请权限 */
        initPermission();
        mSensorClient.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initInterface(){
        /* 显示飞行模式的文本框的初始化 */
        flyMode = findViewById(R.id.ModeViewer);
        angleView = findViewById(R.id.AngleView);
        angleView.setText("角度初始化中");
        flyMode.setText("默认飞行模式");
        /* 输入框和提交按钮的初始化 */
        inputText = findViewById(R.id.commnad);
        inputText.setInputType(InputType.TYPE_CLASS_NUMBER);
        submit = findViewById(R.id.commit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = inputText.getText().toString();
                if (tmp!=null && tmp.length()>0){
                    int commit;
                    commit = Integer.parseInt(tmp);
                    Log.w(TAG, "onClick:输入的数字"+commit);
                    userCommit = (byte) (commit/3);
                }
                else
                    Log.w(TAG, "onClick: 未输入内容");

            }
        });
        /* 设置体感检测开关 */
        sensorSwitch = findViewById(R.id.isSensoring);
        sensorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSensoring = isChecked;
            }
        });
        /* 为ActionBar上的B设置蓝牙连接监听器 */
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_settings:
                        Log.w(TAG, "onMenuItemClick: Setting");
                        break;
                    case R.id.bluetooth_connect:
                        Log.w(TAG, "onMenuItemClick: Bluetooth Connect");
                        try{
                        showDialogue();}catch (Exception e){e.printStackTrace();}
                        return true;

                }
                return false;
            }
        });
//        bluetoothConnect = toolbar.findViewById(R.id.bluetooth_connect);    //这个方法是要指定view的
//        if(bluetoothConnect != null)
//            ((View) bluetoothConnect).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    /* 调出蓝牙链接界面 */
//                    Log.w(TAG, "onClick: 蓝牙链接界面跳出");
//                }
//            });
//        else Log.w(TAG, "initInterface:Bluetooth is null " );
        /* 为录音开关按钮设置监听器 */
        speechControl = findViewById(R.id.speechControl);
        speechControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN){         //按键按下
                    Map<String, Object> params = new LinkedHashMap<String, Object>();
                    params.put(SpeechConstant.APP_ID, appid);
                    params.put(SpeechConstant.APP_KEY,appKey);
                    params.put(SpeechConstant.SECRET,secret);
                    params.put(SpeechConstant.PID, 1536);  //默认1536
                    params.put(SpeechConstant.DECODER, 0); // 纯在线(默认)
                    params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN); // 语音活动检测
//                     params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 2000); // 不开启长语音。开启VAD尾点检测，即静音判断的毫秒数。建议设置800ms-3000ms
                    params.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);// 是否需要语音音频数据回调
                    params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);// 是否需要语音音量数据回调
                    params.put(SpeechConstant.VAD_TOUCH,true);         //这一句控制按下结束录音键之后才会开始发送语音数据
                    // 自动检查是否有错误
                    (new AutoCheck(getApplicationContext(), new Handler() {
                        public void handleMessage(Message msg) {
                            if (msg.what == 100) {
                                AutoCheck autoCheck = (AutoCheck) msg.obj;
                                synchronized (autoCheck) {
                                    String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                                    Log.w("AutoCheckMessage", message);
                                }
                            }
                        }
                    },false)).checkAsr(params);

                    mEventManager.send(SpeechConstant.ASR_START, new JSONObject(params).toString(), null, 0, 0);    //开始录音
                    isRecording = true;
                    speechControl.setText("停止录音");      //改变开关显示文字
                } else if (action == MotionEvent.ACTION_UP){//按键松开
                    mEventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0); // 发送停止录音事件，提前结束录音等待识别结果
//                    mEventManager.send(SpeechConstant.ASR_CANCE, null, null, 0, 0); // 取消本次识别，取消后将立即停止不会返回识别结果
                    isRecording = false;
                    speechControl.setText("开始录音");
                    Toast.makeText(getApplicationContext(),"请稍等识别结果",Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        //尝试使用长按键
        /*
        speechControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording){
                    Map<String, Object> params = new LinkedHashMap<String, Object>();
                    params.put(SpeechConstant.APP_ID, appid);
                    params.put(SpeechConstant.APP_KEY,appKey);
                    params.put(SpeechConstant.SECRET,secret);
                    params.put(SpeechConstant.PID, 1536);  //默认1536
                    params.put(SpeechConstant.DECODER, 0); // 纯在线(默认)
                    params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN); // 语音活动检测
//                     params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 2000); // 不开启长语音。开启VAD尾点检测，即静音判断的毫秒数。建议设置800ms-3000ms
                    params.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);// 是否需要语音音频数据回调
                    params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);// 是否需要语音音量数据回调
                    params.put(SpeechConstant.VAD_TOUCH,true);         //这一句控制按下结束录音键之后才会开始发送语音数据
                    // 自动检查是否有错误
                    (new AutoCheck(getApplicationContext(), new Handler() {
                        public void handleMessage(Message msg) {
                            if (msg.what == 100) {
                                AutoCheck autoCheck = (AutoCheck) msg.obj;
                                synchronized (autoCheck) {
                                    String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                                    Log.w("AutoCheckMessage", message);
                                }
                            }
                        }
                    },false)).checkAsr(params);

                    mEventManager.send(SpeechConstant.ASR_START, new JSONObject(params).toString(), null, 0, 0);    //开始录音
                    isRecording = true;
                    speechControl.setText("停止录音");      //改变开关显示文字
                }else{
                    mEventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0); // 发送停止录音事件，提前结束录音等待识别结果
//                    mEventManager.send(SpeechConstant.ASR_CANCE, null, null, 0, 0); // 取消本次识别，取消后将立即停止不会返回识别结果
                    isRecording = false;
                    speechControl.setText("开始录音");
                    Toast.makeText(getApplicationContext(),"请稍等识别结果",Toast.LENGTH_SHORT).show();
                }

            }
        });*/
    }
    private void init() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message mes){
                switch (mes.what){
                    case BluetoothService.WHAT_CONNECTED:
                        /* 蓝牙是否链接，以便进行判空 */
                        isConnected = true;
                        new SendThread().start();
                        break;
                    case BluetoothService.WHAT_SENSOR:
                        break;
                    case SensorClient.Overturn:
                        /* 传来的角度数据，处理后将其存放到angle数组里 */
                        //这里已经不是判断翻转了，而是直接将全部的角度数据处理后发送给蓝牙设备
                        if (isSensoring) {
                            float[] trueAngle = (float[]) mes.obj;
                            angleView.setText("角度为:x:"+(int)trueAngle[0]+" y:"+(int)trueAngle[1]+" z:"+(int)trueAngle[2]);
                            angle = float2Byte(trueAngle);
                            break;
                        } else {
                            if(angle!=null){
                                angle[0] = 0;
                                angle[1] = 0;
                                angle[2] = 0;
                            }
                        }
                    default:
                        break;
                }
            }
        };
        try{
            /* 初始化BluetooService和EventManager */
            BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothService = new BluetoothService(bm.getAdapter(),mHandler);
            mSensorClient = new SensorClient((SensorManager) getSystemService(SENSOR_SERVICE),mHandler);
            /* EventManager为抽象，使用工厂模式 */
            mEventManager = EventManagerFactory.create(this,"asr"); //这一句报错空指针
            mEventManager.registerListener(this);}catch (Exception e){e.printStackTrace();};
    }

    /* 百度语音的EventListener接口方法的重写。 */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        Toast.makeText(this,"onEvent",Toast.LENGTH_LONG).show();
        String logTxt = "name: " + name;

        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
            try{
                /* 在这里写处理语音识别的结果
                 * 语音识别结果处理分析还需要完善
                  * */
                String res = android.my.util.JsonAnalysis.jsonGetValue(params,"results_recognition");
                /* The result is right */
                if (res!=null)
                    Log.w(TAG,"语音识别处理结果 : "+ res);
                /* show the result directly */
                if (FlyMode.ModeMapping.containsKey(res)){   //说明语音识别结果是飞行模式的一部分
                    asrCommand = FlyMode.ModeMapping.get(res);      //把指令通过蓝牙发送
                    flyMode.setText("飞行模式:"+res);
                }
               Toast.makeText(getApplicationContext(),"result of the recognition : "+res,Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.w(TAG,logTxt);
    }
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    /* 蓝牙选择及界面 */
    private void showBluetoothDevices(){
        try{
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//这一句必须用Activity，但是Mainactivity是空的
        builder.setTitle("选择您的蓝牙设备");
        builder.setIcon(R.drawable.ic_bluetooth_24dp);
        /* 是getDevices出了问题 */
        builder.setItems(getDevices(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        (builder.create()).show();
        Log.w(TAG, "showBluetoothDevices: "+getDevices()[0] );}catch (Exception e){e.printStackTrace();}
    }

    /* 返回的是已配对蓝牙列表的名字的字符串数组 */
    private String[] getDevices(){
        String[] list;
        LinkedList<BluetoothDevice> linkedList = mBluetoothService.getBondedDeivices();
        Log.w(TAG, "getDevices: "+linkedList.size() );
        mDevicesVector = linkedList;
        list = new String[linkedList.size()];

        for (int i = 0;i<linkedList.size();i++){
            list[i] = linkedList.get(i).getName()+":"+linkedList.get(i).getAddress();
        }
        Log.w(TAG, "getDevices: 已配对个数："+list.length);
        return list;
    }

    /* 实现选择蓝牙对话框的方法 */
    public void showDialogue() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        String[] tmp = new String[]{"test","test"};
        builder.setTitle("删除信息");
        String[] tmp = getDevices();
//        Log.w(TAG, "showDialogue:getDevices() Alright?");
//        builder.setMessage("确定删除吗?");
        builder.setItems(tmp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevice ready2Connect = mDevicesVector.get(which);  //通过which来获得点击的蓝牙设备
                mBluetoothService.connect(ready2Connect);
            }
        });
        /* 确定和取消按钮的设定 */
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "你点击了确定了", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "你点击了取消了", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }

    /* 将传感器传来的大小为3的浮点数数组转化为大小为3的字节数组
     * 以便通过蓝牙发送
      * 因为数据大小超限，故数据均除了3*/
    private byte[] float2Byte(float[] angle){
        byte[] result = new byte[3];
        int tmp;
        if (angle == null)
            return null;
         for (int i = 0;i<3;i++) {
            angle[i] /= 3;
            tmp = (int)(angle[i]);
            result[i] = (byte)(tmp & 0xFF);
        }
         byte temp = result[0];
         result[0] = result[2];
         result[2] = temp;
        Log.w(TAG, "float2Byte: 转换前"+angle[0]+":"+angle[1]+":"+angle[2]+"转换后："+result[0]+":"+result[1]+":"+result[2]);
        return result;
    }

    /* 发送线程，所有的向蓝牙发送的操作都在这里面 */
    private class SendThread extends Thread{
        byte[] toSend;
        /* 构造函数，帧头为0x55,0x13, 帧尾为0x42 */
        public SendThread() {
            toSend = new byte[8];
            angle = new byte[3];
            toSend[0] = 0x55;
            toSend[1] = 0x13;
            toSend[7] = 0x42;
            renewMessage();
        }

        @Override
        public void run(){
            try{
                while(true){
                    //发送延迟改为了2
                    sleep(2);
                    renewMessage();
                    mBluetoothService.sendMessage(toSend);
                    Log.w(TAG, "SendMessage:"+toSend[0]+":"+toSend[1]+":"+toSend[2]+":"+toSend[3]+":"+toSend[4]+":"+toSend[5]+":"+toSend[6]+":"+toSend[7]);}
            }catch (Exception e){
                Log.w(TAG,e.toString() );
            }
        }
        /* 更新待发送的字节数组的内容 */
        private void renewMessage(){
            if (angle == null){
                try{ 
                Exception e = new Exception("angle is null!");
                throw e;}catch (Exception e){Log.w(TAG,e.toString() );
                    return;
                }
            }
            toSend[2] = angle[0];
            toSend[3] = angle[1];
            toSend[4] = angle[2];
            /* 默认为0 不进行判空处理 */
//            if (asrCommand != 0){
                toSend[5] = asrCommand;
//                asrCommand = 0;
//            } else
//                toSend[5] = 0;
            toSend[6] = userCommit;
        }
    }
}
