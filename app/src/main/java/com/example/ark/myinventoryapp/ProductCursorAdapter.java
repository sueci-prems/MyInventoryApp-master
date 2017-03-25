package com.example.ark.myinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ark.myinventoryapp.data.ProductContract;

public class ProductCursorAdapter extends CursorAdapter {

    Cursor mCursor;
    Context mContext;

    int rowsAffected;
    int newQuantity;


    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int itemIdIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        final int itemId = cursor.getInt(itemIdIndex);

        String productName = cursor.getString(nameColumnIndex);
        Integer productPrice = cursor.getInt(priceColumnIndex);
        Integer productQuantity = cursor.getInt(quantityColumnIndex);


        if (TextUtils.isEmpty(productName)) {
            productName = context.getString(R.string.unknown_product);
        }

        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        quantityTextView.setText(String.valueOf(productQuantity));

        Button sellButton = (Button) view.findViewById(R.id.button_sell);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowsAffected = saleOfAProduct(itemId, quantityTextView);
                if (rowsAffected != 0) {
                    quantityTextView.setText(String.valueOf(newQuantity));
                } else {
                    Toast.makeText(context, "Update not possible", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public int saleOfAProduct(int rowId, TextView qTextView) {
        mCursor.moveToPosition(rowId);
        int oldQuantity = Integer.parseInt(qTextView.getText().toString());

        if (oldQuantity > 0) {
            newQuantity = oldQuantity - 1;

            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, String.valueOf(newQuantity));
            Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, rowId);

            rowsAffected = mContext.getContentResolver().update(currentProductUri, values, null, null);
        }
        return rowsAffected;
    }
}
