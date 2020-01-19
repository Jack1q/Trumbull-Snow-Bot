package webscrape;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * This program was created so that SJ & Trumbull students will no longer 
 * have to constantly refresh the Snow Day Calculator to see the odds of
 * cancellation. Now, if the percentage is sizable (>50), this bot will
 * post it on twitter, sending a notification to followers with the app.
 * 
 * 
 * @author Jack Donofrio
 * 
 */

public class SnowBot
{
  public static void main(final String[] args)
  {

    // This is so it posts every 8 hours. Additionally, Twitter does not accept
    // duplicate bot posts. Which means if there is no change in the percentage,
    // it will not post again unless it changes after the next 8hr interval

    final long timeInterval = 28000000; // 2.8e7 milliseconds = ~8 hours

    Runnable runnable = new Runnable()
    {

      public void run()
      {

        while (true)
        {
          // just change zip code, works anywhere
          String url = "https://www.swdycalc.com/06611";
          String tomDateRaw = ""; // raw date info
          String tomStatsRaw = ""; // raw % and inches info
          try
          {
            final Document document = Jsoup.connect(url).get();
            for (final Element row : document.select("ul.bargraph"))
            {

              // if it stops working, go back into site html and change values
              tomDateRaw =
                row.select(".row:nth-of-type(2) .col-sm-3:nth-of-type(1)")
                  .text();

              tomStatsRaw =
                row.select(".row:nth-of-type(2) .col-sm-3:nth-of-type(3)")
                  .text();

            }
          }
          catch (final Exception ex)
          {
            ex.printStackTrace();
          }

          double percent = prediction(tomStatsRaw);

          // posts if % is greater than or equal to 50.
          // will not post on weekends.
          if (percent >= 50 && !tomDateRaw.contains("Sat")
            && !tomDateRaw.contains("Sun"))
          {
            Twitter twitter = TwitterFactory.getSingleton();
            try
            {
              // this is what you see on twitter
              Status status =
                twitter.updateStatus("For tomorrow, "
                  + day(tomDateRaw.substring(0, 3) + " "
                    + tomDateRaw.substring(tomDateRaw.indexOf(" ") + 1))
                  + ", there is a " + percent + "% chance of cancellation\n"
                  + likelyResult(percent));

            }
            catch (TwitterException e)
            {
              e.printStackTrace();
            }
          }

          try
          {

            Thread.sleep(timeInterval);

          }
          catch (InterruptedException e)
          {

            e.printStackTrace();

          }

        }

      }

    };

    Thread thread = new Thread(runnable);

    thread.start();

  }

  public static String likelyResult(double percent)
  {
    if (percent > 50 && percent <= 75)
      return "\nDelay possible.";
    else if (percent > 75 && percent <= 99)
      return "\nCancellation likely.";
    return "";
  }

  public static String day(final String dayPrefix)
  {
    final String[] days = {"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};
    final String[] fullDays =
      {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
          "Saturday"};
    for (int i = 0; i < 7; i++)
    {
      if (dayPrefix.equals(days[i]))
        return fullDays[i];
    }
    return "";
  }

  // converts "Jan" to "January"
  public static String month(final String monthPrefix)
  {
    final String[] months =
      {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
          "Nov", "Dec"};
    final String[] fullMonths =
      {"January", "February", "March", "April", "May", "June", "July", "August",
          "September", "October", "November", "December"};
    for (int i = 0; i < 12; i++)
    {
      if (monthPrefix.equals(months[i]))
        return fullMonths[i];
    }
    return "";
  }

  // extracts percentage as a double type from the raw stats string
  public static double prediction(final String str)
  {
    return Double.parseDouble(str.substring(0, str.indexOf("%")));
  }

}
