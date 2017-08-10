package com.pdceng.www.desirepaths;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.SwipeViewBinder;
import com.mindorks.placeholderview.SwipeViewBuilder;

/**
 * Created by alondon on 8/1/2017.
 */

public class CustomSwipePlaceHolderView extends SwipePlaceHolderView {
    public CustomSwipePlaceHolderView(Context context) {
        super(context);
    }

    public CustomSwipePlaceHolderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwipePlaceHolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSwipePlaceHolderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void doSwipe(boolean isSwipeIn) {
        super.doSwipe(isSwipeIn);

    }

    @Override
    public void doSwipe(Object resolver, boolean isSwipeIn) {
        super.doSwipe(resolver, isSwipeIn);
    }
}
