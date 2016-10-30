package in.co.ismdhanbad.hostelportal.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import in.co.ismdhanbad.hostelportal.R;

/**
 * Created by khandelwal on 20/08/16.
 */
public class RegisterComplain extends AppCompatActivity
        implements HttpApiCall.CallResponseListener {

    private boolean isAnonymous = false;
    private CheckBox cb;
    private Button btn;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;
    private ImageButton imagebtn1;
    private ImageButton imagebtn2;
    private ImageButton imagebtn3;
    private ImageButton imagebtn4;
    boolean[] images = {false,false,false,false};
    String[] encodedImages;
    private Bitmap[] decByte = {null,null,null,null};
    int thisImage;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int CLICK_PHOTO_REQUEST = 3;
    boolean doubleBackToExitPressedOnce = false;
    public static final int MEDIA_TYPE_IMAGE = 5;
    ProgressDialog progress;
    Uri mFileuri;
    int count = 0;
    private Uri[] uris = {null,null,null,null};
    private Button btn1;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String admnNumber;
    private EditText editText;
    private Spinner spinner;
    private ProgressDialog progressDialog;
    private String header;
    private String details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_complain);
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

        preferences = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = preferences.edit();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);

        admnNumber = preferences.getString("admissionNumber","");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[2];
            permissions[0] = Manifest.permission.CAMERA;
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissions[1] =  Manifest.permission.WRITE_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(RegisterComplain.this, permissions, 0);

        }
        else{
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

                ActivityCompat.requestPermissions(RegisterComplain.this, permissions, 0);
            }
        }

        cb = (CheckBox) findViewById(R.id.anonymousCheck);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAnonymous = isChecked;
            }
        });


        editText = (EditText) findViewById(R.id.detailsComplain);
        spinner = (Spinner) findViewById(R.id.spinnerComplain);

        btn = (Button) findViewById(R.id.complainBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                header = spinner.getSelectedItem().toString();
                details = editText.getText().toString();
                if(spinner.getSelectedItemId() > 0){
                    if(details.length()>0)
                        createAlert();
                    else
                        editText.setError("");
                }else{
                    Toast.makeText(RegisterComplain.this,"Select type of complain",Toast.LENGTH_SHORT).show();
                }
            }
        });

        encodedImages = new String[4];
    }

    protected void createAlertUpload(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterComplain.this);
        View view = getLayoutInflater().inflate( R.layout.content_image, null );
        image1 = (ImageView) view.findViewById(R.id.image1);
        image2 = (ImageView) view.findViewById(R.id.image2);
        image3 = (ImageView) view.findViewById(R.id.image3);
        image4 = (ImageView) view.findViewById(R.id.image4);
        imagebtn1 = (ImageButton) view.findViewById(R.id.imageaddbtn1);
        imagebtn2 = (ImageButton) view.findViewById(R.id.imageaddbtn2);
        imagebtn3 = (ImageButton) view.findViewById(R.id.imageaddbtn3);
        imagebtn4 = (ImageButton) view.findViewById(R.id.imageaddbtn4);

        final RotateAnimation anim = new RotateAnimation(0, 45,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(0);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        final RotateAnimation animrev = new RotateAnimation(45, 0,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(0);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);

        if(decByte[0]!=null){
            image1.setImageBitmap(decByte[0]);
            imagebtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
        }
        if(decByte[1]!=null){
            image2.setImageBitmap(decByte[1]);
            imagebtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
        }
        if(decByte[2]!=null){
            image3.setImageBitmap(decByte[2]);
            imagebtn3.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
        }
        if(decByte[3]!=null){
            image4.setImageBitmap(decByte[3]);
            imagebtn4.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
        }

        imagebtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(images[0]){
                    images[0] = false;
                    removeImage(0);
                    imagebtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_imageaddbtn));
                }
                else{
                    addImage();
                    thisImage = 0;
                }
            }
        });

        imagebtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(images[1]){
                    images[1] = false;
                    removeImage(1);
                    imagebtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_imageaddbtn));
                }
                else{
                    addImage();
                    thisImage = 1;
                }
            }
        });

        imagebtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(images[2]){
                    images[2] = false;
                    removeImage(2);
                    imagebtn3.setImageDrawable(getResources().getDrawable(R.drawable.ic_imageaddbtn));
                }
                else{
                    addImage();
                    thisImage = 2;
                }
            }
        });

        imagebtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(images[3]){
                    images[3] = false;
                    removeImage(3);
                    imagebtn4.setImageDrawable(getResources().getDrawable(R.drawable.ic_imageaddbtn));
                }
                else{
                    addImage();
                    thisImage = 3;
                }
            }
        });
        builder.setTitle("Upload Photo");
        builder.setView(view);
        builder.setCancelable(true).setPositiveButton("Upload",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setNegativeButton("Not Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_NEGATIVE)
                            finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void addImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterComplain.this);
        builder.setItems(R.array.camera_choices, mDialogListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void removeImage(int no){

        encodedImages[no] = null;
        switch(no){
            case 0:
                image1.setImageDrawable(null);
                break;
            case 1:
                image2.setImageDrawable(null);
                break;
            case 2:
                image3.setImageDrawable(null);
                break;
            case 3:
                image4.setImageDrawable(null);
                break;
        }
        images[no] = false;
        count--;
    }

    public void uploadMultipart(Uri uri) {
        try {
            String uploadId = UUID.randomUUID().toString();

            String url = getResources().getString(R.string.base_url) + "registercomplain.php" ;

            String path = getPath(uri);

            String imageUrl = getResources().getString(R.string.base_url) + "complainimage/" + admnNumber;
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

    protected void createAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterComplain.this);
        builder.setTitle("Confirmation");
        if(isAnonymous)
        builder.setMessage("Are you sure you want to complain anonymously?");
        else
        builder.setMessage("Are you sure you want to complain with your full details?");
        builder.setCancelable(true).setPositiveButton("Submit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.show();
                        String url = getResources().getString(R.string.base_url) + "registercomplain.php";
                        String[] name = {"admissionnumber","isanonymous","header","details"};
                        String[] value = {admnNumber,""+isAnonymous,header,details};
                        new HttpApiCall(RegisterComplain.this,url,name,value,"register");
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("Not Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == Dialog.BUTTON_NEGATIVE)
                            dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    protected void createSuccessAlert(String complainId){
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterComplain.this);
        builder.setTitle("Success");
        builder.setMessage("Your complain has been registered successfully with complain ID - " + complainId + ".");
        builder.setCancelable(true).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        createAlertUpload();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CLICK_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    String deviceName = Build.DEVICE;
                    /*Intent intent = new Intent(this, OrderPlaced.class);
                startActivity(intent);*/
                    count++;
                    imageResize(mFileuri);
                } else if (resultCode == RESULT_CANCELED) {
                    String deviceName = Build.DEVICE;
                    // user cancelled Image capture
                    Toast.makeText(RegisterComplain.this,
                            "User cancelled image capture", Toast.LENGTH_SHORT)
                            .show();

                } else {
                    // failed to capture image
                    Toast.makeText(RegisterComplain.this,
                            "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            case PICK_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    count++;
                    mFileuri = data.getData();
                    imageResize(mFileuri);
                    String deviceName = Build.DEVICE;
                } else if (resultCode == RESULT_CANCELED) {
                    String deviceName = Build.DEVICE;
                    // user cancelled Image capture
                    Toast.makeText(RegisterComplain.this,
                            "User cancelled image selection", Toast.LENGTH_SHORT)
                            .show();

                } else {
                    // failed to capture image
                    Toast.makeText(RegisterComplain.this,
                            "Sorry! Wrong type of file selected", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    protected void imageResize(Uri fileUri) {

        try {
            InputStream input = this.getContentResolver().openInputStream(fileUri);
            Bitmap bm = BitmapFactory.decodeStream(input);

            Bitmap resized = Bitmap.createScaledBitmap(bm, 800, 800, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 30, baos); //bm is the bitmap object
            Log.d("image size", baos.size() + "");
            byte[] byteArray = baos.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            encodedImages[thisImage] = encodedImage;
            drawImage(encodedImage);

        }
        catch(Exception e){
            Log.e("error pic ", e +"");
            Toast.makeText(RegisterComplain.this, "didn't get the picture", Toast.LENGTH_SHORT).show();
        }
    }

    protected void drawImage(String image){
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        String path = MediaStore.Images.Media.insertImage(RegisterComplain.this.getContentResolver(), decodedByte, "Title", null);
        switch(thisImage){
            case 0:
                decByte[0] = decodedByte;
                uris[0] = Uri.parse(path);
                image1.setImageBitmap(decodedByte);
                imagebtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
                break;
            case 1:
                decByte[1] = decodedByte;
                uris[1] = Uri.parse(path);
                image2.setImageBitmap(decodedByte);
                imagebtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
                break;
            case 2:
                decByte[2] = decodedByte;
                uris[2] = Uri.parse(path);
                image3.setImageBitmap(decodedByte);
                imagebtn3.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
                break;
            case 3:
                decByte[3] = decodedByte;
                uris[3] = Uri.parse(path);
                image4.setImageBitmap(decodedByte);
                imagebtn4.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagerembtn));
                break;
        }
        images[thisImage] = true;
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
                        case 1: // Choose video
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void webCallResponse(String response, String flag) {
        if(response != null){
            {
                Log.d("response",response);
                try {
                    JSONObject object = new JSONObject(response);
                    switch (flag) {
                        case "register":
                            String status = object.getString("status");
                            if(status.toLowerCase().equals("success")){
                                String complanId = object.getString("complainid");
                                createSuccessAlert(complanId);

                            }else {
                                Toast.makeText(RegisterComplain.this, "Registration Unsuccessful\n" + status, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
