package cilab.utdallas.edu.ccimobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Provides static helper functions used by multiple activities
 */
class SharedHelper {
    /**
     * Prompts the user for permission.
     */
    private static void makeRequest(Activity activity, int REQUEST_CODE) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE);
    }

    /**
     * Checks for external storage permission
     * @param context c
     * @param activity a
     * @param REQUEST_CODE rc
     * @return true if permission granted
     */
    static boolean getPermission(Context context, Activity activity, int REQUEST_CODE) {
        // Check if permission already exists
        int permission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // If user has previously denied permission, first explain why the permission is needed,
            // then make request
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Permission to access device files is required for this app " +
                        "to load your MAP file(s). Please allow the permission.")
                        .setTitle("Permission required");
                builder.setPositiveButton("OK", (dialog, id) -> makeRequest(activity, REQUEST_CODE));
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // If user has not previously denied permission, make request without explanation
                makeRequest(activity, REQUEST_CODE);
            }
            return false;
        }
    }

    static double getDouble(final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
    }

    static boolean isInteger(View view, String s) {
        boolean result = isInteger(s, 10);
        if (!result) {
            Snackbar.make(view, "Please enter an integer.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
        return result;
    }

    static boolean isInteger(String s, int radix) {
        if (s.isEmpty())
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    static boolean isIntInRange(View view, int number, int upperbound, int lowerbound) {
        if ((number >= lowerbound) && (number <= upperbound)) {
            return true;
        } else {
            Snackbar.make(view, "Value is out bounds.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return false;
        }
    }

    static boolean isDoubleInRange(View view, double number, int upperbound, int lowerbound) {
        if ((number >= lowerbound) && (number <= upperbound)) {
            return true;
        } else {
            Snackbar.make(view, "Value is out bounds.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return false;
        }
    }

    static void putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    static String readExternalFile(String fileName) throws IOException {
        if (isExternalStorageReadable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        }
        return null;
    }

    private static boolean isExternalStorageReadable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }
}
