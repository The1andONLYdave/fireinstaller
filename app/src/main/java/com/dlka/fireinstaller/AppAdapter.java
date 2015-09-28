package com.dlka.fireinstaller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * For mapping a SortablePackageInfo into a view.
 *
 * @author patrick
 */
public class AppAdapter extends ArrayAdapter<SortablePackageInfo> {

    private int layout;

    public AppAdapter(Context context, int textViewResourceId, List<SortablePackageInfo> spi,
                      int layout) {
        super(context, textViewResourceId, spi);
        this.layout = layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret = convertView;
        if (ret == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            ret = inflater.inflate(layout, null);
            ViewHolder vh = new ViewHolder();
            vh.appName = (TextView) ret.findViewById(R.id.appname);
            vh.appPackage = (TextView) ret.findViewById(R.id.apppackage);
            vh.appIcon = (ImageView) ret.findViewById(R.id.icon);
            ret.setTag(vh);
        }
        SortablePackageInfo spi = getItem(position);
        ViewHolder viewHolder = (ViewHolder) ret.getTag();

        viewHolder.appName.setText(spi.displayName);
        viewHolder.appPackage.setText(spi.packageName);
        new IconLoaderTask(getContext().getPackageManager(), viewHolder.appIcon).execute(spi.appInfo);

        CheckBox sel = ((CheckBox) ret.findViewById(R.id.selected));
        sel.setChecked(spi.selected);
        sel.setOnClickListener(spi);

        return ret;
    }

}
