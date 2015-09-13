package com.mgl.totem.models;

/**
 * Created by goofyahead on 9/12/15.
 */
public class Registration {
    private String uniqueId;
    private String video;
    private String name;
    private String phone;

    public Registration(String uniqueId, String video, String name, String phone) {
        this.uniqueId = uniqueId;
        this.video = video;
        this.name = name;
        this.phone = phone;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
