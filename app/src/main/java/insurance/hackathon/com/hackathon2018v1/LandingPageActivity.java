package insurance.hackathon.com.hackathon2018v1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

//import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
//import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LandingPageActivity extends AppCompatActivity {

    ImageView ivCamera, ivGallery, ivUpload, ivImage;
    final int CAMERA_REQUEST = 13323;
    final int GALLERY_REQUEST = 22131;
    final int UPLOAD_REQUEST = 22132;
    private int STORAGE_PERMISSION_CODE = 1;
    String awsImageName;
    String selectedPhoto;
    BasicAWSCredentials credentials;
    private final String TAG = this.getClass().getName();
    CameraPhotoGallery cameraPhotoGallery;
    GalleryPhoto galleryPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        cameraPhotoGallery = new CameraPhotoGallery(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());
        ivCamera = (ImageView) findViewById(R.id.ivCamera);
        ivGallery = (ImageView) findViewById(R.id.ivGallery);
        ivUpload = (ImageView) findViewById(R.id.ivUpload);
        ivImage = (ImageView) findViewById(R.id.ivImage);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //AWS Initialization
        AWSMobileClient.getInstance().initialize(this).execute();

        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int writeExternalStoragePer = ContextCompat.checkSelfPermission(LandingPageActivity.this, Manifest.permission.CAMERA);
                    if (writeExternalStoragePer == PackageManager.PERMISSION_GRANTED) {
                        Log.d("", "permission granted");
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        startActivityForResult(cameraPhotoGallery.takePhotoIntent(), CAMERA_REQUEST);
                        cameraPhotoGallery.addToGallery();
                    } else {
                        requestStoragePermission();
                    }

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error while taking photo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);

            }
        });

        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(galleryPhoto.openGalleryIntent(), UPLOAD_REQUEST);
                try {
                    Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(1024, 1024).getBitmap();
                    String encodedImage = ImageBase64.encode(bitmap);
                    // KEY and SECRET are gotten when we create an IAM user above
                    credentials = new BasicAWSCredentials("AKIAJO5D55X6AN7OOLFA", "vSoGh+v7LtTo6cpuJ+HU3yStwB7HNug4jrGc+p/O");
                    AmazonS3Client s3Client = new AmazonS3Client(credentials);
                    int position = selectedPhoto.lastIndexOf("/");
                    awsImageName = selectedPhoto.substring(position);
                    TransferUtility transferUtility =
                            TransferUtility.builder()
                                    .context(getApplicationContext())
                                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                    .s3Client(s3Client)
                                    .build();

                    TransferObserver uploadObserver =
                            transferUtility.upload("aws-hackathon-bucket/Images" + awsImageName, new File(selectedPhoto));
                    Log.d(TAG, encodedImage);

                    // Attach a listener to the observer to get state update and progress notifications

                    uploadObserver.setTransferListener(new TransferListener() {

                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (TransferState.COMPLETED == state) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                Log.d("Image Name :", awsImageName);
                                Log.d("aws credentials : ", credentials.toString());
                                AmazonRekognition rekognitionClient = new AmazonRekognitionClient(credentials);
                                rekognitionClient.setRegion(Region.getRegion(Regions.US_EAST_1));

                                DetectTextRequest request = new DetectTextRequest()
                                        .withImage(new Image()
                                                .withS3Object(new S3Object()
                                                        .withName("aws-hackathon-bucket/Images"+awsImageName)
                                                        .withBucket("hackathonaws-userfiles-mobilehub-2075272740")));

                                DetectTextResult detectTextResult =  rekognitionClient.detectText(request);
                                String vinNumber = detectTextResult.getTextDetections().get(1).getDetectedText();
                                vinNumber = vinNumber.replace(".","");
                                Intent intent = new Intent(LandingPageActivity.this, PolicyDetailsActivity.class);
                                intent.putExtra("vinNumber",vinNumber);
                                startActivity(intent);
                                Log.d("", "Image to text result: "+ detectTextResult.toString()+" text: "+ vinNumber);
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                            int percentDone = (int) percentDonef;

                            Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                                    + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            // Handle errors
                        }

                    });

                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Error in encoding photo", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(this).setTitle("Permission needed")
                    .setMessage("Permission need to access gallery")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(LandingPageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                String photoPath = cameraPhotoGallery.getPhotoPath();
                selectedPhoto = photoPath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    ivImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Error while loading photo", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST) {
                Uri uri = data.getData();
                galleryPhoto.setPhotoUri(uri);
                String photoPath = galleryPhoto.getPath();
                selectedPhoto = photoPath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    ivImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Error while choosing photo", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}
