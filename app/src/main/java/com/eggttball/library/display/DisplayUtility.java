package com.eggttball.library.display;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import java.lang.reflect.Method;

/**
 * 集合與螢幕顯示相關的常用方法，例如取得螢幕尺寸或解析度
 * Created by eggttball on 2015/1/10.
 */
public class DisplayUtility {

    private Context _context;
    private float _screenDensity = 0.0f;
    private int _screenWidth;
    private int _screenHeight;

    public DisplayUtility(Context context)  {
        _context = context;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        try {
            // 先嘗試 SDK 13 以上版本的方法
            Method getSize = Display.class.getMethod("getSize", new Class[]{ Point.class });
            Point size = new Point();
            getSize.invoke(display, size);

            _screenWidth = size.x;
            _screenHeight = size.y;
        } catch (Exception ex) {
            // 新方法不適用，改用舊方法。但 SDK 13 以上的版本不建議使用
            _screenWidth = display.getWidth();
            _screenHeight = display.getHeight();
        }
    }


    /**
     * 取得螢幕 density，通常為 0.75、  1、  1.5  、2.0
     */
    public float getScreenDensity()	{
        if (_screenDensity == 0.0f)
            _screenDensity = _context.getResources().getDisplayMetrics().density;

        return _screenDensity;
    }


    /**
     * 取得螢幕寬度（px）
     */
    public int getScreenWidth()	{
        return _screenWidth;
    }


    /**
     * 取得螢幕高度（px）
     */
    public int getScreenHeight()	{
        return _screenHeight;
    }


    /**
     * 判斷螢幕幾吋
     */
    public double getScreenInches()	{
        DisplayMetrics dm = _context.getResources().getDisplayMetrics();
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        double screenInches = Math.sqrt(x + y);

        return screenInches;
    }

}
