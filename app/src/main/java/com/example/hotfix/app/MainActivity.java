package com.example.hotfix.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.bt_open_plugin).setOnClickListener(view -> {
            // TODO
            Toast.makeText(MainActivity.this, "点击打开插件按钮", Toast.LENGTH_SHORT).show();
        });
    }
}