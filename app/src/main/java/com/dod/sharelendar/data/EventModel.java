package com.dod.sharelendar.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class EventModel implements Serializable {

    @SerializedName("calendar")
    private String calendar;
    @SerializedName("color")
    private String color;
    @SerializedName("event_date")
    private Date eventDate;
    @SerializedName("event_name")
    private String eventName;
    @SerializedName("event_comment")
    private String eventComment;
    @SerializedName("make_user")
    private String makeUser;
    @SerializedName("every_year")
    private boolean everyYear;
    @SerializedName("make_date")
    private Date makeDate;

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventComment() {
        return eventComment;
    }

    public void setEventComment(String eventComment) {
        this.eventComment = eventComment;
    }

    public String getMakeUser() {
        return makeUser;
    }

    public void setMakeUser(String makeUser) {
        this.makeUser = makeUser;
    }

    public boolean isEveryYear() {
        return everyYear;
    }

    public void setEveryYear(boolean everyYear) {
        this.everyYear = everyYear;
    }

    public Date getMakeDate() {
        return makeDate;
    }

    public void setMakeDate(Date makeDate) {
        this.makeDate = makeDate;
    }

    @Override
    public String toString() {
        return "EventModel{" +
                "calendar='" + calendar + '\'' +
                ", color='" + color + '\'' +
                ", eventDate=" + eventDate +
                ", eventName='" + eventName + '\'' +
                ", eventComment='" + eventComment + '\'' +
                ", makeUser='" + makeUser + '\'' +
                ", everyYear=" + everyYear +
                ", makeDate=" + makeDate +
                '}';
    }
}
