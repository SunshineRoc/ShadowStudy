package com.example.hotfix.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shadow.study.HostMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.bt_open_plugin).setOnClickListener(view -> {
            // 打开SDK首页
            startActivity(new Intent(MainActivity.this, HostMainActivity.class));
        });
    }
}