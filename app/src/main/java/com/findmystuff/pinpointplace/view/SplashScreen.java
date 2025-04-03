package com.findmystuff.pinpointplace.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ppp.pinpointplace.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);
        // Answers.getInstance().logContentView(new ContentViewEvent()
             //  .putContentName("Launching PinPointPlace App"));

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    Intent intent = new Intent(getApplicationContext(), ShowStuffActivity.class);
                    startActivity(intent);
                    // Log.w("GJT", "Loading splashcreen");
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }
}
