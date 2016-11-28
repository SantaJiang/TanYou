package com.santa.tanyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jsd58 on 11/28/2016.
 */

public class MyView extends View {
    public MyView(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);   //整张画布绘制成黑色
        Paint paint = new Paint();
        //去锯齿
        paint.setARGB(0, 255, 255, 255);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Xfermode xFermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        paint.setXfermode(xFermode);
    }

    private static final Xfermode[] sModes = {
            new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
            //new PorterDuffXfermode(PorterDuff.Mode.SRC),
            //new PorterDuffXfermode(PorterDuff.Mode.DST),
            //new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER),
            //new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),
            //new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),
            //new PorterDuffXfermode(PorterDuff.Mode.DST_IN),
            //new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),
            //new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),
            //new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP),
            //new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP),
            //new PorterDuffXfermode(PorterDuff.Mode.XOR),
            //new PorterDuffXfermode(PorterDuff.Mode.DARKEN),
            //new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),
            //new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY),
            //new PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    };
}
