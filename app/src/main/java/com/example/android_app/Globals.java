package com.example.android_app;

public class Globals {

    private static final Globals instance = new Globals();

    private Globals() {}

    private int counter = 0;
    public void increase() {
        counter++;
    }
    public int getCounter() {
        return counter;
    }
    public static Globals getInstance() {
        return instance;
    }
}
