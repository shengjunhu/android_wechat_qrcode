package android.wechat.qrcode.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.wechat.qrcode.QRResolver;
import android.widget.ImageView;
import android.widget.RadioGroup;

/**
 * @Author:Hsj
 * @Date:2023/10/27
 * @Class:MainActivity
 * @Desc:
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE1 = 0x01;
    private static final int REQUEST_CODE2 = 0x02;

    private QRResolver resolver;
    private SurfaceView sv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RadioGroup rg = findViewById(R.id.rg);
        rg.setOnCheckedChangeListener((group, checkedId) -> select(checkedId));
        rg.check(R.id.rb_camera);

        iv = findViewById(R.id.iv);
        sv = findViewById(R.id.sv);
        resolver = new QRResolver(getBaseContext());
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
        if (requestCode == REQUEST_CODE2) {

        }
    }

//==================================================================================================

    private void openCamera() {

    }

    private void openDCIM() {

    }

}