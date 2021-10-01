package authentication;

import java.util.Date;

public class Utils {
	// https://www.tipstocode.com/programming/how-to-calculate-time-ago-and-time-to-go-in-java/
	public static Integer timeAgo(Date currentDate, Date pastDate) {
	  long milliSecPerMinute = 60 * 1000; //Milliseconds Per Minute
	  long milliSecPerHour = milliSecPerMinute * 60; //Milliseconds Per Hour
	  long milliSecPerDay = milliSecPerHour * 24; //Milliseconds Per Day
	  long milliSecPerMonth = milliSecPerDay * 30; //Milliseconds Per Month
	  long milliSecPerYear = milliSecPerDay * 365; //Milliseconds Per Year
	  //Difference in Milliseconds between two dates
	  long msExpired = currentDate.getTime() - pastDate.getTime();
	  //Second or Seconds ago calculation
	    if (Math.round(msExpired / 1000) == 1) {
	      return Math.round(msExpired / 1000);
	    } else {
	      return Math.round(msExpired / 1000);
	    }
	}
}
