package in.co.ismdhanbad.hostelportal.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.paging.gridview.PagingGridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.co.ismdhanbad.hostelportal.R;

/**
 * Created by khandelwal on 20/08/16.
 */
public class ComplainList extends AppCompatActivity
        implements HttpApiCall.CallResponseListener {

    private ComplainAdapter adapter;
    private ArrayList<String> time;
    private ArrayList<String> header;
    private ArrayList<String> details;
    private ArrayList<String> user;
    private ArrayList<String> complainId;
    private int page = 0;
    private PagingGridView ll;
    private int count;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    String admnNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
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

        admnNumber = preferences.getString("admissionNumber","");

        time = new ArrayList<String>();
        details = new ArrayList<String>();
        header = new ArrayList<String>();
        user = new ArrayList<String>();
        complainId = new ArrayList<String>();

        adapter = new ComplainAdapter(this,time,header,details,user,complainId);
        ll = (PagingGridView) findViewById(R.id.listLayout);
        ll.setAdapter(adapter);
        ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ComplainList.this);
                builder.setTitle("Complain ID - " + complainId.get(position));
                builder.setMessage(header.get(position) + "\n\n" + details.get(position) +  "\n\nBy - " + user.get(position) + "\nDated - " + time.get(position));
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
                Log.d("cclicked","click");
            }
        });

        String url = getResources().getString(R.string.base_url) + "showcomplain.php";
        String[] name = {"page","admissionnumber"};
        String pageValue = page*10 + "";
        String[] value = {pageValue,admnNumber};
        page++;
        new HttpApiCall(ComplainList.this,url,name,value,"notice");

    }

    public void setNotification(JSONArray msg) throws JSONException{
        int length = msg.length();
        for(int i=0;i<length;i++){
            Log.d("icount",i+"");
            JSONObject jsonObject = msg.getJSONObject(i);
            time.add(jsonObject.getString("time"));
            header.add(jsonObject.getString("header"));
            details.add(jsonObject.getString("details"));
            String usr = jsonObject.getString("user");
            if(usr.equals(admnNumber))
                user.add(admnNumber);
            else
                user.add("Anonymous");
            complainId.add(jsonObject.getString("complainId"));
        }
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void webCallResponse(String response, String flag) {
        if(response!=null){
            Log.d("response",response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                switch (flag){

                    case "notice":
                        String status = jsonObject.getString("status");
                        if(status.toLowerCase().contains("success")){
                            count = Integer.parseInt(jsonObject.getString("count"));
                            if(count<=page*10){
                                ll.onFinishLoading(false,null);
                            }else {
                                ll.setPagingableListener(new PagingGridView.Pagingable() {
                                    @Override
                                    public void onLoadMoreItems() {
                                        if(count > page*10){
                                            String url = getResources().getString(R.string.base_url) + "showcomplain.php";
                                            String[] name = {"page","admissionnumber"};
                                            String pageValue = page*10 + "";
                                            String[] value = {pageValue,admnNumber};
                                            page++;
                                            new HttpApiCall(ComplainList.this,url,name,value,"notice");
                                        }
                                    }
                                });
                                ll.onFinishLoading(true,null);
                            }
                            JSONArray msg = jsonObject.getJSONArray("msg");
                            setNotification(msg);
                        }
                        break;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
