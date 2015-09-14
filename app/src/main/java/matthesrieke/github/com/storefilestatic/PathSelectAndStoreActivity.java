package matthesrieke.github.com.storefilestatic;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathSelectAndStoreActivity extends AppCompatActivity implements DownloadFileHandler.ProgressListener {

    private ListView listView;
    private PathListViewAdapter listViewAdapter;
    private ActionMode mActionMode;
    private DownloadFileHandler intentHandler;
    private TextView progressStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_select_and_store);

        final Context context = getApplicationContext();
        this.listView = (ListView) findViewById(R.id.listView);
        this.listViewAdapter = new PathListViewAdapter(context, R.layout.path_item, new ArrayList<String>(), false);
        this.listView.setAdapter(this.listViewAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                listViewAdapter.toggleSelection(position);
            }
        });

        this.progressStatus = (TextView) findViewById(R.id.progress_text);

        updateListView();

        ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray item = listViewAdapter.getSelectedIds();
                if (item != null && item.size() > 0) {
                    int theItem = item.keyAt(0);
                    String thePath = listViewAdapter.getItem(theItem);
                    if (intentHandler != null) {
                        intentHandler.execute(thePath);
                    }
                }
            }
        });

        //get the received intent
        Intent receivedIntent = getIntent();

        //get the action
        String receivedAction = receivedIntent.getAction();

        //find out what we are dealing with
        String receivedType = receivedIntent.getType();

        //make sure it's an action and type we can handle
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            ClipData clipData = receivedIntent.getClipData();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence text = clipData.getItemAt(0).getText();
                handleClipData(text);
            }
        }
    }

    private void handleClipData(CharSequence text) {
        try {
            Uri uri = Uri.parse(text.toString());
            String url = uri.toString();
            if (url.contains("https://www.dropbox.com")) {
                handleDropbox(url);
            }
        }
        catch (RuntimeException e) {
            Log.w("storefilestatic", e);
        }

    }

    private void handleDropbox(String url) {
        url = url.replace("dl=0", "dl=1");
        Uri uri = Uri.parse(url);
        String fileName = uri.getLastPathSegment();
        this.intentHandler = new DownloadFileHandler(url, fileName, this);
    }

    private void updateListView() {
        Context context = getApplicationContext();
        Set<String> paths = resolvePathSet();
        List<String> pathArray = new ArrayList<>(paths);
        this.listViewAdapter.clear();
        this.listViewAdapter.addAll(pathArray);
    }

    private Set<String> resolvePathSet() {
        Context context = getApplicationContext();
        SharedPreferences sp = context.getSharedPreferences(SettingProperties.STORAGE_PREF, Context.MODE_PRIVATE);
        Set<String> paths = sp.getStringSet(SettingProperties.PATHS, new HashSet<String>());
        return paths;
    }


    @Override
    public void updateProgress(final Integer... progress) {
        if (progress == null || progress.length == 0) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressStatus.setText("Progress: "+progress[0]);
            }
        });
    }

    @Override
    public void onFinished(String file) {
        Log.i("storefilestatic", "stored file at "+ file);
        finish();
    }
}
