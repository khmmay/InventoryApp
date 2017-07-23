package com.may.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.may.android.inventoryapp.data.InventoryContract;
import com.may.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int Product_Loader = 0;
    private static final int PICK_IMAGE_REQUEST = 0;
    private EditText mNameEditText;
    private EditText mQuantityText;
    private EditText mPackageText;
    private EditText mInfoText;
    private ImageView mImageView;
    private EditText mPrizeText;
    private Uri currentProductUri;

    private boolean mProductHasChanged = false;

    private int mPackageSize;
    private int mQuantity;




    private String imageUriString;//="android.resource://com.may.android.inventroyapp/drawable/no_image_available";
    private Uri imageUri;//=Uri.parse(imageUriString);

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView butDel = (TextView) findViewById(R.id.butDelete);
        TextView butSave = (TextView) findViewById(R.id.butSave);
        TextView butOrder = (TextView) findViewById(R.id.butReorder);

        mNameEditText = (EditText) findViewById(R.id.nameEdit);
        mQuantityText = (EditText) findViewById(R.id.quantity_text_view);
        mPackageText = (EditText) findViewById(R.id.pack_text_view);
        mInfoText = (EditText) findViewById(R.id.infoView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mPrizeText = (EditText) findViewById(R.id.prize);

        Button mPlusButton = (Button) findViewById(R.id.plusButton);
        Button mMinusButton = (Button) findViewById(R.id.minusButton);
        Button mPackPlusButton = (Button) findViewById(R.id.plusPackButton);
        Button mPackMinusButton = (Button) findViewById(R.id.minusPackButton);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityText.setOnTouchListener(mTouchListener);
        mPackageText.setOnTouchListener(mTouchListener);
        mPrizeText.setOnTouchListener(mTouchListener);
        mInfoText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mPackPlusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPackMinusButton.setOnTouchListener(mTouchListener);


        Intent intent = getIntent();
        currentProductUri = intent.getData();
        if (currentProductUri == null) {
            mPackageSize = 1;
            mQuantity = 0;
            setTitle(R.string.details_activity_title_new_product);
            butDel.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.details_activity_title_edit_product));
            getLoaderManager().initLoader(Product_Loader, null, this);
        }


        butDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
        butOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reorder();
            }
        });
        butSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (save()) {
                    finish();
                }
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }

                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                Bitmap bitmap = getBitmapFromUri(imageUri);
                if (!(bitmap == null)) {
                    mImageView.setImageBitmap(getBitmapFromUri(imageUri));
                } else {
                    mImageView.setImageResource(R.drawable.no_image_available);
                }
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementQ();
            }
        });
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementQ();
            }
        });
        mPackMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementP();
            }
        });
        mPackPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementP();
            }
        });
    }


    private void reorder() {

        String name = mNameEditText.getText().toString().trim();
        String subject = "Reorder of " + name;
        String body = "Dear Sirs or Madams,\n\nsince our supply of " + name + " is critically low (" + mQuantityText.getText().toString().trim()
                + " pieces), we would like to order the next charge. The package size we prefer is " + mPackageText.getText().toString().trim()
                + " per package. Thank you very much!\n\nYours sincerely,\nXYZ-shop";
        mProductHasChanged = true;
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "supplier@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        if (item.getItemId() == android.R.id.home) {
            if (!mProductHasChanged) {
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
            }

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(DetailActivity.this);
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean save() {
        String nameString = mNameEditText.getText().toString().trim();
        try {
            mPackageSize = Integer.parseInt(mPackageText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.wrong_pack_size, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        try {
            mQuantity = Integer.parseInt(mQuantityText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.wrong_quantiy, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        int mPrize;
        try {
            mPrize = Integer.parseInt(mPrizeText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid prize", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
        String infoString = mInfoText.getText().toString();
        String imgSrc = "";
        if (!TextUtils.isEmpty(imageUriString)) {
            imgSrc = imageUriString;
        }


        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryEntry.COLUMN_QUANT, mQuantity);
        values.put(InventoryEntry.COLUMN_PACK_SIZE, mPackageSize);
        values.put(InventoryEntry.COLUMN_INFO, infoString);
        values.put(InventoryEntry.COLUMN_IMG_SOURCE, imgSrc);
        values.put(InventoryEntry.COLUMN_PRICE, mPrize);

        if (currentProductUri != null) {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Uri urim = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (urim == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                imageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());

                imageUriString = imageUri.toString();
                Bitmap bitmap = getBitmapFromUri(imageUri);
                if (!(bitmap == null)) {
                    mImageView.setImageBitmap(bitmap);
                } else {
                    mImageView.setImageResource(R.drawable.no_image_available);
                }
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
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


    public void decrementP() {
        try {
            mPackageSize = Integer.parseInt(mPackageText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mPackageSize = 0;
            e.printStackTrace();
        }
        if (mPackageSize > 1) {
            mPackageSize = mPackageSize - 1;
        }
        mPackageText.setText(String.valueOf(mPackageSize));
    }

    public void incrementP() {
        try {
            mPackageSize = Integer.parseInt(mPackageText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mPackageSize = 0;
            e.printStackTrace();
        }
        mPackageSize = mPackageSize + 1;
        mPackageText.setText(String.valueOf(mPackageSize));
    }

    public void decrementQ() {
        try {
            mPackageSize = Integer.parseInt(mPackageText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mPackageSize = 0;
            e.printStackTrace();
        }
        try {
            mQuantity = Integer.parseInt(mQuantityText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mQuantity = 0;
            e.printStackTrace();
        }
        if (mQuantity >= mPackageSize) {
            mQuantity = mQuantity - mPackageSize;
        }
        mQuantityText.setText(String.valueOf(mQuantity));
    }

    public void incrementQ() {
        try {
            mPackageSize = Integer.parseInt(mPackageText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mPackageSize = 0;
            e.printStackTrace();
        }
        try {
            mQuantity = Integer.parseInt(mQuantityText.getText().toString().trim());
        } catch (NumberFormatException e) {
            mQuantity = 0;
            e.printStackTrace();
        }
        mQuantity = mQuantity + mPackageSize;
        mQuantityText.setText(String.valueOf(mQuantity));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_QUANT,
                InventoryEntry.COLUMN_PACK_SIZE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_INFO,
                InventoryEntry.COLUMN_IMG_SOURCE};

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
            int quantIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANT);
            int packIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PACK_SIZE);
            int infoColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INFO);
            int imSrcIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMG_SOURCE);
            int prizeIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);

            String currentName = cursor.getString(nameColumnIndex);
            int currentQuant = cursor.getInt(quantIndex);
            int currentPack = cursor.getInt(packIndex);
            String currentInfo = cursor.getString(infoColumnIndex);
            imageUriString = cursor.getString(imSrcIndex);
            int currentPrize = cursor.getInt(prizeIndex);

            mNameEditText.setText(currentName);
            mQuantityText.setText(String.valueOf(currentQuant));
            mPrizeText.setText(String.valueOf(currentPrize));
            mPackageText.setText(String.valueOf(currentPack));
            mInfoText.setText(currentInfo);

            imageUri = Uri.parse(imageUriString);
            Bitmap bitmap = getBitmapFromUri(imageUri);
            if (!(bitmap == null)) {
                mImageView.setImageBitmap(getBitmapFromUri(imageUri));
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityText.setText("0");
        mPackageText.setText("1");
        mInfoText.setText("");
        mPrizeText.setText("");
        mImageView.setImageResource(R.drawable.no_image_available);
        imageUri = null;
        imageUriString = "";
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsAffected = getContentResolver().delete(currentProductUri, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }
}
