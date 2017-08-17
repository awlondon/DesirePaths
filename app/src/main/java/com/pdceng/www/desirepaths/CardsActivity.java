package com.pdceng.www.desirepaths;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Toast;

import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

/**
 * Created by alondon on 7/27/2017.
 */

public class CardsActivity extends AppCompatActivity {

    private LruCache<String, Bitmap> bitmapMemoryCache;

    private SwipePlaceHolderView mSwipeView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_activity);

        mSwipeView = (SwipePlaceHolderView)findViewById(R.id.swipeView);
        mContext = getApplicationContext();

        mSwipeView.getBuilder()
                .setDisplayViewCount(3)
                .setSwipeDecor(new SwipeDecor()
                        .setPaddingTop(20)
                        .setRelativeScale(0.01f)
                        .setSwipeInMsgLayoutId(R.layout.swipe_in_msg_view)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_out_msg_view));

//        for(PublicInput publicInput : CardsUtils.loadPublicInputs(this.getApplicationContext())){
//            mSwipeView.addView(new PublicInputCard(this, publicInput, mSwipeView));
        PublicInput[] publicInputs = new DatabaseHelper(mContext).getAllPublicInput();
        try {
            if (publicInputs[0] != null) {
                for (PublicInput publicInput : publicInputs) {
                    mSwipeView.addView(new PublicInputCard(this, publicInput, mSwipeView));
                }
            }
        } catch (ArrayIndexOutOfBoundsException exception){
            Toast.makeText(mContext, "You've rated all public input so far", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void noSwipe (View v){
        mSwipeView.doSwipe(false);
        checkCount();
    }
    public void swipe (View v){
        mSwipeView.doSwipe(true);
        checkCount();
    }

    public void checkCount(){
        Log.d("Children",String.valueOf(mSwipeView.getChildCount()));
        if( mSwipeView.getChildCount()<=1){
            Toast.makeText(mContext, "Thank you for your input!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
