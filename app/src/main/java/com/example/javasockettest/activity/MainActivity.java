package com.example.javasockettest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javasockettest.R;

public class MainActivity extends AppCompatActivity {

    Intent intent;

    Button btn_socket_server;
    Button btn_socket_client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    public void init(){

        btn_socket_server = findViewById(R.id.btn_socket_server);
        btn_socket_client = findViewById(R.id.btn_socket_client);

        btn_socket_server.setOnClickListener(listener);
        btn_socket_client.setOnClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_socket_server :

                    intent = new Intent(MainActivity.this, SocketServerActivity.class);
                    startActivity(intent);

                    break;

                case R.id.btn_socket_client :

                    intent = new Intent(MainActivity.this, ClientSettingActivity.class);
                    startActivity(intent);

                    break;

            }

        }
    };


}
