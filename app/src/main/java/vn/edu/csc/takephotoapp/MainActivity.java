package vn.edu.csc.takephotoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.SelectorsKt;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;

public class MainActivity extends AppCompatActivity {
    private CameraView cameraView;
    private Button takePhoto;
    private Fotoapparat fotoapparat;
    private ViewGroup parent;
    private BitmapPhoto bitmapPhoto;
    private int REQUEST_CODE_PERMISSION = 123;
    public static final int REQUEST_CODE_CROPPED_IMAGE = 2009;
    public static String IMAGE_CACHE_FILE_PATH = "data/takephotoapp/tempImages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
        initEvents();
        openCameraDialogAndCheckPermission(parent);
    }

    @SuppressLint("NewApi")
    public void openCameraDialogAndCheckPermission(View layoutParent) {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };
        if (!hasPermission(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        CameraDialog cameraDialog = new CameraDialog(this);
        cameraDialog.showDialog(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermission(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isPermissionGranted()) {
            fotoapparat.start();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                fotoapparat.start();
            } else {
                requestPermission();
            }
        }
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void initEvents() {
        fotoapparat = createFotoapparat();
        takePhoto.setOnClickListener(v -> {
            capturePhoto();
        });
    }

    private void initControls() {
        cameraView = findViewById(R.id.camera_view);
        takePhoto = findViewById(R.id.takePhoto);
        parent = findViewById(R.id.parent);
    }

    public void capturePhoto() {
        PhotoResult photoResult = fotoapparat.takePicture();
        photoResult.saveToFile(getAbsoluteDir(this, null)).whenDone(unit -> {
            Intent intent = new Intent(this, ImageShowActivity.class);
            intent.putExtra("file_path", getAbsoluteDir(this, null).getAbsolutePath());
            startActivity(intent);
        });

    }

    public static File getAbsoluteDir(Context ctx, String optionalPath) {
        String rootPath;
        if (!TextUtils.isEmpty(optionalPath)) {
            rootPath = Objects.requireNonNull(ctx.getExternalFilesDir(optionalPath)).getAbsolutePath();
        } else {
            rootPath = Objects.requireNonNull(ctx.getExternalFilesDir(null)).getAbsolutePath();
        }
        // extraPortion is extra part of file path
        String extraPortion = "Android/data/" + BuildConfig.APPLICATION_ID
                + File.separator + "files" + File.separator + ".jpg";
        // Remove extraPortion
        rootPath = rootPath.replace(extraPortion, "");
        return new File(rootPath);
    }

    private Fotoapparat createFotoapparat() {
        return Fotoapparat
                .with(this)
                .into(cameraView)
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(back())
                .focusMode(SelectorsKt.firstAvailable(
                        FocusModeSelectorsKt.continuousFocusPicture(),
                        FocusModeSelectorsKt.autoFocus(),
                        FocusModeSelectorsKt.fixed())
                )
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(e -> {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                })
                .build();
    }
}