package com.printxpress.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.printxpress.app.database.DatabaseHelper;
import com.printxpress.app.pricing.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public final class MainActivity extends AppCompatActivity {
    private static final int PICK_ARTWORK = 44;
    private DatabaseHelper db;
    private LinearLayout content, navigation;
    private ScrollView scrollContent;
    private TextView title;
    private View splashOverlay;
    private View loginOverlay;
    private View registerOverlay;
    private View homeOverlay;
    private View categoriesOverlay;
    private View listingOverlay;
    private View detailOverlay;
    private View artworkOverlay;
    private View orderSummaryOverlay;
    private View checkoutOverlay;
    private View confirmationOverlay;
    private View ordersOverlay;
    private View notificationsOverlay;
    private View profileOverlay;
    private View supportOverlay;
    private long userId, categoryId, productId, orderId;
    private String artworkUri, artworkName, customText = "";
    private String pendingDesignUri, pendingDesignName;
    private int quantity = 1;
    private boolean homeDeliverySelected;
    private final List<Long> optionIds = new ArrayList<>();
    private BigDecimal unitPrice = Money.amount("0");
    private Runnable backAction;

    private static final class Option {
        long id;
        String value;
        BigDecimal adjustment;
        Option(long id, String value, String price) { this.id = id; this.value = value; this.adjustment = Money.amount(price); }
        @Override public String toString() { return value + (adjustment.signum() == 0 ? "" : " (" + Money.lkr(adjustment) + ")"); }
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);
        content = findViewById(R.id.content);
        scrollContent = findViewById(R.id.scroll_content);
        navigation = findViewById(R.id.navigation);
        title = findViewById(R.id.title);
        splashOverlay = findViewById(R.id.splash_overlay);
        loginOverlay = findViewById(R.id.login_overlay);
        registerOverlay = findViewById(R.id.register_overlay);
        homeOverlay = findViewById(R.id.home_overlay);
        categoriesOverlay = findViewById(R.id.categories_overlay);
        listingOverlay = findViewById(R.id.listing_overlay);
        detailOverlay = findViewById(R.id.detail_overlay);
        artworkOverlay = findViewById(R.id.artwork_overlay);
        orderSummaryOverlay = findViewById(R.id.order_summary_overlay);
        checkoutOverlay = findViewById(R.id.checkout_overlay);
        confirmationOverlay = findViewById(R.id.confirmation_overlay);
        ordersOverlay = findViewById(R.id.orders_overlay);
        notificationsOverlay = findViewById(R.id.notifications_overlay);
        profileOverlay = findViewById(R.id.profile_overlay);
        supportOverlay = findViewById(R.id.support_overlay);
        userId = getPreferences(MODE_PRIVATE).getLong("loggedInUserId", -1);
        showSplash();
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private void screen(String heading, Runnable back, boolean tabs) {
        backAction = back;
        loginOverlay.setVisibility(View.GONE);
        registerOverlay.setVisibility(View.GONE);
        homeOverlay.setVisibility(View.GONE);
        categoriesOverlay.setVisibility(View.GONE);
        listingOverlay.setVisibility(View.GONE);
        detailOverlay.setVisibility(View.GONE);
        artworkOverlay.setVisibility(View.GONE);
        orderSummaryOverlay.setVisibility(View.GONE);
        checkoutOverlay.setVisibility(View.GONE);
        confirmationOverlay.setVisibility(View.GONE);
        ordersOverlay.setVisibility(View.GONE);
        notificationsOverlay.setVisibility(View.GONE);
        profileOverlay.setVisibility(View.GONE);
        supportOverlay.setVisibility(View.GONE);
        getWindow().setStatusBarColor(getColor(R.color.background));
        title.setText(heading);
        content.removeAllViews(); navigation.removeAllViews();
        scrollContent.post(() -> scrollContent.scrollTo(0, 0));
        navigation.setVisibility(tabs ? View.VISIBLE : View.GONE);
        if (back != null) link(content, "← Back", back);
        if (tabs) {
            tab("Home", this::showHome); tab("Orders", this::showOrders);
            tab("Notices", this::showNotifications); tab("Profile", this::showProfile);
        }
    }
    private void tab(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label); b.setAllCaps(false); b.setTextSize(12);
        b.setTextColor(getColor(R.color.primary_text)); b.setBackgroundColor(getColor(R.color.mint));
        navigation.addView(b, new LinearLayout.LayoutParams(0, dp(56), 1));
        b.setOnClickListener(v -> action.run());
    }
    private LinearLayout card(LinearLayout parent) {
        LinearLayout c = (LinearLayout) getLayoutInflater().inflate(R.layout.view_card, parent, false);
        parent.addView(c); return c;
    }
    private TextView text(LinearLayout parent, String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(getColor(R.color.primary_text));
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(0, dp(4), 0, dp(8)); parent.addView(t); return t;
    }
    private Button button(LinearLayout parent, String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label); b.setAllCaps(false); b.setTextSize(16);
        b.setTextColor(getColor(R.color.primary_text)); b.setBackgroundResource(R.drawable.button_background);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        p.bottomMargin = dp(12); parent.addView(b, p);
        b.setOnClickListener(v -> action.run()); return b;
    }
    private void link(LinearLayout parent, String label, Runnable action) {
        TextView t = text(parent, label, 15, true);
        t.setMinHeight(dp(48));
        t.setTextColor(getColor(R.color.coral)); t.setPadding(0, dp(8), 0, dp(16));
        t.setOnClickListener(v -> action.run());
    }
    private EditText field(LinearLayout parent, String label, int inputType) {
        text(parent, label, 14, true);
        EditText e = new EditText(this);
        e.setSingleLine((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        e.setInputType(inputType); e.setTextSize(16); e.setHint(label);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(14); parent.addView(e, p);
        if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
            TextView toggle = text(parent, "Show password", 14, true);
            toggle.setTextColor(getColor(R.color.coral)); toggle.setMinHeight(dp(48));
            toggle.setOnClickListener(v -> {
                boolean showing = toggle.getText().toString().startsWith("Hide");
                e.setInputType(InputType.TYPE_CLASS_TEXT | (showing ? InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
                e.setSelection(e.length()); toggle.setText(showing ? "Show password" : "Hide password");
            });
        }
        return e;
    }
    private String value(EditText e) { return e.getText().toString().trim(); }
    private void message(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private boolean required(EditText e, String label) {
        if (!value(e).isEmpty()) return true;
        e.setError(label + " is required"); e.requestFocus(); return false;
    }
    @Override public void onBackPressed() { if (backAction != null) backAction.run(); else super.onBackPressed(); }

    private void showSplash() {
        backAction = null;
        splashOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        getWindow().setNavigationBarColor(getColor(R.color.mint));
        TextView mark = splashOverlay.findViewById(R.id.splash_wordmark);
        SpannableString wordmark = new SpannableString("PrintXpress");
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.surface)), 0, 5, 0);
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.soft_coral)), 5, wordmark.length(), 0);
        mark.setText(wordmark);
        mark.setTextScaleX(1.035f);
        splashOverlay.post(() -> mark.setTextSize(TypedValue.COMPLEX_UNIT_PX, splashOverlay.getWidth() * .128f));
        TextView tagline = splashOverlay.findViewById(R.id.splash_tagline);
        splashOverlay.post(() -> tagline.setTextSize(TypedValue.COMPLEX_UNIT_PX, splashOverlay.getWidth() * .028f));
        showSplashLeaves();
        showProduct();
        new Handler(getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            splashOverlay.setVisibility(View.GONE);
            getWindow().setStatusBarColor(getColor(R.color.background));
            getWindow().setNavigationBarColor(getColor(R.color.background));
            if (userId > 0 && db.userExists(userId)) showHome(); else showLogin();
        }, 3200);
    }
    private void showSplashLeaves() {
        Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.splash_leaf_detail);
        ((ImageView) splashOverlay.findViewById(R.id.splash_top_leaf))
            .setImageBitmap(feather(Bitmap.createBitmap(source, 400, 0, 420, 650), 90, 0, 0, 80));
        ((ImageView) splashOverlay.findViewById(R.id.splash_left_leaf))
            .setImageBitmap(feather(Bitmap.createBitmap(source, 25, 160, 680, 890), 0, 140, 80, 80));
        ((ImageView) splashOverlay.findViewById(R.id.splash_right_leaf))
            .setImageBitmap(feather(Bitmap.createBitmap(source, 650, 0, 604, 770), 135, 0, 60, 100));
    }
    private void showProduct() {
        Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.splash_product_box);
        ((ImageView) splashOverlay.findViewById(R.id.splash_product)).setImageBitmap(source);
    }
    private Bitmap feather(Bitmap source, int left, int right, int top, int bottom) {
        int width = source.getWidth(), height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = 255;
                if (left > 0) alpha = Math.min(alpha, x * 255 / left);
                if (right > 0) alpha = Math.min(alpha, (width - 1 - x) * 255 / right);
                if (top > 0) alpha = Math.min(alpha, y * 255 / top);
                if (bottom > 0) alpha = Math.min(alpha, (height - 1 - y) * 255 / bottom);
                int color = pixels[y * width + x];
                pixels[y * width + x] = Color.argb(Color.alpha(color) * alpha / 255,
                    Color.red(color), Color.green(color), Color.blue(color));
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }
    private void showLogin() {
        screen("Welcome back", null, false);
        loginOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        EditText email = loginOverlay.findViewById(R.id.login_email);
        EditText password = loginOverlay.findViewById(R.id.login_password);
        email.setText(""); password.setText("");
        TextView mark = loginOverlay.findViewById(R.id.login_wordmark);
        SpannableString wordmark = new SpannableString("PrintXpress");
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.surface)), 0, 5, 0);
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.soft_coral)), 5, wordmark.length(), 0);
        mark.setText(wordmark);
        loginOverlay.post(() -> {
            mark.setTextSize(TypedValue.COMPLEX_UNIT_PX, loginOverlay.getWidth() * .075f);
            ((TextView) loginOverlay.findViewById(R.id.login_tagline))
                .setTextSize(TypedValue.COMPLEX_UNIT_PX, loginOverlay.getWidth() * .019f);
        });
        ImageView eye = loginOverlay.findViewById(R.id.login_eye);
        eye.setContentDescription("Show password");
        eye.setOnClickListener(v -> {
            boolean masked = password.getTransformationMethod() instanceof PasswordTransformationMethod;
            password.setTransformationMethod(masked ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
            password.setSelection(password.length());
            eye.setContentDescription(masked ? "Hide password" : "Show password");
        });
        loginOverlay.findViewById(R.id.login_forgot).setOnClickListener(v ->
            message("Password recovery is not available in this local demo."));
        TextView signUp = loginOverlay.findViewById(R.id.login_sign_up);
        SpannableString signUpText = new SpannableString("Don't have an account? Sign Up");
        signUpText.setSpan(new ForegroundColorSpan(getColor(R.color.coral)), 23, signUpText.length(), 0);
        signUp.setText(signUpText);
        signUp.setOnClickListener(v -> showRegister());
        loginOverlay.findViewById(R.id.login_sign_in).setOnClickListener(v -> {
            if (!required(email, "Email") || !required(password, "Password")) return;
            if (!Patterns.EMAIL_ADDRESS.matcher(value(email)).matches()) { email.setError("Enter a valid email"); return; }
            char[] secret = password.getText().toString().toCharArray();
            long id;
            try { id = db.authenticate(value(email), secret); } finally { Arrays.fill(secret, '\0'); }
            if (id < 0) { password.setError("Incorrect email or password"); message("Incorrect email or password"); return; }
            userId = id; getPreferences(MODE_PRIVATE).edit().putLong("loggedInUserId", userId).apply(); showHome();
        });
    }
    private void showRegister() {
        screen("Create account", this::showLogin, false);
        registerOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        ((ScrollView) registerOverlay).scrollTo(0, 0);
        EditText name = registerOverlay.findViewById(R.id.register_name);
        EditText email = registerOverlay.findViewById(R.id.register_email);
        EditText password = registerOverlay.findViewById(R.id.register_password);
        EditText confirm = registerOverlay.findViewById(R.id.register_confirm);
        name.setText(""); email.setText(""); password.setText(""); confirm.setText("");
        TextView mark = registerOverlay.findViewById(R.id.register_wordmark);
        SpannableString wordmark = new SpannableString("PrintXpress");
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.dark_mint)), 0, 5, 0);
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.coral)), 5, wordmark.length(), 0);
        mark.setText(wordmark);
        registerOverlay.post(() -> {
            mark.setTextSize(TypedValue.COMPLEX_UNIT_PX, registerOverlay.getWidth() * .08f);
            ((TextView) registerOverlay.findViewById(R.id.register_tagline))
                .setTextSize(TypedValue.COMPLEX_UNIT_PX, registerOverlay.getWidth() * .018f);
        });
        bindRegisterEye(password, registerOverlay.findViewById(R.id.register_password_eye), "password");
        bindRegisterEye(confirm, registerOverlay.findViewById(R.id.register_confirm_eye), "password confirmation");
        registerOverlay.findViewById(R.id.register_back).setOnClickListener(v -> showLogin());
        TextView signIn = registerOverlay.findViewById(R.id.register_sign_in);
        SpannableString signInText = new SpannableString("Already have an account? Sign In");
        signInText.setSpan(new ForegroundColorSpan(getColor(R.color.coral)),
            signInText.toString().lastIndexOf("Sign In"), signInText.length(), 0);
        signIn.setText(signInText);
        signIn.setOnClickListener(v -> showLogin());
        registerOverlay.findViewById(R.id.register_create).setOnClickListener(v -> {
            if (!required(name, "Full name") || !required(email, "Email") || !required(password, "Password") || !required(confirm, "Confirmation")) return;
            if (!Patterns.EMAIL_ADDRESS.matcher(value(email)).matches()) { email.setError("Enter a valid email"); return; }
            if (password.length() < 8 || !password.getText().toString().matches(".*[A-Za-z].*") || !password.getText().toString().matches(".*[0-9].*")) { password.setError("Use 8+ characters with a letter and number"); return; }
            if (!password.getText().toString().equals(confirm.getText().toString())) { confirm.setError("Passwords do not match"); return; }
            showRegistrationPhoneDialog(name, email, password);
        });
    }
    private void bindRegisterEye(EditText field, ImageView eye, String label) {
        field.setTransformationMethod(PasswordTransformationMethod.getInstance());
        eye.setContentDescription("Show " + label);
        eye.setOnClickListener(v -> {
            boolean masked = field.getTransformationMethod() instanceof PasswordTransformationMethod;
            field.setTransformationMethod(masked ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
            field.setSelection(field.length());
            eye.setContentDescription((masked ? "Hide " : "Show ") + label);
        });
    }
    private void showRegistrationPhoneDialog(EditText name, EditText email, EditText password) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setPadding(dp(20), dp(6), dp(20), 0);
        EditText phone = new EditText(this);
        phone.setHint("Phone number");
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        phone.setSingleLine(true);
        phone.setBackgroundResource(R.drawable.login_field);
        phone.setPadding(dp(18), 0, dp(18), 0);
        wrapper.addView(phone, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Phone number")
            .setMessage("Add the contact number for your PrintXpress profile.")
            .setView(wrapper)
            .setNegativeButton("Back", null)
            .setPositiveButton("Create account", null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!required(phone, "Phone")) return;
            if (!value(phone).matches("[0-9+ ]{9,15}")) { phone.setError("Enter a valid phone number"); return; }
            char[] secret = password.getText().toString().toCharArray();
            long id;
            try { id = db.register(value(name), value(email), value(phone), secret); } finally { Arrays.fill(secret, '\0'); }
            dialog.dismiss();
            if (id < 0) { email.setError("This email is already registered"); email.requestFocus(); return; }
            message("Account created. Please log in."); showLogin();
        }));
        dialog.show();
    }
    private void showHome() {
        screen("PrintXpress", null, false);
        homeOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        getWindow().setNavigationBarColor(getColor(R.color.background));
        SpannableString wordmark = new SpannableString("PrintXpress");
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.dark_mint)), 0, 5, 0);
        wordmark.setSpan(new ForegroundColorSpan(getColor(R.color.coral)), 5, wordmark.length(), 0);
        ((TextView) homeOverlay.findViewById(R.id.home_wordmark)).setText(wordmark);

        EditText search = homeOverlay.findViewById(R.id.home_search);
        search.setText("");
        search.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            if (value(search).isEmpty()) showCategories(); else showProductList(0, value(search));
            return true;
        });
        homeOverlay.findViewById(R.id.home_hero).setOnClickListener(view -> showCategories());
        homeOverlay.findViewById(R.id.home_offer).setOnClickListener(view -> showNotifications());
        homeOverlay.findViewById(R.id.home_offers_all).setOnClickListener(view -> showNotifications());
        homeOverlay.findViewById(R.id.home_products_all).setOnClickListener(view -> showCategories());
        homeOverlay.findViewById(R.id.home_notifications).setOnClickListener(view -> showNotifications());
        homeOverlay.findViewById(R.id.home_cart).setOnClickListener(view -> showOrders());
        homeOverlay.findViewById(R.id.home_nav_home).setOnClickListener(view -> showHome());
        homeOverlay.findViewById(R.id.home_nav_shop).setOnClickListener(view -> showCategories());
        homeOverlay.findViewById(R.id.home_nav_orders).setOnClickListener(view -> showOrders());
        homeOverlay.findViewById(R.id.home_nav_profile).setOnClickListener(view -> showProfile());

        boolean unread = false;
        try (Cursor notices = db.notifications(userId)) {
            while (notices.moveToNext()) if (notices.getInt(4) == 0) { unread = true; break; }
        }
        homeOverlay.findViewById(R.id.home_notice_dot).setVisibility(unread ? View.VISIBLE : View.GONE);

        Map<String, Long> categoryIds = new LinkedHashMap<>();
        try (Cursor categories = db.categories()) {
            while (categories.moveToNext()) categoryIds.put(categories.getString(1), categories.getLong(0));
        }
        LinearLayout grid = homeOverlay.findViewById(R.id.home_category_rows);
        grid.removeAllViews();
        String[] displayOrder = {"Business Cards", "Flyers", "Posters", "T-Shirts", "Mugs", "Stickers", "Banners"};
        LinearLayout row = null;
        for (int i = 0; i < displayOrder.length; i++) {
            if (i % 4 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i > 0) rowParams.topMargin = dp(10);
                grid.addView(row, rowParams);
            }
            String category = displayOrder[i];
            Long id = categoryIds.get(category);
            if (id != null) addHomeCategory(row, category, id, homeCategoryImage(category));
        }
        // Keep the second row aligned with the four-column reference grid.
        if (row != null) row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));

        LinearLayout products = homeOverlay.findViewById(R.id.home_products);
        products.removeAllViews();
        for (String category : new String[]{"T-Shirts", "Business Cards", "Posters"}) {
            Long id = categoryIds.get(category);
            if (id == null) continue;
            try (Cursor item = db.products(id, null)) {
                if (item.moveToFirst()) addHomeProduct(products, id, item.getLong(0), item.getString(1), item.getString(3), homeCategoryImage(category));
            }
        }
        ((ScrollView) homeOverlay.findViewById(R.id.home_scroll)).scrollTo(0, 0);
    }

    private int homeCategoryImage(String category) {
        switch (category) {
            case "Business Cards": return R.drawable.home_category_business_cards;
            case "Flyers": return R.drawable.home_category_flyers;
            case "Posters": return R.drawable.home_category_posters;
            case "Banners": return R.drawable.home_category_banners;
            case "T-Shirts": return R.drawable.home_category_tshirts;
            case "Mugs": return R.drawable.home_category_mugs;
            case "Stickers": return R.drawable.home_category_stickers;
            default: return R.drawable.home_category_business_cards;
        }
    }

    private void addHomeCategory(LinearLayout row, String name, long id, int imageRes) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tileParams.leftMargin = dp(3); tileParams.rightMargin = dp(3);
        row.addView(tile, tileParams);
        ImageView artwork = new ImageView(this);
        artwork.setImageResource(imageRes);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackgroundResource(R.drawable.home_image_round);
        artwork.setClipToOutline(true);
        artwork.setContentDescription(name);
        artwork.setElevation(dp(2));
        tile.addView(artwork, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(63)));
        TextView label = new TextView(this);
        label.setText(name);
        label.setTextColor(getColor(R.color.primary_text));
        label.setTextSize(11);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);
        label.setPadding(0, dp(4), 0, 0);
        tile.addView(label, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(23)));
        tile.setContentDescription("Browse " + name);
        tile.setOnClickListener(view -> showProductList(id, null));
    }

    private void addHomeProduct(LinearLayout row, long category, long id, String name, String price, int imageRes) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.home_round_white);
        card.setPadding(dp(5), dp(5), dp(5), dp(7));
        card.setElevation(dp(3));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(121), ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp(8); params.bottomMargin = dp(4);
        row.addView(card, params);
        ImageView artwork = new ImageView(this);
        artwork.setImageResource(imageRes);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackgroundResource(R.drawable.home_image_round);
        artwork.setClipToOutline(true);
        artwork.setContentDescription(null);
        card.addView(artwork, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(89)));
        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(getColor(R.color.primary_text));
        title.setTextSize(12);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setMaxLines(2);
        title.setMinHeight(dp(35));
        title.setPadding(dp(3), dp(5), dp(3), 0);
        card.addView(title);
        TextView amount = new TextView(this);
        amount.setText(Money.lkr(Money.amount(price)));
        amount.setTextColor(getColor(R.color.coral));
        amount.setTextSize(12);
        amount.setTypeface(null, android.graphics.Typeface.BOLD);
        amount.setPadding(dp(3), dp(3), dp(3), 0);
        card.addView(amount);
        card.setContentDescription(name + ", from " + amount.getText());
        card.setOnClickListener(view -> { categoryId = category; showProductDetails(id); });
    }
    private void showCategories() {
        screen("Categories", this::showHome, false);
        categoriesOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        categoriesOverlay.findViewById(R.id.categories_back).setOnClickListener(v -> showHome());
        categoriesOverlay.findViewById(R.id.categories_search).setOnClickListener(v -> promptProductSearch());
        Map<String, Long> ids = new LinkedHashMap<>();
        try (Cursor c = db.categories()) {
            while (c.moveToNext()) ids.put(c.getString(1), c.getLong(0));
        }
        LinearLayout grid = categoriesOverlay.findViewById(R.id.categories_grid);
        grid.removeAllViews();
        String[] order = {"Business Cards", "Flyers", "Posters", "Banners", "T-Shirts", "Mugs", "Stickers"};
        LinearLayout row = null;
        for (int i = 0; i < order.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rowParams.bottomMargin = dp(10);
                grid.addView(row, rowParams);
            }
            Long id = ids.get(order[i]);
            if (id != null) addCategoryCard(row, order[i], id);
        }
        if (row != null && row.getChildCount() == 1) row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        ((ScrollView) categoriesOverlay.findViewById(R.id.categories_scroll)).scrollTo(0, 0);
    }

    private void addCategoryCard(LinearLayout row, String name, long id) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.home_round_white);
        card.setPadding(dp(5), dp(5), dp(5), dp(4));
        card.setElevation(dp(3));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cardParams.leftMargin = dp(3); cardParams.rightMargin = dp(3); cardParams.bottomMargin = dp(3);
        row.addView(card, cardParams);
        ImageView image = new ImageView(this);
        image.setImageResource(homeCategoryImage(name));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(R.drawable.home_image_round);
        image.setClipToOutline(true);
        image.setContentDescription(null);
        card.addView(image, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(99)));
        TextView label = new TextView(this);
        label.setText(name);
        label.setTextColor(getColor(R.color.primary_text));
        label.setTextSize(16);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        card.addView(label, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(33)));
        card.setContentDescription("Browse " + name);
        card.setOnClickListener(v -> showProductList(id, null));
    }

    private void promptProductSearch() {
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Search for products...");
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this).setTitle("Search products").setView(search)
            .setPositiveButton("Search", (dialog, which) -> {
                if (value(search).isEmpty()) showCategories(); else showProductList(0, value(search));
            }).setNegativeButton("Cancel", null).show();
    }
    private void showProductList(long category, String search) {
        categoryId = category;
        screen(search == null ? "Products" : "Search results", search == null ? this::showCategories : this::showHome, false);
        listingOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        listingOverlay.findViewById(R.id.listing_back).setOnClickListener(v -> {
            if (search == null) showCategories(); else showHome();
        });
        listingOverlay.findViewById(R.id.listing_search).setOnClickListener(v -> promptProductSearch());
        Map<String, Long> ids = new LinkedHashMap<>();
        try (Cursor c = db.categories()) {
            while (c.moveToNext()) ids.put(c.getString(1), c.getLong(0));
        }
        String heading = search != null ? "Search results" : "Products";
        if (search == null && category > 0) {
            for (Map.Entry<String, Long> entry : ids.entrySet()) if (entry.getValue() == category) heading = entry.getKey();
        }
        ((TextView) listingOverlay.findViewById(R.id.listing_heading)).setText(heading);
        LinearLayout filters = listingOverlay.findViewById(R.id.listing_filters);
        filters.removeAllViews();
        if (search == null && category > 0) {
            for (Map.Entry<String, Long> entry : ids.entrySet()) {
                if (entry.getValue() == category) {
                    long selectedId = entry.getValue();
                    addListingFilter(filters, entry.getKey(), true, () -> showProductList(selectedId, null));
                    break;
                }
            }
        }
        addListingFilter(filters, "All", search == null && category == 0, () -> showProductList(0, null));
        for (Map.Entry<String, Long> entry : ids.entrySet()) {
            long id = entry.getValue();
            if (search == null && category == id) continue;
            addListingFilter(filters, entry.getKey(), search == null && category == id, () -> showProductList(id, null));
        }

        LinearLayout grid = listingOverlay.findViewById(R.id.listing_grid);
        grid.removeAllViews();
        LinearLayout row = null;
        int count = 0;
        for (Map.Entry<String, Long> entry : ids.entrySet()) {
            long catId = entry.getValue();
            if (category > 0 && catId != category) continue;
            try (Cursor c = db.products(catId, null)) {
                while (c.moveToNext()) {
                    String name = c.getString(1), description = c.getString(2);
                    if (search != null && !name.toLowerCase(java.util.Locale.ROOT).contains(search.toLowerCase(java.util.Locale.ROOT))
                        && !description.toLowerCase(java.util.Locale.ROOT).contains(search.toLowerCase(java.util.Locale.ROOT))) continue;
                    if (count % 2 == 0) {
                        row = new LinearLayout(this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        rowParams.bottomMargin = dp(12);
                        grid.addView(row, rowParams);
                    }
                    addListingProduct(row, catId, c.getLong(0), name, c.getString(3),
                        "T-Shirts".equals(entry.getKey()) ? R.drawable.catalog_tee_premium : homeCategoryImage(entry.getKey()));
                    count++;
                }
            }
        }
        if (row != null && row.getChildCount() == 1) row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        if (count == 0) text(grid, "No products found. Try another category or search.", 16, false);
        ((ScrollView) listingOverlay.findViewById(R.id.listing_scroll)).scrollTo(0, 0);
    }

    private void addListingFilter(LinearLayout parent, String label, boolean selected, Runnable action) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextColor(selected ? getColor(R.color.surface) : getColor(R.color.primary_text));
        chip.setTextSize(14);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(17), 0, dp(17), 0);
        chip.setBackgroundResource(selected ? R.drawable.catalog_chip_selected : R.drawable.catalog_chip);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        params.rightMargin = dp(8);
        parent.addView(chip, params);
        chip.setOnClickListener(v -> action.run());
    }

    private void addListingProduct(LinearLayout row, long category, long id, String name, String price, int imageRes) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.home_round_white);
        card.setPadding(dp(5), dp(5), dp(5), dp(10));
        card.setElevation(dp(2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.leftMargin = dp(3); params.rightMargin = dp(3); params.bottomMargin = dp(3);
        row.addView(card, params);
        ImageView artwork = new ImageView(this);
        artwork.setImageResource(imageRes);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackgroundResource(R.drawable.home_image_round);
        artwork.setClipToOutline(true);
        artwork.setContentDescription(null);
        card.addView(artwork, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(148)));
        TextView title = new TextView(this);
        title.setText(name); title.setTextColor(getColor(R.color.primary_text)); title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD); title.setMaxLines(2);
        title.setPadding(dp(8), dp(7), dp(8), 0);
        title.setMinHeight(dp(28));
        card.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView amount = new TextView(this);
        amount.setText(Money.lkr(Money.amount(price))); amount.setTextColor(getColor(R.color.coral));
        amount.setTextSize(15); amount.setTypeface(null, android.graphics.Typeface.BOLD);
        amount.setPadding(dp(8), 0, dp(8), 0);
        card.addView(amount);
        card.setContentDescription(name + ", from " + amount.getText());
        card.setOnClickListener(v -> { categoryId = category; showProductDetails(id); });
    }
    private void showProductDetails(long id) {
        if (productId != id) {
            clearDraft();
            if (pendingDesignUri != null) {
                artworkUri = pendingDesignUri; artworkName = pendingDesignName;
                pendingDesignUri = null; pendingDesignName = null;
            }
        }
        productId = id;
        screen("Product details", () -> showProductList(categoryId, null), false);
        detailOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        detailOverlay.findViewById(R.id.detail_back).setOnClickListener(v -> showProductList(categoryId, null));
        detailOverlay.findViewById(R.id.detail_customize).setOnClickListener(v -> showCustomize());
        BigDecimal base;
        try (Cursor c = db.product(id)) {
            if (!c.moveToFirst()) { detailOverlay.setVisibility(View.GONE); text(content, "Product unavailable", 18, true); return; }
            ((TextView) detailOverlay.findViewById(R.id.detail_name)).setText(c.getString(1));
            ((TextView) detailOverlay.findViewById(R.id.detail_description)).setText(c.getString(2));
            base = Money.amount(c.getString(3));
        }
        String categoryName = "";
        try (Cursor categories = db.categories()) {
            while (categories.moveToNext()) if (categories.getLong(0) == categoryId) categoryName = categories.getString(1);
        }
        int imageRes = homeCategoryImage(categoryName);
        ((ImageView) detailOverlay.findViewById(R.id.detail_hero)).setImageResource(imageRes);
        LinearLayout thumbs = detailOverlay.findViewById(R.id.detail_thumbnails);
        thumbs.removeAllViews();
        addDetailThumbnail(thumbs, imageRes, true);
        if ("T-Shirts".equals(categoryName)) addDetailThumbnail(thumbs, R.drawable.catalog_tee_premium, false);

        Map<String, List<Option>> groups = new LinkedHashMap<>();
        try (Cursor c = db.options(id)) {
            while (c.moveToNext()) groups.computeIfAbsent(c.getString(1), key -> new ArrayList<>())
                .add(new Option(c.getLong(0), c.getString(2), c.getString(3)));
        }
        LinearLayout options = detailOverlay.findViewById(R.id.detail_options);
        options.removeAllViews();
        TextView price = detailOverlay.findViewById(R.id.detail_price);
        Runnable updatePrice = () -> {
            BigDecimal amount = base;
            for (List<Option> choices : groups.values()) for (Option choice : choices)
                if (optionIds.contains(choice.id)) amount = amount.add(choice.adjustment);
            price.setText(Money.lkr(amount));
        };
        for (Map.Entry<String, List<Option>> group : groups.entrySet()) {
            TextView label = new TextView(this);
            label.setText(group.getKey()); label.setTextColor(getColor(R.color.primary_text));
            label.setTextSize(18); label.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(10); labelParams.bottomMargin = dp(7);
            options.addView(label, labelParams);
            HorizontalScrollView scroller = new HorizontalScrollView(this);
            scroller.setHorizontalScrollBarEnabled(false);
            LinearLayout choicesRow = new LinearLayout(this);
            choicesRow.setOrientation(LinearLayout.HORIZONTAL);
            scroller.addView(choicesRow);
            options.addView(scroller);
            boolean hasSelection = false;
            for (Option choice : group.getValue()) if (optionIds.contains(choice.id)) hasSelection = true;
            if (!hasSelection && !group.getValue().isEmpty()) optionIds.add(group.getValue().get(0).id);
            for (Option choice : group.getValue()) {
                TextView chip = new TextView(this);
                chip.setText(choice.value); chip.setTextSize(14); chip.setTypeface(null, android.graphics.Typeface.BOLD);
                chip.setGravity(Gravity.CENTER); chip.setPadding(dp(17), 0, dp(17), 0);
                boolean selected = optionIds.contains(choice.id);
                chip.setBackgroundResource(selected ? R.drawable.catalog_chip_selected : R.drawable.catalog_chip);
                chip.setTextColor(selected ? Color.WHITE : getColor(R.color.primary_text));
                LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(41));
                chipParams.rightMargin = dp(9); choicesRow.addView(chip, chipParams);
                chip.setOnClickListener(v -> {
                    for (Option other : group.getValue()) optionIds.remove(other.id);
                    optionIds.add(choice.id);
                    for (int i = 0; i < choicesRow.getChildCount(); i++) {
                        TextView current = (TextView) choicesRow.getChildAt(i);
                        boolean chosen = i == group.getValue().indexOf(choice);
                        current.setBackgroundResource(chosen ? R.drawable.catalog_chip_selected : R.drawable.catalog_chip);
                        current.setTextColor(chosen ? Color.WHITE : getColor(R.color.primary_text));
                    }
                    updatePrice.run();
                });
            }
        }
        updatePrice.run();
        ((ScrollView) detailOverlay.findViewById(R.id.detail_scroll)).scrollTo(0, 0);
    }

    private void addDetailThumbnail(LinearLayout row, int imageRes, boolean selected) {
        ImageView thumb = new ImageView(this);
        thumb.setImageResource(imageRes);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.WHITE); border.setCornerRadius(dp(15));
        border.setStroke(dp(2), selected ? getColor(R.color.coral) : getColor(R.color.border));
        thumb.setBackground(border); thumb.setClipToOutline(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(70), dp(65));
        params.rightMargin = dp(10); row.addView(thumb, params);
        thumb.setOnClickListener(v -> ((ImageView) detailOverlay.findViewById(R.id.detail_hero)).setImageResource(imageRes));
    }
    private void showCustomize() {
        screen("Customize print", () -> showProductDetails(productId), false);
        String productName = "Product";
        BigDecimal base = Money.amount("0");
        try (Cursor c = db.product(productId)) { if (c.moveToFirst()) { productName = c.getString(1); base = Money.amount(c.getString(3)); } }
        text(content, productName, 20, true);
        EditText qty = field(content, "Quantity", InputType.TYPE_CLASS_NUMBER); qty.setText(String.valueOf(quantity));
        EditText custom = field(content, "Custom text or print instructions", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); custom.setText(customText);
        Map<String,List<Option>> groups = new LinkedHashMap<>();
        try (Cursor c = db.options(productId)) {
            while (c.moveToNext()) groups.computeIfAbsent(c.getString(1), k -> new ArrayList<>()).add(new Option(c.getLong(0), c.getString(2), c.getString(3)));
        }
        List<Spinner> selectors = new ArrayList<>(); List<List<Option>> choices = new ArrayList<>();
        for (Map.Entry<String,List<Option>> group : groups.entrySet()) {
            text(content, group.getKey(), 16, true);
            Spinner spinner = new Spinner(this);
            spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, group.getValue()));
            content.addView(spinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
            for (int i = 0; i < group.getValue().size(); i++) if (optionIds.contains(group.getValue().get(i).id)) spinner.setSelection(i);
            selectors.add(spinner); choices.add(group.getValue());
        }
        final BigDecimal basePrice = base;
        TextView total = text(content, "", 20, true);
        Runnable calculate = () -> {
            BigDecimal unit = basePrice;
            for (int i = 0; i < selectors.size(); i++) unit = unit.add(choices.get(i).get(selectors.get(i).getSelectedItemPosition()).adjustment);
            int q; try { q = Integer.parseInt(value(qty)); } catch (NumberFormatException ex) { q = 0; }
            total.setText("Estimated total: " + Money.lkr(unit.multiply(BigDecimal.valueOf(q))));
        };
        qty.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { calculate.run(); }
            public void afterTextChanged(Editable s) { }
        });
        for (Spinner spinner : selectors) spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { calculate.run(); }
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        calculate.run();
        text(content, artworkName == null ? "No artwork selected" : "Artwork: " + artworkName, 15, false);
        button(content, "Select or change artwork", () -> {
            customText = value(custom);
            try { quantity = Integer.parseInt(value(qty)); } catch (NumberFormatException ex) { quantity = 1; }
            optionIds.clear(); unitPrice = basePrice;
            for (int i = 0; i < selectors.size(); i++) {
                Option option = choices.get(i).get(selectors.get(i).getSelectedItemPosition());
                optionIds.add(option.id); unitPrice = unitPrice.add(option.adjustment);
            }
            showArtwork();
        });
        button(content, "Review order", () -> {
            int q; try { q = Integer.parseInt(value(qty)); } catch (NumberFormatException ex) { q = 0; }
            if (q < 1) { qty.setError("Quantity must be at least 1"); return; }
            if (value(custom).isEmpty() && artworkUri == null) { custom.setError("Add custom text or select artwork"); return; }
            quantity = q; customText = value(custom); optionIds.clear(); unitPrice = basePrice;
            for (int i = 0; i < selectors.size(); i++) {
                Option option = choices.get(i).get(selectors.get(i).getSelectedItemPosition());
                optionIds.add(option.id); unitPrice = unitPrice.add(option.adjustment);
            }
            showSummary();
        });
    }
    private void showArtwork() {
        screen("Artwork selection", this::showCustomize, false);
        artworkOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        SpannableString heading = new SpannableString("Upload Your Design");
        heading.setSpan(new ForegroundColorSpan(getColor(R.color.coral)), 12, heading.length(), 0);
        ((TextView) artworkOverlay.findViewById(R.id.artwork_heading)).setText(heading);
        Bitmap uploadArt = BitmapFactory.decodeResource(getResources(), R.drawable.upload_artwork_illustration);
        ((ImageView) artworkOverlay.findViewById(R.id.artwork_cloud))
            .setImageBitmap(Bitmap.createBitmap(uploadArt, 101, 18, 69, 54));
        TextView uploadLabel = artworkOverlay.findViewById(R.id.artwork_upload_label);
        uploadLabel.setText(artworkName == null ? "Tap to upload" : artworkName);
        uploadLabel.setSingleLine(true);
        uploadLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
        uploadLabel.setPadding(dp(14), 0, dp(14), 0);
        artworkOverlay.findViewById(R.id.artwork_back).setOnClickListener(v -> showCustomize());
        artworkOverlay.findViewById(R.id.artwork_upload_area).setOnClickListener(v -> openArtworkPicker(false));
        artworkOverlay.findViewById(R.id.artwork_gallery).setOnClickListener(v -> openArtworkPicker(true));
        artworkOverlay.findViewById(R.id.artwork_files).setOnClickListener(v -> openArtworkPicker(false));
        artworkOverlay.findViewById(R.id.artwork_camera).setOnClickListener(v ->
            message("Camera capture is unavailable. Choose a file from this device."));
        artworkOverlay.findViewById(R.id.artwork_continue).setOnClickListener(v -> showCustomize());
        View remove = artworkOverlay.findViewById(R.id.artwork_remove);
        View save = artworkOverlay.findViewById(R.id.artwork_save);
        remove.setVisibility(artworkUri == null ? View.GONE : View.VISIBLE);
        save.setVisibility(artworkUri == null ? View.GONE : View.VISIBLE);
        if (artworkUri != null) {
            remove.setOnClickListener(v -> { artworkUri = null; artworkName = null; showArtwork(); });
            save.setOnClickListener(v -> {
                EditText name = new EditText(this); name.setHint("Design name");
                new AlertDialog.Builder(this).setTitle("Save design").setView(name)
                    .setPositiveButton("Save", (dialog, which) -> {
                        if (value(name).isEmpty()) { message("Enter a design name"); return; }
                        db.saveDesign(userId, value(name), artworkUri); message("Design saved");
                    }).setNegativeButton("Cancel", null).show();
            });
        }
        ((ScrollView) artworkOverlay.findViewById(R.id.artwork_scroll)).scrollTo(0, 0);
    }
    private void openArtworkPicker(boolean imagesOnly) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, imagesOnly
            ? new String[]{"image/png", "image/jpeg"}
            : new String[]{"image/png", "image/jpeg", "application/pdf"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_ARTWORK);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_ARTWORK || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
        catch (SecurityException ex) { message("File access could not be retained"); return; }
        artworkUri = uri.toString(); artworkName = uri.getLastPathSegment();
        try (Cursor c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) artworkName = c.getString(0);
        }
        showArtwork();
    }
    private void showSummary() {
        screen("Order summary", this::showCustomize, false);
        orderSummaryOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        orderSummaryOverlay.findViewById(R.id.summary_back).setOnClickListener(v -> showCustomize());
        orderSummaryOverlay.findViewById(R.id.summary_continue).setOnClickListener(v -> showDelivery());
        LinearLayout items = orderSummaryOverlay.findViewById(R.id.summary_items);
        items.removeAllViews();
        String name = "Product";
        try (Cursor c = db.product(productId)) { if (c.moveToFirst()) name = c.getString(1); }
        LinearLayout card = orderCard(items);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row);
        ImageView image = new ImageView(this);
        image.setImageResource(orderProductImage(name)); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(R.drawable.home_image_round); image.setClipToOutline(true);
        image.setContentDescription(name);
        row.addView(image, new LinearLayout.LayoutParams(dp(112), dp(118)));
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL); details.setPadding(dp(14), 0, 0, 0);
        row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        orderText(details, name, 19, true, 0xff0a1e2c);
        try (Cursor c = db.options(productId)) {
            while (c.moveToNext()) if (optionIds.contains(c.getLong(0)))
                orderText(details, c.getString(1) + ": " + c.getString(2), 13, false, 0xff6b7280);
        }
        if (!customText.isEmpty()) orderText(details, "Text: " + customText, 13, false, 0xff6b7280);
        if (artworkName != null) orderText(details, "Artwork: " + artworkName, 13, false, 0xff6b7280);
        orderText(details, Money.lkr(unitPrice) + " each", 16, true, 0xff0a1e2c);
        LinearLayout qtyRow = new LinearLayout(this);
        qtyRow.setGravity(Gravity.CENTER_VERTICAL); qtyRow.setPadding(0, dp(14), 0, 0);
        card.addView(qtyRow);
        orderText(qtyRow, "Quantity", 15, true, 0xff0a1e2c);
        View spacer = new View(this); qtyRow.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
        TextView minus = orderText(qtyRow, "−", 24, true, 0xff0a1e2c);
        minus.setGravity(Gravity.CENTER); minus.setBackgroundResource(R.drawable.order_outline);
        minus.setLayoutParams(new LinearLayout.LayoutParams(dp(43), dp(42)));
        TextView count = orderText(qtyRow, String.valueOf(quantity), 18, true, 0xff0a1e2c);
        count.setGravity(Gravity.CENTER); count.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(42)));
        TextView plus = orderText(qtyRow, "+", 24, true, 0xff0a1e2c);
        plus.setGravity(Gravity.CENTER); plus.setBackgroundResource(R.drawable.order_outline);
        plus.setLayoutParams(new LinearLayout.LayoutParams(dp(43), dp(42)));
        Runnable update = () -> {
            count.setText(String.valueOf(quantity));
            String total = Money.lkr(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            ((TextView) orderSummaryOverlay.findViewById(R.id.summary_subtotal)).setText(total);
            ((TextView) orderSummaryOverlay.findViewById(R.id.summary_total)).setText(total);
        };
        minus.setContentDescription("Decrease quantity"); plus.setContentDescription("Increase quantity");
        minus.setOnClickListener(v -> { if (quantity > 1) { quantity--; update.run(); } });
        plus.setOnClickListener(v -> { quantity++; update.run(); });
        update.run();
        ((ScrollView) orderSummaryOverlay.findViewById(R.id.summary_scroll)).scrollTo(0, 0);
    }
    private void showDelivery() {
        screen("Pickup or delivery", this::showSummary, false);
        checkoutOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        checkoutOverlay.findViewById(R.id.checkout_back).setOnClickListener(v -> showSummary());
        TextView pickup = checkoutOverlay.findViewById(R.id.checkout_pickup);
        TextView home = checkoutOverlay.findViewById(R.id.checkout_home);
        View addressCard = checkoutOverlay.findViewById(R.id.checkout_address_card);
        Spinner addressSpinner = checkoutOverlay.findViewById(R.id.checkout_address);
        List<String> addressTexts = new ArrayList<>();
        try (Cursor c = db.addresses(userId)) {
            while (c.moveToNext()) addressTexts.add(c.getString(1) + ", " + c.getString(2) + ", " + c.getString(3) + (c.getString(4) == null ? "" : " " + c.getString(4)));
        }
        addressSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, addressTexts));
        checkoutOverlay.findViewById(R.id.checkout_add_address).setOnClickListener(v ->
            showAddressForm(0, null, null, null, null, this::showDelivery));
        Runnable toggle = () -> {
            home.setBackgroundResource(homeDeliverySelected ? R.drawable.order_selected : R.drawable.order_outline);
            pickup.setBackgroundResource(homeDeliverySelected ? R.drawable.order_outline : R.drawable.order_selected);
            home.setText((homeDeliverySelected ? "●" : "○") + "    Home Delivery\n       Deliver to a saved address");
            pickup.setText((homeDeliverySelected ? "○" : "●") + "    Store Pickup\n       Collect at the print shop");
            addressCard.setVisibility(homeDeliverySelected ? View.VISIBLE : View.GONE);
        };
        pickup.setOnClickListener(v -> { homeDeliverySelected = false; toggle.run(); });
        home.setOnClickListener(v -> { homeDeliverySelected = true; toggle.run(); });
        toggle.run();
        Calendar schedule = Calendar.getInstance(TimeZone.getTimeZone("Asia/Colombo"));
        schedule.add(Calendar.DAY_OF_MONTH, 1); schedule.set(Calendar.SECOND, 0);
        TextView scheduleText = checkoutOverlay.findViewById(R.id.checkout_schedule);
        scheduleText.setText("Choose a date and time  ›");
        scheduleText.setOnClickListener(v -> chooseSchedule(schedule, scheduleText));
        checkoutOverlay.findViewById(R.id.checkout_continue).setOnClickListener(v -> {
            if (scheduleText.getText().toString().startsWith("Choose a date")) { message("Select a date and time"); return; }
            if (schedule.getTimeInMillis() <= System.currentTimeMillis()) { message("Choose a future date and time"); return; }
            if (homeDeliverySelected && addressTexts.isEmpty()) { message("Add a delivery address"); return; }
            DatabaseHelper.OrderLine line = new DatabaseHelper.OrderLine();
            line.productId = productId; line.quantity = quantity; line.customText = customText; line.artworkUri = artworkUri; line.optionIds.addAll(optionIds);
            String type = homeDeliverySelected ? "Home Delivery" : "Pickup";
            String address = homeDeliverySelected ? addressTexts.get(addressSpinner.getSelectedItemPosition()) : "";
            try {
                orderId = db.createOrder(userId, type, address, utc(schedule), java.util.Collections.singletonList(line));
                clearDraft();
                showConfirmation();
            } catch (RuntimeException ex) { message("Order could not be saved: " + ex.getMessage()); }
        });
        ((ScrollView) checkoutOverlay.findViewById(R.id.checkout_scroll)).scrollTo(0, 0);
    }
    private String utc(Calendar calendar) {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC")); return format.format(calendar.getTime());
    }
    private String localTime(String isoUtc) {
        try {
            java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            java.text.SimpleDateFormat shown = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
            shown.setTimeZone(TimeZone.getTimeZone("Asia/Colombo"));
            return shown.format(parser.parse(isoUtc)) + " (Sri Lanka)";
        } catch (java.text.ParseException ex) { return isoUtc + " UTC"; }
    }
    private void clearDraft() {
        quantity = 1; customText = ""; artworkUri = null; artworkName = null;
        homeDeliverySelected = false;
        optionIds.clear(); unitPrice = Money.amount("0");
    }
    private void chooseSchedule(Calendar schedule, TextView output) {
        DatePickerDialog date = new DatePickerDialog(this, (view, year, month, day) -> {
            schedule.set(year, month, day);
            TimePickerDialog time = new TimePickerDialog(this, (picker, hour, minute) -> {
                schedule.set(Calendar.HOUR_OF_DAY, hour); schedule.set(Calendar.MINUTE, minute);
                output.setText(String.format(java.util.Locale.US, "%04d-%02d-%02d %02d:%02d (Sri Lanka)", year, month + 1, day, hour, minute));
            }, schedule.get(Calendar.HOUR_OF_DAY), schedule.get(Calendar.MINUTE), false);
            time.show();
        }, schedule.get(Calendar.YEAR), schedule.get(Calendar.MONTH), schedule.get(Calendar.DAY_OF_MONTH));
        date.getDatePicker().setMinDate(System.currentTimeMillis()); date.show();
    }
    private void showConfirmation() {
        screen("Order confirmed", null, false);
        confirmationOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        confirmationOverlay.findViewById(R.id.confirmation_back).setOnClickListener(v -> showOrders());
        confirmationOverlay.findViewById(R.id.confirmation_track).setOnClickListener(v -> showOrderDetails(orderId));
        confirmationOverlay.findViewById(R.id.confirmation_shop).setOnClickListener(v -> showHome());
        TextView number = confirmationOverlay.findViewById(R.id.confirmation_number);
        number.setText(displayOrderId(orderId));
        number.setSingleLine(false);
        number.setEllipsize(null);
        try (Cursor c = db.order(userId, orderId)) {
            if (!c.moveToFirst()) number.setText("Order unavailable");
        }
    }
    private void showOrders() {
        showOrders("All");
    }
    private void showOrders(String selected) {
        screen("My orders", null, true);
        ordersOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        ordersOverlay.findViewById(R.id.orders_back).setOnClickListener(v -> showHome());
        LinearLayout filters = ordersOverlay.findViewById(R.id.orders_filters);
        filters.removeAllViews();
        String[] statuses = {"All", "Processing", "Printing", "Ready for Pickup", "Out for Delivery", "Completed", "Cancelled"};
        for (String status : statuses)
            addListingFilter(filters, status, status.equals(selected), () -> showOrders(status));
        LinearLayout list = ordersOverlay.findViewById(R.id.orders_list);
        list.removeAllViews();
        int count = 0;
        try (Cursor c = db.orders(userId)) {
            while (c.moveToNext()) {
                String status = c.getString(5);
                if (!"All".equals(selected) && !selected.equals(status)) continue;
                count++; long id = c.getLong(0);
                String productName = "Print order"; int itemCount = 0;
                try (Cursor items = db.orderItems(id)) {
                    while (items.moveToNext()) {
                        if (itemCount == 0) productName = items.getString(1);
                        itemCount += items.getInt(2);
                    }
                }
                LinearLayout card = orderCard(list);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
                card.addView(row);
                ImageView image = new ImageView(this);
                image.setImageResource(orderProductImage(productName)); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setBackgroundResource(R.drawable.home_image_round); image.setClipToOutline(true);
                image.setContentDescription(productName);
                row.addView(image, new LinearLayout.LayoutParams(dp(99), dp(117)));
                LinearLayout details = new LinearLayout(this);
                details.setOrientation(LinearLayout.VERTICAL); details.setPadding(dp(14), 0, 0, 0);
                row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                TextView orderNumber = orderText(details, displayOrderId(id), 18, true, 0xff0a1e2c);
                orderNumber.setSingleLine(false);
                orderNumber.setEllipsize(null);
                orderText(details, itemCount + " item" + (itemCount == 1 ? "" : "s") + " • " + productName, 13, false, 0xff6b7280);
                orderText(details, localTime(c.getString(1)), 12, false, 0xff6b7280);
                TextView badge = orderText(details, status, 13, true,
                    "Cancelled".equals(status) ? 0xffb24c4c : 0xff129e8d);
                badge.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                badge.setPadding(dp(9), dp(6), dp(9), dp(6));
                GradientDrawable badgeBackground = new GradientDrawable();
                badgeBackground.setColor("Cancelled".equals(status) ? 0xffffe8e6 : 0xffe4faf4);
                badgeBackground.setCornerRadius(dp(19)); badge.setBackground(badgeBackground);
                orderText(row, "›", 28, false, 0xff6b7280);
                card.setContentDescription("Order " + id + ", " + productName + ", " + status);
                card.setOnClickListener(v -> showOrderDetails(id));
            }
        }
        if (count == 0) {
            orderText(list, "All".equals(selected) ? "No orders yet." : "No " + selected + " orders.", 17, false, 0xff6b7280);
            if ("All".equals(selected)) {
                TextView browse = orderText(list, "Browse products  →", 17, true, 0xfff47f7a);
                browse.setOnClickListener(v -> showCategories());
            }
        }
        ((ScrollView) ordersOverlay.findViewById(R.id.orders_scroll)).scrollTo(0, 0);
    }
    private LinearLayout orderCard(LinearLayout parent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setBackgroundResource(R.drawable.home_round_white);
        card.setPadding(dp(16), dp(14), dp(16), dp(14)); card.setElevation(dp(3));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(14); parent.addView(card, params); return card;
    }
    private String displayOrderId(long id) {
        return String.format(java.util.Locale.US, "#PX%06d", id);
    }
    private TextView orderText(LinearLayout parent, String value, int size, boolean bold, int color) {
        TextView label = new TextView(this);
        label.setText(value); label.setTextSize(size); label.setTextColor(color);
        if (bold) label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setPadding(0, 0, 0, dp(5)); parent.addView(label); return label;
    }
    private int orderProductImage(String name) {
        String normalized = name.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("shirt")) return R.drawable.catalog_tee_premium;
        if (normalized.contains("business")) return R.drawable.home_category_business_cards;
        if (normalized.contains("flyer")) return R.drawable.home_category_flyers;
        if (normalized.contains("poster")) return R.drawable.home_category_posters;
        if (normalized.contains("banner")) return R.drawable.home_category_banners;
        if (normalized.contains("mug")) return R.drawable.home_category_mugs;
        if (normalized.contains("sticker")) return R.drawable.home_category_stickers;
        return R.drawable.home_hero_banner;
    }
    private void showOrderDetails(long id) {
        orderId = id;
        screen("Order tracking", this::showOrders, false);
        String status;
        try (Cursor c = db.order(userId, id)) {
            if (!c.moveToFirst()) { text(content, "Order unavailable", 18, true); return; }
            status = c.getString(7);
            text(content, "Order #" + id, 22, true);
            text(content, "Current status: " + status, 18, true);
            text(content, "Placed: " + c.getString(1) + " UTC", 15, false);
            text(content, "Fulfillment: " + c.getString(2), 15, false);
            if (c.getString(3) != null && !c.getString(3).isEmpty()) text(content, "Address: " + c.getString(3), 15, false);
            text(content, "Scheduled: " + localTime(c.getString(4)), 15, false);
            if (c.getString(5) != null) text(content, "Last rescheduled: " + localTime(c.getString(5)), 15, false);
            text(content, "Total: " + Money.lkr(Money.amount(c.getString(6))), 20, true);
            text(content, "Tracking: Processing → Printing → " + ("Pickup".equals(c.getString(2)) ? "Ready for Pickup" : "Out for Delivery") + " → Completed", 14, false);
        }
        try (Cursor items = db.orderItems(id)) {
            while (items.moveToNext()) {
                long itemId = items.getLong(0); LinearLayout row = card(content);
                text(row, items.getString(1) + " × " + items.getInt(2), 17, true);
                if (items.getString(3) != null && !items.getString(3).isEmpty()) text(row, "Text: " + items.getString(3), 14, false);
                if (items.getString(4) != null) text(row, "Artwork selected", 14, false);
                try (Cursor options = db.itemOptions(itemId)) {
                    while (options.moveToNext()) text(row, options.getString(0) + ": " + options.getString(1), 14, false);
                }
                text(row, Money.lkr(Money.amount(items.getString(6))), 17, true);
            }
        }
        if (DatabaseHelper.PROCESSING.equals(status)) {
            button(content, "Reschedule", () -> {
                Calendar schedule = Calendar.getInstance(TimeZone.getTimeZone("Asia/Colombo")); schedule.add(Calendar.DAY_OF_MONTH, 1);
                TextView selected = text(content, "Choose a new date and time", 16, true);
                chooseSchedule(schedule, selected);
                button(content, "Save new schedule", () -> {
                    if (selected.getText().toString().equals("Choose a new date and time") || schedule.getTimeInMillis() <= System.currentTimeMillis()) { message("Choose a future date and time"); return; }
                    if (db.reschedule(userId, id, utc(schedule))) { message("Order rescheduled"); showOrderDetails(id); }
                    else { message("Only a Processing order can be rescheduled"); showOrderDetails(id); }
                });
            });
            button(content, "Cancel order", () -> new AlertDialog.Builder(this).setTitle("Cancel order #" + id + "?")
                .setMessage("This action cannot be undone.")
                .setNegativeButton("Keep order", null)
                .setPositiveButton("Cancel order", (dialog, which) -> {
                    if (db.cancel(userId, id)) message("Order cancelled");
                    else message("Only a Processing order can be cancelled");
                    showOrderDetails(id);
                }).show());
        }
    }
    private void showNotifications() {
        screen("Notifications & offers", null, true);
        notificationsOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        notificationsOverlay.findViewById(R.id.notifications_back).setOnClickListener(v -> showHome());
        LinearLayout list = notificationsOverlay.findViewById(R.id.notifications_list);
        list.removeAllViews();
        int count = 0;
        try (Cursor c = db.notifications(userId)) {
            while (c.moveToNext()) {
                count++;
                String heading = c.getString(1), body = c.getString(2);
                long linkedOrder = 0;
                java.util.regex.Matcher match = java.util.regex.Pattern.compile("Order #(\\d+)").matcher(body);
                if (match.find()) {
                    linkedOrder = Long.parseLong(match.group(1));
                    body = body.substring(0, match.start()) + "Order " + displayOrderId(linkedOrder)
                        + body.substring(match.end());
                }
                final long destination = linkedOrder;
                addNoticeCard(list, "▣", heading, body, relativeTime(c.getString(3)),
                    0xffe3f8f4, 0xff16aa96, () -> {
                        if (destination > 0) showOrderDetails(destination); else showOrders();
                    });
            }
        }
        if (count == 0) orderText(list, "No order notifications yet.", 16, false, 0xff6b7280);
        try (Cursor c = db.promotions()) {
            while (c.moveToNext()) {
                String heading = c.getString(0), description = c.getString(1);
                addNoticeCard(list, "%", heading, description, "Offer", 0xffffe9e8,
                    0xfff47f7a, () -> message("Promotions are information only and do not change order totals."));
            }
        }
        ((ScrollView) notificationsOverlay.findViewById(R.id.notifications_scroll)).scrollTo(0, 0);
    }
    private void showProfile() {
        screen("Profile", null, true);
        profileOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        profileOverlay.findViewById(R.id.profile_back).setOnClickListener(v -> showHome());
        String name = "PrintXpress customer", email = "";
        try (Cursor c = db.user(userId)) {
            if (c.moveToFirst()) {
                name = c.getString(1); email = c.getString(2);
            }
        }
        ((TextView) profileOverlay.findViewById(R.id.profile_name)).setText(name);
        ((TextView) profileOverlay.findViewById(R.id.profile_email)).setText(email);
        String[] words = name.trim().split("\\s+");
        String initials = words.length == 0 || words[0].isEmpty() ? "PX" :
            words[0].substring(0, 1) + (words.length > 1 ? words[words.length - 1].substring(0, 1) : "");
        ((TextView) profileOverlay.findViewById(R.id.profile_initials)).setText(initials.toUpperCase(java.util.Locale.ROOT));
        LinearLayout menu = profileOverlay.findViewById(R.id.profile_menu);
        menu.removeAllViews();
        addMenuCard(menu, "♙", "Edit Profile", null, 0xffe5f8f4, 0xff159e90, this::showEditProfile);
        addMenuCard(menu, "♡", "Saved Designs", null, 0xffffecea, 0xfff47f7a, this::showDesigns);
        addMenuCard(menu, "⌖", "Addresses", null, 0xffe5f8f4, 0xff159e90, this::showAddresses);
        addMenuCard(menu, "▤", "Order History", null, 0xffe5f8f4, 0xff159e90, this::showOrders);
        addMenuCard(menu, "♧", "Notifications", null, 0xffffecea, 0xfff47f7a, this::showNotifications);
        addMenuCard(menu, "?", "Help & Support", null, 0xffe5f8f4, 0xff159e90, this::showHelpSupport);
        profileOverlay.findViewById(R.id.profile_logout).setOnClickListener(v -> {
            getPreferences(MODE_PRIVATE).edit().remove("loggedInUserId").apply();
            userId = -1; showLogin();
        });
        ((ScrollView) profileOverlay.findViewById(R.id.profile_scroll)).scrollTo(0, 0);
    }
    private String relativeTime(String isoUtc) {
        try {
            java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            long minutes = Math.max(0, (System.currentTimeMillis() - parser.parse(isoUtc).getTime()) / 60000);
            if (minutes < 1) return "Now";
            if (minutes < 60) return minutes + "m ago";
            if (minutes < 1440) return (minutes / 60) + "h ago";
            return (minutes / 1440) + "d ago";
        } catch (java.text.ParseException ex) { return localTime(isoUtc); }
    }
    private void addNoticeCard(LinearLayout parent, String icon, String heading, String body,
                               String stamp, int iconBackground, int iconColor, Runnable action) {
        LinearLayout card = orderCard(parent);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row);
        TextView symbol = menuIcon(icon, iconBackground, iconColor);
        row.addView(symbol, new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL); details.setPadding(dp(12), 0, dp(5), 0);
        row.addView(details, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        orderText(details, heading, 16, true, 0xff0a1e2c);
        orderText(details, body, 14, false, 0xff6b7280);
        LinearLayout tail = new LinearLayout(this);
        tail.setGravity(Gravity.END | Gravity.CENTER_VERTICAL); tail.setOrientation(LinearLayout.VERTICAL);
        row.addView(tail, new LinearLayout.LayoutParams(dp(62), ViewGroup.LayoutParams.WRAP_CONTENT));
        orderText(tail, stamp, 11, false, 0xff8a98a5);
        orderText(tail, "›", 25, false, 0xff728393);
        card.setContentDescription(heading + ". " + body);
        card.setOnClickListener(v -> action.run());
    }
    private TextView menuIcon(String symbol, int background, int foreground) {
        TextView icon = new TextView(this);
        icon.setText(symbol); icon.setTextSize(26); icon.setTypeface(null, android.graphics.Typeface.BOLD);
        icon.setTextColor(foreground); icon.setGravity(Gravity.CENTER);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(background); shape.setCornerRadius(dp(15)); icon.setBackground(shape);
        return icon;
    }
    private void addMenuCard(LinearLayout parent, String icon, String heading, String subtitle,
                             int iconBackground, int iconColor, Runnable action) {
        LinearLayout card = orderCard(parent);
        card.setPadding(dp(12), dp(9), dp(12), dp(9));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row);
        row.addView(menuIcon(icon, iconBackground, iconColor), new LinearLayout.LayoutParams(dp(51), dp(51)));
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL); labels.setPadding(dp(17), 0, dp(4), 0);
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        orderText(labels, heading, 17, true, 0xff0a1e2c);
        if (subtitle != null) orderText(labels, subtitle, 13, false, 0xff6b7280);
        orderText(row, "›", 27, false, 0xff80919d);
        card.setContentDescription(heading + (subtitle == null ? "" : ". " + subtitle));
        card.setOnClickListener(v -> action.run());
    }
    private void showHelpSupport() {
        screen("Help & Support", this::showProfile, false);
        supportOverlay.setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getColor(R.color.mint));
        supportOverlay.findViewById(R.id.support_back).setOnClickListener(v -> showProfile());
        EditText search = supportOverlay.findViewById(R.id.support_search);
        Object previousWatcher = search.getTag();
        if (previousWatcher instanceof TextWatcher) search.removeTextChangedListener((TextWatcher) previousWatcher);
        search.setText("");
        Runnable update = () -> {
            LinearLayout list = supportOverlay.findViewById(R.id.support_list);
            list.removeAllViews();
            String query = value(search).toLowerCase(java.util.Locale.ROOT);
            String[] names = {"FAQs", "Contact Us", "Printing Guidelines", "Track Your Order", "Returns & Refunds", "Live Chat"};
            String[] descriptions = {"Find answers to common questions", "Contact the print shop directly",
                "File requirements & tips", "Check your order status", "Ask the print shop about its policy",
                "Unavailable in this local app"};
            String[] symbols = {"▣", "☏", "▤", "▣", "↻", "☏"};
            Runnable[] actions = {
                () -> showInfo(this::showHelpSupport),
                () -> message("This local app does not send support messages. Contact the print shop directly."),
                () -> showInfo(this::showHelpSupport),
                this::showOrders,
                () -> message("Please ask the print shop directly about its returns and refunds policy."),
                () -> message("Live chat is unavailable in this local app.")
            };
            int visible = 0;
            for (int i = 0; i < names.length; i++) {
                if (!query.isEmpty() && !names[i].toLowerCase(java.util.Locale.ROOT).contains(query)
                    && !descriptions[i].toLowerCase(java.util.Locale.ROOT).contains(query)) continue;
                boolean coral = i == 1 || i == 3;
                addMenuCard(list, symbols[i], names[i], descriptions[i], coral ? 0xffffeae8 : 0xffe3f8f4,
                    coral ? 0xfff47f7a : 0xff16aa96, actions[i]);
                visible++;
            }
            if (visible == 0) orderText(list, "No help topics match your search.", 15, false, 0xff6b7280);
        };
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { update.run(); }
            public void afterTextChanged(Editable s) { }
        };
        search.addTextChangedListener(watcher);
        search.setTag(watcher);
        update.run();
        ((ScrollView) supportOverlay.findViewById(R.id.support_scroll)).scrollTo(0, 0);
    }
    private void showEditProfile() {
        screen("Edit profile", this::showProfile, false);
        String name = "", phone = "";
        try (Cursor c = db.user(userId)) { if (c.moveToFirst()) { name = c.getString(1); phone = c.getString(3); } }
        EditText nameField = field(content, "Full name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS); nameField.setText(name);
        EditText phoneField = field(content, "Phone number", InputType.TYPE_CLASS_PHONE); phoneField.setText(phone);
        button(content, "Save profile", () -> {
            if (!required(nameField, "Full name") || !required(phoneField, "Phone")) return;
            if (!value(phoneField).matches("[0-9+ ]{9,15}")) { phoneField.setError("Enter a valid phone number"); return; }
            db.updateProfile(userId, value(nameField), value(phoneField)); message("Profile updated"); showProfile();
        });
    }
    private void showAddresses() {
        screen("Manage addresses", this::showProfile, false);
        button(content, "Add address", () -> showAddressForm(0, null, null, null, null, this::showAddresses));
        int count = 0;
        try (Cursor c = db.addresses(userId)) {
            while (c.moveToNext()) {
                count++; long id = c.getLong(0); String line = c.getString(1), city = c.getString(2), district = c.getString(3), postal = c.getString(4);
                LinearLayout row = card(content);
                text(row, line + ", " + city + ", " + district + (postal == null ? "" : " " + postal), 16, true);
                button(row, "Edit", () -> showAddressForm(id, line, city, district, postal, this::showAddresses));
                link(row, "Delete", () -> new AlertDialog.Builder(this).setTitle("Delete address?")
                    .setNegativeButton("Keep", null).setPositiveButton("Delete", (dialog, which) -> { db.deleteAddress(userId, id); showAddresses(); }).show());
            }
        }
        if (count == 0) text(content, "No saved addresses.", 16, false);
    }
    private void showAddressForm(long id, String line, String city, String district, String postal, Runnable returnTo) {
        screen(id == 0 ? "Add address" : "Edit address", returnTo, false);
        EditText lineField = field(content, "Address line", InputType.TYPE_CLASS_TEXT); if (line != null) lineField.setText(line);
        EditText cityField = field(content, "City", InputType.TYPE_CLASS_TEXT); if (city != null) cityField.setText(city);
        EditText districtField = field(content, "District", InputType.TYPE_CLASS_TEXT); if (district != null) districtField.setText(district);
        EditText postalField = field(content, "Postal code (optional)", InputType.TYPE_CLASS_TEXT); if (postal != null) postalField.setText(postal);
        button(content, "Save address", () -> {
            if (!required(lineField, "Address") || !required(cityField, "City") || !required(districtField, "District")) return;
            db.saveAddress(userId, id, value(lineField), value(cityField), value(districtField), value(postalField));
            message("Address saved"); returnTo.run();
        });
    }
    private void showDesigns() {
        screen("Saved designs", this::showProfile, false);
        int count = 0;
        try (Cursor c = db.designs(userId)) {
            while (c.moveToNext()) {
                count++; long id = c.getLong(0); String name = c.getString(1), uri = c.getString(2);
                LinearLayout row = card(content); text(row, name, 18, true);
                button(row, "Use in customization", () -> {
                    artworkUri = uri; artworkName = name;
                    if (productId > 0) showCustomize();
                    else {
                        pendingDesignUri = uri; pendingDesignName = name;
                        message("Choose a product for this design"); showCategories();
                    }
                });
                link(row, "Delete", () -> new AlertDialog.Builder(this).setTitle("Delete design?")
                    .setNegativeButton("Keep", null).setPositiveButton("Delete", (dialog, which) -> { db.deleteDesign(userId, id); showDesigns(); }).show());
            }
        }
        if (count == 0) text(content, "No saved designs yet. Choose artwork while customizing a product.", 16, false);
    }
    private void showInfo() {
        showInfo(this::showProfile);
    }
    private void showInfo(Runnable returnTo) {
        screen("Guidelines & FAQ", returnTo, false);
        text(content, "Print guidelines", 21, true);
        text(content, "Use a clear, high-resolution image or PDF. Check spelling and layout before placing an order. Artwork stays on this device; this demo has no remote upload.", 16, false);
        text(content, "Frequently asked questions", 21, true);
        text(content, "Can I change an order? You can reschedule or cancel while its current status is Processing.", 16, false);
        text(content, "Can I choose delivery? Yes. Save an address and select Home Delivery at checkout.", 16, false);
        text(content, "Are offers deducted? No. Promotions are informational in this local demo.", 16, false);
        text(content, "Need help? Please contact the print shop directly; this demo does not send support messages.", 16, false);
    }
}
