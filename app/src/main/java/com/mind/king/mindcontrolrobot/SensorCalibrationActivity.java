package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class SensorCalibrationActivity extends Activity {

    Button  brightnessBtn, speedDialBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_calibration);

        brightnessBtn = (Button) findViewById(R.id.brightnessBtn);
        speedDialBtn = (Button) findViewById(R.id.speedDialBtn);


        brightnessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(SensorCalibrationActivity.this, ControlBrightnessActivity.class);
                startActivity(myIntent);
            }
        });

        speedDialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(SensorCalibrationActivity.this, SpeedDialActivity.class);
                startActivity(myIntent);
            }
        });
    }


}
