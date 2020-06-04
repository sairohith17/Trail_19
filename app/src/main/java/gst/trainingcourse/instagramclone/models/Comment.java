package gst.trainingcourse.instagramclone.models;

public class Comment {

    private String comment;
    private String publisher;
    private String commentid;
    private String timecomment;

    public Comment(String comment, String publisher, String commentid, String timecomment) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.timecomment = timecomment;
    }

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public String getTimecomment() {
        return timecomment;
    }

    public void setTimecomment(String timecomment) {
        this.timecomment = timecomment;
    }
}
