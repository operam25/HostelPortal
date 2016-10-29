package in.co.ismdhanbad.hostelportal.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import in.co.ismdhanbad.hostelportal.R;

/**
 * Created by khandelwal on 18/08/16.
 */
public class Login extends AppCompatActivity
        implements HttpApiCall.CallResponseListener{

    private CardView login;
    private CardView SignUp;
    private TextView switchToLogin;
    private TextView switchToSignUp;
    private TextView redirectWeb;
    private TextView redirectTanC;
    private Button LoginBtn;
    private Button SignUpBtn;
    private boolean doubleBackToExitPressedOnce = false;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        preferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = preferences.edit();

        login = (CardView) findViewById(R.id.logInCard);
        SignUp = (CardView) findViewById(R.id.signUpCard);

        login.setVisibility(View.VISIBLE);
        SignUp.setVisibility(View.GONE);

        switchToLogin = (TextView) findViewById(R.id.switchToLogin);
        switchToSignUp = (TextView) findViewById(R.id.switchToSignUp);
        redirectWeb = (TextView) findViewById(R.id.redirectWeb);
        redirectTanC = (TextView) findViewById(R.id.tandc_redirect);
        LoginBtn = (Button) findViewById(R.id.logInBtn);
        SignUpBtn = (Button) findViewById(R.id.signUpBtn);


        switchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setVisibility(View.VISIBLE);
                SignUp.setVisibility(View.GONE);
            }
        });

        switchToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setVisibility(View.GONE);
                SignUp.setVisibility(View.VISIBLE);
            }
        });

        redirectWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ismdhanbad.ac.in"));
                startActivity(browserIntent);
            }
        });

        redirectTanC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zerowaste.co.in/terms-conditions.php"));
                //startActivity(browserIntent);
            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignUp();
            }
        });

    }

    public void doLogin(){
        EditText ID = (EditText) findViewById(R.id.ZWidLogin);
        EditText passcodeLogin = (EditText) findViewById(R.id.ZWidPasscodeLogin);

        if(ID.getText().length() >= 3){

            if(passcodeLogin.getText().length() >= 8){
                String[] names = {"admissionnumber","password"};
                String[] values = {ID.getText().toString(),passcodeLogin.getText().toString()};
                //Log.d("value", String.valueOf(values));
                String Url = getResources().getString(R.string.base_url) + "login.php" ;
                new HttpApiCall(Login.this, Url, names, values, "login");
            }else {
                passcodeLogin.setError("Password consists of atleast 8 characters");
            }

        }else {
            ID.setError("Enter Correct Admission Number");
        }
    }

    public void doSignUp(){
        EditText name = (EditText) findViewById(R.id.nameSignUp);
        final EditText number = (EditText) findViewById(R.id.numberSignUp);
        EditText mail = (EditText) findViewById(R.id.mailSignUp);
        EditText admnno = (EditText) findViewById(R.id.businessSignUp);
        EditText passcode = (EditText) findViewById(R.id.PasscodeSignUp);
        EditText passcodeAgain = (EditText) findViewById(R.id.confirmPasswordSignUp);
        final CheckBox checkBoxSignUp = (CheckBox) findViewById(R.id.checkboxSignUp);

        if (checkBoxSignUp != null) {
            checkBoxSignUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked)
                        checkBoxSignUp.setError(null);

                }
            });
        }

        if(name.getText().length() >= 3){

            if(number.getText().length() >= 10){

                if(admnno.getText().length() >= 3){

                    if(passcode.getText().length()>=8){

                        if(passcode.getText().toString().equals(passcodeAgain.getText().toString())){

                            if(checkBoxSignUp.isChecked() == true){

                                String[] names = {"name","email","password","admissionnumber","contactnumber"};
                                String[] values = {name.getText().toString(),mail.getText().toString(),passcode.getText().toString(),admnno.getText().toString(),number.getText().toString()};
                                //Log.d("value", String.valueOf(values));
                                String Url = getResources().getString(R.string.base_url) + "register.php" ;
                                new HttpApiCall(Login.this, Url, names, values, "register");

                            }else {
                                checkBoxSignUp.setError("Do you Agree?");
                            }

                        }else {
                            passcodeAgain.setError("Password does not Match");
                        }

                    }else {
                        passcode.setError("Password consists of atleast 8 characters");
                    }

                }else {
                    admnno.setError("Admission Number consists of atleast 3 characters");
                }

            }else {
                number.setError("Invalid Format");
            }

        }else {
            name.setError("Name consists of atleasts 3 Characters");
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent i = new Intent();
            setResult(4, i);
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
                                switchToLogin.performClick();
                                Toast.makeText(Login.this,"Registration Successful",Toast.LENGTH_SHORT).show();

                            }else {
                                Toast.makeText(Login.this, "Registration Unsuccessful\n" + status, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "login":
                            status = object.getString("status");
                            if(status.toLowerCase().equals("success")){
                                isLoggedIn = true;
                                JSONObject msg = object.getJSONObject("msg");
                                String name = msg.getString("name");
                                String eMail = msg.getString("email");
                                String admissionNumber = msg.getString("admissionnumber");
                                String contactNumber = msg.getString("contactnumber");
//                              Log.d("shello",eMail);
                                editor.putString("name",name);
                                editor.putString("eMail",eMail);
                                editor.putString("admissionNumber",admissionNumber);
                                editor.putString("contactNumber",contactNumber);
                                editor.putBoolean("isLoggedIn",isLoggedIn);
                                editor.commit();
//                                Log.d("name",preferences.getString("name",""));
                                Intent i = getIntent();
                                setResult(1,i);
                                finish();
                                Toast.makeText(Login.this,"Login Successful",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(Login.this, status + " Please try again", Toast.LENGTH_SHORT).show();
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
