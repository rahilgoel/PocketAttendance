package iit.iitr.pocketattendance;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Mac on 6/4/16.
 */
public class SingleListAdapter extends BaseAdapter {
    List<SingleData> list;
    Activity a;

    SingleListAdapter(Activity a, List<SingleData> list) {
        this.a = a;
        this.list = list;
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
        viewHolderItem viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = a.getLayoutInflater();
            convertView = inflater.inflate(R.layout.single_data_layout, parent, false);
            viewHolder = new viewHolderItem();
            viewHolder.day = (TextView) convertView.findViewById(R.id.day);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (viewHolderItem) convertView.getTag();
        }
        viewHolder.day.setText(list.get(position).getDay());
        viewHolder.time.setText(list.get(position).getTime());
        viewHolder.status.setText(list.get(position).getStatus());
        return convertView;
    }

    static class viewHolderItem {
        TextView day;
        TextView status;
        TextView time;
    }
}
