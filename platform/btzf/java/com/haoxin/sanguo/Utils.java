package com.haoxin.sanguo;

import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.util.TreeMap;

/**
 * Created by Administrator on 2021/4/26.
 */

public class Utils {
    public static void logE(String str){
        Log.e("BiaoGan","\n"+str+"\n");
    }
    public static void logD(String str){
        Log.d("BiaoGan","\n"+str+"\n");
    }
    public static void unityCallback(TreeMap<String,String> map){
        int index = 0;
        String str = "";
        for (String key:map.keySet()){
            index ++;
            Log.e("BiaoGan","index : " + index);
            if (index == map.size()){
                str += "\""+ key +"\":\""+map.get(key)+"\"";
            }else {
                str += "\""+ key +"\":\""+map.get(key)+"\",";
            }
        }
        Log.e("SDK","str : " + str);
        UnityPlayer.UnitySendMessage("SDKHelper","SDKCallLua","{"+str+"}");
    }
}
