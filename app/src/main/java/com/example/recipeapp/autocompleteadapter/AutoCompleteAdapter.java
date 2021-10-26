package com.example.recipeapp.autocompleteadapter;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Handler;

import com.example.recipeapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


// TODO
// - Contemplate removing selected items from the dropdown
// - Maybe design improvement
// - Maybe integrating a library
public class AutoCompleteAdapter extends ArrayAdapter<AdapterItem> {
    private List<AdapterItem> data;
    private CharSequence constraint = "";
    private TextView textView = null;
    private boolean customCreationEnabled = true;

    public AutoCompleteAdapter(@NonNull Context context, List<AdapterItem> data) {
        super(context, 0, new ArrayList<>());

        setData(data, false);
    }

    public AutoCompleteAdapter(@NonNull Context context, List<AdapterItem> data, TextView textView) {
        super(context, 0, new ArrayList<>());

        setData(data, false);
        this.textView = textView;
    }

    // Sets dropdown list data and reloads if applicable
    // TODO
    // Chek if customFilter.filter(...) works or not!
    public void setData(List<AdapterItem> data, Boolean shouldUpdateView) {
        this.data = data == null ? new ArrayList<>() : data;

        if (shouldUpdateView) {
            customFilter.filter(constraint);
        }
    }

    // Allows to create custom values
    public void setCustomCreationEnabled(boolean value) {
        customCreationEnabled = value;
    }

    // Custom filter helps to display all items containing the filtering text,
    // as opposed to regular filter which ignores if the the filtering text doesn't
    // start matching from the beginning
    @NonNull
    @Override
    public Filter getFilter() {
        return customFilter;
    }

    private Filter customFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<AdapterItem> suggestions = new ArrayList<>();

            // Saving the `constraint` to use inside `setData`
            AutoCompleteAdapter.this.constraint = constraint;

            if (constraint == null || constraint.length() == 0) {
                // Empty constraint validates all the items
                suggestions.addAll(new ArrayList<>(data));
            } else {
                // Adding the 'superset' (filtering text / constraint in the 'subset' here) items
                String filterPattern = constraint.toString().toLowerCase().trim();

                if (customCreationEnabled) suggestions.add(new AdapterItem(filterPattern, 0));

                // Extracting the selected values for this adapter
                List<String> selectedValues = null;
                if (textView != null) {
                    View view = (View) textView;
                    selectedValues = (List<String>) view.getTag(R.string.AUTO_COMPLETE_ADAPTER_SELECTED_VALUES_KEY);
                }

                if (selectedValues == null) {
                    selectedValues = new ArrayList<String>();
                }
                
                for (AdapterItem item : data) {
                    String itemName = item.getLabel().toLowerCase();

                    if (!selectedValues.contains(itemName) &&
                            (!customCreationEnabled || !itemName.equals(filterPattern)) &&
                            itemName.contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((AdapterItem) resultValue).getLabel();
        }
    };

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            // Creating the view using customized layout
            convertView = LayoutInflater.from(getContext()).inflate(
                R.layout.auto_complete_row, parent, false
            );
        }

        TextView textView = convertView.findViewById(R.id.auto_complete_item_text);
        ImageView imageView = convertView.findViewById(R.id.auto_complete_item_image);

        AdapterItem item = getItem(position);

        if (item != null) {
            String label = item.getLabel();
            if (label != null) {
                textView.setText(label);
            }

            // `0` implies no image provided
            int image = item.getImage();
            if (image != 0) {
                imageView.setImageResource(image);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}