package com.learnbymistakes.journal;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
    ImageView journalImg, taskImg, plannerImg;
    ImageView journalCam, taskCam, plannerCam;
    ImageView journalDelete, taskDelete, plannerDelete;
    Button journalSave;
    String selectedDate;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    Uri journalImgUri, taskImgUri, plannerImgUri;
    private ActivityResultLauncher<Uri> journalCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    displayJournalImage();
                    Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });
    private ActivityResultLauncher<Uri> taskCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    displayTaskImage();
                    Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });
    private ActivityResultLauncher<Uri> plannerCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    displayPlannerImage();
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

        registerViews();

        loadJournalEntry(selectedDate);

        journalCam.setOnClickListener(v -> openJournalCamera());
        taskCam.setOnClickListener(v -> openTaskCamera());
        plannerCam.setOnClickListener(v -> openPlannerCamera());

        journalDelete.setOnClickListener(v -> deleteImage("journal"));
        taskDelete.setOnClickListener(v -> deleteImage("task"));
        plannerDelete.setOnClickListener(v -> deleteImage("planner"));

        journalImg.setOnClickListener(view -> {
            Intent intent = new Intent(JournalEntryActivity.this, OpenImage.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("Type", "journalImagePath");
            startActivity(intent);
        });

        taskImg.setOnClickListener(view -> {
            Intent intent = new Intent(JournalEntryActivity.this, OpenImage.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("Type", "taskImagePath");
            startActivity(intent);
        });

        plannerImg.setOnClickListener(view -> {
            Intent intent = new Intent(JournalEntryActivity.this, OpenImage.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("Type", "plannerImagePath");
            startActivity(intent);
        });

        journalSave.setOnClickListener(v -> saveJournalEntry());
    }

    private void registerViews() {

        journalText = findViewById(R.id.journalText);
        journalDate = findViewById(R.id.journalDate);

        journalImg = findViewById(R.id.journalImg);
        taskImg = findViewById(R.id.taskImg);
        plannerImg = findViewById(R.id.plannerImg);

        journalCam = findViewById(R.id.journalCam);
        taskCam = findViewById(R.id.taskCam);
        plannerCam = findViewById(R.id.plannerCam);

        journalDelete = findViewById(R.id.journalDelete);
        taskDelete = findViewById(R.id.taskDelete);
        plannerDelete = findViewById(R.id.plannerDelete);

        journalSave = findViewById(R.id.journalSave);
        selectedDate = getIntent().getStringExtra("selectedDate");
        journalDate.setText(selectedDate);
    }

    private void openJournalCamera() {
        checkCameraPermission();
        journalImgUri = createImageUri("Journal_");
        if (journalImgUri != null) {
            journalCameraLauncher.launch(journalImgUri);
        } else {
            Toast.makeText(this, "Unable to create Journal image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTaskCamera() {
        checkCameraPermission();
        taskImgUri = createImageUri("Task_");
        if (taskImgUri != null) {
            taskCameraLauncher.launch(taskImgUri);
        } else {
            Toast.makeText(this, "Unable to create Task image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlannerCamera() {
        checkCameraPermission();
        plannerImgUri = createImageUri("Planner_");
        if (plannerImgUri != null) {
            plannerCameraLauncher.launch(plannerImgUri);
        } else {
            Toast.makeText(this, "Unable to create Planner image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            return;
        }
    }

    private Uri createImageUri(String imgName) {
        String imageName = imgName + System.currentTimeMillis() + ".png";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JournalImages");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private String setCursor(Uri imgUri) throws ParseException {
        String imagePath = "";
        if (imgUri != null) {
            Cursor cursor = getContentResolver().query(imgUri,
                    new String[]{MediaStore.Images.Media.DATA},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                if (dataIndex != -1) {
                    imagePath = cursor.getString(dataIndex);
                }
                cursor.close();
            }
        }
        return imagePath;
    }

    private void saveJournalEntry() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String journalImagePath = "", taskImagePath = "", plannerImagePath = "";
        try {
            String journalImgPath = setCursor(journalImgUri);
            String taskImgPath = setCursor(taskImgUri);
            String plannerImgPath = setCursor(plannerImgUri);

            Date selected = dateFormat.parse(selectedDate);

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            Cursor cursor = dbHelper.getEntryByDate(selectedDate);
            if (cursor != null && cursor.moveToFirst()) {
                journalImagePath = cursor.getString((int) cursor.getColumnIndex(
                        "journalImagePath"));
                taskImagePath = cursor.getString((int) cursor.getColumnIndex("taskImagePath"));
                plannerImagePath = cursor.getString((int) cursor.getColumnIndex("plannerImagePath"));
                cursor.close();
            }

            journalImgPath = journalImgPath.isEmpty() ? journalImagePath : journalImgPath;
            taskImgPath = taskImgPath.isEmpty() ? taskImagePath : taskImgPath;
            plannerImgPath = plannerImgPath.isEmpty() ? plannerImagePath : plannerImgPath;

            Date today = new Date();
            long entryDiff = (today.getTime() - selected.getTime()) / (1000 * 60 * 60 * 24);
            if (entryDiff < 1) {
                String text = journalText.getText().toString();

                boolean isSaved = dbHelper.saveEntry(selectedDate, text, journalImgPath,
                        taskImgPath, plannerImgPath);

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

    private void deleteImage(String img) {
        switch(img) {
            case "journal":
                journalImg.setImageResource(R.drawable.add_image);
                journalImgUri = null;
                break;

            case "task":
                taskImg.setImageResource(R.drawable.add_image);
                taskImgUri = null;
                break;

            case "planner":
                plannerImg.setImageResource(R.drawable.add_image);
                plannerImgUri = null;
                break;
        }
    }

    private void loadJournalEntry(String date) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getEntryByDate(date);

        if (cursor != null && cursor.moveToFirst()) {
            String text = cursor.getString((int) cursor.getColumnIndex("text"));
            String journalImgPath = cursor.getString((int) cursor.getColumnIndex("journalImagePath"));
            String taskImagePath = cursor.getString((int) cursor.getColumnIndex("taskImagePath"));
            String plannerImagePath = cursor.getString((int) cursor.getColumnIndex("plannerImagePath"));

            journalText.setText(text);
            setImg(journalImgPath, journalImg);
            setImg(taskImagePath, taskImg);
            setImg(plannerImagePath, plannerImg);
        } else {
            journalText.setText("");
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void setImg(String imgPath, ImageView imgView) {
        if (imgPath != null) {
            if (imgPath.isEmpty()) {
                imgView.setImageResource(R.drawable.add_image);
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                imgView.setImageBitmap(bitmap);
            }
        }
    }

    private void displayJournalImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(journalImgUri));
            journalImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayTaskImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(taskImgUri));
            taskImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPlannerImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(plannerImgUri));
            plannerImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
}