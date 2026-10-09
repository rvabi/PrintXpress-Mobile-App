package com.printxpress.app.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private static final String TEST_DATABASE = "PrintXpressTestDB";
    private Context context;
    private DatabaseHelper db;

    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        db = new DatabaseHelper(context, TEST_DATABASE);
    }

    @After public void tearDown() {
        if (db != null) db.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    private int count(String table) {
        try (Cursor c = db.getReadableDatabase().rawQuery("SELECT count(*) FROM " + table, null)) {
            c.moveToFirst(); return c.getInt(0);
        }
    }

    private long register() {
        return db.register("Test Customer", "test@example.com", "0771234567", "Secure123".toCharArray());
    }

    private DatabaseHelper.OrderLine line(long optionId) {
        DatabaseHelper.OrderLine item = new DatabaseHelper.OrderLine();
        item.productId = 1; item.quantity = 2; item.customText = "Sample print";
        item.optionIds.add(optionId); return item;
    }

    @Test public void registrationUsesHashAndSeedsOnlyOnce() {
        assertEquals(7, count(DatabaseContract.CATEGORIES));
        assertEquals(7, count(DatabaseContract.PRODUCTS));
        assertEquals(34, count(DatabaseContract.PRODUCT_OPTIONS));
        long userId = register();
        assertTrue(userId > 0);
        assertEquals(userId, db.authenticate("TEST@example.com", "Secure123".toCharArray()));
        assertEquals(-1, db.authenticate("test@example.com", "Wrong123".toCharArray()));
        assertEquals(-1, db.register("Another", "TEST@example.com", "0771234567", "Secure123".toCharArray()));
        try (Cursor c = db.getReadableDatabase().rawQuery("SELECT password FROM users WHERE user_id=?", new String[]{String.valueOf(userId)})) {
            assertTrue(c.moveToFirst());
            assertTrue(c.getString(0).startsWith(Build.VERSION.SDK_INT >= 26 ? "pbkdf2_sha256$" : "pbkdf2_sha1$"));
            assertFalse(c.getString(0).contains("Secure123"));
        }
        db.close(); db = new DatabaseHelper(context, TEST_DATABASE);
        assertEquals(7, count(DatabaseContract.CATEGORIES));
        assertEquals(1, count(DatabaseContract.USERS));
    }

    @Test public void invalidOptionRollsBackEntireOrder() {
        long userId = register();
        try {
            db.createOrder(userId, "Pickup", "", "2026-12-01T10:00:00Z", Collections.singletonList(line(7)));
            fail("Option 7 belongs to a different product");
        } catch (IllegalArgumentException expected) {
            assertEquals(0, count(DatabaseContract.ORDERS));
            assertEquals(0, count(DatabaseContract.ORDER_ITEMS));
            assertEquals(0, count(DatabaseContract.ORDER_ITEM_OPTIONS));
            assertEquals(0, count(DatabaseContract.NOTIFICATIONS));
        }
        long orderId = db.createOrder(userId, "Pickup", "", "2026-12-01T10:00:00Z", Collections.singletonList(line(2)));
        assertTrue(orderId > 0);
        assertEquals(1, count(DatabaseContract.ORDERS));
        assertEquals(1, count(DatabaseContract.ORDER_ITEMS));
        assertEquals(1, count(DatabaseContract.ORDER_ITEM_OPTIONS));
        assertEquals(1, count(DatabaseContract.NOTIFICATIONS));
    }

    @Test public void statusIsRecheckedForCancelAndReschedule() {
        long userId = register();
        long orderId = db.createOrder(userId, "Pickup", "", "2026-12-01T10:00:00Z", Collections.singletonList(line(2)));
        assertTrue(db.reschedule(userId, orderId, "2026-12-02T10:00:00Z"));
        try (Cursor c = db.order(userId, orderId)) {
            assertTrue(c.moveToFirst());
            assertEquals("2026-12-02T10:00:00Z", c.getString(4));
            assertTrue(c.getString(5) != null);
        }
        assertTrue(db.cancel(userId, orderId));
        assertFalse(db.cancel(userId, orderId));
        assertFalse(db.reschedule(userId, orderId, "2026-12-03T10:00:00Z"));
        try (Cursor c = db.order(userId, orderId)) {
            assertTrue(c.moveToFirst());
            assertEquals("Cancelled", c.getString(7));
            assertEquals("2026-12-02T10:00:00Z", c.getString(4));
        }
    }

    @Test public void everyNonProcessingStatusRejectsCustomerChanges() {
        long userId = register();
        long orderId = db.createOrder(userId, "Pickup", "", "2026-12-01T10:00:00Z", Collections.singletonList(line(2)));
        String[] locked = {"Printing", "Ready for Pickup", "Out for Delivery", "Completed", "Cancelled"};
        for (String status : locked) {
            db.getWritableDatabase().execSQL("UPDATE orders SET status=? WHERE order_id=?", new Object[]{status, orderId});
            assertFalse(db.cancel(userId, orderId));
            assertFalse(db.reschedule(userId, orderId, "2026-12-03T10:00:00Z"));
            try (Cursor c = db.order(userId, orderId)) {
                assertTrue(c.moveToFirst());
                assertEquals(status, c.getString(7));
                assertEquals("2026-12-01T10:00:00Z", c.getString(4));
                assertTrue(c.isNull(5));
            }
        }
        assertEquals(1, count(DatabaseContract.NOTIFICATIONS));
    }
}
