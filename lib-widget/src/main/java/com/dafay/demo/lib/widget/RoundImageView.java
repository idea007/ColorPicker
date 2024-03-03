package com.dafay.demo.lib.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by idea on 2018/4/3.
 */

public class RoundImageView extends AppCompatImageView {

    private static float radius = 0f;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        radius = ta.getDimensionPixelSize(R.styleable.RoundImageView_radius, 16);

        setOutlineProvider(CIRCULAR_OUTLINE);
        setClipToOutline(true);
    }


    public static final ViewOutlineProvider CIRCULAR_OUTLINE = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getWidth() - view.getPaddingRight(),
                    view.getHeight() - view.getPaddingBottom(), radius);
        }
    };
}

