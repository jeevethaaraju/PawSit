package com.example.petsit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RoleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.role);  // make sure your layout file is named first.xml

        Button btnowner = findViewById(R.id.btn_pet_owner);
        Button btnsitter = findViewById(R.id.btn_pet_sitter);
        Button btnadmin = findViewById(R.id.btn_pet_admin);

        btnowner.setOnClickListener(view -> {
            Intent intent = new Intent(RoleActivity.this, LoginActivity.class);
            startActivity(intent);
        });



        btnsitter.setOnClickListener(view -> {
            Intent intent = new Intent(RoleActivity.this, LoginSitActivity.class);
            startActivity(intent);
        });

        btnadmin.setOnClickListener(view -> {
            Intent intent = new Intent(RoleActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }
}
