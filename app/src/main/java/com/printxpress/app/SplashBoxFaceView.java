package com.printxpress.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** A printed face over the approved package mockup, following the reference perspective. */
public final class SplashBoxFaceView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SplashBoxFaceView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        Path front = new Path();
        front.moveTo(.305f * w, .715f * h);
        front.lineTo(.895f * w, .632f * h);
        front.lineTo(.895f * w, .715f * h);
        front.lineTo(.305f * w, .785f * h);
        front.close();
        paint.setShader(new LinearGradient(.46f * w, .67f * h, .54f * w, .785f * h,
            Color.rgb(255, 250, 249), Color.rgb(248, 222, 218), Shader.TileMode.CLAMP));
        canvas.drawPath(front, paint);
        paint.setShader(null);

        Path face = new Path();
        face.moveTo(.14f * w, .552f * h);
        face.lineTo(.685f * w, .49f * h);
        face.lineTo(.895f * w, .632f * h);
        face.lineTo(.305f * w, .715f * h);
        face.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(253, 252, 250));
        canvas.drawPath(face, paint);
        canvas.save();
        canvas.clipPath(face);

        paint.setColor(Color.argb(105, 248, 201, 195));
        canvas.drawCircle(.12f * w, .625f * h, .15f * w, paint);
        canvas.drawCircle(.80f * w, .735f * h, .15f * w, paint);
        paint.setColor(Color.argb(105, 168, 221, 215));
        canvas.drawCircle(.70f * w, .48f * h, .13f * w, paint);
        canvas.restore();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, .0015f * w));
        paint.setColor(Color.rgb(231, 237, 234));
        canvas.drawPath(face, paint);
        paint.setStyle(Paint.Style.FILL);

        canvas.save();
        canvas.rotate(-17f, .53f * w, .61f * h);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(.052f * w);
        paint.setColor(Color.rgb(31, 41, 44));
        canvas.drawText("GOOD", .52f * w, .575f * h, paint);
        canvas.drawText("IDEAS", .52f * w, .602f * h, paint);
        canvas.drawText("PRINT", .52f * w, .629f * h, paint);
        paint.setColor(Color.rgb(244, 127, 122));
        canvas.drawText("BETTER", .52f * w, .656f * h, paint);
        canvas.restore();
    }
}
