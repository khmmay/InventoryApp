package com.may.android.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.may.android.inventoryapp.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Henrik on 23.07.2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();
    public InventoryCursorAdapter(Context context, Cursor c){super(context,c,0);}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView mImageView = (ImageView) view.findViewById(R.id.image);
        TextView mNameView = (TextView) view.findViewById(R.id.name);
        TextView mInfoView = (TextView) view.findViewById(R.id.summary);
        final TextView mStockView = (TextView) view.findViewById(R.id.amountInStock);
        TextView mSellButton = (TextView) view.findViewById(R.id.butSell);

        final int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID));
        String imgSrcString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_IMG_SOURCE));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_NAME));
        String info = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INFO));
        final int inStock = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_QUANT));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRICE));

        String priceString="Sell for\n"+String.valueOf(price/100)+"."+String.valueOf(price%100)+"€";
        mSellButton.setText(priceString);

        mNameView.setText(name);
        if (!TextUtils.isEmpty(info)){
            mInfoView.setText(info);
        }else{
            mInfoView.setText(R.string.no_info);
        }
        mStockView.setText(String.valueOf(inStock));
        if (!imgSrcString.isEmpty()){
            Uri imgUri=Uri.parse(imgSrcString);
            Bitmap bitmap=getBitmapFromUri(imgUri,context,mImageView);
            if (!(bitmap==null)){
                mImageView.setImageBitmap(bitmap);
            }else{
                mImageView.setImageResource(R.drawable.no_image_available);
            }
        }else{
            mImageView.setImageResource(R.drawable.no_image_available);
        }

        //Sell Button logic
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentInStock = Integer.parseInt(mStockView.getText().toString());
                if (currentInStock>=1){
                    currentInStock=currentInStock-1;
                }
                mStockView.setText(String.valueOf(currentInStock));
                ContentValues values =new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_QUANT,currentInStock);
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                int rowsAffected = context.getContentResolver().update(currentItemUri,values,null,null);
                if (rowsAffected==0){
                    Toast.makeText(context.getApplicationContext(),"Error with sale",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    public Bitmap getBitmapFromUri(Uri uri, Context context, ImageView mImageView) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            if (targetW==0||targetH==0){ //TODO:Fix
                targetH=142;
                targetW=142;
            }
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


}
