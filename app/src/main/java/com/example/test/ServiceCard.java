package com.example.test;

public class ServiceCard {

    private int image;
    public services flag;

    public ServiceCard(int image, services flag) {
        this.image = image;
        this.flag = flag;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}



