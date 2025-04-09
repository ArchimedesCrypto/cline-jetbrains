package com.cline.ui.settings;

public class CheckedItem {
    private String label;
    private boolean selected;

    public CheckedItem(String label, boolean selected) {
        this.label = label;
        this.selected = selected;
    }

    public String getLabel() {
        return label;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return label; // Display label in list
    }
}