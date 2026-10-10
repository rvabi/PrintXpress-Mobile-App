package com.printxpress.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Curved mint heading used by the order screens. */
public final class OrderHeaderView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public OrderHeaderView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        paint.setShader(new LinearGradient(0, 0, w, h, 0xffc9f3ed, 0xffa8ddd7, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        paint.setColor(0xfffafffe);
        Path wave = new Path();
        wave.moveTo(0, h * .81f);
        wave.cubicTo(w * .15f, h * .55f, w * .31f, h * .67f, w * .52f, h * .79f);
        wave.cubicTo(w * .68f, h * .90f, w * .76f, h * .56f, w, h * .67f);
        wave.lineTo(w, h); wave.lineTo(0, h); wave.close();
        canvas.drawPath(wave, paint);
    }
}
