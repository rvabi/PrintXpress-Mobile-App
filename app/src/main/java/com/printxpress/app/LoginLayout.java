package com.printxpress.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/** Keeps the login composition aligned while a short device can scroll it. */
public final class LoginLayout extends FrameLayout {
    public LoginLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.max(Math.round(width * 1.96f), MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width == 0 || height == 0) return;
        place(R.id.login_top_leaf, width, height, .53f, -.025f, .56f, .30f);
        place(R.id.login_wordmark, width, height, .085f, .055f, .53f, .07f);
        place(R.id.login_tagline, width, height, .088f, .112f, .49f, .027f);
        place(R.id.login_heading, width, height, .10f, .325f, .82f, .057f);
        place(R.id.login_subtitle, width, height, .105f, .382f, .84f, .045f);
        place(R.id.login_email, width, height, .105f, .438f, .79f, .061f);
        place(R.id.login_password, width, height, .105f, .513f, .79f, .061f);
        place(R.id.login_eye, width, height, .805f, .518f, .075f, .05f);
        place(R.id.login_forgot, width, height, .55f, .582f, .345f, .037f);
        place(R.id.login_sign_in, width, height, .105f, .629f, .79f, .068f);
        place(R.id.login_social_divider, width, height, .13f, .725f, .74f, .04f);
        place(R.id.login_socials, width, height, .23f, .768f, .54f, .077f);
        place(R.id.login_sign_up, width, height, .14f, .877f, .72f, .04f);
        place(R.id.login_bottom_leaf, width, height, -.13f, .785f, .36f, .23f);
    }

    private void place(int id, int width, int height, float x, float y, float w, float h) {
        View view = findViewById(id);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Math.round(width * w), Math.round(height * h));
        params.leftMargin = Math.round(width * x);
        params.topMargin = Math.round(height * y);
        view.setLayoutParams(params);
    }
}
