package com.example.javasockettest.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.javasockettest.R;

public class LoginActivity extends AppCompatActivity {

    Intent intent;

    // Layout
    EditText edit_email;
    EditText edit_pwd;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        intent = new Intent(LoginActivity.this, MainActivity.class);

        edit_email = findViewById(R.id.edit_email);
        edit_pwd = findViewById(R.id.edit_pwd);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                intent.putExtra("email",edit_email.getText().toString());
//                intent.putExtra("pwd",edit_pwd.getText().toString());

                startActivity(intent);

            }
        });

    }




}
