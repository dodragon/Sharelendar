package com.dod.sharelendar.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class EventModel implements Serializable {

    @SerializedName("calendar")
    private String calendar;
    @SerializedName("color")
    private String color;
    @SerializedName("event_name")
    private String eventName;
    @SerializedName("make_user")
    private String makeUser;
    @SerializedName("start_date")
    private Date startDate;
    @SerializedName("end_date")
    private Date endDate;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getMakeUser() {
        return makeUser;
    }

    public void setMakeUser(String makeUser) {
        this.makeUser = makeUser;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
                ", eventName='" + eventName + '\'' +
                ", makeUser='" + makeUser + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", everyYear=" + everyYear +
                ", makeDate=" + makeDate +
                '}';
    }
}
