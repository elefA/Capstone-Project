package com.example.client_poker;

import android.media.Image;
import android.widget.ImageView;

public class MyRunnable1 implements Runnable {
    private int[] data;
    private ImageView card1;
    private ImageView card2;
    public MyRunnable1(int[] data, ImageView card1, ImageView card2) {
        this.data = data;
        this.card1 = card1;
        this.card2 = card2;
    }

    @Override
    public void run() {
        card1.setImageResource(data[0]);
        card2.setImageResource(data[1]);
    }
}
