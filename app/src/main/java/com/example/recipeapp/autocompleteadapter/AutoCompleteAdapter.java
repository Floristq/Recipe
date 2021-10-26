package com.example.recipeapp.autocompleteadapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Handler;

import com.example.recipeapp.R;
import com.google.android.material.chip.ChipGroup;

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
    private List<AdapterItem> data = new ArrayList<AdapterItem>();
    // TODO: Change type `String` to `AdapterItem`
    private List<String> selectedData = new ArrayList<String>();
    private CharSequence constraint = "";

    private Activity activity;
    private ChipGroup chipGroup;
    // TODO: Later allow more type of `input`s
    private AutoCompleteTextView input;

    private boolean customCreationEnabled = true;
    private boolean allowMultipleSelection = true;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public AutoCompleteAdapter(@NonNull Context context, AutoCompleteTextView input, ChipGroup chipGroup) {
        super(context, 0, new ArrayList<>());

        activity = (Activity) context;
        this.input = input;
        this.chipGroup = chipGroup;

        setCustomCreationEnabled(false);

        input.setAdapter(this);

        input.setOnItemClickListener((parent, v, position, id) -> {
            addSelectedData(input.getText().toString());

            input.setText("");
        });
    }


    // Sets dropdown list data and reloads if applicable
    // TODO
    // Chek if customFilter.filter(...) works or not applying the following
    // if (shouldUpdateView) {
    //     customFilter.filter(constraint);
    // }
    public void setData(List<?> rawData) {
        if (rawData != null) {
            if (rawData.size() > 0 && rawData.get(0).getClass().equals(String.class)) {
                for (Object item: rawData) {
                    data.add(new AdapterItem(item.toString(), 0));
                }
            } else {
                data = (List<AdapterItem>) rawData;
            }
        } else {
            data = new ArrayList<>();
        }
    }

    public void setData(String[] rawData) {
        if (rawData != null) {
            for (String item: rawData) {
                data.add(new AdapterItem(item, 0));
            }
        } else {
            data = new ArrayList<>();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addSelectedData(String value) {
        if (allowMultipleSelection) {
            chipGroup.clearCheck();
            selectedData = new ArrayList<String>();
        }

        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(activity);
        chip.setText(value);
        chip.setId(input.generateViewId());
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(chipView -> {
            chipGroup.removeView(chipView);
        });

        chipGroup.addView(chip);

        ChipGroup.LayoutParams chipLayoutParams = (ChipGroup.LayoutParams) chip.getLayoutParams();
        chipLayoutParams.rightMargin = 20;

        selectedData.add(value);
    }

    // Allows to create custom values
    public void setCustomCreationEnabled(boolean value) {
        customCreationEnabled = value;
    }

    public void setAllowMultipleSelection(boolean value) {
        allowMultipleSelection = value;
    }

    public List<String> getSelectedData() {
        return selectedData;
    }

    // Only when `allowMultipleSelection` is `false`
    public String getSelectedItem() {
        if (!allowMultipleSelection && selectedData.size() > 0) {
            return selectedData.get(0);
        }
        return null;
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
                
                for (AdapterItem item : data) {
                    String itemName = item.getLabel().toLowerCase();

                    if (!selectedData.contains(itemName) &&
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