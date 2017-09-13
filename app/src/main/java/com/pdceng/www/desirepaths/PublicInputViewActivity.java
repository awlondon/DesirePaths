package com.pdceng.www.desirepaths;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static android.graphics.Color.LTGRAY;
import static android.view.View.OVER_SCROLL_ALWAYS;
import static android.widget.LinearLayout.VERTICAL;

public class PublicInputViewActivity extends AppCompatActivity implements AfterGetAll, CommentsAdapterInterface {
    MyItem myItem;
    ImageView mImageView;
    private int ivHeightSetting = 700;
    private ListView listView;
    private DatabaseHelper dh = new DatabaseHelper(this);
    private CommentsAdapter adapter;
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_input_view);

        myItem = new MyItem(getIntent().getExtras());

        //Creates CardView to be used for content
        final RelativeLayout topView = (RelativeLayout) findViewById(R.id.topView);
        final CardView cardView = new CardView(this);

        final int margin = (int) getResources().getDimension(R.dimen.card_margin); //Default margin

        //START: set CardView layout
        CardView.LayoutParams params = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        params.setMargins(30,30,30,200);
//        cardView.setPadding(0, 200, 0, 200);
        cardView.setLayoutParams(params);
        cardView.setCardBackgroundColor(getColor(R.color.white));
        cardView.bringToFront();
        cardView.setRadius(0);
        cardView.setClickable(true);
        cardView.setElevation(20f);
        //Add CardView
        topView.addView(cardView);
        //END: set CardView layout

        //START: create holder layouts
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.setLayoutParams(llParams);

        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setLayoutParams(llParams);
        entryLayout.setOrientation(VERTICAL);
        //END: create holder layouts

        //START: create ImageView
        mImageView = new ImageView(this);
        ViewGroup.LayoutParams ivParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ivHeightSetting);
        mImageView.setLayoutParams(ivParams);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //END: create ImageView

        //START: create circular progress bar
        RelativeLayout.LayoutParams pbParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(pbParams);
        //END: create circular progress bar

        //START: create TextViews
        LinearLayout.LayoutParams snippetParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snippetParams.setMargins(margin * 2, 0, 0, 0);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(margin * 2, 0, 0, 0);

        TextView tvUserDate = new TextView(this);
        TextView tvTitle = new TextView(this);
        TextView tvSnippet = new TextView(this);

        LinearLayout.LayoutParams userDateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        userDateParams.gravity = Gravity.END;
        userDateParams.setMargins(0, margin, margin, 0);

        tvUserDate.setLayoutParams(userDateParams);
        tvTitle.setLayoutParams(titleParams);
        tvSnippet.setLayoutParams(snippetParams);

        final String addedBy = "Added by: ";
        SpannableString italicUserDate = new SpannableString(addedBy + myItem.getUser() + " on " + formatDateForTimestamp(myItem.getTimestamp()));
        italicUserDate.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), addedBy.length(), addedBy.length() + myItem.getUser().length(), 0);

        SpannableString boldTitle = new SpannableString(myItem.getTitle());
        boldTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, myItem.getTitle().length(), 0);

        tvUserDate.setText(italicUserDate);
        tvTitle.setText(boldTitle);
        tvSnippet.setText(myItem.getSnippet());
        //END: create TextViews

        //Close window 'button'
        ImageView closeWindow = createCloseWindowButton(cardView, topView);

        //START: add views to layouts
        relativeLayout.addView(mImageView);
        relativeLayout.addView(closeWindow);
        relativeLayout.addView(progressBar);

        linearLayout.addView(relativeLayout);

        linearLayout.addView(entryLayout);

        entryLayout.addView(tvUserDate);
        entryLayout.addView(tvTitle);
        entryLayout.addView(tvSnippet);
        //END: add views to layouts

        //START: create and set comment button & posting functions
        LinearLayout.LayoutParams commentButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commentButtonParams.gravity = Gravity.END;
        ImageButton ibComment = new ImageButton(this);
        ibComment.setImageResource(R.drawable.ic_comment);
