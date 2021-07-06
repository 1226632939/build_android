package com.haoxin.sanguo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import com.example.androidlearning.R;
import com.zqhy.sdk.platform.TsGameSDKApi;
import com.zqhy.sdk.ui.FloatWindowManager;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.unity3d.player.UnityPlayer;

public class PlatformActivity extends MainActivity
{
    private Handler m_downloadHandler;
    private static final int DOWNLOAD = 1;
    private static final int DOWNLOAD_FINISH = 2;
    private final static int INSTALL_PACKAGES_REQUESTCODE = 0x12;
    private int m_progress;

    private String m_url;
    private String m_savePath;
    private String m_saveName;

    private String PackageName = "ANSCSGWS";

    @Override
    public void MainHandleUnityCall(String msg) {
        try {
            JSONObject json = new JSONObject(msg);
            String methodName = json.getString("methodName");
            switch (methodName) {
                case "HXInit":
                    HXInit();
                    break;
                case "DownLoadGame":
                    String url = json.getString("url");
                    String saveName = json.getString("saveName");
                    DownLoadGame(url, saveName);
                break;
                case "InstallApk": {
                    InstallApk();
                }
                break;
                default:
                    SdkManager.INSTANCE().MainHandleUnityCall(msg);
                break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("UnityCallAndroid", "HandleUnityCall error : " + e.getMessage());
        }
    }

    @SuppressLint("HandlerLeak")
    private void runDownLoadGame() {
        m_downloadHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DOWNLOAD:
                        UnityPlayer.UnitySendMessage("SDKHelper", "SDKCallLua", "{\"methodName\":\"DownLoadGameProgressCallback\",\"progress\":" + String.valueOf(m_progress) + "}");
                        break;
                    case DOWNLOAD_FINISH:
                        InstallApk();
                        break;
                    default:
                        break;
                }
            }

            ;
        };

        new downloadApkThread().start();
    }

    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    m_savePath = sdpath + "Download";
                    File file = new File(m_savePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }

                    File apkFile = new File(m_savePath, m_saveName);

                    if (apkFile.exists()) {
                        m_downloadHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                        return;
                    }

                    File tmpFile = new File(m_savePath, m_saveName + ".tmp");
                    int loadedLength = (int) tmpFile.length();

                    URL url = new URL(m_url);
                    // ��������
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("range", "bytes=" + loadedLength + "-");
                    conn.connect();
                    // ��ȡ�ļ���С
                    int length = conn.getContentLength();
                    length += loadedLength;
                    // ����������
                    InputStream is = conn.getInputStream();

                    FileOutputStream fos = new FileOutputStream(tmpFile, tmpFile.exists());
                    int count = loadedLength;
                    // ����
                    byte buf[] = new byte[1024];
                    m_progress = 0;
                    m_downloadHandler.sendEmptyMessage(DOWNLOAD);
                    // д�뵽�ļ���
                    while (true) {
                        int numread = is.read(buf);
                        count += numread;
                        // ���������λ��
                        int newProgress = (int) (((float) count / length) * 100);
                        if (newProgress > m_progress) {
                            m_progress = newProgress;
                            m_downloadHandler.sendEmptyMessage(DOWNLOAD);
                        }
                        if (numread <= 0) {
                            // �������
                            break;
                        }
                        // д���ļ�
                        fos.write(buf, 0, numread);
                    }
                    fos.close();
                    is.close();

                    tmpFile.renameTo(apkFile);
                    m_downloadHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                } else {
                    UnityPlayer.UnitySendMessage("SDKHelper", "SDKCallLua", "{\"methodName\":\"DownLoadGameCallback\",\"ret\":-1}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                UnityPlayer.UnitySendMessage("SDKHelper", "SDKCallLua", "{\"methodName\":\"DownLoadGameCallback\",\"ret\":-1}");
            }
        }
    }

    public void InnerInstallApk() {
        File apkfile = new File(m_savePath, m_saveName);
        if (!apkfile.exists()) {
            return;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", apkfile);
            Log.d("HaoXinSdk", "APKURI : " + apkUri.getAuthority());
            i.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            i.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        }

        this.startActivity(i);

        UnityPlayer.UnitySendMessage("SDKHelper", "SDKCallLua", "{\"methodName\":\"DownLoadGameCallback\",\"ret\":0}");
    }

    /**
     * 判断是否是8.0系统,是的话需要获取此权限，判断开没开，没开的话处理未知应用来源权限问题,否则直接安装
     */
    public void InstallApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //PackageManager类中在Android Oreo版本中添加了一个方法：判断是否可以安装未知来源的应用
            boolean bRet = getPackageManager().canRequestPackageInstalls();
            if (bRet) {
                InnerInstallApk();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.app_icon);
                builder.setTitle(getString(R.string.new_version_title));
                builder.setMessage(getString(R.string.new_version_tips));
                builder.setPositiveButton(getString(R.string.haoxin_setting), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startInstallPermissionSettingActivity();
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.haoxin_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        } else {
            InnerInstallApk();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, INSTALL_PACKAGES_REQUESTCODE);
    }



    /***************************************************************************生命周期**********************************************************************************/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onDestroy() {
        super.onDestroy();
        FloatWindowManager.getInstance(this.getApplicationContext()).destroyFloat();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatWindowManager.getInstance(this.getApplicationContext()).showFloat();
        //统计功能
        TsGameSDKApi.getInstance().onStatResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        //统计功能
        TsGameSDKApi.getInstance().onStatPause(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FloatWindowManager.getInstance(this.getApplicationContext()).hideFloat();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INSTALL_PACKAGES_REQUESTCODE && resultCode == RESULT_OK) {
            InstallApk();
        }
    }

    @Override
public void onConfigurationChanged(Configuration arg0) {
    super.onConfigurationChanged(arg0);
}


    public void DownLoadGame(String url, String saveName) {
        m_url = url;
        m_saveName = saveName;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                runDownLoadGame();
            }
        });
    }


    public void HXInit() {
        UnityPlayer.UnitySendMessage("SDKHelper", "SDKCallLua", "{\"methodName\":\"InitSDKComplete\",\"packageName\":\"" + PackageName + "\"}");
    }

    //调用示例
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果SDK带登陆 直接调用SDkManager中的退出 没有则是调用父类的退出
            SdkManager.INSTANCE().exit();
//            ShowExitDialog(); //父类的原生退出方法
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
