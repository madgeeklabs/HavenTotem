package com.mgl.totem.activities;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Credentials;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.mgl.totem.utils.ImageHelper;
import com.mgl.totem.utils.Utils;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final String TAG = MainActivity.class.getName();
    public static String EXTRA_USER_ID = "EXTRA_USER_ID";
    private TransferUtility transferUtility;
    private String mCurrentPhotoPath;
    private int tries = 0;
    private WebView mWebView;
    private LinkedList<PrintJob> mPrintJobs = new LinkedList<>();
    private String userUniqueId;
    private String mCurrentVideoName;

    @InjectView(R.id.collectMoney)
    Button collectMoney;
    @InjectView(R.id.userNameDisplay)
    TextView userName;
    @InjectView(R.id.userImage)
    ImageView userImage;
    @InjectView(R.id.phone_number)
    TextView userPhone;

    @Inject
    TotemApiInterface api;
    private Registration mUser;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        ((TotemApplication) getApplication()).inject(this);

        transferUtility = Utils.getTransferUtility(this);

        userUniqueId = getIntent().getStringExtra(EXTRA_USER_ID);

        api.getUserWithId(userUniqueId, new Callback<Registration>() {
            @Override
            public void success(Registration registration, Response response) {
                mUser = registration;
                // fill UI
                Log.d(TAG, "SUCCESS: " + mUser.getName());

                userName.setText(mUser.getName());
                userPhone.setText(mUser.getPhone());
                new getBitmap(Constants.S3_URL + mUser.getPicture()).execute();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "some error happened");
            }
        });

        collectMoney.setOnClickListener(new View.OnClickListener() {
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
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        takePictureIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 4);
                        takePictureIntent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS, false);
                        takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            doWebViewPrint();
            mUser.setVideo(mCurrentVideoName);

            api.postUser(mUser, new Callback<String>() {
                @Override
                public void success(String s, Response response) {
                    Log.d(TAG, "user updated");
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "some error while updateing user: " + error.getMessage());
                }
            });
        }
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
                public void onStateChanged(int id, TransferState state){
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
        String imageFileName = "VIDEO_" + timeStamp + "_";
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

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    private void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(MainActivity.this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body><h1>This is your voucher " + mUser.getName() + "</h1><p>You can go to an ATM, " +
                "and pick up your 20E.</p><img src='file:///android_asset/barcode.png'></body></html>";
        webView.loadDataWithBaseURL("file:///android_asset/images/", htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) MainActivity.this.getSystemService(Context.PRINT_SERVICE);

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

        // Save the job object for later status checking
        mPrintJobs.add(printJob);
    }

    private class getBitmap extends AsyncTask<Void, Void, Void> {

        private String uri = "";
        public Bitmap myBitmap;

        public getBitmap (String url) {
            this.uri = url;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = ImageHelper.cropImageCircular(BitmapFactory.decodeStream(input), MainActivity.this);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            userImage.setImageBitmap(myBitmap);
        }
    }
}
