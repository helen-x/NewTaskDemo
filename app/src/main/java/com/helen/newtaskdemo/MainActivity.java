package com.helen.newtaskdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_start_wihout_task_display).setOnClickListener(this);
        findViewById(R.id.btn_start_with_new_task).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_start_wihout_task_display:
                startActivityDisableTaskDisplay();
                break;
            case R.id.btn_start_with_new_task:
                startActivityWithNewTask();
                break;
        }
    }

    private void startActivityWithNewTask() {

        //以新Task启动Activity
        Intent intent = new Intent(this, TaskTestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startActivityDisableTaskDisplay() {

        //Activity不显示在recent列表中.
        Intent intent = new Intent(this, TaskTestActivity.class);
        //这里即使设置了以新Task启动Activity,也会显示在recent列表中
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }
}
