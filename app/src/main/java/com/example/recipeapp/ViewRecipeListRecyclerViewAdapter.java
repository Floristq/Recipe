package com.example.recipeapp;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipeapp.databinding.FragmentViewRecipeItemBinding;

import java.util.List;

public class ViewRecipeListRecyclerViewAdapter extends RecyclerView.Adapter<ViewRecipeListRecyclerViewAdapter.ViewHolder> {

    private final List<ViewRecipe.RecipeItem> data;

    public ViewRecipeListRecyclerViewAdapter(List<ViewRecipe.RecipeItem> items) {
        data = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentViewRecipeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ViewRecipe.RecipeItem curItem = holder.item = data.get(position);
        holder.recipeNameText.setText(curItem.name);
        holder.recipeImage.setImageURI(curItem.imageUri);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView recipeNameText;
        public final ImageView recipeImage;
        public ViewRecipe.RecipeItem item;

        public ViewHolder(FragmentViewRecipeItemBinding binding) {
            super(binding.getRoot());
            recipeNameText = binding.recipeName;
            recipeImage = binding.recipeImage;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item.id + "'";
        }
    }
}