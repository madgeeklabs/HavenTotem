package com.mgl.totem.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.mgl.totem.R;
import com.mgl.totem.base.TotemApplication;
import com.mgl.totem.interfaces.TotemApiInterface;
import com.mgl.totem.models.Registration;
import com.mgl.totem.utils.Constants;
import com.mgl.totem.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegisterActivity extends AppCompatActivity {

    @InjectView(R.id.done_finger)
    ImageView fingerRegistration;
    @InjectView(R.id.user_name)
    EditText userName;
    @InjectView(R.id.user_phone)
    EditText userPhone;
    @InjectView(R.id.record_video_register)
    ImageView recordVideo;

    @Inject
    TotemApiInterface api;

    private String mCurrentPhotoPath;
    private String imageFileName;
    private String TAG = RegisterActivity.class.getName();
    private TransferUtility transferUtility;
    private int REQUEST_TAKE_PHOTO = 33;
    private String mCurrentVideoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.inject(this);
        ((TotemApplication) getApplication()).inject(this);

        transferUtility = Utils.getTransferUtility(this);

        try {
            createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
               // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.d(TAG, "some horrible thing happened " + ex.getMessage());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        takePictureIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 4);
                        takePictureIntent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS, false);
                        takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        fingerRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.postUser(new Registration("12345", mCurrentVideoName, userName.getText().toString(), userPhone.getText().toString()), new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        Log.d(TAG, "SUCCESS: " + s);
                        finish();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(TAG, "ERROR: " + error.getMessage());
                    }
                });
            }
        });
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(String filePath) {

        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
                file);

        observer.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                //Do something on state change
                Log.d(TAG, "state changed " + state.name());
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                //Do something on progress change.
                Log.d(TAG, "progress changed");
            }

            @Override
            public void onError(int id, Exception ex) {
                //Do something on error
                Log.d(TAG, "error on transfer " + ex.getMessage());
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentVideoName = image.getName();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            beginUpload(mCurrentPhotoPath);
        }
    }
}
