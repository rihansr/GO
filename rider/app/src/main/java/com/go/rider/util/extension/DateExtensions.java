package com.go.rider.util.extension;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.go.rider.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateExtensions {

    private Context mContext;
    private long    longTime;
    private String  date;
    private String  time;

    public DateExtensions() {}

    public DateExtensions(Context mContext, String date) {
        this.mContext = mContext;
        this.date = date;
    }

    public DateExtensions(String date) {
        this.date = date;
    }

    public DateExtensions(String date, String time) {
        this.date = date;
        this.time = time;
    }

    public DateExtensions(Context mContext, long longTime) {
        this.mContext = mContext;
        this.longTime = longTime;
    }

    public DateExtensions(long longTime) {
        this.longTime = longTime;
    }

    public static long currentTime(){
        return System.currentTimeMillis();
    }

    public long longFormat() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());

        long totalTime = 0;

        try {
            if(date == null || time == null) return 0;
            Date d = format.parse(date + " " + time);
            long loadTime = d != null ? d.getTime() : 0;
            long currentTime = System.currentTimeMillis();

            if(currentTime < loadTime){
                totalTime = loadTime;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return totalTime;
    }

    public long getBirthDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        long finalTime = 0;

        try {
            if(date == null) return 0;
            Date d = format.parse(date);
            finalTime = d != null ? d.getTime() : 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return finalTime;
    }

    public String dateFormat() {
        return new SimpleDateFormat("d MMM", Locale.getDefault()).format(longTime);
    }

    public String expenseDateFormat() {
        return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(longTime);
    }

    public String defaultDateFormat() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(longTime);
    }

    public String defaultTimeFormat() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(longTime);
    }

    public String tripDateFormat() {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(longTime);
    }

    public String defaultDateTimeFormat() {
        return new SimpleDateFormat("d/M/yy, h:mm a", Locale.getDefault()).format(longTime);
    }

    public Integer getAge() {
        if(date == null) return 0;
        int age = 0;
        try {
            Date birthDate = new Date();
            Date currentDate = new Date(System.currentTimeMillis());

            try {
                birthDate = new SimpleDateFormat("MM/DD/YYYY", Locale.getDefault()).parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (birthDate != null) {
                DateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                int d1 = Integer.parseInt(formatter.format(birthDate));
                int d2 = Integer.parseInt(formatter.format(currentDate));
                age = (d2 - d1) / 10000;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return age == 0 ? null : age;
    }

    public int getYears() {
        if(longTime <= 0) return 0;
        Date givenDate = new Date(longTime);
        Date currentDate = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int d1 = Integer.parseInt(formatter.format(givenDate));
        int d2 = Integer.parseInt(formatter.format(currentDate));
        return (d2-d1) / 10000;
    }


    public String[] getTime(){
        String[] time = new String[2];
        int seconds = (int) (this.longTime / 1000);
        int minutes = seconds / 60;
        int hour = minutes / 60;
        int day = hour / 24;

        if (minutes >= 1) {
            if (hour >= 1) {
                if (day >= 1) {
                    time[0] = String.valueOf(day);
                    time[1] = mContext.getResources().getString(R.string.day);
                } else {
                    time[0] = String.valueOf(hour);
                    time[1] = mContext.getResources().getString(R.string.hour);
                }
            } else {
                time[0] = String.valueOf(minutes);
                time[1] = mContext.getResources().getString(R.string.minute);
            }
        } else {
            time[0] = String.valueOf(seconds);
            time[1] = mContext.getResources().getString(R.string.second);
        }

        return time;
    }
}
