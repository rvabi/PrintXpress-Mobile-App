package com.printxpress.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/** The mint upper corner and pastel lower curves from the registration reference. */
public final class RegisterBackdropView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RegisterBackdropView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        canvas.drawColor(0xfffffdfc);
        Path mintTop = new Path();
        mintTop.moveTo(0, 0); mintTop.lineTo(.61f*w, 0);
        mintTop.cubicTo(.50f*w, .09f*h, .43f*w, .135f*h, .29f*w, .125f*h);
        mintTop.cubicTo(.13f*w, .11f*h, .09f*w, .13f*h, 0, .17f*h);
        mintTop.close();
        paint.setColor(0xffc4f1e9); canvas.drawPath(mintTop, paint);

        Path rightMint = new Path();
        rightMint.moveTo(w, .20f*h);
        rightMint.cubicTo(.83f*w, .17f*h, .80f*w, .21f*h, .92f*w, .27f*h);
        rightMint.lineTo(w, .32f*h); rightMint.close();
        paint.setColor(0xffdcf7f2); canvas.drawPath(rightMint, paint);

        Path bottomMint = new Path();
        bottomMint.moveTo(0, .86f*h);
        bottomMint.cubicTo(.19f*w, .87f*h, .15f*w, .93f*h, .34f*w, .94f*h);
        bottomMint.cubicTo(.44f*w, .95f*h, .47f*w, .98f*h, .52f*w, h);
        bottomMint.lineTo(0, h); bottomMint.close();
        paint.setColor(0xffdef6f1); canvas.drawPath(bottomMint, paint);
        Path bottomPeach = new Path();
        bottomPeach.moveTo(0, .91f*h);
        bottomPeach.cubicTo(.11f*w, .93f*h, .16f*w, .98f*h, .26f*w, h);
        bottomPeach.lineTo(0, h); bottomPeach.close();
        paint.setColor(0xfffce6e2); canvas.drawPath(bottomPeach, paint);
    }
}
