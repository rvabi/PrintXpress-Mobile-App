package com.printxpress.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Mint catalogue header with the curved white transition from the approved references. */
public final class CatalogHeaderView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CatalogHeaderView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        paint.setShader(new LinearGradient(0, 0, w, h, 0xffd0f5f0, 0xffa8ddd7, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        Path white = new Path();
        white.moveTo(0, h * .94f);
        white.cubicTo(w * .16f, h * .91f, w * .28f, h * .20f, w * .52f, h * .21f);
        white.cubicTo(w * .73f, h * .19f, w * .77f, h * .79f, w, h * .87f);
        white.lineTo(w, h); white.lineTo(0, h); white.close();
        paint.setColor(0xfffafffe);
        canvas.drawPath(white, paint);
    }
}
