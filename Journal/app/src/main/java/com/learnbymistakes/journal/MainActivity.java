package com.learnbymistakes.journal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    boolean isFirstTime;
    TextView mainName, mainDate, mainLastJournalDate, mainStreak;
    CalendarView mainCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("JournalApp", MODE_PRIVATE);
        isFirstTime = preferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            // Redirect to name input screen
            startActivity(new Intent(this, NameActivity.class));
            finish();
        } else {
            mainCalendar = findViewById(R.id.mainCalendar);
            mainName = findViewById(R.id.mainName);
            mainDate = findViewById(R.id.mainDate);
            mainLastJournalDate = findViewById(R.id.mainLastJournalDate);
            mainStreak = findViewById(R.id.mainStreak);

            mainCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
                String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                Date selectDate = new Date();
                try {
                    selectDate = dateFormat.parse(selectedDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                int days = selectDate.compareTo(currentDate);
                if(days > 0 ) {
                    Toast.makeText(this, "Can't log future entry!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, JournalEntryActivity.class);
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                }
            });
            updateMain();
        }
    }

    private void updateMain() {
        SharedPreferences preferences = getSharedPreferences("JournalApp", MODE_PRIVATE);
        String lastDate = preferences.getString("lastJournalDate", "No entries yet");
        String streak = "" + preferences.getInt("streak", 0);
        mainLastJournalDate.setText(lastDate);
        mainStreak.setText(streak);
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());
        String today = dateFormat.format(currentDate);
        mainDate.setText(today);
        mainName.setText(String.format("Welcome %s", preferences.getString("userName", "")));
    }
}