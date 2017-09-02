package com.pdceng.www.desirepaths;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdceng.www.desirepaths.PublicInputListFragment.OnListFragmentInteractionListener;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PublicInput} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyPublicInputRecyclerViewAdapter extends RecyclerView.Adapter<MyPublicInputRecyclerViewAdapter.ViewHolder> {

    private final List<PublicInput> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyPublicInputRecyclerViewAdapter(List<PublicInput> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_publicinput, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        try {
            holder.mImageView.setImageBitmap(mValues.get(position).getBitmap());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mSnippetView.setText(mValues.get(position).getSnippet());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mSnippetView;
        public final ImageView mImageView;
        public PublicInput mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.imageView);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mSnippetView = (TextView) view.findViewById(R.id.snippet);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mSnippetView.getText() + "'";
        }
    }
}
