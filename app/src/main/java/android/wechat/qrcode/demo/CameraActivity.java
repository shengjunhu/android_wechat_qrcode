package android.wechat.qrcode.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.wechat.qrcode.QRResolver;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author:Hsj
 * @Date:2023/10/27
 * @Class:CameraActivity
 * @Desc:
 */
@SuppressWarnings("deprecation")
public final class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CODE = 0x02;
    private Handler workHandler;
    private QRResolver resolver;
    private TextView tv_tips;
    private SurfaceView sv;
    private BoxView box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        sv = findViewById(R.id.sv);
        box = findViewById(R.id.box);
        tv_tips = findViewById(R.id.tv_tips);

        resolver = new QRResolver(getBaseContext());
        HandlerThread thread = new HandlerThread("thread_work");
        thread.start();
        workHandler = new Handler(thread.getLooper());
        isRunning = new AtomicBoolean(false);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            addCallback();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addCallback();
        }
    }

//==================================================================================================

    private AtomicBoolean isRunning;
    private volatile int length;
    private volatile int height;
    private volatile int width;
    private ByteBuffer img;
    private Camera camera;

    private void addCallback() {
        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                closeCamera();
            }
        });
    }

    private void closeCamera() {
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void openCamera(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            Camera.Parameters param = camera.getParameters();
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            Camera.Size size = param.getPreviewSize();
            param.setPictureFormat(ImageFormat.NV21);
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.setParameters(param);

            width = size.width;
            height = size.height;
            length = width * height;
            // Display rotate 90. (0, 90, 180, 270)
            box.setSize(width, height, 90);
            img = ByteBuffer.allocateDirect(length);

            camera.setPreviewCallback(callback);
            camera.startPreview();
        } catch (Exception e) {
            tv_tips.setText(e.getMessage());
        }
        isRunning.set(false);
    }

    private final Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (!isRunning.get()) {
                isRunning.set(true);
                img.clear();
                img.put(data, 0, length);
                workHandler.post(()->decodeQR(img));
            }
        }
    };

//==================================================================================================

    private List<String> codes = new ArrayList<>();
    private List<Float> points = new ArrayList<>();

    @WorkerThread
    private void decodeQR(ByteBuffer data) {
        int num = resolver.decodeY8(data, width, height, codes, points);
        StringBuilder sb = new StringBuilder();
        box.drawBox(points);
        if (num > 0) {
            for (int i = 0; i < num; ++i) {
                sb.append(codes.get(i)).append("\n");
            }
        } else {
            sb.append("result: ").append(num);
        }
        runOnUiThread(()-> tv_tips.setText(sb.toString()));
        isRunning.set(false);
    }

}