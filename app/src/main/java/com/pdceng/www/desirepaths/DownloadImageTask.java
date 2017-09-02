package com.pdceng.www.desirepaths;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by alexlondon on 9/1/17.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    Context context;

    DownloadImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Bitmap doInBackground(String... urls) {
        String urlDisplay = urls[0];
        Bitmap bitmap = null;
        if (!URLUtil.isValidUrl(urlDisplay)) {
            if (!Universals.isBitmapInMemoryCache(urlDisplay)) {
                FTPClient ftpClient = new FTPClient();
                System.out.println("Starting connection to FTP site!");
                try {
                    ftpClient.connect("153.92.6.4");
                    ftpClient.login(context.getString(R.string.ftp_username), context.getString(R.string.ftp_password));
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + urlDisplay);
                    Log.d("filepath:", file.getAbsolutePath());
                    FileOutputStream fos = new FileOutputStream(file);
                    ftpClient.retrieveFile(urlDisplay, fos);
                    fos.flush();
                    fos.close();
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Universals.addBitmapToMemoryCache(urlDisplay, bitmap);
            }
            bitmap = Universals.getBitmapFromMemoryCache(urlDisplay);
        } else {
            if (!Universals.isBitmapInMemoryCache(urlDisplay)) {
                Universals.addBitmapToMemoryCache(urlDisplay, Universals.getBitmapFromURL(urlDisplay, 200, 200));
            }
            bitmap = Universals.getBitmapFromMemoryCache(urlDisplay);
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
    }
}