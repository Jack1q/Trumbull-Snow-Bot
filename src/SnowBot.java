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
 * This program was created so that SJ & Trumbull students will no longer have
 * to constantly refresh the Snow Day Calculator to see the odds of
 * cancellation. Now, if the percentage is > 50%, this bot will post it on
 * twitter, notifying followers with ease.
 * 
 * The code is bad. I know. This was one of my first projects I made only 1.5
 * months after I started learning Java and I was super hyped when it worked.
 * 
 * @author Jack Donofrio
 * 
 */

public class SnowBot
{
  public static void main(final String[] args)
  {
    String url = "https://www.swdycalc.com/06611";
    String tommorowsDateRaw = ""; // raw date info
    String tommorowsStatsRaw = ""; // raw % and inches info
    try
    {
      final Document document = Jsoup.connect(url).get();
      for (final Element row : document.select("ul.bargraph"))
      {
        tommorowsDateRaw =
          row.select(".row:nth-of-type(2) .col-sm-3:nth-of-type(1)").text();
        tommorowsStatsRaw =
          row.select(".row:nth-of-type(2) .col-sm-3:nth-of-type(3)").text();
      }
    }

    catch (final Exception ex)
    {
      ex.printStackTrace();
    }

    double percent =
      Double.parseDouble(
        tommorowsStatsRaw.substring(0, tommorowsStatsRaw.indexOf("%")));

    // posts if % is greater than or equal to 50.
    // will not post on weekends.
    if (percent >= 50 && !tommorowsDateRaw.contains("Sat")
      && !tommorowsDateRaw.contains("Sun"))
    {
      Twitter twitter = TwitterFactory.getSingleton();
      try
      {
        // this is what you see on twitter
        Status status =
          twitter
            .updateStatus(
              "For tomorrow, "
                + fullWeekdayName(tommorowsDateRaw.substring(0, 3) + " "
                  + tommorowsDateRaw
                    .substring(tommorowsDateRaw.indexOf(" ") + 1))
                + ", there is a " + percent + "% chance of cancellation\n"
                + likelyResult(percent));
      }
      catch (TwitterException e)
      {
        e.printStackTrace();
      }
    }
  }

  public static String likelyResult(double percent)
  {
    if (percent > 50 && percent <= 75)
      return "\nDelay possible.";
    else if (percent > 75 && percent <= 99)
      return "\nCancellation likely.";
    return "";
  }

  // converts "Sun" to "Sunday"
  public static String fullWeekdayName(final String dayPrefix)
  {
    final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
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
  public static String fullMonthName(final String monthPrefix)
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
}