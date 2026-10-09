package com.printxpress.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.printxpress.app.pricing.Money;
import com.printxpress.app.security.PasswordHasher;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DatabaseHelper extends SQLiteOpenHelper {
    public static final String PROCESSING = "Processing";

    public DatabaseHelper(Context context) {
        this(context, DatabaseContract.NAME);
    }

    DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DatabaseContract.VERSION);
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, full_name TEXT NOT NULL, email TEXT NOT NULL COLLATE NOCASE UNIQUE, phone TEXT NOT NULL, password TEXT NOT NULL)");
        db.execSQL("CREATE TABLE addresses (address_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(user_id), address_line TEXT NOT NULL, city TEXT NOT NULL, district TEXT NOT NULL, postal_code TEXT)");
        db.execSQL("CREATE TABLE categories (category_id INTEGER PRIMARY KEY AUTOINCREMENT, category_name TEXT NOT NULL UNIQUE)");
        db.execSQL("CREATE TABLE products (product_id INTEGER PRIMARY KEY AUTOINCREMENT, category_id INTEGER NOT NULL REFERENCES categories(category_id), product_name TEXT NOT NULL, description TEXT, base_price REAL NOT NULL CHECK(base_price >= 0), image_name TEXT)");
        db.execSQL("CREATE TABLE product_options (option_id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL REFERENCES products(product_id), option_type TEXT NOT NULL, option_value TEXT NOT NULL, price_adjustment REAL NOT NULL DEFAULT 0, UNIQUE(product_id, option_type, option_value))");
        db.execSQL("CREATE TABLE orders (order_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(user_id), order_date TEXT NOT NULL, order_type TEXT NOT NULL CHECK(order_type IN ('Pickup','Home Delivery')), delivery_address TEXT, scheduled_for TEXT NOT NULL, rescheduled_at TEXT, total_amount REAL NOT NULL CHECK(total_amount >= 0), status TEXT NOT NULL CHECK(status IN ('Processing','Printing','Ready for Pickup','Out for Delivery','Completed','Cancelled')))");
        db.execSQL("CREATE TABLE order_items (order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL REFERENCES orders(order_id), product_id INTEGER NOT NULL REFERENCES products(product_id), quantity INTEGER NOT NULL CHECK(quantity > 0), custom_text TEXT, artwork_path TEXT, unit_price REAL NOT NULL CHECK(unit_price >= 0), subtotal REAL NOT NULL CHECK(subtotal >= 0))");
        db.execSQL("CREATE TABLE order_item_options (order_item_option_id INTEGER PRIMARY KEY AUTOINCREMENT, order_item_id INTEGER NOT NULL REFERENCES order_items(order_item_id), option_id INTEGER NOT NULL REFERENCES product_options(option_id), price_adjustment REAL NOT NULL DEFAULT 0, UNIQUE(order_item_id, option_id))");
        db.execSQL("CREATE TABLE saved_designs (design_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(user_id), design_name TEXT NOT NULL, file_path TEXT NOT NULL)");
        db.execSQL("CREATE TABLE notifications (notification_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(user_id), title TEXT NOT NULL, message TEXT NOT NULL, created_at TEXT NOT NULL, is_read INTEGER NOT NULL DEFAULT 0 CHECK(is_read IN (0,1)))");
        db.execSQL("CREATE TABLE promotions (promotion_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT, discount REAL NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL)");
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("A non-destructive database migration is required");
    }

    private void seed(SQLiteDatabase db) {
        String[][] catalogue = {
            {"Business Cards", "Classic Business Cards", "Sharp business cards for first impressions.", "1500.00", "Size|Standard|0,Size|Large|300,Finish|Matte|0,Finish|Glossy|250,Print Side|Front Only|0,Print Side|Front and Back|350"},
            {"Flyers", "Promotional Flyers", "Colourful flyers for events and offers.", "1200.00", "Size|A5|0,Size|A4|400,Paper Type|Standard|0,Paper Type|Premium|300,Print Side|Front Only|0,Print Side|Front and Back|250"},
            {"Posters", "Event Posters", "Bold posters for announcements.", "900.00", "Size|A3|0,Size|A2|600,Finish|Matte|0,Finish|Glossy|200"},
            {"Banners", "Vinyl Banners", "Durable indoor or outdoor banners.", "2500.00", "Size|Small|0,Size|Large|1500,Material|Indoor Vinyl|0,Material|Outdoor Vinyl|650"},
            {"Stickers", "Custom Stickers", "Stickers for packaging and promotions.", "700.00", "Size|Small|0,Size|Large|200,Finish|Matte|0,Finish|Glossy|150"},
            {"T-Shirts", "Printed T-Shirts", "Custom apparel for teams and events.", "1800.00", "Size|S|0,Size|M|0,Size|L|150,Size|XL|250,Colour|White|0,Colour|Black|200"},
            {"Mugs", "Photo Mugs", "Personalized ceramic mugs.", "950.00", "Colour|White|0,Colour|Mint|150,Print Side|One Side|0,Print Side|Wraparound|300"}
        };
        for (String[] row : catalogue) {
            ContentValues category = new ContentValues();
            category.put("category_name", row[0]);
            long categoryId = db.insertOrThrow("categories", null, category);
            ContentValues product = new ContentValues();
            product.put("category_id", categoryId);
            product.put("product_name", row[1]);
            product.put("description", row[2]);
            product.put("base_price", Double.parseDouble(row[3]));
            long productId = db.insertOrThrow("products", null, product);
            for (String choice : row[4].split(",")) {
                String[] fields = choice.split("\\|");
                ContentValues option = new ContentValues();
                option.put("product_id", productId);
                option.put("option_type", fields[0]);
                option.put("option_value", fields[1]);
                option.put("price_adjustment", Double.parseDouble(fields[2]));
                db.insertOrThrow("product_options", null, option);
            }
        }
        ContentValues promotion = new ContentValues();
        promotion.put("title", "Welcome to PrintXpress");
        promotion.put("description", "Explore custom printing. Offers shown here are informational only.");
        promotion.put("discount", 10.0);
        promotion.put("start_date", "2026-01-01T00:00:00Z");
        promotion.put("end_date", "2027-12-31T23:59:59Z");
        db.insertOrThrow("promotions", null, promotion);
    }

    public long register(String name, String email, String phone, char[] password) {
        ContentValues values = new ContentValues();
        values.put("full_name", name.trim());
        values.put("email", email.trim().toLowerCase(Locale.ROOT));
        values.put("phone", phone.trim());
        values.put("password", PasswordHasher.hash(password));
        try {
            return getWritableDatabase().insertOrThrow("users", null, values);
        } catch (SQLiteConstraintException duplicate) {
            return -1;
        }
    }

    public long authenticate(String email, char[] password) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT user_id,password FROM users WHERE email=?", new String[]{email.trim().toLowerCase(Locale.ROOT)})) {
            if (c.moveToFirst() && PasswordHasher.verify(password, c.getString(1))) return c.getLong(0);
        }
        return -1;
    }

    public boolean userExists(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM users WHERE user_id=?", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst();
        }
    }

    public Cursor user(long userId) { return getReadableDatabase().rawQuery("SELECT user_id,full_name,email,phone FROM users WHERE user_id=?", new String[]{String.valueOf(userId)}); }
    public Cursor categories() { return getReadableDatabase().rawQuery("SELECT category_id,category_name FROM categories ORDER BY category_id", null); }
    public Cursor products(long categoryId, String search) {
        if (search != null && !search.trim().isEmpty()) return getReadableDatabase().rawQuery("SELECT product_id,product_name,description,base_price FROM products WHERE product_name LIKE ? OR description LIKE ? ORDER BY product_name", new String[]{"%" + search.trim() + "%", "%" + search.trim() + "%"});
        return getReadableDatabase().rawQuery("SELECT product_id,product_name,description,base_price FROM products WHERE category_id=? ORDER BY product_name", new String[]{String.valueOf(categoryId)});
    }
    public Cursor product(long productId) { return getReadableDatabase().rawQuery("SELECT product_id,product_name,description,base_price FROM products WHERE product_id=?", new String[]{String.valueOf(productId)}); }
    public Cursor options(long productId) { return getReadableDatabase().rawQuery("SELECT option_id,option_type,option_value,price_adjustment FROM product_options WHERE product_id=? ORDER BY option_type,option_id", new String[]{String.valueOf(productId)}); }
    public Cursor addresses(long userId) { return getReadableDatabase().rawQuery("SELECT address_id,address_line,city,district,postal_code FROM addresses WHERE user_id=? ORDER BY address_id DESC", new String[]{String.valueOf(userId)}); }
    public Cursor designs(long userId) { return getReadableDatabase().rawQuery("SELECT design_id,design_name,file_path FROM saved_designs WHERE user_id=? ORDER BY design_id DESC", new String[]{String.valueOf(userId)}); }
    public Cursor orders(long userId) { return getReadableDatabase().rawQuery("SELECT order_id,order_date,order_type,scheduled_for,total_amount,status FROM orders WHERE user_id=? ORDER BY order_id DESC", new String[]{String.valueOf(userId)}); }
    public Cursor order(long userId, long orderId) { return getReadableDatabase().rawQuery("SELECT order_id,order_date,order_type,delivery_address,scheduled_for,rescheduled_at,total_amount,status FROM orders WHERE user_id=? AND order_id=?", new String[]{String.valueOf(userId),String.valueOf(orderId)}); }
    public Cursor orderItems(long orderId) { return getReadableDatabase().rawQuery("SELECT oi.order_item_id,p.product_name,oi.quantity,oi.custom_text,oi.artwork_path,oi.unit_price,oi.subtotal FROM order_items oi JOIN products p ON p.product_id=oi.product_id WHERE oi.order_id=?", new String[]{String.valueOf(orderId)}); }
    public Cursor itemOptions(long itemId) { return getReadableDatabase().rawQuery("SELECT po.option_type,po.option_value,oio.price_adjustment FROM order_item_options oio JOIN product_options po ON po.option_id=oio.option_id WHERE oio.order_item_id=?", new String[]{String.valueOf(itemId)}); }
    public Cursor notifications(long userId) { return getReadableDatabase().rawQuery("SELECT notification_id,title,message,created_at,is_read FROM notifications WHERE user_id=? ORDER BY notification_id DESC", new String[]{String.valueOf(userId)}); }
    public Cursor promotions() { return getReadableDatabase().rawQuery("SELECT title,description,discount FROM promotions ORDER BY promotion_id DESC", null); }

    public void updateProfile(long userId, String name, String phone) {
        ContentValues values = new ContentValues();
        values.put("full_name", name.trim());
        values.put("phone", phone.trim());
        getWritableDatabase().update("users", values, "user_id=?", new String[]{String.valueOf(userId)});
    }

    public long saveAddress(long userId, long addressId, String line, String city, String district, String postal) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("address_line", line.trim()); v.put("city", city.trim()); v.put("district", district.trim()); v.put("postal_code", postal.trim());
        if (addressId > 0) {
            return getWritableDatabase().update("addresses", v, "address_id=? AND user_id=?", new String[]{String.valueOf(addressId), String.valueOf(userId)}) == 1 ? addressId : -1;
        }
        return getWritableDatabase().insertOrThrow("addresses", null, v);
    }

    public void deleteAddress(long userId, long addressId) { getWritableDatabase().delete("addresses", "address_id=? AND user_id=?", new String[]{String.valueOf(addressId),String.valueOf(userId)}); }
    public long saveDesign(long userId, String name, String uri) {
        ContentValues v = new ContentValues(); v.put("user_id", userId); v.put("design_name", name); v.put("file_path", uri);
        return getWritableDatabase().insertOrThrow("saved_designs", null, v);
    }
    public void deleteDesign(long userId, long designId) { getWritableDatabase().delete("saved_designs", "design_id=? AND user_id=?", new String[]{String.valueOf(designId),String.valueOf(userId)}); }

    public static final class OrderLine {
        public long productId;
        public int quantity;
        public String customText;
        public String artworkUri;
        public final List<Long> optionIds = new ArrayList<>();
    }

    public long createOrder(long userId, String type, String address, String scheduledUtc, List<OrderLine> lines) {
        if (!userExists(userId) || lines == null || lines.isEmpty() || scheduledUtc == null || scheduledUtc.isEmpty()) throw new IllegalArgumentException("Incomplete order");
        if (!"Pickup".equals(type) && !"Home Delivery".equals(type)) throw new IllegalArgumentException("Invalid fulfillment type");
        if ("Home Delivery".equals(type) && (address == null || address.trim().isEmpty())) throw new IllegalArgumentException("Delivery address required");
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            BigDecimal total = Money.amount("0");
            List<BigDecimal> units = new ArrayList<>();
            List<List<BigDecimal>> adjustments = new ArrayList<>();
            for (OrderLine line : lines) {
                if (line.quantity < 1 || ((line.customText == null || line.customText.trim().isEmpty()) && (line.artworkUri == null || line.artworkUri.isEmpty()))) throw new IllegalArgumentException("Quantity and print content required");
                BigDecimal unit;
                try (Cursor p = db.rawQuery("SELECT base_price FROM products WHERE product_id=?", new String[]{String.valueOf(line.productId)})) {
                    if (!p.moveToFirst()) throw new IllegalArgumentException("Product unavailable");
                    unit = Money.amount(p.getString(0));
                }
                List<BigDecimal> optionPrices = new ArrayList<>();
                for (long optionId : line.optionIds) {
                    try (Cursor option = db.rawQuery("SELECT price_adjustment FROM product_options WHERE option_id=? AND product_id=?", new String[]{String.valueOf(optionId),String.valueOf(line.productId)})) {
                        if (!option.moveToFirst()) throw new IllegalArgumentException("Option does not belong to product");
                        BigDecimal adjustment = Money.amount(option.getString(0));
                        optionPrices.add(adjustment);
                        unit = unit.add(adjustment);
                    }
                }
                unit = unit.setScale(2, java.math.RoundingMode.HALF_UP);
                if (unit.signum() < 0) throw new IllegalArgumentException("Invalid unit price");
                units.add(unit); adjustments.add(optionPrices);
                total = total.add(unit.multiply(BigDecimal.valueOf(line.quantity)));
            }
            total = total.setScale(2, java.math.RoundingMode.HALF_UP);
            ContentValues order = new ContentValues();
            order.put("user_id", userId); order.put("order_date", nowUtc()); order.put("order_type", type);
            order.put("delivery_address", address); order.put("scheduled_for", scheduledUtc); order.put("total_amount", total.doubleValue()); order.put("status", PROCESSING);
            long orderId = db.insertOrThrow("orders", null, order);
            for (int i = 0; i < lines.size(); i++) {
                OrderLine line = lines.get(i);
                ContentValues item = new ContentValues();
                item.put("order_id", orderId); item.put("product_id", line.productId); item.put("quantity", line.quantity);
                item.put("custom_text", line.customText); item.put("artwork_path", line.artworkUri);
                item.put("unit_price", units.get(i).doubleValue()); item.put("subtotal", units.get(i).multiply(BigDecimal.valueOf(line.quantity)).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue());
                long itemId = db.insertOrThrow("order_items", null, item);
                for (int j = 0; j < line.optionIds.size(); j++) {
                    ContentValues selection = new ContentValues();
                    selection.put("order_item_id", itemId); selection.put("option_id", line.optionIds.get(j)); selection.put("price_adjustment", adjustments.get(i).get(j).doubleValue());
                    db.insertOrThrow("order_item_options", null, selection);
                }
            }
            notifyUser(db, userId, "Order placed", "Order #" + orderId + " is Processing.");
            db.setTransactionSuccessful();
            return orderId;
        } finally {
            db.endTransaction();
        }
    }

    public boolean cancel(long userId, long orderId) { return changeProcessingOrder(userId, orderId, null); }
    public boolean reschedule(long userId, long orderId, String newUtc) {
        if (newUtc == null || newUtc.isEmpty()) return false;
        return changeProcessingOrder(userId, orderId, newUtc);
    }

    private boolean changeProcessingOrder(long userId, long orderId, String newUtc) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor c = db.rawQuery("SELECT status FROM orders WHERE order_id=? AND user_id=?", new String[]{String.valueOf(orderId),String.valueOf(userId)})) {
                if (!c.moveToFirst() || !PROCESSING.equals(c.getString(0))) return false;
            }
            ContentValues values = new ContentValues();
            if (newUtc == null) values.put("status", "Cancelled");
            else { values.put("scheduled_for", newUtc); values.put("rescheduled_at", nowUtc()); }
            int updated = db.update("orders", values, "order_id=? AND user_id=? AND status=?", new String[]{String.valueOf(orderId),String.valueOf(userId),PROCESSING});
            if (updated != 1) return false;
            notifyUser(db, userId, newUtc == null ? "Order cancelled" : "Order rescheduled", "Order #" + orderId + (newUtc == null ? " was cancelled." : " has a new schedule."));
            db.setTransactionSuccessful();
            return true;
        } finally { db.endTransaction(); }
    }

    private static void notifyUser(SQLiteDatabase db, long userId, String title, String message) {
        ContentValues v = new ContentValues(); v.put("user_id", userId); v.put("title", title); v.put("message", message); v.put("created_at", nowUtc());
        db.insertOrThrow("notifications", null, v);
    }

    public static String nowUtc() {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        return iso.format(new Date());
    }
}
