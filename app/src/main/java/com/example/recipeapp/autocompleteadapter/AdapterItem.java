package com.example.recipeapp.autocompleteadapter;

public class AdapterItem {
    private String label;
    private int image;

    public AdapterItem(String label, int image) {
        this.label = label;
        this.image = image;
    }

    public String getLabel() {
        return label;
    }

    public int getImage() {
        return image;
    }
}