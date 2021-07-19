package com.example.tools.logCat;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * @program:
 * @description: 日志系统
 * @author: authorName
 * @create: 2020-10-16 11:39
 **/
public class Logger {
    private static boolean isLog=true;
    private static String TAG = "GetLogCat";

    public static void isLog(boolean bool){
        isLog=bool;
    }

    //弹窗信息
    public static void t(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
    public static void t(Context context, int r){
        Toast.makeText(context, r, Toast.LENGTH_SHORT).show();
    }

    //错误信息
    public static void e(String str){
        if (isLog){ Log.e(TAG,"_____      \n| error ： " + str + "\n_____      "+Thread.currentThread().getStackTrace()[3]);}
    }
    //错误信息
    public static void e(String str,int index){
        if (isLog){ Log.e(TAG,"_____      \n| " + str + "\n_____      "+Thread.currentThread().getStackTrace()[index]);}
    }

    //警告信息
    public static void w(String str){
        if (isLog){Log.w(TAG,"_____      w : \n" + str + "\n      _____");}
    }

    //调试信息
    public  static void d(String str){
        if (isLog){Log.d(TAG,"_____      d : \n" + str + "\n      _____");}
    }

    //重要数据
    public static void i(String str){
        if (isLog){Log.i(TAG,"_____      i : \n" + str + "\n      _____");}
    }
}
