package com.learnbymistakes.journal;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JournalEntryActivity extends AppCompatActivity {

    EditText journalText;
    TextView journalDate;
    ImageView journalImg;
    Button journalCamera, journalImgDelete, journalSave;
    Uri imageUri;
    String selectedDate;
    String imagePath;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    Uri photoUri;
    private ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    displayImage();
                    Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        journalText = findViewById(R.id.journalText);
        journalDate = findViewById(R.id.journalDate);
        journalImg = findViewById(R.id.journalImg);
        journalCamera = findViewById(R.id.journalCamera);
        journalImgDelete = findViewById(R.id.journalImgDelete);
        journalSave = findViewById(R.id.journalSave);

        selectedDate = getIntent().getStringExtra("selectedDate");
        journalText.setText(selectedDate);

        loadJournalEntry(selectedDate);

        journalCamera.setOnClickListener(v -> openCamera());
        journalSave.setOnClickListener(v -> saveJournalEntry());
        journalImgDelete.setOnClickListener(v -> deleteImage());
        journalImg.setOnClickListener(view -> {
            Intent intent = new Intent(JournalEntryActivity.this, OpenImage.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            return;
        }

        photoUri = createImageUri();
        if (photoUri != null) {
            cameraLauncher.launch(photoUri);
        } else {
            Toast.makeText(this, "Unable to create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri() {
        String imageName = "Journal_" + System.currentTimeMillis() + ".png";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JournalImages");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    private void displayImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
            journalImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveJournalEntry() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Cursor cursor = getContentResolver().query(photoUri,
                    new String[]{MediaStore.Images.Media.DATA},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                if (dataIndex != -1) {
                    imagePath = cursor.getString(dataIndex);
                }
                cursor.close();
            }
            Date selected = dateFormat.parse(selectedDate);
            Date today = new Date();
            long entryDiff = (today.getTime() - selected.getTime()) / (1000 * 60 * 60 * 24);
            if (entryDiff < 1) {
                String text = journalText.getText().toString();

                DatabaseHelper dbHelper = new DatabaseHelper(this);
                boolean isSaved = dbHelper.saveEntry(selectedDate, text, imagePath);

                if (isSaved) {
                    Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show();

                    SharedPreferences sharedPreferences = getSharedPreferences("JournalApp", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    int currentStreak = sharedPreferences.getInt("streak", 0);

                    String lastJournalDate = sharedPreferences.getString("lastJournalDate", null);
                    Date lastDate = lastJournalDate != null
                            ? dateFormat.parse(lastJournalDate)
                            : new Date();


                    long streakDiff =
                            (selected.getTime() - lastDate.getTime()) / (1000 * 60 * 60 * 24);
                    if (streakDiff == 1) {
                        currentStreak++;
                    } else if (streakDiff > 1) {
                        currentStreak = 1;
                    }

                    editor.putInt("streak", currentStreak);
                    editor.putString("lastJournalDate", selectedDate);
                    editor.apply();

                } else {
                    Toast.makeText(this, "Failed to save entry", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Can't update old date!", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        finish();
    }

    private void deleteImage() {
        journalImg.setImageResource(0);
        imageUri = null;
    }

    private void loadJournalEntry(String date) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getEntryByDate(date);

        if (cursor != null && cursor.moveToFirst()) {
            String text = cursor.getString((int) cursor.getColumnIndex("text"));
            String imagePath = cursor.getString((int) cursor.getColumnIndex("imagePath"));

            journalText.setText(text);

            if (imagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                journalImg.setImageBitmap(bitmap);
            }
        } else {
            journalText.setText("");
            journalImg.setImageResource(0);
        }

        if (cursor != null) {
            cursor.close();
        }
    }


}