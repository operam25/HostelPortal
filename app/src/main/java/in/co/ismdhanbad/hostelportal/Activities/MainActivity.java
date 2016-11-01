package in.co.ismdhanbad.hostelportal.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
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
    private Bitmap decodedByte;
    private int countRetry = 0;

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

        String urls = getResources().getString(R.string.base_url) + "notification.php";
        String[] names = {"page"};
        String[] values = {"0"};
        new HttpApiCall(MainActivity.this,urls,names,values,"notice");

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

    public void imageAlert(boolean flag1){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate( R.layout.content_pic, null );
        imageView1 = (CircleImageView) view.findViewById(R.id.profilePic1);
        String uri = preferences.getString("userImage","");
        imageButton = (ImageButton) view.findViewById(R.id.imageaddbtn1);
        Log.d("flag1",flag1+"");
        if(flag1 || decodedByte != null) {
            imageButton.setVisibility(View.GONE);
            imageView1.setImageBitmap(decodedByte);
        }else {
            imageButton.setVisibility(View.VISIBLE);
            imageView1.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
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
                    imageResize();
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
                    imageResize();
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

    public void uploadMultipart(Uri uri) {
        try {
            String uploadId = UUID.randomUUID().toString();

            String url = getResources().getString(R.string.base_url) + "saveimage.php" ;

            String path = getPath(uri);

            String imageUrl = getResources().getString(R.string.base_url)  + admnNumber;
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

    protected void imageResize() {

        try {
            InputStream input = this.getContentResolver().openInputStream(mFileuri);
            Bitmap bm = BitmapFactory.decodeStream(input);

            Bitmap resized = Bitmap.createScaledBitmap(bm, 800, 800, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 30, baos); //bm is the bitmap object
            Log.d("image size", baos.size() + "");
            byte[] byteArray = baos.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            encodedImages = encodedImage;
            drawImage(encodedImage);

        }
        catch(Exception e){
            Log.e("error pic ", e +"");
            Toast.makeText(MainActivity.this, "didn't get the picture", Toast.LENGTH_SHORT).show();
        }
    }

    protected void drawImage(String image){
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        String path = MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), decodedByte, "Title", null);
        imageView.setImageBitmap(decodedByte);
        imageView1.setImageBitmap(decodedByte);
        imageButton.setVisibility(View.GONE);
        uploadMultipart(Uri.parse(path));
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

    protected void setNotice(JSONArray msg) throws JSONException {
        JSONObject jsonObject = msg.getJSONObject(0);
        TextView tv = (TextView) findViewById(R.id.timeStampNotice1);
        final String time = jsonObject.getString("time");
        final String header = jsonObject.getString("header");
        final String details = jsonObject.getString("details");
        final String notifier = jsonObject.getString("notifier");
        if (tv != null) {
            tv.setText("Dated - " + time);
        }
        tv = (TextView) findViewById(R.id.headerNotice1);
        if (tv != null) {
            tv.setText(header);
        }
        tv = (TextView) findViewById(R.id.detailsNotice1);
        if (tv != null) {
            tv.setText(details);
        }
        tv = (TextView) findViewById(R.id.notifierNotice1);
        if (tv != null) {
            tv.setText("Posted by - " + notifier);
        }
        LinearLayout nll = (LinearLayout) findViewById(R.id.noticeLayout1);
        if (nll != null) {
            nll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(header);
                    builder.setMessage(details + "\n\nBy - " + notifier + "\nDated - " + time);
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
            });
        }
        jsonObject = msg.getJSONObject(1);
        final String time1 = jsonObject.getString("time");
        final String header1 = jsonObject.getString("header");
        final String details1 = jsonObject.getString("details");
        final String notifier1 = jsonObject.getString("notifier");
        tv = (TextView) findViewById(R.id.timeStampNotice2);
        if (tv != null) {
            tv.setText("Dated - " + time1);
        }
        tv = (TextView) findViewById(R.id.headerNotice2);
        if (tv != null) {
            tv.setText(header1);
        }
        tv = (TextView) findViewById(R.id.detailsNotice2);
        if (tv != null) {
            tv.setText(details1);
        }
        tv = (TextView) findViewById(R.id.notifierNotice2);
        if (tv != null) {
            tv.setText("Posted by - " + notifier1);
        }
        nll = (LinearLayout) findViewById(R.id.noticeLayout2);
        if (nll != null) {
            nll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(header1);
                    builder.setMessage(details1 + "\n\nBy - " + notifier1 + "\nDated - " + time1);
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
            });
        }
    }


    @Override
    public void webCallResponse(String response, String flag) {
        if(response!=null){
            Log.d("response",response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                switch (flag){
                    case "loadimage":
                        String status = jsonObject.getString("status");
                        if(status.toLowerCase().equals("success")){
                            String imageUrl = getResources().getString(R.string.base_url)  + jsonObject.getString("msg");
                            editor.putString("userImage",imageUrl);
                            editor.apply();
                            picassoloader(imageUrl);

                        }else {
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageAlert(false);
                                }
                            });
                        }
                        break;

                    case "notice":
                        status = jsonObject.getString("status");
                        if(status.toLowerCase().contains("success")){
                            JSONArray msg = jsonObject.getJSONArray("msg");
                            setNotice(msg);
                        }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    protected void picassoloader(final String imageUrl){
        Picasso.with(MainActivity.this)
                .load(imageUrl)
                .noFade()
                .placeholder(R.drawable.avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        decodedByte = bitmap;
                        if (imageView1 != null)
                            imageView1.setImageBitmap(decodedByte);
                        imageView.setImageBitmap(decodedByte);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageAlert(true);
                            }
                        });
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        if(countRetry > 3) {
                            Toast.makeText(MainActivity.this,"Failed to load the image. Retry manually by clicking on the icon.",Toast.LENGTH_SHORT).show();
                            countRetry = 0;
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageView.setOnClickListener(null);
                                    picassoloader(imageUrl);
                                }
                            });
                        }else {
                            countRetry++;
                            Toast.makeText(MainActivity.this, "Failed to load the image. Retrying...", Toast.LENGTH_SHORT).show();
                            picassoloader(imageUrl);
                        }
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

}
