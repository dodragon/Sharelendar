package com.dod.sharelendar.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DayModel {
    private String calendarNumber;
    private int year;
    private int month;
    private int day;
    private String week;
    private Date date;
    private int moonMonth;
    private int moonDay;
    private List<EventModel> schedules = new ArrayList<>();

    public String getCalendarNumber() {
        return calendarNumber;
    }

    public void setCalendarNumber(String calendarNumber) {
        this.calendarNumber = calendarNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMoonMonth() {
        return moonMonth;
    }

    public void setMoonMonth(int moonMonth) {
        this.moonMonth = moonMonth;
    }

    public int getMoonDay() {
        return moonDay;
    }

    public void setMoonDay(int moonDay) {
        this.moonDay = moonDay;
    }

    public List<EventModel> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<EventModel> schedules) {
        this.schedules = schedules;
    }

    @Override
    public String toString() {
        return "DayModel{" +
                "calendarNumber='" + calendarNumber + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", week='" + week + '\'' +
                ", date=" + date +
                ", moonMonth=" + moonMonth +
                ", moonDay=" + moonDay +
                ", schedules=" + schedules +
                '}';
    }
}
