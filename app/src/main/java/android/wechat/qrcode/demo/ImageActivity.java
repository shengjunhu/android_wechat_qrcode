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
import android.wechat.qrcode.QRResolver;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Hsj
 * @Date:2023/10/27
 * @Class:ImageActivity
 * @Desc:
 */
public final class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";
    private static final int REQUEST_CODE = 0x01;

    private Handler workHandler;
    private QRResolver resolver;
    private TextView tv_result;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        iv = findViewById(R.id.iv);
        tv_result = findViewById(R.id.tv_result);
        resolver = new QRResolver(getBaseContext());
        HandlerThread thread = new HandlerThread("thread_work");
        thread.start();
        workHandler = new Handler(thread.getLooper());

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
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

    private List<String> codes = new ArrayList<>(5);
    private List<Float> points = new ArrayList<>(40);

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void showImage(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
            workHandler.post(()->decodeQR(bitmap));
        } else {
            tv_result.setText(String.format("read %s failed.", path));
        }
    }

    private byte[] readFile(String file) {
        byte[] data = null;
        if (file != null) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "r");
                data = new byte[(int) raf.length()];
                raf.readFully(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }

    @WorkerThread
    private void decodeQR(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height * 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        bitmap.copyPixelsToBuffer(buffer);
        buffer.clear();
        int num = resolver.decodeRGBA(buffer, width, height, codes, points);
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        runOnUiThread(()-> tv_result.setText(String.valueOf(num)));
    }
}