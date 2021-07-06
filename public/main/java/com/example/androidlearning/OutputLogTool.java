package com.example.androidlearning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
//import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
 *          1.再Appliction中调用init方法，获取到地址
 *          2.调用isCleanFile来判定是否需要对日志文件进行清理
 *          3.打印报错日志就掉下面的方法（有注释是干啥用的）
 * @time: 17:15
 */
public class OutputLogTool {

    public static int pid;

    private static int m_maxFileSize = 10 * 1024 * 1024;
    private static int SAVE_ERROR_LOG = 1;
    private static int CLEAN_LOGFILE = 2;

    private String game_log_path = "";
    private String android_log_path = "";
    private String TAG = "OutputLogTool";

    private boolean is_write = false;

    private static OutputPreCallback m_outPreCallback;

    private static OutputLogTool m_instance = new OutputLogTool();
    public static OutputLogTool getInstance(){
        return m_instance;
    }

    /**
     * 输出日志完成回调的接口
     * */
    public interface OutputPreCallback{
        void prefection();
    }

    public void init(Context context){
        pid = android.os.Process.myPid();
        game_log_path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getParent()+ File.separator+"game_log.txt";;
        android_log_path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getParent()+ File.separator+"android_log.txt";
    }

    public int getPid(){return pid;}
    public String getGameLogPath(){return game_log_path;}
    public String getAndroidLogPath(){return android_log_path;}

    /**
     *  获取到是否可以写入log
     * */
    public void setIsWrite(Activity activity){
        is_write = checkPermission(activity);
    }
    /**
     * 设置日志输出完成，关闭Activity的回调
     * */
    public void setCallback(OutputPreCallback outputPreCallback){
        m_outPreCallback = outputPreCallback;
    }
    /**
     * 判断写入权限是否存在
     * */
    private boolean checkPermission(Activity activity){
//        int permission = ActivityCompat.checkSelfPermission(activity,
//                "android.permission.WRITE_EXTERNAL_STORAGE");
//        if (permission != PackageManager.PERMISSION_GRANTED)
//            return false;
        return true;
    }
    /**
     * 判断是否需要清理日志文件
     * */
    public void isCleanFile(String path){
        if (!is_write)
            return;
        File file = new File(path);
        if (!file.isFile()&&!file.exists()) {
            Log.e(TAG,"文件找不到，或者为空'");
            return;
        }
        if (file.length()>m_maxFileSize)
            cleanFile(path);
    }
    /**
     * 切换Activity打印日志
     * */
    public void switchActivity(final Activity activity){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i  = new Intent(activity,TemporaryActivity.class);
                i.putExtra("pid",pid);
                i.putExtra("path",android_log_path);
                i.putExtra("is_write",is_write);
                activity.startActivity(i);
            }
        });
    }
    /**
     * 启动输出Android日志线程
     * */
    public void getAndroidLog(int m_pid,String path){
        if (!is_write)
            return;
        SaveLogInfoThread m_locatOutputThread = new SaveLogInfoThread(m_pid,path);
        m_locatOutputThread.start();
    }
    /**
     * 启动输入崩溃日志线程
     * */
    public void getErrorLog( String path, Throwable ex){
        if (!is_write)
            return;
        SaveLogInfoThread logThread = new SaveLogInfoThread(SAVE_ERROR_LOG,path,ex);
        logThread.start();
    }
    /**
     * 开启清理Log文件线程
     * */
    private void cleanFile(String path){
        SaveLogInfoThread thread = new SaveLogInfoThread(path,CLEAN_LOGFILE);
        thread.start();

    }
    /**
     * 输出日志线程
     * */
    private static class SaveLogInfoThread extends Thread {

        private String LOG_TAG = "SaveLogInfoThread";
        private int m_pid ;
        private String m_path;
        private int m_taskType = 0;
        private String m_ext;
        private Throwable m_ex;
        /**
         * 删减log日志文件
         * */
        public SaveLogInfoThread(String path,int type){
            m_taskType = type;
            m_path = path;
        }
        /**
         * 正常输出log日志
         * */
        public SaveLogInfoThread(int pid, String path){
            m_pid = pid;
            m_path = path;
        }
        /**
         * 备用
         * */
        public SaveLogInfoThread(int type, int pid , String path,String ext){
            this.m_taskType = type;
            this.m_pid = pid;
            this.m_path = path;
            this.m_ext = ext;
        }
        /**
         * 写入崩溃日志
         * */
        public SaveLogInfoThread(int type , String path, Throwable ex){
            this.m_taskType = type;
            this.m_path = path;
            this.m_ex = ex;
        }

        @Override
        public void run() {
            super.run();

            if (m_taskType == 0){
                saveLogInfoFile(m_pid,m_path);
            }else if (m_taskType == 1){
                saveCrashInfoFile(m_path,m_ex);
            }else if (m_taskType == 2){
                cleanFile(m_path);
            }
        }

        /**
         * 获取到命令行
         * */
        private ArrayList<String> getCmd(int pid){
            ArrayList<String> cmdLine = new ArrayList<String>();
            cmdLine.add("logcat");
            cmdLine.add("-d");
            cmdLine.add(pid+"");
            return cmdLine;
        }
        /**
         * 崩溃信息写入
         * */
        private void saveCrashInfoFile(String path,Throwable ex){
            DateFormat formatter =  new SimpleDateFormat("yyyy-MM-dd HH-mm-ss SSS");
            String time = formatter.format(new Date());

            StringBuffer sb = new StringBuffer();
            sb.append("\n"+ time +"\ndevice data ：  "+"device_brand : " + android.os.Build.BRAND + "     "+"device_model : " + android.os.Build.MODEL +  "      system_version : " + android.os.Build.VERSION.RELEASE + "\n");
            sb.append(parsingThrowable(ex));

            writeFile(sb.toString(),path,true);
        }
        /**
         * 删减log文件大小;
         * */
        private void cleanFile(String path){
            File file = new File(path);
            try {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int lineIndex=0;//判断第几行,readLine方法整行读取
                String cacheLog = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    lineIndex++;
                    if(lineIndex>=5 * 1024 * 1024){
                        cacheLog += lineTxt +"\n";
                    }
                }
                read.close();
                writeFile(cacheLog,path,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * 写入Txt
         * */
        private void writeFile(String str,String path,boolean is_overload){
            try {
                FileOutputStream outStream = getOutStream(path,is_overload);
                outStream.write(str.getBytes("UTF-8"));
                outStream.close();
            }catch (Exception e){
                Log.e(LOG_TAG, "an error occured while writing file...", e);
            }
        }
        /**
         * 开启日志写入
         * */
        private void saveLogInfoFile(int pid,String path){
            try {
                ArrayList<String> cmdLine = getCmd(pid);

                Process process=Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()]));
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));

                String str;
                String newline = System.getProperty("line.separator");

                FileOutputStream outStream =getOutStream(path,true);

                while((str=bufferedReader.readLine())!=null){
                    outStream.write(str.getBytes());
                    outStream.write(newline.getBytes("UTF-8"));
                }
                outStream.close();

            }catch (Exception e){

            }
            Log.i("SaveLogInfoThread","--------func end--------");
            if (m_outPreCallback !=null)
                m_outPreCallback.prefection();
        }
        /***
         * 获取到输出流
         */
        private FileOutputStream getOutStream(String path,boolean bool) throws Exception{
            File file = new File(path);
            FileOutputStream outStream = new FileOutputStream(file,bool);
            return outStream;
        }
        /**
         * 解析 崩溃信息
         * */
        private String  parsingThrowable(Throwable ex){
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
