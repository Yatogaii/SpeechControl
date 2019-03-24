package android.my.speechcontrol;

import java.util.HashMap;

public class FlyMode {
    private final static byte TAKE_OFF  = 0x06;
    private final static byte LANDING = 0x07;
    private final static byte FORWARD = 0x08;
    private final static byte BACKWARD = 0x09;
    private final static byte LEFT = 0x10;
    private final static byte RIGHT = 0x11;
    private final static byte LEFT_DRIFT = 0x12;
    private final static byte RIGHT_DRIFT = 0x13;

    public static HashMap<String,Byte> ModeMapping;

    static{
        ModeMapping = new HashMap<>();
        ModeMapping.put("起飞",TAKE_OFF);
        ModeMapping.put("降落",LANDING);
        ModeMapping.put("前进",FORWARD);
        ModeMapping.put("后退",BACKWARD);
        ModeMapping.put("左移",LEFT);
        ModeMapping.put("右移",RIGHT);
        ModeMapping.put("左偏航",LEFT_DRIFT);
        ModeMapping.put("右偏航",RIGHT_DRIFT);
    }
}
