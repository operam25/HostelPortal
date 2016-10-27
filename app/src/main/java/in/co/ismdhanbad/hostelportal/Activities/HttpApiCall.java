package in.co.ismdhanbad.hostelportal.Activities;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.co.ismdhanbad.hostelportal.R;

/**
 * Created by khandelwal on 22/06/16.
 */
public class HttpApiCall {



    Context mContext;
    String result;
    CallResponseListener listener;
    String[] mNames;
    String[] mValues;
    String mFlag;

    public interface CallResponseListener{
        void webCallResponse(String response, String flag);
    }


    public HttpApiCall(Context context, String url, String[] names, String[] values, String which) {
        mContext = context;
        this.listener = (CallResponseListener) context;
        mNames = names;
        mValues = values;
        mFlag = which;
        new HttpCall().execute(url);
    }

    public class HttpCall extends AsyncTask<String, String, String> {
        HttpResponse response;

        @Override
        protected String doInBackground(String... params) {


            HttpClient client = new DefaultHttpClient();
            String address = params[0];
            String responseText = null;

            //deviceName = DeviceName.getDeviceName();
            org.apache.http.client.methods.HttpPost post = new org.apache.http.client.methods.HttpPost(address);
            try {


                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("username", mContext.getString(R.string.userName)));
                nameValuePairs.add(new BasicNameValuePair("apikey", mContext.getString(R.string.apiKey)));
                for (int i = 0; i < mValues.length; i++) {
                    nameValuePairs.add(new BasicNameValuePair(mNames[i], mValues[i]));
                }

                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = client.execute(post);
                responseText = EntityUtils.toString(response.getEntity());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseText;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            result = s;
           // if(listener!=null){
                listener.webCallResponse(result, mFlag);
          //  }
        }
    }
}