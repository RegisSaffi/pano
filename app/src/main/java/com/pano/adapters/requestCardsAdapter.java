package com.pano.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.pano.R;
import com.pano.models.requestCard;

import java.util.List;


public class requestCardsAdapter extends RecyclerView.Adapter<requestCardsAdapter.ViewHolder> {

    Context context;
    private List<requestCard> requestCardList;

    public requestCardsAdapter(List<requestCard> source, Context context) {
        super();
        this.requestCardList = source;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        requestCard pp = requestCardList.get(position);
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_request_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final requestCard requestCard1 = requestCardList.get(position);

        Viewholder.from.setText(requestCard1.getFrom());
        Viewholder.name.setText(requestCard1.getName());
        Viewholder.to.setText(requestCard1.getTo());

        if (requestCard1.getStatus().equals("pending")) {
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_access_time_black_24dp, context.getTheme()));
        } else if (requestCard1.getStatus().equals("accepted")) {
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_check_black_24dp, context.getTheme()));
        } else if (requestCard1.getStatus().equals("confirmed")) {
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_check_circle_black_24dp, context.getTheme()));
        } else {
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_report_problem_black_24dp, context.getTheme()));
        }

    }

    @Override
    public int getItemCount() {
        return requestCardList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView from, to, name;
        ImageView statusImg;

        public ViewHolder(View itemView) {

            super(itemView);

            statusImg = itemView.findViewById(R.id.statusImg);
            name = itemView.findViewById(R.id.tvName);
            from = itemView.findViewById(R.id.tvFrom);
            to = itemView.findViewById(R.id.tvTo);

        }
    }

}