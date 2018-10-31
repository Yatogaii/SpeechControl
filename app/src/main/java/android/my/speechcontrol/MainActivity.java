package android.my.speechcontrol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptC;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements EventListener {
    final String TAG = "MainActivity";
    Button speechControl;
    Handler mHandler;
    EventManager mEventManager;
    boolean isRecording = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        init();
        initPermission();
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

    private void init(){
        speechControl = findViewById(R.id.speechControl);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message mes){
                if(true){

                }
            }
        };
        /* EventManager为抽象，使用工厂模式 */
        mEventManager = EventManagerFactory.create(this,"asr"); //这一句报错空指针
        mEventManager.registerListener(this);
        /* 为录音开关按钮设置监听器 */
        speechControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording){
                    Map<String, Object> params = new LinkedHashMap<String, Object>();
                    params.put(SpeechConstant.ACCEPT_AUDIO_DATA,false);
                    params.put(SpeechConstant.DISABLE_PUNCTUATION,false);
                    mEventManager.send(SpeechConstant.ASR_START, new JSONObject(params).toString(), null, 0, 0);
                    isRecording = true;
                    speechControl.setText("停止录音");
                }else{
                    mEventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0); // 发送停止录音事件，提前结束录音等待识别结果
//                    mEventManager.send(SpeechConstant.ASR_CANCE, null, null, 0, 0); // 取消本次识别，取消后将立即停止不会返回识别结果
                    isRecording = false;
                    speechControl.setText("开始录音");
                    Toast.makeText(getApplicationContext(),"请稍等识别结果",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /* 百度地图的EventListener接口方法的重写。 */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)){
            // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
            Toast.makeText(getApplicationContext(),"开始说话",Toast.LENGTH_SHORT).show();
        }
        if(name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)){
            if (data!=null)
                Toast.makeText(getApplicationContext(),new String(data,offset,length),Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            // 识别结束
        }
        // ... 支持的输出事件和事件支持的事件参数见“输入和输出参数”一节
    }
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
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
}
