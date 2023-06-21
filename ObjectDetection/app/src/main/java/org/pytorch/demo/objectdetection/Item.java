package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;

public class Item {
    String Title;
    String Description;
    String path;
    String latitude;
    String longitude;
    Bitmap Image;


    public  Item(String title, String Description, Bitmap image, String path, String latitude, String longitude){
        this.Title=title;
        this.Description=Description;
        this.Image=image;
        this.path=path;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Bitmap getImage() {
        return Image;
    }

    public void setImage(Bitmap image) {
        Image = image;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
