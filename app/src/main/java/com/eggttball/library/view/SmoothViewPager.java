package com.eggttball.library.view;

import java.lang.reflect.Field;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

/**
 * <pre>
 * 可平滑翻頁的 ViewPager
 * 若需要更進一步客製化，可參考 http://stackoverflow.com/questions/10812009/change-viewpager-animation-duration-when-sliding-programmatically
 * </pre>
 * Created by eggttball on 2015/1/11.
 */
public class SmoothViewPager extends ViewPager {

    public SmoothViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }


    private void setMyScroller() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class MyScroller extends Scroller {

        public MyScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, 500);
        }
    }

}
