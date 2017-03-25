package com.example.ark.myinventoryapp;


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
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ark.myinventoryapp.data.ProductContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ProductEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = ProductEditActivity.class.getSimpleName();


    private static final int EXISTING_PRODUCT_LOADER = 0;


    private static final int PICK_IMAGE_REQUEST = 0;
    private static final String STATE_URI = "STATE_URI";

    Button addQuantity, sellQuantity;
    int quantity = 0;
    String contact, imageString;
    int flag = 0;


    private Uri mCurrentProductUri;
    private EditText mInputName;
    private EditText mInputPrice;
    private EditText mInputQty;
    private EditText mInputCellnbr;
    private boolean mProdDetailsChanged = false;
    private ImageView mImageView;
    private TextView mTextView;
    private Button mUploadImage;

    private Uri mUri;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProdDetailsChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextView = (TextView) findViewById(R.id.image_uri);
        mImageView = (ImageView) findViewById(R.id.image);
        mUploadImage = (Button) findViewById(R.id.uploadButton);


        mUploadImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openImageSelector();
            }
        });

        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        });

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            Button orderMore = (Button) findViewById(R.id.order_more);
            orderMore.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mInputName = (EditText) findViewById(R.id.edit_product_name);
        mInputPrice = (EditText) findViewById(R.id.edit_product_price);
        mInputQty = (EditText) findViewById(R.id.edit_product_quantity);
        mInputCellnbr = (EditText) findViewById(R.id.edit_supplier_contact);
        addQuantity = (Button) findViewById(R.id.increment_quantity);
        sellQuantity = (Button) findViewById(R.id.decrement_quantity);


        mInputName.setOnTouchListener(mTouchListener);
        mInputPrice.setOnTouchListener(mTouchListener);
        mInputQty.setOnTouchListener(mTouchListener);
        mInputCellnbr.setOnTouchListener(mTouchListener);
        mUploadImage.setOnTouchListener(mTouchListener);
        addQuantity.setOnTouchListener(mTouchListener);
        sellQuantity.setOnTouchListener(mTouchListener);

        addQuantity.setEnabled(false);
        sellQuantity.setEnabled(false);

        mInputQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues();
            }
        });
    }

    private void checkFieldsForEmptyValues() {
        boolean isReady = mInputQty.getText().toString().length() > 0;
        addQuantity.setEnabled(isReady);
        sellQuantity.setEnabled(isReady);
    }

    public void addOneToQuantity(View view) {
        quantity = Integer.parseInt(mInputQty.getText().toString());
        quantity = quantity + 1;
        displayQuantity(quantity);
    }

    public void subOneFromQuantity(View view) {
        quantity = Integer.parseInt(mInputQty.getText().toString());
        quantity = quantity - 1;
        if (quantity < 0) {
            quantity = 0;
        }
        displayQuantity(quantity);
    }

    public void displayQuantity(int quantities) {
        EditText quantityView = (EditText) findViewById(R.id.edit_product_quantity);
        quantityView.setText(String.valueOf(quantities));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUri != null)
            outState.putString(STATE_URI, mUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mUri = Uri.parse(savedInstanceState.getString(STATE_URI));
            mTextView.setText(mUri.toString());
        }
    }

    public void openImageSelector() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mTextView.setText(mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

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

    private void saveProduct() {

        String nameString = mInputName.getText().toString().trim();
        String priceString = mInputPrice.getText().toString().trim();
        String quantityString = mInputQty.getText().toString().trim();
        String contactString = mInputCellnbr.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(contactString) && mUri == null) {
            flag = 1;
            return;
        }
        if ((TextUtils.isEmpty(contactString)) || (TextUtils.isEmpty(nameString)) || (TextUtils.isEmpty(priceString)) ||
                (TextUtils.isEmpty(quantityString))) {
            Toast.makeText(this, getString(R.string.prompt_for_details), Toast.LENGTH_SHORT).show();
            flag = 0;
            return;
        }

        if (mUri == null) {
            Uri uri = Uri.parse("android.resource://com.example.ark.myinventoryapp/drawable/default_image");
            imageString = uri.toString().trim();
        } else {
            imageString = mUri.toString().trim();
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NBR, contactString);


        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        flag = 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                if (flag == 1)
                    finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProdDetailsChanged) {
                    NavUtils.navigateUpFromSameTask(ProductEditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(ProductEditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!mProdDetailsChanged) {
            super.onBackPressed();
            return;
        }


        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NBR};

        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }


        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int contactColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_NBR);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            contact = cursor.getString(contactColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            mUri = Uri.parse(image);

            mInputName.setText(name);
            mInputPrice.setText(Integer.toString(price));
            mInputQty.setText(Integer.toString(quantity));
            mInputCellnbr.setText(contact);
            mImageView.setImageBitmap(getBitmapFromUri(mUri));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInputName.setText("");
        mInputPrice.setText("");
        mInputQty.setText("");
        mInputCellnbr.setText("");
        mImageView.setImageBitmap(null);
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

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

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteProduct(View view) {
        showDeleteConfirmationDialog();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteProduct() {
        if (mCurrentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_product_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    public void orderProduct(View view) {
        Intent contactIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact));
        startActivity(contactIntent);
    }
}