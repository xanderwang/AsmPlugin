package com.xander.xaop.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.xander.dev.tool.DebugLine;
import com.xander.dev.tool.DebugTime;

/**
 * @author Xander
 */
public class MainActivity extends AppCompatActivity {

    @DebugTime
    @DebugLine
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @DebugTime
    @DebugLine
    @Override
    protected void onResume() {
        super.onResume();
    }

}
