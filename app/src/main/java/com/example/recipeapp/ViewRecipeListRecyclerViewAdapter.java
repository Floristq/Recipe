package com.example.recipeapp;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.recipeapp.databinding.FragmentViewRecipeItemBinding;
import com.example.recipeapp.ViewRecipe;

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

        ImageView img = holder.recipeImage;

        Glide.with(img.getContext())
            .load(curItem.imageUrl)
            .placeholder(R.drawable.recipe_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(img);

        ((View) img.getParent()).setOnClickListener(view -> {
            this.onItemClick(view, holder);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void onItemClick(View view, ViewHolder holder) {
        Bundle bundle = new Bundle();
        bundle.putString("id", holder.item.id);

        Navigation.findNavController(view).navigate(R.id.recipe_Item, bundle);
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