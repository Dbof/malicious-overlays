package com.davidebove.maliciousoverlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

/**
 * @author Davide Bove <davide.bove@fau.de>
 */

public class FillView extends View {
    public final static int SEE_THROUGH = Color.argb(180, 0, 0, 0);
    final DisplayMetrics metrics = new DisplayMetrics();
    Paint mLoadPaint;

    public FillView(Context context) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        mLoadPaint = new Paint();
        this.setColor(SEE_THROUGH);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mLoadPaint.setAntiAlias(true);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // draw rect
        canvas.drawRect(0, 0, metrics.widthPixels, metrics.heightPixels, mLoadPaint);
    }

    public void setColor(int color) {
        mLoadPaint.setColor(color);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }
}
