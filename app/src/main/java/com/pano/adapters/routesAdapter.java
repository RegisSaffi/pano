package com.pano.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.pano.R;
import com.pano.models.requestCard;

import java.util.ArrayList;
import java.util.List;


public class routesAdapter extends RecyclerView.Adapter<routesAdapter.ViewHolder> implements Filterable {

    Context context;
    private List<requestCard> requestCardList;
    private List<requestCard> requestCardListFiltered;

    public routesAdapter(List<requestCard> source, Context context) {
        super();
        this.requestCardList = source;
        this.context = context;
        this.requestCardListFiltered=source;
    }

    @Override
    public int getItemViewType(int position) {
        requestCard pp = requestCardList.get(position);
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final requestCard requestCard1 = requestCardListFiltered.get(position);

        Viewholder.name.setText(requestCard1.getName());
        Viewholder.zone.setText(requestCard1.getTo());
    }

    @Override
    public int getItemCount() {
        return requestCardListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    requestCardListFiltered = requestCardList;
                } else {
                    List<requestCard> filteredList = new ArrayList<>();
                    for (requestCard row : requestCardList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    requestCardListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = requestCardListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                requestCardListFiltered = (ArrayList<requestCard>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView zone,name;

        ImageView statusImg;
        public ViewHolder(View itemView) {

            super(itemView);

            zone = itemView.findViewById(R.id.tvZone);
            name = itemView.findViewById(R.id.tvName);


        }
    }

}