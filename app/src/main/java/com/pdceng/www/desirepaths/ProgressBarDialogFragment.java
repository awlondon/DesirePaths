package com.pdceng.www.desirepaths;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alondon on 8/16/2017.
 */

public class ProgressBarDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ViewGroup viewGroup = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View viewRoot = inflater.inflate(R.layout.progress_dialog, viewGroup);

        builder.setView(viewRoot);

        return builder.create();
    }
}
