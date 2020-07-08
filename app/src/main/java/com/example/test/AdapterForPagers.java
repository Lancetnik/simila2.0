package com.example.test;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class AdapterForPagers extends PagerAdapter {

    private List<ServiceCard> serviceCards;
    private LayoutInflater layoutInflater;
    private Context context;

    AdapterForPagers(List<ServiceCard> serviceCards, Context context) {
        this.serviceCards = serviceCards;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (serviceCards != null && serviceCards.size() > 0) return serviceCards.size() * 1000;
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
        int i = position % serviceCards.size();

        imageView = view.findViewById(R.id.current_img);
        imageView.setImageResource(serviceCards.get(i).getImage());

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