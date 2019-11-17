package com.example.javasockettest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javasockettest.R;

public class ClientSettingActivity extends AppCompatActivity {

    EditText edit_client_setting_ip;
    EditText edit_client_setting_port;

    Button btn_client_setting_back;
    Button btn_client_setting_next;

    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_setting);

        init();

    }

    private void init(){
        edit_client_setting_ip = findViewById(R.id.edit_client_setting_ip);
        edit_client_setting_port = findViewById(R.id.edit_client_setting_port);

        btn_client_setting_back = findViewById(R.id.btn_client_setting_back);
        btn_client_setting_next = findViewById(R.id.btn_client_setting_next);

        btn_client_setting_back.setOnClickListener(listener);
        btn_client_setting_next.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_client_setting_back :

                    finish();

                    break;


                case R.id.btn_client_setting_next :

                    String hopstIP = edit_client_setting_ip.getText().toString();
                    String port = edit_client_setting_port.getText().toString();

                    if(hopstIP.length() <= 0 || port.length()<=0){
                        Toast.makeText(getApplicationContext(),"아이피와 포트를 입력해주세요",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    intent = new Intent(ClientSettingActivity.this, SocketClientActivity.class);
                    intent.putExtra("ip",hopstIP);
                    intent.putExtra("port",port);
                    startActivity(intent);

                    break;
            }


        }
    };


}
