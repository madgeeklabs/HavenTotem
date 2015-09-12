package com.mgl.totem.models;

import java.util.LinkedList;

/**
 * Created by goofyahead on 9/12/15.
 */
public class User {
    private String name;
    private String uniqueUserId;
    private String profilePic;
    private LinkedList<String> videoUrls;

    public User(String name, String uniqueUserId, String profilePic, LinkedList<String> videoUrls) {
        this.name = name;
        this.uniqueUserId = uniqueUserId;
        this.profilePic = profilePic;
        this.videoUrls = videoUrls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueUserId() {
        return uniqueUserId;
    }

    public void setUniqueUserId(String uniqueUserId) {
        this.uniqueUserId = uniqueUserId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public LinkedList<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(LinkedList<String> videoUrls) {
        this.videoUrls = videoUrls;
    }
}
