package com.dod.sharelendar.data;

import java.util.List;

public class YearModel {
    private int year;
    private String calendarNumber;
    private List<MonthModel> monthList;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCalendarNumber() {
        return calendarNumber;
    }

    public void setCalendarNumber(String calendarNumber) {
        this.calendarNumber = calendarNumber;
    }

    public List<MonthModel> getMonthList() {
        return monthList;
    }

    public void setMonthList(List<MonthModel> monthList) {
        this.monthList = monthList;
    }

    @Override
    public String toString() {
        return "YearModel{" +
                "year=" + year +
                ", calendarNumber='" + calendarNumber + '\'' +
                ", monthList=" + monthList +
                '}';
    }
}
