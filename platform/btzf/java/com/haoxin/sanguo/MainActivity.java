package com.haoxin.sanguo;


import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import com.ytb.ylws.R;


public class MainActivity extends UnityPlayerActivity {
	
	SGNotificationService notificationService;
	ArrayList<GameNotification> notificationList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void onDestroy() {
		super.onDestroy();
//		unbindService(conn);
	}
	
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
	}

	private WakeLock wakeLock = null;

	private void ShowWAKE_LOCK() {
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
					"PostLocationService");
			if (null != wakeLock) {
				wakeLock.acquire();
			}
		}
	}

	public void Unsleep() {
		ShowWAKE_LOCK();
	}

	private ServiceConnection conn = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i("HaoXinSDK", "onServiceConnected");
			notificationService = ((SGNotificationService.ServiceBinder) service).getService();
			if (notificationList != null){
				for (GameNotification notification : notificationList)
					{
						notificationService.NotificationListAdd(notification.id, notification.day, notification.hour, notification.min, notification.title, notification.msg);
					}
				notificationList.clear();
			}
		}

		public void onServiceDisconnected(ComponentName name)
		{
			// TODO Auto-generated method stub
			notificationService = null;
		}
	};

	public void InstallNotification() {
		Intent intent = new Intent(this, SGNotificationService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	public void NotificationListAdd(int id, int day, int hour, int min, final String title, final String msg) {
		if(notificationService != null)
		{
			notificationService.NotificationListAdd(id, day, hour, min, title, msg);
		}
		else{
			if (notificationList == null){
				notificationList = new ArrayList<>();
			}
			notificationList.add(new GameNotification(id, day, hour, min, title, msg));
		}
	}

	public void HandleUnityCall(String msg){
		try{
			JSONObject json = new JSONObject(msg);
			String methodName = json.getString("methodName");
			switch(methodName){
				case "InstallNotification":
					InstallNotification();
				break;
				case "NotificationListAdd":{
					int id = json.getInt("id");
					int day = json.getInt("day");
					int hour = json.getInt("hour");
					int min = json.getInt("min");
					String title = json.getString("title");
					String content = json.getString("msg");
					NotificationListAdd(id, day, hour, min, title, content);
				}
				break;
				default:
					MainHandleUnityCall(msg);
				break;
			}
		}catch (JSONException e) {
			e.printStackTrace();
			Log.e("UnityCallAndroid", "HandleUnityCall error : " + e.getMessage());
		}
	}

	public void MainHandleUnityCall(String msg){

	}

	public void ShowDialog(final String mTitle, final String mContent,
			final String yesStr,final String noStr, final String gameName, final String yesFunc,
			final String noFunc) {
		/* ��UI�߳���ִ����ط��� */
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OnClickListener noImpl = null;
				OnClickListener yesImpl = null;
				if (gameName!=null&&yesFunc != null) {
					yesImpl = new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UnityPlayer.UnitySendMessage(gameName,yesFunc, "");
						}
					};
				}

				if (gameName!=null&&noFunc != null) {
					noImpl = new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UnityPlayer.UnitySendMessage(gameName,noFunc, "");
						}
					};
				}

				// ����Builder
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(
						MainActivity.this);
				// �����Ի���
				mBuilder.setTitle(mTitle).setMessage(mContent);

				mBuilder.setPositiveButton(yesStr, yesImpl);

				mBuilder.setNegativeButton(noStr, noImpl);

				// ��ʾ�Ի���
				mBuilder.setCancelable(false);
				mBuilder.show();
			}
		});
	}
	
	public void TrackInit()
	{
		//ReYunTrack.initWithKeyAndChannelId(this, reyunKey, "jituo");
	}
	
//	public String GetMacAddress()
//	{
//		try {
//			// ��ȡwifi����
//			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//			// �ж�wifi�Ƿ���
//			if (!wifiManager.isWifiEnabled()) {
//				return "";
//			}
//			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//			String macAddress = wifiInfo.getMacAddress();
//			return macAddress;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "";
//		}
//    }
//
//	private String GetDeviceID()
//	{
//		String did = "";
//
//		try
//		{
//			TelephonyManager phonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//			if (phonyMgr != null)
//			{
//				//Log.d("sanguo", "now getDeviceID");
//
//				did = phonyMgr.getDeviceId();
//			}
//		}
//		catch(Exception ex)
//		{
//			Log.d("sanguo", "getDeviceID ex " + ex.toString());
//		}
//
//		if (did == null)
//		{
//			did = "";
//		}
//
//		return did;
//	}
	
	public void GaeaLogin(String loginUid, String loginType)
	{
	}
	
	public void TrackPayStart(String transactionId, String paymentType, String  currencyType, float currencyAmount){
		//ReYunTrack. setPaymentStart (transactionId, paymentType, currencyType, currencyAmount);
	}
	
	public void TrackPaySuccess(String transactionId, String paymentType, String  currencyType, float currencyAmount ){
		//ReYunTrack.setPayment(transactionId, paymentType, currencyType, currencyAmount);
	}
	
	public void TrackRegist(String accountID, String serverID)
	{
		//ReYunTrack.setRegisterWithAccountID(accountID);
	}
	
	public void TrackLogin(String accountID, String serverID, int level)
	{
		//ReYunTrack.setLoginSuccessBusiness(accountID);
	}
	
	public void TrackExit(){
		//ReYunTrack.exitSdk ();
	}
	
	public void GATALogout(String accoundID)
	{
	}
	
	public void GATASetLevel(String accountID, int level)
	{
	}
	
	public void GATAInitCoin(long totalCoin, String coinType)
	{
	}
	
	public void GATAAddCoin(String reason, String coinType, long addCoin, long totalCoin)
	{
	}
	
	public void GATALoseCoin(String reason, String coinType, long loseCoin, long totalCoin)
	{
	}
	
	public void GATASetEvent(String identifier)
	{
	}

	public void ShowExitDialog() {
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
				mBuilder.setTitle(getString(R.string.haoxin_exit_title)).setMessage(getString(R.string.haoxin_exit_msg));

				mBuilder.setPositiveButton(getString(R.string.haoxin_exit_ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
						System.exit(1);
					}
				});

				mBuilder.setNegativeButton(getString(R.string.haoxin_exit_cancle), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

				mBuilder.setCancelable(false);
				mBuilder.show();
			}
}
