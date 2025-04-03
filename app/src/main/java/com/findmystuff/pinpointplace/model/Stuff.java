package com.findmystuff.pinpointplace.model;

/**
 * Created by Greg on 26/12/2016.
 */

public class Stuff {
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer idCategory;
    private String description;
    private String imagePath;
    private byte[] thumbnailByte;
    private String date;

    public Stuff (String name, Double latitude, Double longitude, String imagePath, Integer idCategory, String description, byte[] thumbnailByte, String date) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
        this.idCategory = idCategory;
        this.description = description;
        this.thumbnailByte = thumbnailByte;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public byte[] getThumbnailByte() {
        return thumbnailByte;
    }

    public void setThumbnailByte(byte[] thumbnailByte) {
        this.thumbnailByte = thumbnailByte;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
