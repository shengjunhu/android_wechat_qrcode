package android.wechat.qrcode.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
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
    private TextView tv_tips;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        iv = findViewById(R.id.iv);
        tv_tips = findViewById(R.id.tv_tips);
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

    private List<String> codes = new ArrayList<>();
    private List<Float> points = new ArrayList<>();

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
            tv_tips.setText(String.format("read %s failed.", path));
        }
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
        StringBuilder sb = new StringBuilder();
        if (num > 0) {
            Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(copy);
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            for (int i = 0; i < num; ++i) {
                int j = i * 8;
                sb.append(codes.get(i)).append("\n");
                canvas.drawLine(points.get(j),   points.get(j+1), points.get(j+2), points.get(j+3), paint);
                canvas.drawLine(points.get(j+2), points.get(j+3), points.get(j+4), points.get(j+5), paint);
                canvas.drawLine(points.get(j+4), points.get(j+5), points.get(j+6), points.get(j+7), paint);
                canvas.drawLine(points.get(j+6), points.get(j+7), points.get(j),   points.get(j+1), paint);
            }
            runOnUiThread(()-> iv.setImageBitmap(copy));
        } else {
            sb.append("result: ").append(num);
        }
        runOnUiThread(()-> tv_tips.setText(sb.toString()));
    }
}