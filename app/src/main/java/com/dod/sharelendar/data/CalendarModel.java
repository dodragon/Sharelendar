package com.dod.sharelendar.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CalendarModel implements Serializable {

    @SerializedName("calendar_name")
    private String calendarName;
    @SerializedName("calendar_uuid")
    private String uuid;
    @SerializedName("host")
    private String host;
    @SerializedName("host_nickname")
    private String hostNickname;
    @SerializedName("calendar_img")
    private String img;
    @SerializedName("invite_link")
    private String link;
    @SerializedName("make_date")
    private Date makeDate;

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHostNickname() {
        return hostNickname;
    }

    public void setHostNickname(String hostNickname) {
        this.hostNickname = hostNickname;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getMakeDate() {
        return makeDate;
    }

    public void setMakeDate(Date makeDate) {
        this.makeDate = makeDate;
    }

    @Override
    public String toString() {
        return "CalendarModel{" +
                "calendarName='" + calendarName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", host='" + host + '\'' +
                ", hostNickname='" + hostNickname + '\'' +
                ", img='" + img + '\'' +
                ", link='" + link + '\'' +
                ", makeDate=" + makeDate +
                '}';
    }
}
