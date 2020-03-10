package com.example.client_poker;

import android.widget.ImageView;

public class MyRunnable3 implements Runnable {
    private int data;
    private ImageView card1;


    public MyRunnable3 (int data, ImageView card1) {
        this.data = data;
        this.card1 = card1;



    }

    @Override
    public void run() {
        card1.setImageResource(data);
    }
}
