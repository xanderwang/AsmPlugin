package com.xander.xaop.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

  boolean log = true;
  String msg = "ssss";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    myLog("onCreate" + msg);
  }

  public String myLog(String msg) {
    long time = System.currentTimeMillis();
    Log.d("time", "cost time:" + (System.currentTimeMillis() - time));
    return msg;
  }

  @Override protected void onResume() {
    super.onResume();
    myLog("onResume" + msg);
  }
}
