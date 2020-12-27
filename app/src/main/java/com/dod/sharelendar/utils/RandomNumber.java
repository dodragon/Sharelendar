package com.dod.sharelendar.utils;

import java.util.Random;

public class RandomNumber {

    private int length;



    public RandomNumber(int length) {
        super();
        this.length = length;
    }

    public String numberGen() {
        Random rand = new Random();
        String numStr = "";

        for(int i=0;i<length;i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr += ran;
        }
        return numStr;
    }
}
