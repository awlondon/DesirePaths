package com.pdceng.www.desirepaths;

import android.animation.Animator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class PublicInputListActivity extends AppCompatActivity implements PublicInputListFragment.OnListFragmentInteractionListener{

    private long animDur = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PublicInputContent(this);
        setContentView(R.layout.activity_public_input_list);

    }

    @Override
    public void onListFragmentInteraction(PublicInput item) {

    }


}
