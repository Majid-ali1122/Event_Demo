package com.example.event_demo;

public class DataModel {

    private String eventtitle;
    private String date;
    private String time;
    private String status;

    public DataModel(String eventtitle, String date, String time, String status) {
        this.eventtitle = eventtitle;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getEventtitle() {
        return eventtitle;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

}
