package gst.trainingcourse.instagramclone.models;

import java.util.ArrayList;

public class Post {

    private String postid;
    private String description;
    private String publisher;
    private ArrayList<String> postimages;
    private String timepost;

    public Post() {
    }

    public Post(String postid, String description, String publisher, ArrayList<String> postimages, String timepost) {
        this.postid = postid;
        this.description = description;
        this.publisher = publisher;
        this.postimages = postimages;
        this.timepost = timepost;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public ArrayList<String> getPostimages() {
        return postimages;
    }

    public void setPostimages(ArrayList<String> postimages) {
        this.postimages = postimages;
    }

    public String getTimepost() {
        return timepost;
    }

    public void setTimepost(String timepost) {
        this.timepost = timepost;
    }
}
