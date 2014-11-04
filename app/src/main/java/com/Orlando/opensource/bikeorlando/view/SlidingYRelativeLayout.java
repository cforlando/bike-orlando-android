package com.Orlando.opensource.bikeorlando.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * {@link android.widget.RelativeLayout} implementation that allows for animating slide in on the Y axis.
 */
public class SlidingYRelativeLayout extends RelativeLayout {

    private float yFraction = 0;
    private ViewTreeObserver.OnPreDrawListener preDrawListener = null;

    public SlidingYRelativeLayout(Context context) {
        super(context);
    }

    public SlidingYRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingYRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public float getYFraction() {
        return this.yFraction;
    }

    public void setYFraction(float fraction) {

        this.yFraction = fraction;

        if (getHeight() == 0) {
            if (preDrawListener == null) {
                preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                        setYFraction(yFraction);
                        return true;
                    }
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }

        float translationY = getHeight() * fraction;
        setTranslationY(translationY);
    }

}
