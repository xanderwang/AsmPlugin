package io.github.xander.plugin.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.xander.dev.DevLine;
import io.github.xander.dev.DevTime;

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
