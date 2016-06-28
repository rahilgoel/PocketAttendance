package iit.iitr.pocketattendance;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mac on 6/4/16.
 */
public class AllListAdapter extends BaseAdapter {

    Activity a;
    List<AllData> list;

    AllListAdapter(Activity a, List<AllData> list) {
        this.list = list;
        this.a = a;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = a.getLayoutInflater();
            convertView = inflater.inflate(R.layout.all_data_layout, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.day = (TextView) convertView.findViewById(R.id.day);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }
        viewHolder.day.setText(list.get(position).getDay());
        viewHolder.name.setText(list.get(position).getName());
        viewHolder.status.setText(list.get(position).getStatus());
        viewHolder.time.setText(list.get(position).getTime());


        return convertView;
    }

    static class ViewHolderItem {
        TextView day;
        TextView name;
        TextView status;
        TextView time;


    }
}
