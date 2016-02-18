package com.core;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * @author hkff
 */
public class MainActivity extends ActionBarActivity {

    Switch serverStatus;
    ServerThread server;
    public static int PORT = 5000;
    private Activity _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _this = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // We get our switch from the graphical interface
        this.serverStatus = (Switch) findViewById(R.id.serverStatus);

        // We add a listener to our switch to turn on/off the server
        this.serverStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //if(1==1) return;
                if(isChecked){
                    // If the switch is on ON position, we run the server
                    server = new ServerThread(_this, PORT);
                    server.start();

                }else{
                    // We stop the server, (but we check first if the server is running and not null
                    // to avoid NULL pointer exception)
                    if(server != null && server.isAlive())
                        server.exit();

                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferences(Activity.MODE_PRIVATE).edit().putBoolean("SSBTN", this.serverStatus.isChecked()).commit();
        System.out.println("paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        this.serverStatus.setChecked(getPreferences(Activity.MODE_PRIVATE).getBoolean("SSBTN", false));
        System.out.println("resumed");
    }

}
