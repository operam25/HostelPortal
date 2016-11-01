package in.co.ismdhanbad.hostelportal.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.co.ismdhanbad.hostelportal.R;


/**
 * Created by khandelwal on 31/10/16.
 */

public class ComplainAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<String> mTime;
    ArrayList<String> mHeader;
    ArrayList<String> mDetails;
    ArrayList<String> mNotifier;
    ArrayList<String> mComplainId;

    public ComplainAdapter(Context context,ArrayList<String> time ,ArrayList<String> header ,
                               ArrayList<String> details,ArrayList<String> notifier,ArrayList<String> complainId) {
        mContext = context;
        mTime = time;
        mHeader = header;
        mDetails = details;
        mNotifier = notifier;
        mComplainId = complainId;
    }

    public String getOrderNumber(int position){
        return "";
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return mDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.content_complain, null);
            holder = new ViewHolder();
            holder.time = (TextView) convertView.findViewById(R.id.timeComplain);
            holder.header = (TextView) convertView.findViewById(R.id.headerComplain);
            holder.details = (TextView) convertView.findViewById(R.id.detailsComplain1);
            holder.complainId = (TextView) convertView.findViewById(R.id.complainId);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.time.setText("Dated - " + mTime.get(position));
        holder.header.setText(mHeader.get(position));
        holder.details.setText(mDetails.get(position));
        holder.complainId.setText("Complain ID - " + mComplainId.get(position));
        return convertView;
    }


    private static class ViewHolder {
        TextView time;
        TextView header;
        TextView details;
        TextView complainId;
    }
}
