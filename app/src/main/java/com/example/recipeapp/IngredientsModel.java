package com.example.recipeapp;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

public class IngredientsModel implements ChipInterface {
    private String ingredient;

    public IngredientsModel(String ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public Object getId() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return ingredient;
    }

    @Override
    public String getInfo() {
        return null;
    }
}
