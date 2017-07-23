package com.may.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Henrik on 22.07.2017.
 */

public class InventoryContract {
    public static final String CONTENT_AUTHORITY = "com.may.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";


    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /*The MIME type of the URI for a list of products*/
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /*The MIME type of the URI for a single product*/
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /*Name of the table*/
        public static final String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_QUANT = "quantity";
        public final static String COLUMN_PACK_SIZE = "packageSize";
        public final static String COLUMN_INFO = "productInfo";
        public final static String COLUMN_IMG_SOURCE = "productImage";
        public final static String COLUMN_PRICE = "price";

    }


}
