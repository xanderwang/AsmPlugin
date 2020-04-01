package com.xander.xaop.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.xander.dev.tool.DebugLine;
import com.xander.dev.tool.DebugTime;

/**
 * @author Xander
 */
public class MainActivity extends AppCompatActivity {

  boolean log = true;
  String msg = "ssss";

  @DebugTime
  @DebugLine
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //long time = System.currentTimeMillis();
    setContentView(R.layout.activity_main);
    //myLog("onCreate" + msg);
    //long costTime = System.currentTimeMillis() - time;
    //costTime = costTime - time;
    //logTime("sssss",123L);
    //L.d("sss",23L);
  }

  public String myLog(String msg) {
    return msg;
  }

  @DebugTime @DebugLine @Override protected void onResume() {
    super.onResume();
    myLog("onResume" + msg);
  }

  public static void logTime(String methodName, long time) {
    Log.d("time", "cost time:" + time);
  }
}
