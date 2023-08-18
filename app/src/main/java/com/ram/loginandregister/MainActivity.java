package com.ram.loginandregister;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the title
        Toolbar toolbar = findViewById(R.id.main_toolbar);

        toolbar.setTitle("Ranjith App");
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Sri Ram App");


        //Open the Login Activity
        Button buttonLogin = findViewById(R.id.btn_login);

        buttonLogin.setOnClickListener(v -> {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        // Open Register Activity
        TextView textViewRegister = findViewById(R.id.tv_register_link);

        textViewRegister.setOnClickListener(v -> {
            Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });
    }
}