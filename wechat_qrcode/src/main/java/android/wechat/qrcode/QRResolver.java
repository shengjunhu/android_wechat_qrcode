package android.wechat.qrcode;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @Author:Hsj
 * @Date:2023/10/27
 * @Class:QRResolver
 * @Desc:
 */
public final class QRResolver {

    private static final String TAG    = "QRResolver";
    private static final String MODEL1 = "qr/detect.caffemodel";
    private static final String PROTO1 = "qr/detect.prototxt";
    private static final String MODEL2 = "qr/sr.caffemodel";
    private static final String PROTO2 = "qr/sr.prototxt";
    private static final String DIR    = "qr";

    public static final int STATE_OK   =  0x00;
    public static final int ERROR_EXE  = -0x01;
    public static final int ERROR_ARG  = -0x02;
    public static final int ERROR_INIT = -0x03;

    public QRResolver(Context context) {
        if (context == null) throw new NullPointerException("QRResolver: Context is null.");
        File dir = new File(context.getFilesDir(), DIR);
        if (!dir.exists()) Log.i(TAG, "mkdir：" + dir.mkdir());
        if (checkModel(context.getAssets(), dir)) {
            String path   = dir.getAbsolutePath();
            String model1 = path + File.separator + MODEL1;
            String proto1 = path + File.separator + PROTO1;
            String model2 = path + File.separator + MODEL2;
            String proto2 = path + File.separator + PROTO2;
            this.handle = create(model1, proto1, model2, proto2);
        } else {
            Log.e(TAG, "QRResolver: load model failed.");
        }
    }

    public int decodeImg(byte[] img, int length, List<String> codes, List<Float> points) {
        int ret = ERROR_ARG;
        if (codes == null) {
            Log.e(TAG, "decodeImg: codes is null.");
        } else if (points == null) {
            Log.e(TAG, "decodeImg: points is null.");
        } else if (img == null) {
            Log.e(TAG, "decodeImg: img is null.");
        } else if (handle == 0) {
            ret = ERROR_INIT;
            Log.e(TAG, "decodeImg: QRResolver init error.");
        } else {
            codes.clear(); points.clear();
            ret = decode(handle, img, length, 1, codes, points);
        }
        return ret;
    }

    public int decodeImg(ByteBuffer img, int length, List<String> codes, List<Float> points) {
        int ret = ERROR_ARG;
        if (codes == null) {
            Log.e(TAG, "decodeImg: codes is null.");
        } else if (points == null) {
            Log.e(TAG, "decodeImg: points is null.");
        } else if (img == null) {
            Log.e(TAG, "decodeImg: img is null.");
        } else if (handle == 0) {
            ret = ERROR_INIT;
            Log.e(TAG, "decodeImg: QRResolver init error.");
        } else {
            codes.clear(); points.clear();
            ret = decode2(handle, img, length, 1, codes, points);
        }
        return ret;
    }

    public int decodeY8(byte[] img, int width, int height, List<String> codes, List<Float> points) {
        int ret = ERROR_ARG;
        if (codes == null) {
            Log.e(TAG, "decodeY8: codes is null.");
        } else if (points == null) {
            Log.e(TAG, "decodeY8: points is null.");
        } else if (img == null) {
            Log.e(TAG, "decodeY8: img is null.");
        } else if (handle == 0) {
            ret = ERROR_INIT;
            Log.e(TAG, "decodeY8: QRResolver init error.");
        } else {
            codes.clear(); points.clear();
            ret = decode(handle, img, width, height, codes, points);
        }
        return ret;
    }

    public int decodeY8(ByteBuffer img, int width, int height, List<String> codes, List<Float> points) {
        int ret = ERROR_ARG;
        if (codes == null) {
            Log.e(TAG, "decodeY8: codes is null.");
        } else if (points == null) {
            Log.e(TAG, "decodeY8: points is null.");
        } else if (img == null) {
            Log.e(TAG, "decodeY8: img is null.");
        } else if (handle == 0) {
            ret = ERROR_INIT;
            Log.e(TAG, "decodeY8: QRResolver init error.");
        } else {
            codes.clear(); points.clear();
            ret = decode2(handle, img, width, height, codes, points);
        }
        return ret;
    }

    public void destroy() {
        if (handle != 0) destroy(handle);
    }

//==================================================================================================

    private boolean loadModel(AssetManager am, String name, File file) {
        int read;
        boolean ret = false;
        InputStream is = null;
        OutputStream os = null;
        try {
            is = am.open(name);
            os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    private boolean checkModel(AssetManager am, File dir) {
        String path = dir.getAbsolutePath();
        File file1  = new File(path + File.separator + MODEL1);
        File file2  = new File(path + File.separator + PROTO1);
        File file3  = new File(path + File.separator + MODEL2);
        File file4  = new File(path + File.separator + PROTO2);
        boolean ret1 = (file1.exists() && file1.length() == 0);
        boolean ret2 = (file2.exists() && file2.length() == 0);
        boolean ret3 = (file3.exists() && file3.length() == 0);
        boolean ret4 = (file4.exists() && file4.length() == 0);
        if (!ret1) ret1 = loadModel(am, MODEL1, file1);
        if (!ret2) ret2 = loadModel(am, PROTO1, file2);
        if (!ret3) ret3 = loadModel(am, MODEL2, file3);
        if (!ret4) ret4 = loadModel(am, PROTO2, file4);
        return (ret1 && ret2 && ret3 && ret4);
    }

//======================================= Native ===================================================

    static {
        System.loadLibrary("barcode");
    }

    private long handle;

    private native long create(String model1, String proto1,
                               String model2, String proto2);

    private native int decode(long handle, byte[] data, int width, int height,
                              List<String> codes, List<Float> points);

    private native int decode2(long handle, ByteBuffer frame, int width, int height,
                              List<String> codes, List<Float> points);

    private native void destroy(long handle);

//==================================================================================================


}
