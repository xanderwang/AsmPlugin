package com.xander.xaop.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.xander.dev.tool.DevLine;
import com.xander.dev.tool.DevTime;

/**
 * @author Xander
 */
public class MainActivity extends AppCompatActivity {

  @DevTime
  @DevLine
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @DevTime
  @DevLine
  @Override
  protected void onResume() {
    super.onResume();
  }

}
