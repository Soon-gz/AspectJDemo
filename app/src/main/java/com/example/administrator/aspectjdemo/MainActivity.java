package com.example.administrator.aspectjdemo;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();
    }

    @AspectJAnnotation(value = Manifest.permission.CAMERA)
    public void test(){
        Log.i("tag00","检查权限");
    }
}
