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
    @SerializedName("userNickname")
    private String userNickname;
    @SerializedName("repeat")
    private String repeat;
    @SerializedName("make_date")
    private Date makeDate;
    @SerializedName("event_uuid")
    private String eventUuid;
    @SerializedName("continuous")
    private boolean continuous;

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

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public Date getMakeDate() {
        return makeDate;
    }

    public void setMakeDate(Date makeDate) {
        this.makeDate = makeDate;
    }

    public String getEventUuid() {
        return eventUuid;
    }

    public void setEventUuid(String eventUuid) {
        this.eventUuid = eventUuid;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
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
                ", userNickname='" + userNickname + '\'' +
                ", repeat='" + repeat + '\'' +
                ", makeDate=" + makeDate +
                ", eventUuid='" + eventUuid + '\'' +
                ", continuous=" + continuous +
                '}';
    }
}
