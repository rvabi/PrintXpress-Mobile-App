package com.printxpress.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
        userId = getPreferences(MODE_PRIVATE).getLong("loggedInUserId", -1);
        showSplash();
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private void screen(String heading, Runnable back, boolean tabs) {
        backAction = back;
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
        screen("PrintXpress", null, false);
        text(content, "Printing made personal", 22, true);
        text(content, "Custom prints, local ordering, easy pickup or delivery.", 16, false);
        new Handler(getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            if (userId > 0 && db.userExists(userId)) showHome(); else showLogin();
        }, 450);
    }
    private void showLogin() {
        screen("Welcome back", null, false);
        text(content, "Sign in to PrintXpress", 20, true);
        EditText email = field(content, "Email", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password = field(content, "Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        button(content, "Login", () -> {
            if (!required(email, "Email") || !required(password, "Password")) return;
            if (!Patterns.EMAIL_ADDRESS.matcher(value(email)).matches()) { email.setError("Enter a valid email"); return; }
            char[] secret = password.getText().toString().toCharArray();
            long id;
            try { id = db.authenticate(value(email), secret); } finally { Arrays.fill(secret, '\0'); }
            if (id < 0) { password.setError("Incorrect email or password"); return; }
            userId = id; getPreferences(MODE_PRIVATE).edit().putLong("loggedInUserId", userId).apply(); showHome();
        });
        link(content, "Create an account", this::showRegister);
    }
    private void showRegister() {
        screen("Create account", this::showLogin, false);
        EditText name = field(content, "Full name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText email = field(content, "Email", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText phone = field(content, "Phone number", InputType.TYPE_CLASS_PHONE);
        EditText password = field(content, "Password (8+ characters)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText confirm = field(content, "Confirm password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        button(content, "Register", () -> {
            if (!required(name, "Full name") || !required(email, "Email") || !required(phone, "Phone") || !required(password, "Password") || !required(confirm, "Confirmation")) return;
            if (!Patterns.EMAIL_ADDRESS.matcher(value(email)).matches()) { email.setError("Enter a valid email"); return; }
            if (!value(phone).matches("[0-9+ ]{9,15}")) { phone.setError("Enter a valid phone number"); return; }
            if (password.length() < 8 || !password.getText().toString().matches(".*[A-Za-z].*") || !password.getText().toString().matches(".*[0-9].*")) { password.setError("Use 8+ characters with a letter and number"); return; }
            if (!password.getText().toString().equals(confirm.getText().toString())) { confirm.setError("Passwords do not match"); return; }
            char[] secret = password.getText().toString().toCharArray();
            long id;
            try { id = db.register(value(name), value(email), value(phone), secret); } finally { Arrays.fill(secret, '\0'); }
            if (id < 0) { email.setError("This email is already registered"); return; }
            message("Account created. Please log in."); showLogin();
        });
    }
    private void showHome() {
        screen("PrintXpress", null, true);
        String name = "there";
        try (Cursor c = db.user(userId)) { if (c.moveToFirst()) name = c.getString(1); }
        text(content, "Hello, " + name + "!", 23, true);
        text(content, "What would you like to print?", 16, false);
        EditText search = field(content, "Search products", InputType.TYPE_CLASS_TEXT);
        button(content, "Search", () -> showProductList(0, value(search)));
        button(content, "Browse categories", this::showCategories);
        LinearLayout offer = card(content);
        text(offer, "Made for your next big idea", 18, true);
        text(offer, "Explore seven print categories. Offers are informational and do not reduce order totals.", 15, false);
        link(content, "Print guidelines & FAQ", () -> showInfo(this::showHome));
    }
    private void showCategories() {
        screen("Categories", this::showHome, false);
        try (Cursor c = db.categories()) {
            while (c.moveToNext()) {
                long id = c.getLong(0); String name = c.getString(1);
                LinearLayout row = card(content);
                text(row, name, 19, true);
                button(row, "Explore " + name, () -> showProductList(id, null));
            }
        }
    }
    private void showProductList(long category, String search) {
        categoryId = category;
        screen(search == null ? "Products" : "Search results", search == null ? this::showCategories : this::showHome, false);
        if (search != null) text(content, "Results for “" + search + "”", 16, false);
        int count = 0;
        try (Cursor c = db.products(category, search)) {
            while (c.moveToNext()) {
                count++; long id = c.getLong(0);
                LinearLayout row = card(content);
                text(row, c.getString(1), 19, true);
                text(row, c.getString(2), 15, false);
                text(row, "From " + Money.lkr(Money.amount(c.getString(3))), 17, true);
                button(row, "View details", () -> showProductDetails(id));
            }
        }
        if (count == 0) text(content, "No products found. Try another category or search.", 16, false);
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
        try (Cursor c = db.product(id)) {
            if (!c.moveToFirst()) { text(content, "Product unavailable", 18, true); return; }
            text(content, c.getString(1), 24, true);
            text(content, c.getString(2), 16, false);
            text(content, "Starting at " + Money.lkr(Money.amount(c.getString(3))), 20, true);
        }
        text(content, "Available choices", 18, true);
        try (Cursor c = db.options(id)) {
            while (c.moveToNext()) text(content, c.getString(1) + ": " + c.getString(2), 15, false);
        }
        button(content, "Customize print", this::showCustomize);
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
        text(content, artworkName == null ? "No artwork selected" : "Selected: " + artworkName, 19, true);
        text(content, "Choose a local image or PDF. The app stores a file reference on this device; it does not upload artwork.", 15, false);
        button(content, artworkUri == null ? "Choose artwork" : "Change artwork", () -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/png", "image/jpeg", "application/pdf"});
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, PICK_ARTWORK);
        });
        if (artworkUri != null) {
            button(content, "Remove artwork", () -> { artworkUri = null; artworkName = null; showArtwork(); });
            button(content, "Save this design", () -> {
                EditText name = new EditText(this); name.setHint("Design name");
                new AlertDialog.Builder(this).setTitle("Save design").setView(name)
                    .setPositiveButton("Save", (dialog, which) -> {
                        if (value(name).isEmpty()) { message("Enter a design name"); return; }
                        db.saveDesign(userId, value(name), artworkUri); message("Design saved");
                    }).setNegativeButton("Cancel", null).show();
            });
        }
        button(content, "Return to customization", this::showCustomize);
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
        try (Cursor c = db.product(productId)) { if (c.moveToFirst()) text(content, c.getString(1), 22, true); }
        text(content, "Quantity: " + quantity, 16, false);
        try (Cursor c = db.options(productId)) {
            while (c.moveToNext()) if (optionIds.contains(c.getLong(0))) text(content, c.getString(1) + ": " + c.getString(2), 16, false);
        }
        if (!customText.isEmpty()) text(content, "Text: " + customText, 16, false);
        if (artworkName != null) text(content, "Artwork: " + artworkName, 16, false);
        text(content, "Unit price: " + Money.lkr(unitPrice), 18, true);
        text(content, "Total: " + Money.lkr(unitPrice.multiply(BigDecimal.valueOf(quantity))), 21, true);
        text(content, "Promotions are display-only and do not change this total.", 14, false);
        button(content, "Continue to pickup or delivery", this::showDelivery);
    }
    private void showDelivery() {
        screen("Pickup or delivery", this::showSummary, false);
        text(content, "Choose how to receive your print", 18, true);
        RadioGroup types = new RadioGroup(this);
        RadioButton pickup = new RadioButton(this); pickup.setText("Pickup"); types.addView(pickup);
        RadioButton home = new RadioButton(this); home.setText("Home Delivery"); types.addView(home);
        if (homeDeliverySelected) home.setChecked(true); else pickup.setChecked(true); content.addView(types);
        TextView addressLabel = text(content, "Saved delivery address", 16, true);
        Spinner addressSpinner = new Spinner(this);
        List<String> addressTexts = new ArrayList<>();
        try (Cursor c = db.addresses(userId)) {
            while (c.moveToNext()) addressTexts.add(c.getString(1) + ", " + c.getString(2) + ", " + c.getString(3) + (c.getString(4) == null ? "" : " " + c.getString(4)));
        }
        addressSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, addressTexts));
        content.addView(addressSpinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        Button addAddress = button(content, "Add address", () -> showAddressForm(0, null, null, null, null, this::showDelivery));
        View.OnClickListener toggle = v -> {
            boolean visible = home.isChecked(); homeDeliverySelected = visible;
            addressLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
            addressSpinner.setVisibility(visible ? View.VISIBLE : View.GONE); addAddress.setVisibility(visible ? View.VISIBLE : View.GONE);
        };
        pickup.setOnClickListener(toggle); home.setOnClickListener(toggle); toggle.onClick(pickup);
        Calendar schedule = Calendar.getInstance(TimeZone.getTimeZone("Asia/Colombo"));
        schedule.add(Calendar.DAY_OF_MONTH, 1); schedule.set(Calendar.SECOND, 0);
        TextView scheduleText = text(content, "Choose a date and time", 17, true);
        button(content, "Select date and time", () -> chooseSchedule(schedule, scheduleText));
        button(content, "Place order", () -> {
            if (scheduleText.getText().toString().equals("Choose a date and time")) { message("Select a date and time"); return; }
            if (schedule.getTimeInMillis() <= System.currentTimeMillis()) { message("Choose a future date and time"); return; }
            if (home.isChecked() && addressTexts.isEmpty()) { message("Add a delivery address"); return; }
            DatabaseHelper.OrderLine line = new DatabaseHelper.OrderLine();
            line.productId = productId; line.quantity = quantity; line.customText = customText; line.artworkUri = artworkUri; line.optionIds.addAll(optionIds);
            String type = home.isChecked() ? "Home Delivery" : "Pickup";
            String address = home.isChecked() ? addressTexts.get(addressSpinner.getSelectedItemPosition()) : "";
            try {
                orderId = db.createOrder(userId, type, address, utc(schedule), java.util.Collections.singletonList(line));
                clearDraft();
                showConfirmation();
            } catch (RuntimeException ex) { message("Order could not be saved: " + ex.getMessage()); }
        });
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
        text(content, "Your order was saved", 23, true);
        try (Cursor c = db.order(userId, orderId)) {
            if (c.moveToFirst()) {
                text(content, "Order #" + orderId, 20, true);
                text(content, c.getString(2) + " • " + c.getString(7), 16, false);
                text(content, "Scheduled: " + localTime(c.getString(4)), 16, false);
                text(content, Money.lkr(Money.amount(c.getString(6))), 20, true);
            }
        }
        button(content, "View my orders", this::showOrders);
        button(content, "Back to home", this::showHome);
    }
    private void showOrders() {
        screen("My orders", null, true);
        int count = 0;
        try (Cursor c = db.orders(userId)) {
            while (c.moveToNext()) {
                count++; long id = c.getLong(0);
                LinearLayout row = card(content);
                text(row, "Order #" + id + " • " + c.getString(5), 18, true);
                text(row, c.getString(2) + " • " + localTime(c.getString(3)), 14, false);
                text(row, Money.lkr(Money.amount(c.getString(4))), 17, true);
                button(row, "View details", () -> showOrderDetails(id));
            }
        }
        if (count == 0) { text(content, "No orders yet.", 17, false); button(content, "Browse products", this::showCategories); }
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
        text(content, "Order notifications", 19, true);
        int count = 0;
        try (Cursor c = db.notifications(userId)) {
            while (c.moveToNext()) {
                count++; LinearLayout row = card(content);
                text(row, c.getString(1), 17, true); text(row, c.getString(2), 15, false);
                text(row, c.getString(3) + " UTC", 13, false);
            }
        }
        if (count == 0) text(content, "No order notifications yet.", 15, false);
        text(content, "Promotions", 19, true);
        try (Cursor c = db.promotions()) {
            while (c.moveToNext()) {
                LinearLayout row = card(content);
                text(row, c.getString(0), 17, true); text(row, c.getString(1), 15, false);
                text(row, c.getInt(2) + "% offer information only. Not applied at checkout.", 14, false);
            }
        }
    }
    private void showProfile() {
        screen("Profile", null, true);
        try (Cursor c = db.user(userId)) {
            if (c.moveToFirst()) {
                text(content, c.getString(1), 22, true);
                text(content, c.getString(2), 16, false);
                text(content, c.getString(3), 16, false);
            }
        }
        button(content, "Edit profile", this::showEditProfile);
        button(content, "Manage addresses", this::showAddresses);
        button(content, "Saved designs", this::showDesigns);
        button(content, "Print guidelines & FAQ", this::showInfo);
        button(content, "Logout", () -> {
            getPreferences(MODE_PRIVATE).edit().remove("loggedInUserId").apply();
            userId = -1; showLogin();
        });
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