//        ibComment.setImageDrawable(getDrawable(R.drawable.ic_action_comment));
        ibComment.setBackground(null);
        ibComment.setLayoutParams(commentButtonParams);
        ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Universals.isAnon) {
                    Toast.makeText(mContext, "You cannot add comments anonymously", Toast.LENGTH_SHORT).show();
                    return;
                }
                final LinearLayout linearLayout1 = new LinearLayout(mContext);
                linearLayout1.setOrientation(VERTICAL);
                cardView.addView(linearLayout1);
                linearLayout1.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                linearLayout1.setBackgroundColor(getColor(R.color.lightGreyTransparent));

                LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
                etParams.setMargins(margin, margin, margin, margin);
                etParams.gravity = Gravity.TOP;

                final EditText etComment = new EditText(mContext);
                etComment.setHint("Add a comment...");
                etComment.setBackgroundColor(getColor(R.color.white));
                etComment.setPadding(margin, 0, margin, 0);
                etComment.setLayoutParams(etParams);
                etComment.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                etComment.setMinLines(5);
                etComment.setMaxLines(20);
                etComment.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                etComment.setLines(5);
                etComment.setOverScrollMode(OVER_SCROLL_ALWAYS);
                etComment.canScrollHorizontally(View.SCROLL_AXIS_VERTICAL);

                Button postButton = new Button(mContext);
                postButton.setText("POST");
                postButton.getBackground().setColorFilter(getColor(R.color.darkBlue), PorterDuff.Mode.MULTIPLY);
                Button cancelButton = new Button(mContext);
                cancelButton.setText("CANCEL");

                linearLayout1.addView(etComment);
                linearLayout1.addView(postButton);
                linearLayout1.addView(cancelButton);

                etComment.requestFocus();
                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

                postButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etComment.getText().toString().isEmpty()) {
                            inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "A comment must be entered to post",
                                    Snackbar.LENGTH_INDEFINITE).setAction("OKAY",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                                        }
                                    }).show();
//                            Toast.makeText(MapActivity.this, "A comment must be entered to post", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString(CommentsTable.PIEntry_ID, String.valueOf(myItem.getId()));
                        bundle.putString(CommentsTable.RATING, "0");
                        bundle.putString(CommentsTable.COMMENT, etComment.getText().toString());
                        bundle.putString(CommentsTable.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                        bundle.putString(CommentsTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);
                        dh.insert(bundle, new CommentsTable());
                        inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        cardView.removeView(linearLayout1);
                        setCommentsAdapter(String.valueOf(myItem.getId()));

//                        if (noPicture) {
//                            cardView.animate()
//                                    .translationY(translationY)
//                                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                                    .setDuration(animDur);
//                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardView.removeView(linearLayout1);
                        inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

//                        if (noPicture) {
//                            cardView.animate()
//                                    .translationY(translationY)
//                                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                                    .setDuration(animDur);
//                        }
                    }
                });
            }
        });
        entryLayout.addView(ibComment);
        //END: create and set comment button & posting functions

        //START: create ListView for comments
        listView = new ListView(this);

        ListView.LayoutParams lvParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        listView.setLayoutParams(lvParams);

        setCommentsAdapter(String.valueOf(myItem.getId()));

        linearLayout.addView(listView);
        //END: create ListView for comments

        cardView.addView(linearLayout);

        //START: load image
        String bitmapURL = myItem.getBitmapUrlString();
        if (URLUtil.isValidUrl(bitmapURL) || !bitmapURL.isEmpty()) {
            Log.d("Bitmap URL", bitmapURL);
            new DownloadImageTask(mImageView, progressBar).execute(bitmapURL);
        } else {
            relativeLayout.removeView(mImageView);
            relativeLayout.removeView(progressBar);
//            translationY = ivHeightSetting + 100;
//            noPicture = true;
        }
        //END: load image
    }

    String formatDateForTimestamp(Timestamp timestamp) {
        String timeString = timestamp.toString();
        timeString = timeString.substring(0, 10);
        String year = timeString.substring(0, 4);
        timeString = timeString.replace(year, "");
        timeString = timeString.substring(1);
        timeString += "-" + year;
        return timeString;
    }

    @Override
    public void setCommentsAdapter(String id) {
        listView.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        List<String> commentIds = dh.getComments(id);
        adapter = new CommentsAdapter(this, commentIds);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setAlpha(0f);
        listView.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    private ImageView createCloseWindowButton(final View cardView, final RelativeLayout topView) {
        final ImageView closeWindow = new ImageView(mContext);
        closeWindow.setImageDrawable(getDrawable(R.drawable.ic_dialog_close_dark));
        closeWindow.setColorFilter(LTGRAY);
        RelativeLayout.LayoutParams fabParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        fabParams.setMargins(10, 10, 0, 0);
        fabParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        closeWindow.setLayoutParams(fabParams);
        closeWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dh.getAllFromSQL((AfterGetAll) mContext);
                finish();
            }
        });
        return closeWindow;
    }

    @Override
    public void afterGetAll() {
        Universals.mapActivity.setUpCluster();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ProgressBar progressBar;

        DownloadImageTask(ImageView bmImage, ProgressBar progressBar) {
            this.bmImage = bmImage;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            System.out.println(urlDisplay);
            Bitmap bitmap = null;
            if (!URLUtil.isValidUrl(urlDisplay)) {
                if (!Universals.isBitmapInMemoryCache(urlDisplay)) {
                    FTPClient ftpClient = new FTPClient();
                    System.out.println("Starting connection to FTP site!");
                    try {
                        ftpClient.connect("153.92.6.4");
                        ftpClient.login(getString(R.string.ftp_username), getString(R.string.ftp_password));
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
            bmImage.setAlpha(0f);
            bmImage.setImageBitmap(result);
            bmImage.animate()
                    .setDuration(500)
                    .alphaBy(1f);
            progressBar.setVisibility(View.GONE);
        }
    }
}
