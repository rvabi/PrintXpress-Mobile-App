package com.printxpress.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Decorative social marks only; this app offers local email/password sign in. */
public final class LoginSocialIconView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LoginSocialIconView(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float r = Math.min(getWidth(), getHeight()) * .27f;
        String name = String.valueOf(getTag());
        if ("google".equals(name)) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(r * .32f);
            paint.setStrokeCap(Paint.Cap.BUTT);
            RectF oval = new RectF(cx-r, cy-r, cx+r, cy+r);
            paint.setColor(0xff4285f4); canvas.drawArc(oval, -42, 85, false, paint);
            paint.setColor(0xff34a853); canvas.drawArc(oval, 43, 90, false, paint);
            paint.setColor(0xfffbbc05); canvas.drawArc(oval, 133, 73, false, paint);
            paint.setColor(0xffea4335); canvas.drawArc(oval, 206, 112, false, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xff4285f4);
            canvas.drawRect(cx, cy-r*.15f, cx+r*1.12f, cy+r*.19f, paint);
        } else if ("facebook".equals(name)) {
            paint.setColor(0xff1877f2); canvas.drawCircle(cx, cy, r*1.18f, paint);
            paint.setColor(Color.WHITE); paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(r*2.1f); paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("f", cx+r*.08f, cy+r*.92f, paint);
        } else {
            paint.setColor(0xff0b101a);
            canvas.drawOval(cx-r*.85f, cy-r*.35f, cx+r*.15f, cy+r*.95f, paint);
            canvas.drawOval(cx-r*.05f, cy-r*.42f, cx+r*.89f, cy+r*.95f, paint);
            Path leaf = new Path();
            leaf.moveTo(cx, cy-r*.65f); leaf.quadTo(cx+r*.05f, cy-r*1.28f, cx+r*.55f, cy-r*1.2f);
            leaf.quadTo(cx+r*.42f, cy-r*.72f, cx, cy-r*.65f);
            canvas.drawPath(leaf, paint);
            paint.setColor(Color.WHITE); canvas.drawCircle(cx+r*.93f, cy-r*.12f, r*.28f, paint);
        }
    }
}
