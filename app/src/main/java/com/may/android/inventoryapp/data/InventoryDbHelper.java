package com.may.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.may.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Henrik on 22.07.2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {



    private static final String DATABASE_NAME="inventory.db";
    private static final int DATABASE_VERSION=1;

    public InventoryDbHelper(Context context){super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE="CREATE TABLE "+ InventoryEntry.TABLE_NAME+" ("
                + InventoryEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_NAME+" TEXT NOT NULL, "
                + InventoryEntry.COLUMN_QUANT+" INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_PACK_SIZE+" INTEGER NOT NULL DEFAULT 1, "
                + InventoryEntry.COLUMN_PRICE+" INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_INFO+" TEXT, "
                + InventoryEntry.COLUMN_IMG_SOURCE+" TEXT" +");";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothing to be done here.
    }
}
