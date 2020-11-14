package vn.edu.csc.takephotoapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ImageShowActivity extends AppCompatActivity {
    private ImageView ivImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);
        initControls();
    }

    private void initControls() {
        ivImage = findViewById(R.id.ivImage);
        Uri uri = Uri.parse(getIntent().getStringExtra("Uri"));
        ivImage.setImageURI(uri);
    }
}
