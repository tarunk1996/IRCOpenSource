package com.example.ta.ircopensource;

/**
 * Created by TA on 06-07-2017.
 */

public class DataManager{

    private static DataManager dataManager;
    private String imageUrl;

    public static DataManager getInstance(){

        if(dataManager == null){
            dataManager = new DataManager();
        }
        return dataManager;
    }

    private DataManager(){}

    public String getImageUrl(){
        return imageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }




}