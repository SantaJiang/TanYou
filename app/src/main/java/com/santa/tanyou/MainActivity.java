package com.santa.tanyou;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.amap.api.location.AMapLocation;//定位信息类
import com.amap.api.location.AMapLocationClient;//定位服务类
import com.amap.api.location.AMapLocationClientOption;//定位参数设置（在定位服务时需要这些参数）
import com.amap.api.location.AMapLocationListener;//定位回调接口
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;


public class MainActivity extends Activity implements LocationSource, AMapLocationListener {
    private FrameLayout act_main;

    private int SCREEN_W;

    private int SCREEN_H;
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    private TextView mLocationErrText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Fog
        act_main = (FrameLayout) findViewById(R.id.activity_main);
        MyView myView = new MyView(this);
        act_main.addView(myView);
        //获取地图控件引用
        mapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        init();

        ToggleButton tb = (ToggleButton) findViewById(R.id.tb);
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //设置使用卫星地图
                    aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                } else {
                    //设置使用普通地图
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                }
            }
        });
    }

    //初始化AMap对象
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mLocationErrText = (TextView) findViewById(R.id.location_errInfo_text);
        mLocationErrText.setVisibility(View.GONE);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);//定位模式
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                mLocationErrText.setVisibility(View.GONE);
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                mLocationErrText.setVisibility(View.VISIBLE);
                mLocationErrText.setText(errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mapView.onSaveInstanceState(outState);
    }


    //Fog
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
            setBackGround();

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


        private void setBackGround() {
            setBackgroundResource(R.drawable.transparent);
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
