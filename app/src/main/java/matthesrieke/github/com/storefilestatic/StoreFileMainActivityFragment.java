package matthesrieke.github.com.storefilestatic;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreFileMainActivityFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final int FILE_CODE = 1000;
    private ListView listView;
    private ActionMode actionMode;
    private PathListViewAdapter listViewAdapter;

    public StoreFileMainActivityFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getContext();
        View view = inflater.inflate(R.layout.fragment_store_file_main, container, false);
        this.listView = (ListView) view.findViewById(R.id.listView);
        this.listViewAdapter = new PathListViewAdapter(context, R.layout.path_item, new ArrayList<String>());
        this.listView.setAdapter(this.listViewAdapter);

        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent,
                                           View view, int position, long id) {
                onListItemSelect(position);
                return true;
            }
        });

        updateListView();

        ImageButton button = (ImageButton) view.findViewById(R.id.addPathButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This always works
                Intent i = new Intent(context, FilePickerActivity.class);
                // This works if you defined the intent filter
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, FILE_CODE);
            }
        });

        return view;
    }

    private void updateListView() {
        Context context = getContext();
        Set<String> paths = resolvePathSet();
        List<String> pathArray = new ArrayList<>(paths);
        this.listViewAdapter.clear();
        this.listViewAdapter.addAll(pathArray);
    }

    private Set<String> resolvePathSet() {
        Context context = getContext();
        SharedPreferences sp = context.getSharedPreferences(SettingProperties.STORAGE_PREF, Context.MODE_PRIVATE);
        Set<String> paths = sp.getStringSet(SettingProperties.PATHS, new HashSet<String>());
        return paths;
    }

    private void addPathItem(String path) {
        Set<String> pathArray = resolvePathSet();
        pathArray.add(path);

        Context context = getContext();
        SharedPreferences sp = context.getSharedPreferences(SettingProperties.STORAGE_PREF, Context.MODE_PRIVATE);
        sp.edit().putStringSet(SettingProperties.PATHS, pathArray).commit();
    }

    private void removePathItem(String path) {
        Set<String> pathArray = resolvePathSet();
        pathArray.remove(path);

        Context context = getContext();
        SharedPreferences sp = context.getSharedPreferences(SettingProperties.STORAGE_PREF, Context.MODE_PRIVATE);
        sp.edit().putStringSet(SettingProperties.PATHS, pathArray).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                ClipData clip = data.getClipData();

                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        Uri uri = clip.getItemAt(i).getUri();
                        // Do something with the URI
                    }
                }
                // For Ice Cream Sandwich

            }
            else {
                Uri uri = data.getData();
                Log.i("YEAH", uri.getPath());
                addPathItem(uri.getPath());
                updateListView();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (actionMode != null) {
            onListItemSelect(position);
        }
    }

    private void onListItemSelect(int position) {
        this.listViewAdapter.toggleSelection(position);
        boolean hasCheckedItems = this.listViewAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && actionMode == null) {
            // there are some selected items, start the actionMode
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
        }

        else if (!hasCheckedItems && actionMode != null) {
            // there no selected items, finish the actionMode
            actionMode.finish();
        }

        if (actionMode != null) {
            actionMode.setTitle(String.valueOf(listViewAdapter
                    .getSelectedCount()) + " selected");
        }

    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_action_bar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_delete:
                    // retrieve selected items and delete them out
                    SparseBooleanArray selected = listViewAdapter.getSelectedIds();
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            String selectedItem = listViewAdapter.getItem(selected.keyAt(i));
                            removePathItem(selectedItem);
                            updateListView();
                        }
                    }
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            listViewAdapter.removeSelection();
            actionMode = null;
        }
    }

}
