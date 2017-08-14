package com.pdceng.www.desirepaths;

import android.app.Activity;
import android.app.Application;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.ExceptionCatchingInputStream;
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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by alondon on 7/27/2017.
 */


@Layout(R.layout.card_view)
public class PublicInputCard {

    @View(R.id.imageView)
    private ImageView publicInputImageView;

    @View(R.id.title)
    private TextView tvTitle;

    @View(R.id.snippet)
    private TextView tvSnippet;

    private PublicInput mPublicInput;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private Activity mActivity;

    public PublicInputCard(Context context, PublicInput publicInput, SwipePlaceHolderView swipeView) {
        mContext = context;
        mPublicInput = publicInput;
        mSwipeView = swipeView;
    }

    @Resolve
    private void onResolved(){
        new DownloadImageTask(publicInputImageView).execute(mPublicInput.getUrl());
//        Glide.with(mContext).load(mPublicInput.getUrl()).into(publicInputImageView);
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

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            publicInputImageView.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            System.out.println(urldisplay);
            Bitmap mIcon11 = null;
            if (URLUtil.isValidUrl(urldisplay)) {
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

            } else {
                FTPClient ftpClient = new FTPClient();
                System.out.println("Starting connection to FTP site!");
                try {
                    ftpClient.connect("host2.bakop.com");
                    ftpClient.login("pdceng","Anchorage_0616");
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

//                    urldisplay = "458226604560086_1502489214879.jpg";
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + urldisplay);
                    Log.d("filepath:", file.getAbsolutePath());
                    FileOutputStream fos = new FileOutputStream(file);
                    ftpClient.retrieveFile(urldisplay,fos);
                    fos.flush();
                    fos.close();
                    mIcon11 = BitmapFactory.decodeFile(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
