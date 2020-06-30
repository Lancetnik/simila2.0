package com.example.test;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class Adapter extends PagerAdapter {

    private List<Model> models;
    private LayoutInflater layoutInflater;
    private Context context;

    Adapter(List<Model> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (models != null && models.size() > 0) return models.size() * 1000;
        else return 1;
    }

    @Override
    public boolean isViewFromObject( View view,  Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem( ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item, container, false);

        ImageView imageView;
        int i = position % models.size();

        imageView = view.findViewById(R.id.current_img);
        imageView.setImageResource(models.get(i).getImage());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem( ViewGroup container, int position,  Object object) {
        container.removeView((View)object);
    }
}