package android.my.util;
import android.support.v4.util.LogWriter;
import android.util.Log;

import org.json.JSONObject;


public class JsonAnalysis {

    public static String jsonGetValue(String json,String key){
        try{
        JSONObject jsonObject = new JSONObject(json);
            Log.w("Util", "jsonGetValue: "+jsonObject.toString());
        String res = jsonObject.get(key).toString();
        Log.w("Util", "jsonGetValue: "+res );
        return res.substring(2,res.length()-2);
        }catch (Exception e ){
            Log.w("Util", e.toString() );
        }
        return null;
    }
}
