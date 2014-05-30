package com.example.testgradle;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.android.volley.AuthFailureError;

public class MainActivity extends Activity {
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        tv.append(ManifestUtils.getMetaData(this, "UMENG_CHANNEL").toString());
        tv.append("\n" + ManifestUtils.getVersionName(this));
        tv.append("\n" + ManifestUtils.getVersionCode(this));
        tv.append("\n" + new AuthFailureError().getMessage());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
