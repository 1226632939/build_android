package com.example.tools.logCat;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.TimeoutException;

/**
 * @author ：水泥蛋子
 * @date ：Created in 2021/7/19 10:57
 * @description： 全局异常捕获，所有没有try catch 的异常全部捕获，可以配置忽略一些不影响游戏进程的异常，防止闪退。（主线程异常不行）
 * @modified By：
 * @time: 10:57
 */
public class CommonCrashHandler implements Thread.UncaughtExceptionHandler{

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // CrashHandler实例
    private static CommonCrashHandler instance;
    // 程序的Context对象
    private Context mContext;
    /**
     * 忽略的异常的特征值，每次三个都要配，配成"IGNORE_ANYWAY"表示忽略
     * IgnoreThread 报错线程
     * IgnoreStack  报错堆栈节选
     * IgnoreType  报错类型
     */
    String IGNORE_ANYWAY = "IGNORE_ANYWAY";
    String[] IgnoreThread = new String[]
            {"installToast", "FinalizerWatchdogDaemon"};
    int[] IgnoreType = new int[] //0-Throwable, 1-TimeoutException, 2-NullPointerException, 后面需要再加
            {2, 1};
    String[] IgnoreStack = new String[]
            {"com.vivo.mobilead.b.c$2.run", IGNORE_ANYWAY};

    private CommonCrashHandler() {
    }

    public static CommonCrashHandler getInstance() {
        if (instance == null)
            instance = new CommonCrashHandler();
        return instance;
    }
    /**
     * Application中调用初始化
     * */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置当前CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        OutputLogTool.getInstance().init(context);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(thread, ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 这里做自己的异常处理，返回 true 则直接结束，返回 false 则进行系统的异常处理闪退
     */
    private boolean handleException(Thread thread, Throwable ex)
    {
        try {
            Logger.e("Throwable: " + ex);
            boolean DontCrash = false;

            for(int i = 0; i < IgnoreThread.length; i++)
            {
                DontCrash = CheckThread(thread, i) && CheckType(ex, i) && CheckStack(ex, i);
            }

            if(DontCrash) {//手动上报Bugly
//                if(!CommonApplication.mChannelName.equals("TMGP")) {
//                    CrashReport.putUserData(mContext, "SelfReport", "true");
//                    CrashReport.postCatchedException(ex);
//                }
            }
            // 写入报错日志到game_log
            OutputLogTool.getInstance().getErrorLog(OutputLogTool.getInstance().getGameLogPath(),ex);
            // 打印Android日志
            OutputLogTool.getInstance().getAndroidLog(OutputLogTool.getInstance().getPid(),OutputLogTool.getInstance().getAndroidLogPath());


            return DontCrash;
        } catch (Throwable e)
        {
            return false;
        }
    }

    Boolean CheckThread(Thread thread, int index) {
        return IgnoreThread[index].equals(IGNORE_ANYWAY) || thread.getName().equals(IgnoreThread[index]);
    }

    Boolean CheckType(Throwable ex, int index) {
        switch (IgnoreType[index])
        {
            case 0:
                return true;
            case 1:
                return ex instanceof TimeoutException || ex.getCause() instanceof TimeoutException;
            case 2:
                return ex instanceof NullPointerException || ex.getCause() instanceof NullPointerException;
        }
        return false;
    }

    Boolean CheckStack(Throwable ex, int index) {
        if(IgnoreStack[index].equals(IGNORE_ANYWAY))
            return true;

        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0].toString().contains(IgnoreStack[index]);
        }

        Throwable causeBy = ex.getCause();
        if (causeBy != null) {
            StackTraceElement[] causeTrace = causeBy.getStackTrace();
            if (causeTrace.length > 0) {
                return causeTrace[0].toString().contains(IgnoreStack[index]);
            }
        }
        return false;
    }
}
