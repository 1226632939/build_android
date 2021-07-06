package com.example.androidlearning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
/**
 * @author ：水泥蛋子
 * @date ：Created in 2021/6/28 17:15
 * @description：测试Activity
 * @modified By：
 * @time: 17:15
 */
public class TemporaryActivity extends Activity
{
    String pid ;
    public static Activity testActivity;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        int pid = i.getIntExtra("pid",0);
        String path = i.getStringExtra("path");
        Log.i("测试","str : pid = " + pid);

        testActivity = this;


        OutputLogTool.getInstance().getAndroidLog(pid,path);

        OutputLogTool.getInstance().setCallback(new OutputLogTool.OutputPreCallback() {
            @Override
            public void prefection() {
                finish();
            }
        });
    }
}
