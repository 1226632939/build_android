package com.haoxin.sanguo;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;

import com.zqhy.sdk.callback.*;
import com.zqhy.sdk.model.PayParams;
import com.zqhy.sdk.platform.TsGameSDKApi;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TreeMap;

/**
 * Created by Administrator on 2021/4/26.
 */

public class SdkManager {
    private static SdkManager m_instance;

    private String m_uname;
    private String m_token;

    public static SdkManager INSTANCE(){
        if (m_instance==null){
            m_instance = new SdkManager();
        }
        return m_instance;
    }
    private Activity m_activity;

    private String etPid="A9755BA7F1EFC583BD33B95562BA4BC52";
    private String etAppkey="c8e9fd0eaf57c9d93bdddfa12fd948bb";
    public void init(Activity activity){
        m_activity = activity;
    }
    public void MainHandleUnityCall(String msg){
        try {
            JSONObject json = new JSONObject(msg);
            String methodName = json.getString("methodName");
            switch (methodName) {
                case "HXSDKInit":
                    sdkInit();
                    break;
                case "HXLogin":
                    login();
                    break;
                case "HXSwitchAccount":
                    switchAccount();
                    break;
                case "HXPay": {
                    String appUserId = json.getString("appUserId");
                    String productId = json.getString("itemid");
                    String appOrderId = json.getString("order");
                    int moneyAmount = json.getInt("price");
                    String productName = json.getString("productName");
                    String appUserName = json.getString("appUserName");
                    String serverName =json.getString("serverName");
                    String serverId = json.getString("serverId");
                    pay(appUserId,productId,appOrderId,moneyAmount,productName,appUserName,serverId,serverName);
                }
                break;
                case "OnGameLoginComplete": {
                    String roleID = json.getString("roleID");
                    String roleName = json.getString("roleName");
                    String level = json.getString("level");
                    String registerTime = json.getString("registerTime");
                    String currentTime = json.getString("currentTime");
                    String balance = json.getString("balance");
                    String vipLevel = json.getString("vipLevel");
                    String partyName = json.getString("partyName");
                    String serverID = json.getString("serverID");
                    String serverName = json.getString("serverName");
                    int power = json.getInt("power");
                    onGameLoginComplete(roleID, roleName, level, registerTime, currentTime, balance, vipLevel, partyName, serverID, serverName, power);
                }
                break;
                case "OnGameUpgradeLevel": {
                    String roleID = json.getString("roleID");
                    String roleName = json.getString("roleName");
                    String level = json.getString("level");
                    String registerTime = json.getString("registerTime");
                    String currentTime = json.getString("currentTime");
                    String balance = json.getString("balance");
                    String vipLevel = json.getString("vipLevel");
                    String partyName = json.getString("partyName");
                    String serverID = json.getString("serverID");
                    String serverName = json.getString("serverName");
                    int power = json.getInt("power");
                    onGameUpgradeLevel(roleID, roleName, level, registerTime, currentTime, balance, vipLevel, partyName, serverID, serverName, power);
                }
                break;
                case "OnRoleNameChanged": {
                    String roleID = json.getString("roleID");
                    String roleName = json.getString("roleName");
                    String level = json.getString("level");
                    String registerTime = json.getString("registerTime");
                    String currentTime = json.getString("currentTime");
                    String balance = json.getString("balance");
                    String vipLevel = json.getString("vipLevel");
                    String partyName = json.getString("partyName");
                    String serverID = json.getString("serverID");
                    String serverName = json.getString("serverName");
                    int power = json.getInt("power");
                    String oldName = json.getString("oldName");
                    onRoleNameChanged(roleID, roleName, level, registerTime, currentTime, balance, vipLevel, partyName, serverID, serverName, power, oldName);
                }
                break;
                case "OnGameRegistComplete": {
                    String roleID = json.getString("roleID");
                    String roleName = json.getString("roleName");
                    String level = json.getString("level");
                    String registerTime = json.getString("registerTime");
                    String currentTime = json.getString("currentTime");
                    String balance = json.getString("balance");
                    String vipLevel = json.getString("vipLevel");
                    String partyName = json.getString("partyName");
                    String serverID = json.getString("serverID");
                    String serverName = json.getString("serverName");
                    int power = json.getInt("power");
                    onGameRegistComplete(roleID, roleName, level, registerTime, currentTime, balance, vipLevel, partyName, serverID, serverName, power);
                }
                break;
                default:
                    Utils.logE("??????????????? ??? " + methodName);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("UnityCallAndroid", "HandleUnityCall error : " + e.getMessage());
        }
    }

    // SDK?????????
    private void sdkInit(){
        Utils.logE("?????????");
    }

    // SDK??????
    private void login(){
        Utils.logD("??????");
        TsGameSDKApi.getInstance().login(m_activity, new LoginCallBack() {
            @Override
            public void onLoginSuccess(String username, String token, int authentication, String birthday, String pi) {
                Utils.logE("onLoginSuccess");
                m_uname = username;
                m_token = token;
                // ?????????????????????
                TreeMap<String,String> map = new TreeMap<String,String>();
                map.put("methodName", "LoginCallback");
                map.put("platform_id", username);
                map.put("token", token);
                Utils.unityCallback(map);
            }
            @Override
            public void onLoginFailure(String message) {
                Utils.logE("onLoginFailure message:" + message);
            }
            @Override
            public void onLoginCancel() {
                Utils.logE("onLoginCancel");
            }
        });


        // ????????????????????? ???
        //UnityPlayer.UnitySendMessage("SDKHelper", "SDKCallLua", "{\"methodName\":\"LoginCallback\",\"platform_id\":\"" + userid + "\",\"token\":\"" + token +"\"}");

    }

    // ????????????
    private void switchAccount(){

    }

    // ??????
    private void pay(String appUserId,String productId,String appOrderId,int moneyAmount,String productName,String appUserName,String serverId,String serverName){
        try {
            PayParams payParams = getPayParams(appUserId,productId,appOrderId,moneyAmount,productName,appUserName,serverId,serverName);
            TsGameSDKApi.getInstance().pay(m_activity,payParams , new PayCallBack() {
                @Override
                public void onPaySuccess(String message) {
                    Utils.logD("onPaySuccess message:" + message);
                    TreeMap<String,String> map = new TreeMap<String,String>();
                    map.put("methodName", "PayCallback");
                    map.put("ret", "0");
                    Utils.unityCallback(map);
                }
                @Override
                public void onPayFailure(String message) {
                    Utils.logE("onPayFailure message:" + message);
                    TreeMap<String,String> map = new TreeMap<String,String>();
                    map.put("methodName", "PayCallback");
                    map.put("ret", "-1");
                    Utils.unityCallback(map);
                }
                @Override
                public void onPayCancel() {
                    Utils.logE("onPayCancel");
                    TreeMap<String,String> map = new TreeMap<String,String>();
                    map.put("methodName", "PayCallback");
                    map.put("ret", "-1");
                    Utils.unityCallback(map);
                }
            });
        } catch (Exception e) {
            Toast.makeText(m_activity, "????????????????????????", Toast.LENGTH_LONG).show();
            TreeMap<String,String> map = new TreeMap<String,String>();
            map.put("methodName", "PayCallback");
            map.put("ret", "-1");
            Utils.unityCallback(map);
        }
    }

    // ????????????
    private void onGameLoginComplete(String roleID, String roleName, String level, String registerTime, String currentTime, String balance, String vipLevel, String partyName, String serverID, String serverName, int power) {

    }

    // ????????????
    private void onGameUpgradeLevel(String roleID, String roleName, String level, String registerTime, String currentTime, String balance, String vipLevel, String partyName, String serverID, String serverName, int power) {

    }

    // ????????????
    private void onRoleNameChanged(String roleID, String roleName, String level, String registerTime, String currentTime, String balance, String vipLevel, String partyName, String serverID, String serverName, int power, String oldName) {

    }

    // ????????????
    private void onGameRegistComplete(String roleID, String roleName, String level, String registerTime, String currentTime, String balance, String vipLevel, String partyName, String serverID, String serverName, int power) {

    }

    // ??????
    public void exit(){
        int orientation = 0; // ?????? = 0 ?????? = 1
        TsGameSDKApi.getInstance().exit(m_activity, orientation, new ExitCallBack() {
            @Override
            public void onExit() {
                Utils.logD("onExit");
                m_activity.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            @Override
            public void onContinueGame() {
                Utils.logE("onContinueGame");
            }
            @Override
            public void onCancel() {
                Utils.logE("onCancel");
            }
        }, null);
    }

    /******************************************************** ???SDK????????????????????????????????????????????????SDK????????????????????? **************************************************************************/

    public void init(){
        try {
            String strPid = etPid;
            String strAppkey = etAppkey;
            TsGameSDKApi.getInstance().init(m_activity, strPid, strAppkey, new InitCallBack() {
                @Override
                public void onInitSuccess() {
                    Utils.logD("init Success");
                    TsGameSDKApi.getInstance().registerReLoginCallBack(reLoginCallBack);
                }
                @Override
                public void onInitWarning(String message) {
                    Utils.logE("onInitWarning = " + message);
                }
                @Override
                public void onInitFailure(String message) {
                    Utils.logE("init failure");
                    Utils.logE("message:" + message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private PayParams getPayParams(String appUserId,String productId,String appOrderId,int moneyAmount,String productName,String appUserName,String serverId,String serverName){

        PayParams payParams = new PayParams();
        payParams.extendsinfo = appOrderId;
        payParams.username = m_uname;
        payParams.token = m_token;
        payParams.serverid = Integer.parseInt(serverId);
        payParams.amount = moneyAmount;
        payParams.role_id = appUserId;
        payParams.role_name = appUserName;
        payParams.product_name = productName;
        payParams.servername = serverName;
        payParams.out_trade_no = appOrderId;
        return payParams;
    }

    ReLoginCallBack reLoginCallBack = new ReLoginCallBack() {
        @Override
        public void onReLogin() {//???????????? cp???????????????????????????????????????
            Utils.logD( "RELOGIN");
            TreeMap<String,String> map = new TreeMap<String,String>();
            map.put("methodName", "LogoutCallback");
            Utils.unityCallback(map);
        }
    };
}
