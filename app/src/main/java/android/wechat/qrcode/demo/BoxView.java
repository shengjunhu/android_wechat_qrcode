package android.wechat.qrcode.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.WorkerThread;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Hsj
 * @Date:2023/6/13
 * @Class:BoxView
 * @Desc:
 */
public final class BoxView extends View {

    private static final String TAG = "BoxView";
    private Paint paint;
    private int width, height;
    private int frameW, frameH;
    private float scaleX, scaleY;
    private List<Float> points;
    private int len;

    public BoxView(Context context) {
        this(context, null);
    }

    public BoxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStrokeWidth(2f);
        this.paint.setColor(Color.GREEN);
        this.paint.setStyle(Paint.Style.STROKE);
        this.points = new ArrayList<>(5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        if (frameW != 0) scaleX = 1.00f * w / frameW;
        if (frameH != 0) scaleY = 1.00f * h / frameH;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < len; i += 8) {
            canvas.drawLine(points.get(i),   points.get(i+1), points.get(i+2), points.get(i+3), paint);
            canvas.drawLine(points.get(i+2), points.get(i+3), points.get(i+4), points.get(i+5), paint);
            canvas.drawLine(points.get(i+4), points.get(i+5), points.get(i+6), points.get(i+7), paint);
            canvas.drawLine(points.get(i+6), points.get(i+7), points.get(i),   points.get(i+1), paint);
        }
    }

    public void setSize(int frameW, int frameH) {
        this.frameW = frameW;
        this.frameH = frameH;
        if (width  != 0) scaleX = 1.0f * width  / frameW;
        if (height != 0) scaleY = 1.0f * height / frameH;
    }

    @WorkerThread
    public void drawBox(List<String> codes, List<Float> pts) {
        points.clear();
        len = pts.size();
        for (int i = 0; i < len;) {
            this.points.add(pts.get(i++) * scaleX);
            this.points.add(pts.get(i++) * scaleY);
        }
        postInvalidate();
    }

}
