package com.printxpress.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/** Mint header and reference-shaped white transition, drawn at the current screen size. */
public final class LoginBackdropView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LoginBackdropView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        canvas.drawColor(0xffa8ddd7);
        Path white = new Path();
        white.moveTo(0, .185f * h);
        white.cubicTo(.17f * w, .12f * h, .28f * w, .145f * h, .43f * w, .225f * h);
        white.cubicTo(.53f * w, .285f * h, .56f * w, .30f * h, .65f * w, .29f * h);
        white.cubicTo(.79f * w, .28f * h, .92f * w, .29f * h, w, .30f * h);
        white.lineTo(w, h); white.lineTo(0, h); white.close();
        paint.setColor(0xfffffdfc); canvas.drawPath(white, paint);

        Path mintCorner = new Path();
        mintCorner.moveTo(.70f * w, h);
        mintCorner.cubicTo(.81f * w, .94f * h, .91f * w, .99f * h, w, .88f * h);
        mintCorner.lineTo(w, h); mintCorner.close();
        paint.setColor(0xffdcf3ef); canvas.drawPath(mintCorner, paint);
        Path peachCorner = new Path();
        peachCorner.moveTo(0, .91f * h);
        peachCorner.cubicTo(.18f * w, .89f * h, .28f * w, .95f * h, .40f * w, h);
        peachCorner.lineTo(0, h); peachCorner.close();
        paint.setColor(0xfff8d8d3); canvas.drawPath(peachCorner, paint);
    }
}
