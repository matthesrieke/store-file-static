package matthesrieke.github.com.storefilestatic;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileHandler extends AsyncTask<String, Integer, Void> {


    private final String targetUrl;
    private final String fileName;
    private final ProgressListener listener;

    public DownloadFileHandler(String url, String fileName, ProgressListener l) {
        this.targetUrl = url;
        this.fileName = fileName;
        this.listener = l;
    }


    /**
     * Downloading file in background thread
     * */
    @Override
    protected Void doInBackground(String... path) {
        publishProgress(0);
        int count;
        String targetFile = path[0].concat("/").concat(this.fileName);
        try {
            URL url = new URL(targetUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            long contentLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(targetFile);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress((int) ((total * 100) / contentLength));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        if (this.listener != null) {
            this.listener.onFinished(targetFile);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (this.listener != null) {
            this.listener.updateProgress(values);
        }
    }

    public static interface ProgressListener {

        void updateProgress(Integer... progress);

        void onFinished(String file);
    }

}
