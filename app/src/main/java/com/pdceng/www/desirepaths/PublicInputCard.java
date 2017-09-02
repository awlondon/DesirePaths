package com.pdceng.www.desirepaths;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by alondon on 7/27/2017.
 */


@Layout(R.layout.card_view)
public class PublicInputCard {

    @View(R.id.progressBar)
    private ProgressBar progressBar;

    @View(R.id.imageView)
    private ImageView publicInputImageView;

    @View(R.id.title)
    private TextView tvTitle;

    @View(R.id.snippet)
    private TextView tvSnippet;

    private PublicInput mPublicInput;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private Universals universals;

    public PublicInputCard(Context context, PublicInput publicInput, SwipePlaceHolderView swipeView) {
        mContext = context;
        mPublicInput = publicInput;
        mSwipeView = swipeView;
    }

    @Resolve
    private void onResolved(){
        universals = new Universals(mContext);
        new DownloadImageTask(publicInputImageView).execute(mPublicInput.getUrl());
        tvTitle.setText(mPublicInput.getTitle());
        tvSnippet.setText(mPublicInput.getSnippet());
    }

    @SwipeOut
    private void onSwipedOut(){
        new DatabaseHelper(mContext).updateUserPIRatings(mPublicInput.getID(),false);
        checkCount();
        Log.d("EVENT", "onSwipedOut");
    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
        new DatabaseHelper(mContext).updateUserPIRatings(mPublicInput.getID(),true);
        checkCount();
        Log.d("EVENT", "onSwipedIn");
    }

    @SwipeInState
    private void onSwipeInState(){
        Log.d("EVENT", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState(){
        Log.d("EVENT", "onSwipeOutState");
    }

    private void checkCount(){
        Log.d("Children",String.valueOf(mSwipeView.getChildCount()));
        if(mSwipeView.getChildCount()<=1){
            Toast.makeText(mContext, "Thank you for your input!", Toast.LENGTH_SHORT).show();
            ((Activity)mContext).finish();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(android.view.View.VISIBLE);
            publicInputImageView.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
//            System.out.println(urlDisplay);
            Bitmap bitmap = null;

            if (!URLUtil.isValidUrl(urlDisplay)) {
                if (!universals.isBitmapInMemoryCache(urlDisplay)) {
                    FTPClient ftpClient = new FTPClient();
                    System.out.println("Starting connection to FTP site!");
                    try {
                        ftpClient.connect("153.92.6.4");
                        ftpClient.login(mContext.getResources().getString(R.string.ftp_username), mContext.getResources().getString(R.string.ftp_password));
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                        File file = new File(Environment.getExternalStorageDirectory() + File.separator + urlDisplay);
                        Log.d("filepath", file.getAbsolutePath());
                        FileOutputStream fos = new FileOutputStream(file);
                        ftpClient.retrieveFile(urlDisplay, fos);
                        fos.flush();
                        fos.close();
                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("urlDisplay: " + urlDisplay);
                    System.out.println("bitmap: " + bitmap);
                    universals.addBitmapToMemoryCache(urlDisplay, bitmap);
                }
                bitmap = universals.getBitmapFromMemoryCache(urlDisplay);
            } else {
                if (!universals.isBitmapInMemoryCache(urlDisplay)) {
                    universals.addBitmapToMemoryCache(urlDisplay, universals.getBitmapFromURL(urlDisplay, 500, 500));
                }
                bitmap = universals.getBitmapFromMemoryCache(urlDisplay);
            }

            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(android.view.View.GONE);
            bmImage.setImageBitmap(result);
        }
    }
}
