package android.my.speechcontrol;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SensorClient extends Thread implements SensorEventListener {
    public static final int Overturn = 100;
    private boolean isTurned;       //是否翻转标志位，防止连续触发翻转
    private SensorManager mSensorManager;
    private Sensor mAccelerateSensor,mMagneticSensor,mOrentationSensor ;
    final private Handler handler;
    private float[] data;
    private final String TAG = "Sensor";
    protected float[] accelerate,magnetic,r;
    public SensorClient(SensorManager sensorManager,Handler handler){
        this.handler=handler;
        accelerate = new float[3];  //存放加速度传感器的值
        magnetic = new float[3];    //存放地磁传感器的值
        r = new float[9];
        data = new float[3];
        mSensorManager = sensorManager;
//        mAccelerateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrentationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);      //方向传感器被废弃，要用getOrientation来获取值，需要输入地磁传感器和加速度传感器的值
//        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        mSensorManager.registerListener(this, mAccelerateSensor,SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this,mMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(this,mOrentationSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
    /* 三个值分别代表z,x,y轴上的角度
     * 范围是0~360,-180~180,-90~90 */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor ==  null)
            return;
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {    //判断传感器是否为陀螺仪传感器。
            accelerate = event.values;
        }
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magnetic = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
//            Log.w(TAG, "onSensorChanged: "+event.values[0]+' '+event.values[1]+' '+event.values[2]);
            /* 实时向飞行器发送数据 */
            Message message = new Message();
            message.what = Overturn;
            message.obj = event.values;
            handler.sendMessage(message);


            if((event.values[1]>150 && event.values[1]<180 || event.values[1]<-150 && event.values[1]>-180) && !isTurned ){
                Message mes = new Message();
                mes.what=Overturn;
                handler.sendMessage(mes);
                isTurned = true;
            }
            else
                isTurned = false;
        }
//        if(null != magnetic && null != accelerate){
//            SensorManager.getRotationMatrix(r,null,accelerate,magnetic);
//            SensorManager.getOrientation(r,data);
//            Log.w(TAG, "onSensorChanged: "+data[0]*57.29577951+' '+data[1]*57.29577951+' '+data[2]*57.29577951);
//        }
    }

    @Override
    public void run(){
        mSensorManager.registerListener(this,mOrentationSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
