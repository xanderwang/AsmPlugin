package com.xander.xaop.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    myLog("onCreate");

  }

  public void myLog(String msg) {

  }

  @Override protected void onResume() {
    super.onResume();
    myLog("onResume");
  }
}
