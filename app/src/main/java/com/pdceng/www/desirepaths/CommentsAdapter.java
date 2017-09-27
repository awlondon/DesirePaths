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


class CommentsAdapter extends BaseAdapter {
    private static final int GET_BUNDLES_COMPLETE = 0;
    private Bundle bundle;
    private List<String> mData;
    private DatabaseHelper dh;
    private Context mContext;
    private LayoutInflater inflater = null;


//    public CommentsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Bundle> objects) {
//        super(context, resource, objects);
//        layoutResource = resource;
//    }

    CommentsAdapter(Context context, List<String> mData) {
        super();
        this.mData = mData;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
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

        final ViewGroup viewGroup = null;

        dh = new DatabaseHelper(mContext);
        if(convertView==null)
            convertView = inflater.inflate(R.layout.comment, viewGroup);

        String id = mData.get(position);

        bundle = dh.getRow(new CommentsTable(), CommentsTable.ID, id);
        String socialMediaID = bundle.getString(CommentsTable.SOCIAL_MEDIA_ID);
        System.out.println("socialMediaId: " + socialMediaID);
        Bundle userBundle = dh.getRow(new UserTable(), UserTable.SOCIAL_MEDIA_ID, socialMediaID);

        TextView tvRating = (TextView) convertView.findViewById(R.id.rating);
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

        ImageView ivProfile = (ImageView) convertView.findViewById(R.id.ivProfile);

        new DownloadImageTask(ivProfile).execute(userBundle.getString(UserTable.PHOTO_URL));

        int ratingGiven = dh.checkCommentRatingGiven(mData.get(position));
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
        final ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            System.out.println(urlDisplay);
            Bitmap bitmap;
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
            if (result != null) {
                bmImage.setAlpha(0f);
                bmImage.setImageBitmap(result);
                bmImage.animate()
                        .setDuration(500)
                        .alphaBy(1f);
            } else {
                bmImage.setAlpha(0f);
                bmImage.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                bmImage.animate()
                        .setDuration(500)
                        .alphaBy(1f);
            }
        }
    }
}
