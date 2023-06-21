package org.pytorch.demo.objectdetection;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class ImageUtils {
    private static ResultView mResultView;
    public static void saveImageMetadata2(Context context, Bitmap bitmap, String title, String description,double latitude,double longitude) {
        String savedImagePath = null;
        String imageFileName = "IMG_" + new Date().getTime() + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DrBerry");
        boolean success = true;

        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // Guardar metadatos en la galería
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.DATA, savedImagePath);
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                // Escanear la imagen para que esté disponible en la galería
                MediaScannerConnection.scanFile(context, new String[]{savedImagePath}, null, null);

                ExifInterface exifInterface = new ExifInterface(savedImagePath);
                exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description);
                exifInterface.setAttribute(ExifInterface.TAG_USER_COMMENT, title);
                exifInterface.setAttribute(ExifInterface.TAG_ARTIST,savedImagePath);
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, formatLatLong(latitude));
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, formatLatLong(longitude));
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude < 0 ? "S" : "N");
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude < 0 ? "W" : "E");
                exifInterface.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static String formatLatLong(double value) {
        StringBuilder builder = new StringBuilder();
        builder.append((int) Math.abs(value));
        builder.append("/1,");
        value = (value % 1) * 60;
        builder.append((int) Math.abs(value));
        builder.append("/1,");
        value = (value % 1) * 60000;
        builder.append((int) Math.abs(value));
        builder.append("/1000");
        return builder.toString();
    }

}
