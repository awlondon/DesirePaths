package com.pdceng.www.desirepaths;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by alondon on 8/3/2017.
 */

public class CommentsAdapter extends BaseAdapter {
    private static final int GET_BUNDLES_COMPLETE = 0;

    TextView tvRating;
    Bundle bundle;

    List<String> mData;
    DatabaseHelper dh;

    Context mContext;
    LayoutInflater inflater=null;
    String id;
    ImageView ivProfile;

    Bundle userBundle;
    int ratingGiven;

    Universals universals;


//    public CommentsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Bundle> objects) {
//        super(context, resource, objects);
//        layoutResource = resource;
//    }

    public CommentsAdapter(Context context, List<String> mData){
        super();
        this.mData = mData;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
        universals = new Universals(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<String> getData(){
        return mData;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        dh = new DatabaseHelper(mContext);
        if(convertView==null)
            convertView = inflater.inflate(R.layout.comment,null);

        id = mData.get(position);

//        checkSync();
        bundle = dh.getRow(new CommentsTable(), CommentsTable.ID, id);
        String socialMediaID = bundle.getString(CommentsTable.SOCIAL_MEDIA_ID);
        System.out.println("socialMediaId: " + socialMediaID);
        userBundle = dh.getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, socialMediaID);

        tvRating = (TextView) convertView.findViewById(R.id.rating);
        TextView tvUser = (TextView) convertView.findViewById(R.id.user);
        TextView tvComment = (TextView) convertView.findViewById(R.id.comment);
        TextView tvDate = (TextView) convertView.findViewById(R.id.date);

        tvRating.setText(bundle.getString(CommentsTable.RATING));
        tvComment.setText(bundle.getString(CommentsTable.COMMENT));
        tvUser.setText(userBundle.getString(UserTable.NAME));
        tvDate.setText(Universals.getDuration(bundle.getString(CommentsTable.TIMESTAMP)));

        tvDate.setTextSize(12);

        ImageButton ibUp = (ImageButton) convertView.findViewById(R.id.upArrow);
        ImageButton ibDown = (ImageButton) convertView.findViewById(R.id.downArrow);

        ivProfile = (ImageView) convertView.findViewById(R.id.ivProfile);

        new DownloadImageTask(ivProfile).execute(userBundle.getString(UserTable.PHOTO_URL));

        ratingGiven = dh.checkCommentRatingGiven(mData.get(position));
        switch (ratingGiven){
            case DatabaseHelper.NO_RATING_GIVEN:
                ibDown.setAlpha(.5f);
                ibUp.setAlpha(.5f);
                break;
            case DatabaseHelper.NEG_RATING_GIVEN:
                ibDown.setAlpha(1f);
                ibUp.setAlpha(.5f);
                break;
            case DatabaseHelper.POS_RATING_GIVEN:
                ibUp.setAlpha(1f);
                ibDown.setAlpha(.5f);
            }

        ibUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Universals.isAnon) {
                    Toast.makeText(mContext, "You cannot vote on comments anonymously", Toast.LENGTH_SHORT).show();
                    return;
                }
                rateChangerClicked(true,mData.get(position));
            }
        });

        ibDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Universals.isAnon) {
                    Toast.makeText(mContext, "You cannot vote on comments anonymously", Toast.LENGTH_SHORT).show();
                    return;
                }
                rateChangerClicked(false,mData.get(position));
            }
        });

        return convertView;
    }

    private void rateChangerClicked(boolean positive, String commentId){
        dh.adjustRating(positive,commentId);
        ((CommentsAdapterInterface) mContext).setCommentsAdapter(bundle.getString(CommentsTable.PIEntry_ID));
    }

    private void checkSync() {
        if (Universals.SYNCHRONIZING) {
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
//            ivProfile.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            System.out.println(urlDisplay);
            Bitmap bitmap = null;
            if (urlDisplay == null || !URLUtil.isValidUrl(urlDisplay)) {
                return null;
            } else {
                if (!Universals.isBitmapInMemoryCache(urlDisplay)) {
                    Universals.addBitmapToMemoryCache(urlDisplay, Universals.getBitmapFromURL(urlDisplay, 50, 50));
                }
                bitmap = Universals.getBitmapFromMemoryCache(urlDisplay);
                return bitmap;
            }
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setAlpha(0f);
            bmImage.setImageBitmap(result);
            bmImage.animate()
                    .setDuration(500)
                    .alphaBy(1f);
        }
    }
}
