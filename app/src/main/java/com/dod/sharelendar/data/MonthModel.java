package com.dod.sharelendar.data;

import java.util.List;

public class MonthModel {
    private int year;
    private int month;
    private String calendarNumber;
    private List<DayModel> dayList;

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

    public String getCalendarNumber() {
        return calendarNumber;
    }

    public void setCalendarNumber(String calendarNumber) {
        this.calendarNumber = calendarNumber;
    }

    public List<DayModel> getDayList() {
        return dayList;
    }

    public void setDayList(List<DayModel> dayList) {
        this.dayList = dayList;
    }

    @Override
    public String toString() {
        return "MonthModel{" +
                "year=" + year +
                ", month=" + month +
                ", calendarNumber='" + calendarNumber + '\'' +
                ", dayList=" + dayList +
                '}';
    }
}
