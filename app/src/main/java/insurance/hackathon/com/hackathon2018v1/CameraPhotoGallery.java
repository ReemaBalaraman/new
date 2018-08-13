package insurance.hackathon.com.hackathon2018v1;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.ExternalStorageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class CameraPhotoGallery {

    final String TAG = this.getClass().getSimpleName();

    private String photoPath;

    public String getPhotoPath() {
        return photoPath;
    }

    private Context context;
    public CameraPhotoGallery(Context context){
        this.context = context;
    }

    public Intent takePhotoIntent() throws IOException {
        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (in.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                in.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
        }
        return in;
    }

    private File createImageFile() throws IOException {
        //checking app permission
        int writeExternalStoragePer = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
       if(writeExternalStoragePer != PackageManager.PERMISSION_GRANTED){
           Log.d("","no per granted");
           /*ActivityCompat.requestPermissions(context.getApplicationContext();,new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1332);*/
       }
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        return image;
    }

    public void addToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(f)));
    }

}
