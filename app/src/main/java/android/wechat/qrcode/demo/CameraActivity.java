package android.wechat.qrcode.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.wechat.qrcode.QRResolver;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * @Author:Hsj
 * @Date:2023/10/27
 * @Class:MainActivity
 * @Desc:
 */
public final class CameraActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE1 = 0x01;
    private static final int REQUEST_CODE2 = 0x02;

    private Handler workHandler;
    private QRResolver resolver;
    private SurfaceView sv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        RadioGroup rg = findViewById(R.id.rg);
        rg.setOnCheckedChangeListener((group, checkedId) -> select(checkedId));
        rg.check(R.id.rb_camera);

        iv = findViewById(R.id.iv);
        sv = findViewById(R.id.sv);
        resolver = new QRResolver(getBaseContext());
        HandlerThread thread = new HandlerThread("thread_work");
        thread.start();
        workHandler = new Handler(thread.getLooper());
    }

    private void select(int id) {
        if (id == R.id.rb_camera) {
            selectCamera();
        } else {
            selectImage();
        }
    }

    private void selectCamera() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CODE1);
        }
    }

    private void selectImage() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openDCIM();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera();
        } else if (requestCode == REQUEST_CODE2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) openDCIM();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE2 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(uri, filePathColumn,
                    null, null, null);
            if (cursor == null) return;
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String path = cursor.getString(columnIndex);
            cursor.close();
            showImage(path);
        }
    }

//==================================================================================================

    private void openDCIM() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE2);
    }

    private void showImage(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        iv.setImageBitmap(bitmap);
    }

    private void openCamera() {

    }


}