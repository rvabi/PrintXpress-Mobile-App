package com.printxpress.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/** Positions the reference registration composition and lets shorter devices scroll. */
public final class RegisterLayout extends FrameLayout {
    public RegisterLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.max(Math.round(width * 1.96f), MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width == 0 || height == 0) return;
        place(R.id.register_top_leaf, width, height, .59f, -.025f, .46f, .245f);
        place(R.id.register_back, width, height, .075f, .053f, .075f, .04f);
        place(R.id.register_wordmark, width, height, .105f, .153f, .58f, .065f);
        place(R.id.register_tagline, width, height, .108f, .208f, .54f, .025f);
        place(R.id.register_heading, width, height, .105f, .245f, .83f, .06f);
        place(R.id.register_subtitle, width, height, .11f, .302f, .83f, .04f);
        place(R.id.register_name, width, height, .105f, .359f, .79f, .063f);
        place(R.id.register_email, width, height, .105f, .435f, .79f, .063f);
        place(R.id.register_password, width, height, .105f, .511f, .79f, .063f);
        place(R.id.register_password_eye, width, height, .805f, .517f, .075f, .051f);
        place(R.id.register_confirm, width, height, .105f, .587f, .79f, .063f);
        place(R.id.register_confirm_eye, width, height, .805f, .593f, .075f, .051f);
        place(R.id.register_create, width, height, .105f, .676f, .79f, .067f);
        place(R.id.register_social_divider, width, height, .15f, .77f, .70f, .04f);
        place(R.id.register_socials, width, height, .235f, .815f, .53f, .073f);
        place(R.id.register_sign_in, width, height, .15f, .89f, .70f, .04f);
    }

    private void place(int id, int width, int height, float x, float y, float w, float h) {
        View view = findViewById(id);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Math.round(width * w), Math.round(height * h));
        params.leftMargin = Math.round(width * x);
        params.topMargin = Math.round(height * y);
        view.setLayoutParams(params);
    }
}
