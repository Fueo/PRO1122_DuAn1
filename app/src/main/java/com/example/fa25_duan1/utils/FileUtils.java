package com.example.fa25_duan1.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileUtils {

    /**
     * Lấy đường dẫn thực từ Uri
     * @param context Context
     * @param uri Uri của file (từ Gallery hoặc FilePicker)
     * @return đường dẫn thực trên bộ nhớ, hoặc null nếu không tìm thấy
     */
    public static String getPath(Context context, Uri uri) {
        if (uri == null) return null;

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
}