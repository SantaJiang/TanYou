package com.santa.tanyou;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class Eraser_Use_drawPath extends Activity {

    private int SCREEN_W;

    private int SCREEN_H;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));

    }

    class MyView extends View {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mPaint;
        private Path  mPath;
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        public MyView(Context context) {
            super(context);
            setFocusable(true);
            setScreenWH();

            // 1.if cover is a image,you can open MENU_ITEM_COMMENT bellow
            //Bitmap bm = createBitmapFromSRC();
            // if you want to set cover image's alpha,you can open MENU_ITEM_COMMENT bellow
            //bm = setBitmapAlpha(bm, 100);
            // if you want to scale cover image,you can open MENU_ITEM_COMMENT bellow
            //bm = scaleBitmapFillScreen(bm);

            // 2.if cover is color
            Bitmap bm = createBitmapFromARGB(0xFF000000, SCREEN_W, SCREEN_H);
            bm = setBitmapAlpha(bm, 100);
            setCoverBitmap(bm);

        }

        private void setScreenWH() {
            // get screen info
            DisplayMetrics dm = new DisplayMetrics();
            dm = this.getResources().getDisplayMetrics();
            // get screen width
            int screenWidth = dm.widthPixels;
            // get screen height
            int screenHeight = dm.heightPixels;

            SCREEN_W = screenWidth;
            SCREEN_H = screenHeight;
        }


        /**
         *
         * @param colorARGB should like 0x8800ff00
         * @param width
         * @param height
         * @return
         */
        private Bitmap createBitmapFromARGB(int colorARGB, int width, int height) {
            int[] argb = new int[width * height];

            for (int i = 0; i < argb.length; i++) {

                argb[i] = colorARGB;

            }
            return Bitmap.createBitmap(argb, width, height, Config.ARGB_8888);
        }

        /**
         *
         * @param bm
         * @param alpha ,and alpha should be like ox00000000-oxff000000
         * @note set bitmap's alpha
         * @return
         */
       /* private Bitmap setBitmapAlpha(Bitmap bm, int alpha) {
            int[] argb = new int[bm.getWidth() * bm.getHeight()];
            bm.getPixels(argb, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm
                    .getHeight());


            for (int i = 0; i < argb.length; i++) {

                argb[i] = ((alpha) | (argb[i] & 0x00FFFFFF));
            }
            return Bitmap.createBitmap(argb, bm.getWidth(), bm.getHeight(),
                                       Config.ARGB_8888);
        }*/

        /**
         *
         * @param bm
         * @param alpha ,alpha should be between 0 and 255
         * @note set bitmap's alpha
         * @return
         */
        private Bitmap setBitmapAlpha(Bitmap bm, int alpha) {
            int[] argb = new int[bm.getWidth() * bm.getHeight()];
            bm.getPixels(argb, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm
                    .getHeight());

            for (int i = 0; i < argb.length; i++) {

                argb[i] = ((alpha << 24) | (argb[i] & 0x00FFFFFF));
            }
            return Bitmap.createBitmap(argb, bm.getWidth(), bm.getHeight(),
                    Config.ARGB_8888);
        }

        /**
         *
         * @param bm
         * @note if bitmap is smaller than screen, you can scale it fill the screen.
         * @return
         */
        private Bitmap scaleBitmapFillScreen(Bitmap bm) {
            return Bitmap.createScaledBitmap(bm, SCREEN_W, SCREEN_H, true);
        }

        /**
         *
         * @param bm
         * @note set cover bitmap , which  overlay on background.
         */
        private void setCoverBitmap(Bitmap bm) {
            // setting paint
            mPaint = new Paint();
            mPaint.setAlpha(0);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mPaint.setAntiAlias(true);

            mPaint.setDither(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(20);

            //set path
            mPath =  new Path();;

            // converting bitmap into mutable bitmap
            mBitmap = Bitmap.createBitmap(SCREEN_W, SCREEN_H, Config.ARGB_8888);
            mCanvas = new Canvas();
            mCanvas.setBitmap(mBitmap);
            // drawXY will result on that Bitmap
            // be sure parameter is bm, not mBitmap
            mCanvas.drawBitmap(bm, 0, 0, null);
        }



        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mCanvas.drawPath(mPath, mPaint);
            super.onDraw(canvas);
        }

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}