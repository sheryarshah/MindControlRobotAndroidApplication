package com.mind.king.mindcontrolrobot;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class RoombaControlActivity extends Activity {

    Button controlBtn, accleromterBtn, mindBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomba_control);

        controlBtn = (Button) findViewById(R.id.roombaControlBtn);
        accleromterBtn = (Button) findViewById(R.id.roombaAccleromterBtn);
        mindBtn = (Button) findViewById(R.id.roombaMindBtn);

        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(RoombaControlActivity.this, RoombaButtonControlActivity.class);
                startActivity(myIntent);
            }
        });

        accleromterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(RoombaControlActivity.this, RoombaAcclerometerActivity.class);
                startActivity(myIntent);
            }
        });

        mindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(RoombaControlActivity.this, RoombaMindActivity.class);
                startActivity(myIntent);
            }
        });


    }
}
