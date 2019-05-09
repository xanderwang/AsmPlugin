package com.xander.xaop.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.xander.xaop.tool.DebugLine;
import com.xander.xaop.tool.DebugTime;

public class MainActivity extends AppCompatActivity {

  boolean log = true;
  String msg = "ssss";

  @DebugTime @DebugLine @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //long time = System.currentTimeMillis();
    setContentView(R.layout.activity_main);
    //myLog("onCreate" + msg);
    //long costTime = System.currentTimeMillis() - time;
    //costTime = costTime - time;
    //logTime("myLog", costTime);
  }

  public String myLog(String msg) {
    return msg;
  }

  @DebugTime @DebugLine @Override protected void onResume() {
    super.onResume();
    myLog("onResume" + msg);
  }

  public static void logTime(String msgName, long time) {
    Log.d("time", msgName + " cost time:" + time);
  }
}
