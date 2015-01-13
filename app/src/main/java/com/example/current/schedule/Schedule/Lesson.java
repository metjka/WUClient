package com.example.current.schedule.Schedule;

/**
 * Created by Current on 08.01.2015.
 */
public class Lesson {

    String lessonName;          //english, math, programming
    String lessonType;          //lection, lesson, lab
    String lessonImportance;    //exam, test, usual

    String room;                //RA 3
    String date;                //day, month, year
    String fromHour;            //8:00
    String toHour;              //12:00

    public Lesson(String lessonName, String lessonType, String lessonImportance, String room, String date, String fromHour, String toHour){
        this.lessonName = lessonName;
        this.lessonType = lessonType;
        this.lessonImportance = lessonImportance;

        this.room = room;
        this.date = date;
        this.fromHour = fromHour;
        this.toHour = toHour;
    }

    String getLessonName(){ return lessonName; }
    String getLessonType(){ return lessonType; }
    String getLessonImportance(){ return lessonImportance; }
    String getRoom() { return room; }
    String getDate() { return date; }
    String getFromHour(){ return fromHour; }
    String getToHour() {return toHour; }

}
