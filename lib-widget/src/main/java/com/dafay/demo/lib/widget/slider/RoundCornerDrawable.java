package com.dafay.demo.lib.widget.slider;

/**
 * @ClassName RoundCornerDrawable
 * @Des TODO
 * @Author lipengfei
 * @Date 2023/12/8 13:51
 */

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 自定义Drawable 实现圆角图片
 * Created by hust_twj on 2017/9/26.
 */

public class RoundCornerDrawable extends Drawable {
    private Paint mPaint;
    private RectF rectF;//定义矩形区域
    private Bitmap mBitmap;

    public RoundCornerDrawable(Bitmap bitmap) {
        this.mBitmap = bitmap;
        BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);//着色器 水平和竖直都需要填充满
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(bitmapShader);
        //rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        //RectF:圆角矩形区域
        canvas.drawRoundRect(new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), mBitmap.getWidth()/2, mBitmap.getWidth()/2, mPaint);//rx：x方向上的圆角半径; ry：y方向上的圆角半径。
    }

/*    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        rectF = new RectF(left, top, right, bottom);
    }*/

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT; //设置系统默认，让drawable支持和窗口一样的透明度
    }

    //还需要从重写以下2个方法，返回drawable实际宽高
    @Override
    public int getIntrinsicWidth() {
        return mBitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmap.getHeight();
    }

}
