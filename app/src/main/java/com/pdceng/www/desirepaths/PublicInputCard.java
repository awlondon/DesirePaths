package com.pdceng.www.desirepaths;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
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
        Glide.with(mContext).load(mPublicInput.getUrl()).into(publicInputImageView);
        tvTitle.setText(mPublicInput.getTitle());
        tvSnippet.setText(mPublicInput.getSnippet());
    }

    @SwipeOut
    private void onSwipedOut(){
        checkCount();
        Log.d("EVENT", "onSwipedOut");
    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
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
}
