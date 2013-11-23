package city.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class CityClock {
	
	CityGui cityGui;
	Timer cityTime;
	Timer checkTimer;
	long beginTime;
	dayStates dayState;
	int day;
	int week;
	
	public enum dayStates {morning, afternoon, night};
	
	public CityClock(CityGui cityGui){
		this.cityGui= cityGui;
	}

	public void startTime(){
		
		// Begin actual city timer
		cityTime = new Timer(179999, // Start of day, 179999 (day is three minutes)
				new ActionListener() { public void actionPerformed(ActionEvent event) {
					// Timer is done, it is now a new day
					if (day == 7){
						day = 0;
						week++;
					} else {
						day++;
					}
					cityTime.restart();
					beginTime = System.currentTimeMillis();
					System.out.println("Today is a new day: " + day + " - " + getDayOfWeek());
		      }
		});
		cityTime.start();
		beginTime = System.currentTimeMillis();
		
		// Initial setup
		dayState = dayStates.morning;
		day = 1;
		week = 1;
		
		// Begin checker/notification timer to manually adjust day/night
		checkTimer = new Timer(1875, // Messages to people in city fire every 2 seconds
				new ActionListener() { public void actionPerformed(ActionEvent event) {
					// Message all people saying the current time is:
					// System.currentTimeMillis() - beginTime
					if (getCurrentTime() <= 59999){
						dayState = dayStates.morning;
					} else if (getCurrentTime() >= 60000 && getCurrentTime() <= 119999){
						dayState = dayStates.afternoon;
					} else  if (getCurrentTime() >= 120000){
						dayState = dayStates.night;
					}
					//System.out.println("Time since start is " + getCurrentTime() + ", day is " + day + " (" + getDayOfWeek() + "), portion of day is " + getDayState());
					//System.out.println("Human time is " + getHumanTime());
					cityGui.timerTick(getCurrentTime(), getHourOfDayInHumanTime(), getMinuteOfDay(), getDayState(), getAmPm());
					checkTimer.restart(); // Restarts every two seconds
		      }
		});
		checkTimer.start();
	}
	
	public int getCurrentTime(){
		return (int)(System.currentTimeMillis() - beginTime);
	}
	
	public long getCurrentLongTime(){
		return (System.currentTimeMillis() - beginTime);
	}
	
	public String getDayOfWeek(){
		String dayText = "";
		switch(day){
			case 1:
				dayText = "Monday";
				break;
			case 2:
				dayText = "Tuesday";
				break;
			case 3:
				dayText = "Wednesday";
				break;
			case 4:
				dayText = "Thursday";
				break;
			case 5:
				dayText = "Friday";
				break;
			case 6:
				dayText = "Saturday";
				break;
			case 7:
				dayText = "Sunday";
				break;
			default:
				dayText = "";
				break;
		}
		return dayText;
	}
	
	public String getDayState(){
		if (dayState.equals(dayState.morning)){
			return "Morning";
		} else if (dayState.equals(dayState.afternoon)){
			return "Afternoon";
		} else {
			return "Night";
		}
	}
	
	public long getHourOfDayInMilTime(long baseTime){ // You should normally pass in getCurrentTime() by default
		long calcHour = baseTime * 1440;
		long hourFinalCalc = calcHour / 179999;
		return hourFinalCalc / 60;
	}
	
	public int getHourOfDayInHumanTime(){
		long hours = getHourOfDayInMilTime(getCurrentLongTime()) % 12;
		return (int)hours;
	}
	
	public long getMinuteOfDay(long baseTime){ // You should normally pass in getCurrentTime() by default
		long calcMinute = baseTime * 1440;
		long minuteFinalCalc = calcMinute / 179999;
		return minuteFinalCalc % 60;
	}
	
	public long getMinuteOfDay(){ // You should normally pass in getCurrentTime() by default
		long calcMinute = getCurrentTime() * 1440;
		long minuteFinalCalc = calcMinute / 179999;
		return minuteFinalCalc % 60;
	}
	
	public String getHumanTime(int baseHours, int baseMinutes){
		String amPm = "";
		int hours = baseHours % 12;
		if (baseHours <= 11){
			amPm = "am"; 
		} else {
			amPm = "pm";
		}
		return hours + ":" + baseMinutes + amPm;
	}
	
	public String getHumanTime(){
		String amPm = "";
		long minutes = getMinuteOfDay(getCurrentLongTime());
		long hours = getHourOfDayInMilTime(getCurrentLongTime()) % 12;
		String leadingDisplayZero = "";
		if (hours == 0){
			hours = 12;
		}
		if (minutes < 10){
			leadingDisplayZero = "0";
		}
		if (getHourOfDayInMilTime(getCurrentTime()) <= 11){
			amPm = "am"; 
		} else {
			amPm = "pm";
		}
		return hours + ":" + leadingDisplayZero + minutes + amPm;
	}
	
	public String getAmPm(){
		String amPm = "";
		if (getHourOfDayInMilTime(getCurrentTime()) <= 11){
			amPm = "am"; 
		} else {
			amPm = "pm";
		}
		return amPm;
	}
	
}
