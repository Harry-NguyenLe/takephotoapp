package vn.edu.csc.takephotoapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

public class CameraDialog extends Dialog {
    public static final int REQUEST_CODE_CAMERA = 2000;

    public CameraDialog(@NonNull Context context) {
        super(context);
    }

    public void showDialog(Activity context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.photo_dialog);

        LinearLayout layout_take_photo_dialog = dialog.findViewById(R.id.layout_take_photo_dialog);
        layout_take_photo_dialog.setOnClickListener(v -> {
            if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                dialog.dismiss();
            } else {
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivityForResult(intent, REQUEST_CODE_CAMERA);
                dialog.dismiss();
            }
        });
    }
}
