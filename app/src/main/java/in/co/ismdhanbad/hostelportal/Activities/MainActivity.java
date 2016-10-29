package in.co.ismdhanbad.hostelportal.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import in.co.ismdhanbad.hostelportal.R;

/**
 * Created by khandelwal on 18/08/16.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HttpApiCall.CallResponseListener {

    private boolean doubleBackToExitPressedOnce = false;
    private TextView tv;
    private LinearLayout ll;
    private NavigationView navigationView;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean isLoggedIn = false;
    private View header;
    private CircleImageView imageView;
    private String admnNumber;
    boolean image = false;
    String encodedImages;
    int thisImage;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int CLICK_PHOTO_REQUEST = 3;
    public static final int MEDIA_TYPE_IMAGE = 5;
    ProgressDialog progress;
    Uri mFileuri;
    private ImageButton imageButton;
    private CircleImageView imageView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        preferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = preferences.edit();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        isLoggedIn = preferences.getBoolean("isLoggedIn",false);

        if(!isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivityForResult(intent, 1);
        }else {
            header = navigationView.getHeaderView(0);
            TextView navHeaderTitle = (TextView) header.findViewById(R.id.navName);
            navHeaderTitle.setText(preferences.getString("name",""));
            TextView navHeaderCity = (TextView) header.findViewById(R.id.navAdmn);
            admnNumber = preferences.getString("admissionNumber","");
            navHeaderCity.setText(preferences.getString("admissionNumber",""));
            imageView = (CircleImageView) header.findViewById(R.id.profilePic);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageAlert();
                }
            });
            String[] name = {"admissionnumber"};
            String[] value = {admnNumber};
            String url = getResources().getString(R.string.base_url) + "loadimage.php";
            new HttpApiCall(MainActivity.this,url,name,value,"loadimage");
        }


        tv = (TextView) findViewById(R.id.seeMoreNotices);
        if(tv!=null){
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,NotificationsList.class);
                    startActivity(intent);
                }
            });
        }

        ll = (LinearLayout) findViewById(R.id.previousComplainsLayout);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ComplainList.class);
                startActivity(intent);
            }
        });

        ll = (LinearLayout) findViewById(R.id.registerComplainLayout);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RegisterComplain.class);
                startActivity(intent);
            }
        });

        ll = (LinearLayout) findViewById(R.id.contactsLayout);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Contact.class);
                startActivity(intent);
            }
        });

    }

    public void imageAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate( R.layout.content_pic, null );
        imageView1 = (CircleImageView) view.findViewById(R.id.profilePic1);
        String uri = preferences.getString("userImage","");
        imageButton = (ImageButton) view.findViewById(R.id.imageaddbtn1);
        if(uri != "") {
            imageButton.setVisibility(View.GONE);
            Picasso.with(MainActivity.this)
                    .load(uri)
                    .noFade()
                    .placeholder(R.drawable.avatar)
                    .into(imageView1);
        }else {
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addImage();
                }
            });
        }
        builder.setTitle("Profile Pic");
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_NEGATIVE)
                            dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void addImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(R.array.camera_choices, mDialogListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == 4) {
                    this.finish();
                }else if(resultCode == 1){
                    if (navigationView != null) {
                        navigationView.setNavigationItemSelectedListener(this);
                        header = navigationView.getHeaderView(0);
                        TextView navHeaderTitle = (TextView) header.findViewById(R.id.navName);
                        navHeaderTitle.setText(preferences.getString("name",""));
                        TextView navHeaderCity = (TextView) header.findViewById(R.id.navAdmn);
                        admnNumber = preferences.getString("admissionNumber","");
                        navHeaderCity.setText(admnNumber);
                        imageView = (CircleImageView) header.findViewById(R.id.profilePic);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageAlert();
                            }
                        });
                        String[] name = {"admissionnumber"};
                        String[] value = {admnNumber};
                        String url = getResources().getString(R.string.base_url) + "loadimage.php";
                        new HttpApiCall(MainActivity.this,url,name,value,"loadimage");
                    }
                }
                break;
            case CLICK_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    String deviceName = Build.DEVICE;
                    uploadMultipart(mFileuri);
                } else if (resultCode == RESULT_CANCELED) {
                    String deviceName = Build.DEVICE;
                    // user cancelled Image capture
                    Toast.makeText(MainActivity.this,
                            "User cancelled image capture", Toast.LENGTH_SHORT)
                            .show();

                } else {
                    // failed to capture image
                    Toast.makeText(MainActivity.this,
                            "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            case PICK_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    mFileuri = data.getData();
                    uploadMultipart();
                    String deviceName = Build.DEVICE;
                } else if (resultCode == RESULT_CANCELED) {
                    String deviceName = Build.DEVICE;
                    // user cancelled Image capture
                    Toast.makeText(MainActivity.this,
                            "User cancelled image selection", Toast.LENGTH_SHORT)
                            .show();

                } else {
                    // failed to capture image
                    Toast.makeText(MainActivity.this,
                            "Sorry! Wrong type of file selected", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    public void uploadMultipart(Uri fileUri) {
        try {
            String uploadId = UUID.randomUUID().toString();

            String url = getResources().getString(R.string.base_url) + "saveimage.php" ;

            String path = fileUri.getPath();

            imageView1.setImageURI(mFileuri);
            imageView.setImageURI(mFileuri);
            imageButton.setVisibility(View.GONE);

            String imageUrl = getResources().getString(R.string.base_url) + "imagebase/" + admnNumber;
            editor.putString("userImage",imageUrl);
            editor.apply();

            Log.d("path",path);

            new MultipartUploadRequest(this, uploadId, url)
                    .addFileToUpload(path,"image") //Adding file
                    .addParameter("name",admnNumber) //Adding text parameter to the request
                    .addParameter("username",getResources().getString(R.string.userName))
                    .addParameter("apikey",getResources().getString(R.string.apiKey))
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadMultipart() {
        try {
            String uploadId = UUID.randomUUID().toString();

            String url = getResources().getString(R.string.base_url) + "saveimage.php" ;

            String path = getPath(mFileuri);

            imageView1.setImageURI(mFileuri);
            imageView.setImageURI(mFileuri);
            imageButton.setVisibility(View.GONE);

            String imageUrl = getResources().getString(R.string.base_url) + "imagebase/" + admnNumber;
            editor.putString("userImage",imageUrl);
            editor.apply();

            Log.d("path",path);

            new MultipartUploadRequest(this, uploadId, url)
                    .addFileToUpload(path,"image") //Adding file
                    .addParameter("name",admnNumber) //Adding text parameter to the request
                    .addParameter("username",getResources().getString(R.string.userName))
                    .addParameter("apikey",getResources().getString(R.string.apiKey))
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {

                        case 0: // Choose picture
                            Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            choosePhotoIntent.setType("image/*");
                            startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                            break;
                        case 1: // Click picture
                            mFileuri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileuri);
                            startActivityForResult(intent, CLICK_PHOTO_REQUEST);
                            break;
                    }
                }
            };

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "zwImage");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("DeclarationActivity", "Oops! Failed create "
                        + "zwImage" + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click back again to Exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            editor.clear();
            editor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }


    @Override
    public void webCallResponse(String response, String flag) {
        if(response!=null){
            Log.d("response",response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.getString("status");
                if(status.toLowerCase().contains("success")){
                    String imageUrl = getResources().getString(R.string.base_url) + "imagebase/" + jsonObject.getString("msg");
                    editor.putString("userImage",imageUrl);
                    editor.apply();
                    Picasso.with(MainActivity.this)
                            .load(imageUrl)
                            .noFade()
                            .placeholder(R.drawable.avatar)
                            .into(imageView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
