package com.example.tools.logCat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
//import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.example.androidlearning.TemporaryActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 * @author ：水泥蛋子
 * @date ：Created in 2021/6/28 17:15
 * @description：日志记录工具
 * @modified By：
 *        使用方式：
 *          1.在Appliction中调用init方法，获取到地址
 *          2.调用isCleanFile来判定是否需要对日志文件进行清理
 *          3.打印报错日志就掉下面的方法（有注释是干啥用的）
 * @time: 17:15
 */
public class OutputLogTool {

    public static int pid;
    private static int SAVE_ERROR_LOG = 1;

    private String game_log_path = "";
    private String android_log_path = "";
    private String TAG = "OutputLogTool";


    private static OutputPreCallback m_outPreCallback;

    private static OutputLogTool m_instance = new OutputLogTool();

    public static OutputLogTool getInstance() {
        return m_instance;
    }

    /**
     * 输出日志完成回调的接口
     */
    public interface OutputPreCallback {
        void prefection();
    }

    public void init(Context context) {
        pid = android.os.Process.myPid();
        game_log_path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getParent() + File.separator + "game_log.txt";
        android_log_path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getParent() + File.separator + "android_log.txt";
    }

    public int getPid() {
        return pid;
    }

    public String getGameLogPath() {
        return game_log_path;
    }

    public String getAndroidLogPath() {
        return android_log_path;
    }


    /**
     * 设置日志输出完成，关闭Activity的回调
     */
    public void setCallback(OutputPreCallback outputPreCallback) {
        m_outPreCallback = outputPreCallback;
    }

    /**
     * 切换Activity打印日志
     */
    public void switchActivity(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(activity, TemporaryActivity.class);
                i.putExtra("pid", pid);
                i.putExtra("path", android_log_path);
                activity.startActivity(i);
            }
        });
    }

    /**
     * 启动输出Android日志线程
     */
    public void getAndroidLog(int m_pid, String path) {
        Log.i(TAG,"start output android log thread");
        SaveLogInfoThread m_locatOutputThread = new SaveLogInfoThread(m_pid, path);
        m_locatOutputThread.start();
    }

    /**
     * 启动输入崩溃日志线程
     */
    public void getErrorLog(String path, Throwable ex) {
        Log.i(TAG,"start output game log thread");
        SaveLogInfoThread logThread = new SaveLogInfoThread(SAVE_ERROR_LOG, path, ex);
        logThread.start();
    }

    /**
     * 输出日志线程
     */
    private static class SaveLogInfoThread extends Thread {

        private String LOG_TAG = "SaveLogInfoThread";
        private int m_pid;
        private String m_path;
        private int m_taskType = 0;
        private String m_ext;
        private Throwable m_ex;
        private long start;
        private long end;

        /**
         * 正常输出log日志
         */
        public SaveLogInfoThread(int pid, String path) {
            m_pid = pid;
            m_path = path;
        }

        /**
         * 备用
         */
        public SaveLogInfoThread(int type, int pid, String path, String ext) {
            this.m_taskType = type;
            this.m_pid = pid;
            this.m_path = path;
            this.m_ext = ext;
        }

        /**
         * 写入崩溃日志
         */
        public SaveLogInfoThread(int type, String path, Throwable ex) {
            this.m_taskType = type;
            this.m_path = path;
            this.m_ex = ex;
        }

        @Override
        public void run() {
            super.run();

            start = System.currentTimeMillis();

            if (m_taskType == 0) {
                saveLogInfoFile(m_pid, m_path);
            } else if (m_taskType == 1) {
                saveCrashInfoFile(m_path, m_ex);
            }
        }

        /**
         * 获取到命令行
         */
        private ArrayList<String> getCmd(int pid) {
            ArrayList<String> cmdLine = new ArrayList<String>();
            cmdLine.add("logcat");
            cmdLine.add("-d");
            cmdLine.add(pid + "");
            return cmdLine;
        }

        /**
         * 崩溃信息写入
         */
        private void saveCrashInfoFile(String path, Throwable ex) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            String time = formatter.format(new Date());

            StringBuffer sb = new StringBuffer();
            sb.append("\n" + time + "\ndevice data ：  " + "device_brand : " + android.os.Build.BRAND + "     " + "device_model : " + android.os.Build.MODEL + "      system_version : " + android.os.Build.VERSION.RELEASE + "\n");
            sb.append(parsingThrowable(ex));

            writeFile(sb.toString(), path, true);
        }

        /**
         * 写入Txt
         */
        private void writeFile(String str, String path, boolean is_overload) {
            try {
                FileOutputStream outStream = getOutStream(path, is_overload);
                outStream.write(str.getBytes("UTF-8"));
                outStream.close();
            } catch (Exception e) {
                Log.e(LOG_TAG, "an error occured while writing file...", e);
            }
        }

        /**
         * 开启日志写入
         */
        private void saveLogInfoFile(int pid, String path) {
            try {

                ArrayList<String> cmdLine = getCmd(pid);

                Process process = Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()]));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String str;
                String newline = System.getProperty("line.separator");

                FileOutputStream outStream = getOutStream(path, false);

                while ((str = bufferedReader.readLine()) != null) {
                    outStream.write(str.getBytes());
                    outStream.write(newline.getBytes("UTF-8"));
                }
                outStream.close();


            } catch (Exception e) {

            }

            end = System.currentTimeMillis();
            long time = end - start;

            Log.i(LOG_TAG, "--------func end! Task executed: " + time + ".ms--------");

            if (m_outPreCallback != null)
                m_outPreCallback.prefection();
        }

        /***
         * 获取到输出流
         */
        private FileOutputStream getOutStream(String path, boolean bool) throws Exception {
            File file = new File(path);
            FileOutputStream outStream = new FileOutputStream(file, bool);
            return outStream;
        }

        /**
         * 解析 崩溃信息
         */
        private String parsingThrowable(Throwable ex) {

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();

            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();

            return writer.toString();
        }
    }
}
