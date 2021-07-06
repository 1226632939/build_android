package com.haoxin.sanguo;

/**
 * Created by Administrator on 2015/2/12.
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Binder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import com.ytb.ylws.R;

class GameNotification implements Parcelable {

	public int id;
	public int day;
	public int hour;
	public int min;
	public String title;
	public String msg;

	public GameNotification(int id, int day, int hour, int min, String title, String msg) {
		this.id = id;
		this.day = day;
		this.hour = hour;
		this.min = min;
		this.title = title;
		this.msg = msg;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeInt(day);
		out.writeInt(hour);
		out.writeInt(min);
		out.writeString(title);
		out.writeString(msg);
	}

	public static final Parcelable.Creator<GameNotification> CREATOR = new Parcelable.Creator<GameNotification>() {
		public GameNotification createFromParcel(Parcel in) {
			return new GameNotification(in);
		}

		public GameNotification[] newArray(int size) {
			return new GameNotification[size];
		}
	};

	private GameNotification(Parcel in) {
		id = in.readInt();
		day = in.readInt();
		hour = in.readInt();
		min = in.readInt();
		title = in.readString();
		msg = in.readString();
	}
}

public class SGNotificationService extends Service {
	private final long ONE_DAY_MS = 1000 * 24 * 60 * 60;//一天的毫秒数
	NotificationManager mNotificationManager = null;

	ArrayList<GameNotification> notificationList = null;
	//保存推送数据的文件名
	static final String NOTIFY_FILE_NAME = "NotifiDataFile";

	// 定时器
	ScheduledThreadPoolExecutor mTimer = new ScheduledThreadPoolExecutor(1);

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	long mLastCheckTimeMS = 0;

	public SGNotificationService() {
		notificationList = new ArrayList<GameNotification>();

		mTimer.scheduleAtFixedRate(new GameNotificationChecker(), 1, 1,
				TimeUnit.SECONDS);

		Calendar calendar = Calendar.getInstance();
		Date currDate = calendar.getTime();
		mLastCheckTimeMS = currDate.getTime();

	}

	public void NotificationListAdd(int id, int day, int hour, int min, String title, String msg)
	{
		GameNotification notify = new GameNotification(id, day, hour, min, title, msg);
		int i = 0;
		for(	; i < notificationList.size(); i++)
		{
			if(id == notificationList.get(i).id)
			{
				notificationList.set(i, notify);
				break;
			}
		}
		if(i >= notificationList.size())
		{
			notificationList.add(notify);
		}
		SaveNotifyDataFile();//将推送数据保存到文件中
	}

	@Override
	public IBinder onBind(Intent intent) {
		//return null;
		return new ServiceBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null != intent) {
			ArrayList<GameNotification> notificationList = intent
					.getParcelableArrayListExtra("notify_condition");
			if (null != notificationList) {
				// passGameNotifition(notificationList);
			}
		}

		Date dt = new Date();
		mLastCheckTimeMS = dt.getTime();

		return START_STICKY;
	}

	class GameNotificationChecker extends TimerTask {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(0);
		}
	}

	private void SendNotification(int nID, String title, String msg) {
//		NotificationCompat.Builder builder;
//		if (mNotificationManager == null) {
//			mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//		}
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//			String channelId = getString(R.string.notification_channel_id);
//			NotificationChannel mChannel = mNotificationManager.getNotificationChannel(channelId);
//			if (mChannel == null) {
//				mChannel = new NotificationChannel(channelId, getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_HIGH);
//				mChannel.enableVibration(true);
//				mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//				mNotificationManager.createNotificationChannel(mChannel);
//			}
//			builder = new NotificationCompat.Builder(this, channelId);
//			Intent intent = new Intent(this, MainActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			PendingIntent pendingIntent = PendingIntent.getActivity(this, nID, intent, 0);
//			builder.setContentTitle(title)  // required
//					.setSmallIcon(R.drawable.app_icon) // required
//					.setContentText(msg)  // required
//					.setDefaults(Notification.DEFAULT_ALL)
//					.setAutoCancel(true)
//					.setContentIntent(pendingIntent)
//					.setTicker(msg)
//					.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//		} else {
//			builder = new NotificationCompat.Builder(this);
//			Intent intent = new Intent(this, MainActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			PendingIntent pendingIntent = PendingIntent.getActivity(this, nID, intent, 0);
//			builder.setContentTitle(title)                           // required
//					.setSmallIcon(R.drawable.app_icon) // required
//					.setContentText(msg)  // required
//					.setDefaults(Notification.DEFAULT_ALL)
//					.setAutoCancel(true)
//					.setContentIntent(pendingIntent)
//					.setTicker(msg)
//					.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
//					.setPriority(Notification.PRIORITY_HIGH);
//		}
//		Notification notification = builder.build();
//		mNotificationManager.notify(nID, notification);
	}

	void CheckIfNeedNotification() {
		Calendar calendar = Calendar.getInstance();
		Date currDate = calendar.getTime();

		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DATE));

		long currTimeMS = currDate.getTime();
		if(notificationList.size() <= 0)
		{
			LoadNotifyDataFile();//从文件中读取出推送数据
		}

//		Log.i("HaoXinSDK", "CheckIfNeedNotification " + notificationList.size());
		for (GameNotification notification : notificationList) {
			calendar.set(Calendar.HOUR_OF_DAY, notification.hour);
			calendar.set(Calendar.MINUTE, notification.min);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			long tempTimeMS = calendar.getTime().getTime();
			if (notification.day > 0){
				tempTimeMS += ONE_DAY_MS * (notification.day-1);
			}

//			Log.i("HaoXinSDK", "id: " + notification.id + " currTimeMS" + currTimeMS + " tempTimeMS" + tempTimeMS + " mLastCheckTimeMS" + mLastCheckTimeMS);
			if (currTimeMS >= tempTimeMS && mLastCheckTimeMS < tempTimeMS) {
				if (isActivityInForeground()) {
					continue;
				}
//				Log.i("HaoXinSDK", "Notify : " + notification.id	);
				SendNotification(notification.id, notification.title, notification.msg);
			}
		}
		mLastCheckTimeMS = currTimeMS;
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 这里要检查时间
			CheckIfNeedNotification();
			super.handleMessage(msg);
		}
	};

	public boolean isActivityInForeground() {
		String selfPackageName = getApplicationContext().getPackageName();
		ActivityManager am = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo process : processes) {
			if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				if (process.processName.equals(selfPackageName)) {
					return true;
				}
			}
		}
		return false;
	}

	class ServiceBinder extends Binder
	{
		public SGNotificationService getService()
		{
			return SGNotificationService.this;
		}
	}

	//写数据
	public void writeFile(String fileName,String writestr) //throws IOException
	{
		try
		{
			FileOutputStream fOut = openFileOutput(fileName, MODE_PRIVATE);
			byte[] bytes = writestr.getBytes();
			fOut.write(bytes);
			fOut.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//读数据
	public String readFile(String fileName) //throws IOException
	{
		String res = "";
		try
		{
			FileInputStream fin = openFileInput(fileName);
			int length = fin.available();
			byte [] buffer = new byte[length];
			fin.read(buffer);
			res = new String(buffer, "UTF-8");
			fin.close();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
		return res;
	}

	//将推送数据保存到文件中
	public void SaveNotifyDataFile()
	{
		String strNotifyData = "";
		for (GameNotification notification : notificationList)
		{
			strNotifyData += notification.id;
			strNotifyData += "@";
			strNotifyData += notification.day;
			strNotifyData += "@";
			strNotifyData += notification.hour;
			strNotifyData += "@";
			strNotifyData += notification.min;
			strNotifyData += "@";
			strNotifyData += notification.title;
			strNotifyData += "@";
			strNotifyData += notification.msg;
			strNotifyData += "@";
		}

		writeFile(NOTIFY_FILE_NAME, strNotifyData);
	}

	//从文件中读取出推送数据
	public void LoadNotifyDataFile()
	{
		String strNotifyData = readFile(NOTIFY_FILE_NAME);
		String[] strSplit = strNotifyData.split("@");
		try
		{
			for(int i = 0; i + 5 < strSplit.length; i += 6)
			{
				int id = Integer.parseInt(strSplit[i]);
				int day = Integer.parseInt(strSplit[i + 1]);
				int hour = Integer.parseInt(strSplit[i + 2]);
				int min = Integer.parseInt(strSplit[i + 3]);
				String title = strSplit[i + 4];
				String msg = strSplit[i + 5];
				GameNotification notification = new GameNotification(id, day, hour, min, title, msg);
				notificationList.add(notification);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
