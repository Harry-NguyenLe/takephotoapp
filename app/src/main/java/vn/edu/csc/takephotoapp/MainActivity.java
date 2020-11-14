package vn.edu.csc.takephotoapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private ImageButton takePhoto;
    private ViewGroup parent;
    private PreviewView previewView;
    private int REQUEST_CODE_PERMISSION = 123;
    private MainActivityViewModel activityViewModel;
    private ImageCapture imageCapture;
    private File outputDirectory;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForCurrentPermissions();
        activityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        initControls();
        observeVM();
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        ImageCapture.Builder builder = new ImageCapture.Builder();
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }
        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
        takePhoto.setOnClickListener(v -> {
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(getOutputFileDirectory()).build();
            imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Uri savedUri = Uri.fromFile(outputDirectory);
                    activityViewModel.setImage(savedUri);
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void checkForCurrentPermissions() {
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestNewPermissions();
        }
    }

    private void requestNewPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                },
                REQUEST_CODE_PERMISSION);
    }

    private File getOutputFileDirectory() {
        boolean isDirectoryCreated = false;
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.JAPAN);
        String fileName = simpleDateFormat.format(date) + ".jpg";
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!Objects.requireNonNull(mediaStorageDir).exists()) {
            isDirectoryCreated = mediaStorageDir.mkdir();
        }
        if (!mediaStorageDir.exists() && !isDirectoryCreated) {
            Toast.makeText(this, "Fail to create file", Toast.LENGTH_SHORT).show();
        }
        outputDirectory = new File(mediaStorageDir.getAbsolutePath() + File.separator + fileName);
        return outputDirectory;
    }

    private void addImageToGallery(final String filePath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void initControls() {
        previewView = findViewById(R.id.camera_view);
        takePhoto = findViewById(R.id.takePhoto);
        parent = findViewById(R.id.parent);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void observeVM() {
        activityViewModel.getImage().observe(this, uri -> {
            addImageToGallery(uri.getPath());
            Intent intent = new Intent(this, ImageShowActivity.class);
            intent.putExtra("Uri", uri.getPath());
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            checkForCurrentPermissions();
        }
    }
}