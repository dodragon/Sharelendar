package com.dod.sharelendar.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class UserCalendar implements Serializable {

    @SerializedName("email")
    private String email;
    @SerializedName("calendar_uuid")
    private String calendarUuid;
    @SerializedName("div")
    private String div;
    @SerializedName("join_date")
    private Date joinDate;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCalendarUuid() {
        return calendarUuid;
    }

    public void setCalendarUuid(String calendarUuid) {
        this.calendarUuid = calendarUuid;
    }

    public String getDiv() {
        return div;
    }

    public void setDiv(String div) {
        this.div = div;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public String toString() {
        return "UserCalendar{" +
                "email='" + email + '\'' +
                ", calendarUuid='" + calendarUuid + '\'' +
                ", div='" + div + '\'' +
                ", joinDate=" + joinDate +
                '}';
    }
}
