package com.example.simpleexpensetracker.ui.theme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.simpleexpensetracker.R;

public class CategoryListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] categories;
    private final int[] icons;

    public CategoryListAdapter(Context context, String[] categories, int[] icons) {
        super(context, R.layout.item_category, categories);
        this.context = context;
        this.categories = categories;
        this.icons = icons;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder pattern for better performance
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.item_category, parent, false);
        }


        ImageView icon = rowView.findViewById(R.id.imgCategoryIcon);
        TextView text = rowView.findViewById(R.id.tvCategoryName);

        icon.setImageResource(icons[position]);
        text.setText(categories[position]);

        return rowView;
    }
}
