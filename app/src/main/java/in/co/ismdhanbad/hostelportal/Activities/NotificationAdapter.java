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
 * Created by khandelwal on 29/10/16.
 */
public class NotificationAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<String> mTime;
    ArrayList<String> mHeader;
    ArrayList<String> mDetails;
    ArrayList<String> mNotifier;

    public NotificationAdapter(Context context,ArrayList<String> time ,ArrayList<String> header ,
                               ArrayList<String> details,ArrayList<String> notifier) {
        mContext = context;
        mTime = time;
        mHeader = header;
        mDetails = details;
        mNotifier = notifier;
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

            convertView = LayoutInflater.from(mContext).inflate(R.layout.content_notification, null);
            holder = new ViewHolder();
            holder.time = (TextView) convertView.findViewById(R.id.timeStampNotice);
            holder.header = (TextView) convertView.findViewById(R.id.headerNotice);
            holder.details = (TextView) convertView.findViewById(R.id.detailsNotice);
            holder.notifier = (TextView) convertView.findViewById(R.id.notifierNotice);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.time.setText("Dated - " + mTime.get(position));
        holder.header.setText(mHeader.get(position));
        holder.details.setText(mDetails.get(position));
        holder.notifier.setText("Posted by - " + mNotifier.get(position));
        return convertView;
    }


    private static class ViewHolder {
        TextView time;
        TextView header;
        TextView details;
        TextView notifier;
    }
}
