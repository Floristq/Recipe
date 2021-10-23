package com.example.recipeapp;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.recipeapp.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.recipeapp.databinding.FragmentAdvanceItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> implements Filterable {

    private final List<String> mValues;
    private List<String> mValuesFiltered;
    private final List<String> selectedItem=new ArrayList<>();

    public MyItemRecyclerViewAdapter(List<String> items) {
        mValues = items;
        mValuesFiltered = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentAdvanceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.checkBox.setText(mValuesFiltered.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    selectedItem.add(mValuesFiltered.get(position));
                }else{
                    selectedItem.remove(mValuesFiltered.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValuesFiltered.size();
    }

    public List<String> getSelectedItem() {
        return selectedItem;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mValuesFiltered = mValues;
                } else {
                    List<String> filteredList = new ArrayList<>();
                    for (String row : mValues) {

                        // Name match condition.
                        // Looking for name match
                        if (row.toLowerCase().contains(charString.toLowerCase()) || row.contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    mValuesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mValuesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mValuesFiltered = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final CheckBox checkBox;

        public ViewHolder(FragmentAdvanceItemBinding binding) {
            super(binding.getRoot());
            checkBox = binding.checkbox;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

}