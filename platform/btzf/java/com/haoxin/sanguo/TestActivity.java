package com.haoxin.sanguo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.unity3d.player.UnityPlayer;
import com.ytb.ylws.R;

import java.util.TreeMap;

/**
 * Created by Administrator on 2021/4/26.
 */

public class TestActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SdkManager.INSTANCE().init(this);
        setContentView(R.layout.main_activity);
        findViewById(R.id.init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TreeMap<String ,String> map = new TreeMap<>();
                map.put("methodName","HXSDKInit");
                SdkManager.INSTANCE().MainHandleUnityCall(jsonStr(map));
            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TreeMap<String ,String> map = new TreeMap<>();
                map.put("methodName","HXLogin");
                SdkManager.INSTANCE().MainHandleUnityCall(jsonStr(map));
            }
        });

        findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TreeMap<String ,String> map = new TreeMap<>();
                map.put("methodName","HXPay");
                SdkManager.INSTANCE().MainHandleUnityCall(jsonStr(map));
            }
        });

        findViewById(R.id.loadLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SdkManager.INSTANCE().MainHandleUnityCall(loadStr("OnGameLoginComplete"));
            }
        });

        findViewById(R.id.loadUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SdkManager.INSTANCE().MainHandleUnityCall(loadStr("OnGameUpgradeLevel"));
            }
        });

        findViewById(R.id.loadRes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SdkManager.INSTANCE().MainHandleUnityCall(loadStr("OnRoleNameChanged"));
            }
        });

    }

    public static String jsonStr(TreeMap<String,String> map){
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
        return "{"+str+"}";
    }

    private String loadStr(String str){
        TreeMap<String,String> map = new TreeMap<>();
        map.put("methodName",str);
        map.put("roleID","roleID");
        map.put("roleName","roleName");
        map.put("level","level");
        map.put("registerTime","registerTime");
        map.put("currentTime","currentTime");
        map.put("vipLevel","vipLevel");
        map.put("partyName","partyName");
        map.put("serverID","serverID");
        map.put("serverName","serverName");
        map.put("power","power");
        return jsonStr(map);
    }
}
