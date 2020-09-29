package vn.edu.csc.takephotoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import static vn.edu.csc.takephotoapp.MainActivity.IMAGE_CACHE_FILE_PATH;

public class ImageShowActivity extends AppCompatActivity {
    private ImageView image;
    private Uri currentUri;
    String filePath;
    private File file_saved;
    Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);
        initControls();
    }

    private void initControls() {
        image = findViewById(R.id.image);
        Intent intent = getIntent();
        filePath = intent.getStringExtra("file_path");
        if (filePath != null) {
            currentUri = Uri.fromFile(new File(filePath));
        }
        bitmap = BitmapFactory.decodeFile(currentUri.getPath());
        saveToLocalFile(bitmap);
    }

    private void saveToLocalFile(Bitmap bitmap) {
        file_saved = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGE_CACHE_FILE_PATH + "/"
                + UUID.randomUUID().toString() + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(file_saved);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        image.setImageBitmap(bitmap);
    }
}
