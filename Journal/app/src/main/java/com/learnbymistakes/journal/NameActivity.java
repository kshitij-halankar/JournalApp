package com.learnbymistakes.journal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NameActivity extends AppCompatActivity {

    EditText regUsername;
    Button regNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_name);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        regUsername = findViewById(R.id.regUsername);
        regNextButton = findViewById(R.id.regNextButton);

        regNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = regUsername.getText().toString();
                if (!userName.isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences("JournalApp", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userName", userName);
                    editor.putBoolean("isFirstTime", false);
                    editor.apply();

                    startActivity(new Intent(NameActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(NameActivity.this, "Please Enter Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}