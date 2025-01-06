package com.learnbymistakes.journal;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OpenImage extends AppCompatActivity {

    ImageView fullImage;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_open_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullImage = findViewById(R.id.fullImage);
        selectedDate = getIntent().getStringExtra("selectedDate");
        loadJournalEntry(selectedDate);

    }

    private void loadJournalEntry(String date) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getEntryByDate(date);

        if (cursor != null && cursor.moveToFirst()) {
            String existingImagePath = cursor.getString((int) cursor.getColumnIndex("imagePath"));

            if (existingImagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(existingImagePath);
                Log.e("Compressed dimensions", bitmap.getWidth()+" "+bitmap.getHeight());
                fullImage.setImageBitmap(bitmap);
            }
        } else {
            fullImage.setImageResource(0);
        }

        if (cursor != null) {
            cursor.close();
        }
    }
}