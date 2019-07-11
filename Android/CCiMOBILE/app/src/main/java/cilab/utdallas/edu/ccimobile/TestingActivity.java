package cilab.utdallas.edu.ccimobile;

import android.Manifest;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestingActivity extends AppCompatActivity {

    Button buttonSample;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing);

        buttonSample = findViewById(R.id.buttonSample);
    }

    /**
     * Download the sample MAP from assets folder on button click
     * @param view v
     */
    public void downloadSampleMAP(View view) {
        // Check if permission already exists
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If permission does not already exist
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.e("TestingActivity.java", "Permission DOES NOT ALREADY EXIST for " +
                    "WRITE_EXTERNAL_STORAGE.");
            // If user has previously denied permission, first explain why the permission is needed,
            // then make request
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access device files is required for this app " +
                        "to load your MAP file(s). Please allow the permission.")
                        .setTitle("Permission required");
                builder.setPositiveButton("OK", (dialog, id) -> {
                    Log.e("TestingActivity.java", "Alert dialogue clicked.");
                    makeRequest();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // If user has not previously denied permission, make request without explanation
                makeRequest();
            }
        } else {
            // If permission already exists, perform file search
            Log.e("TestingActivity.java", "Permission was already granted.");
            //verifyFolderExists();
            getSampleMAP();
        }
    }

    public void getSampleMAP() {
        String MAPfilename = "Sample MAP.txt";
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), MAPfilename);

        // File f = new File(getFilesDir(), "Sample MAP.txt");

        if (!f.exists()) {
            AssetManager assets = getAssets();

            try {
                copy(assets.open("Sample MAP.txt"), f);

                // Download the file to make it appear in the Downloads folder
                File dir = new File("//sdcard//Download//");
                File MAPfile = new File(dir, MAPfilename);
                DownloadManager downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);

                if (downloadManager != null) {
                    downloadManager.addCompletedDownload(MAPfile.getName(), MAPfile.getName(), true, "text/plain", MAPfile.getAbsolutePath(), f.length(), true);
                }

                Snackbar.make(findViewById(R.id.testingLayout), "Successfully downloaded sample MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            } catch (IOException e) {
                Snackbar.make(findViewById(R.id.testingLayout), "Error: Unable to download MAP.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

        } else {
            Snackbar.make(findViewById(R.id.testingLayout), "Sample MAP is already downloaded.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    /**
     * Prompts the user for permission.
     */
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Receives result from requesting permission (for file access).
     * @param requestCode request code
     * @param permissions permission
     * @param grantResults result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                Log.e("MainActivity.java", "Permission GRANTED for " +
                        "WRITE_EXTERNAL_STORAGE.");
                //verifyFolderExists();
                getSampleMAP();
            } else {
                // permission denied
                Log.e("MainActivity.java", "Permission DENIED for " +
                        "WRITE_EXTERNAL_STORAGE.");
            }
        }
    }

    /**
     * Copies the file
     * @param in i
     * @param dst d
     * @throws IOException e
     */
    static private void copy(InputStream in, File dst) throws IOException {
        FileOutputStream out = new FileOutputStream(dst);
        byte[] buf=new byte[1024];
        int len;

        while ((len=in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

}