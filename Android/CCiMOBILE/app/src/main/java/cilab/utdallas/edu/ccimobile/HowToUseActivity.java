package cilab.utdallas.edu.ccimobile;

import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static cilab.utdallas.edu.ccimobile.SharedHelper.getPermission;

public class HowToUseActivity extends AppCompatActivity {

    Button buttonSample;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);

        buttonSample = findViewById(R.id.buttonSample);
    }

    /**
     * Download the sample MAP from assets folder on button click
     * @param view v
     */
    public void downloadSampleMAP(View view) {
        if (getPermission(this,this,MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)) {
            getSampleMAP();
        }
    }

    public void getSampleMAP() {
        String MAPfilename = "Sample MAP.txt";
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), MAPfilename);
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
                Log.e("HowToUseActivity.java", "Permission GRANTED for " +
                        "WRITE_EXTERNAL_STORAGE.");
                //verifyFolderExists();
                getSampleMAP();
            } else {
                // permission denied
                Log.e("HowToUseActivity.java", "Permission DENIED for " +
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