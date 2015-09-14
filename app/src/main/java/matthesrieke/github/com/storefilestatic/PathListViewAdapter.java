package matthesrieke.github.com.storefilestatic;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PathListViewAdapter extends ArrayAdapter<String> {

    private final boolean allowMultipleSelection;
    private Context context;
    private List<String> paths;
    private SparseBooleanArray selectedItems;

    public PathListViewAdapter(Context context, int resId, List<String> paths) {
        this(context, resId, paths, true);
    }

    public PathListViewAdapter(Context context, int resId, List<String> paths, boolean multiple) {
        super(context, resId, paths);
        selectedItems = new SparseBooleanArray();
        this.context = context;
        this.paths = paths;
        this.allowMultipleSelection = multiple;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.path_item, null);
            holder = new ViewHolder();
            holder.pathText = (TextView) convertView
                    .findViewById(R.id.path_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String laptop = getItem(position);
        holder.pathText.setText(laptop);
        convertView.setBackgroundColor(selectedItems.get(position) ? Color.LTGRAY : Color.TRANSPARENT);

        return convertView;
    }

    @Override
    public void add(String laptop) {
        paths.add(laptop);
        notifyDataSetChanged();
        Toast.makeText(context, paths.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void remove(String object) {
        paths.remove(object);
        notifyDataSetChanged();
    }

    public List<String> getPaths() {
        return paths;
    }

    public void toggleSelection(int position) {
        if (!this.allowMultipleSelection) {
            removeSelection();
        }
        selectView(position, !selectedItems.get(position));
    }

    public void removeSelection() {
        selectedItems = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value) {
            selectedItems.put(position, value);
        }
        else{
            selectedItems.delete(position);
        }

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return selectedItems;
    }

    private class ViewHolder {
        TextView pathText;
    }
}
