package com.printxpress.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/** Positions decorative splash elements in proportion to the available Android window. */
public final class SplashLayout extends FrameLayout {
    public SplashLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width == 0 || height == 0) return;
        place(R.id.splash_wave, width, height, 0f, .318f, 1f, .28f);
        place(R.id.splash_top_leaf, width, height, .75f, -.025f, .30f, .22f);
        place(R.id.splash_top_pearl, width, height, .55f, .07f, .09f, .043f);
        place(R.id.splash_wordmark, width, height, .08f, .183f, .84f, .10f);
        place(R.id.splash_tagline, width, height, .09f, .273f, .82f, .035f);
        place(R.id.splash_accent, width, height, .44f, .335f, .12f, .006f);
        place(R.id.splash_left_leaf, width, height, -.07f, .395f, .34f, .29f);
        place(R.id.splash_right_leaf, width, height, .70f, .405f, .34f, .27f);
        place(R.id.splash_product, width, height, .09f, .435f, .82f, .39f);
        place(R.id.splash_box_face, width, height, 0f, 0f, 1f, 1f);
        place(R.id.splash_side_pearl, width, height, .91f, .36f, .065f, .031f);
        place(R.id.splash_bottom_pearl, width, height, -.035f, .75f, .17f, .082f);
        place(R.id.splash_indicators, width, height, .36f, .875f, .28f, .023f);
    }

    private void place(int id, int width, int height, float x, float y, float w, float h) {
        View view = findViewById(id);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Math.round(width * w), Math.round(height * h));
        params.leftMargin = Math.round(width * x);
        params.topMargin = Math.round(height * y);
        view.setLayoutParams(params);
    }
}
