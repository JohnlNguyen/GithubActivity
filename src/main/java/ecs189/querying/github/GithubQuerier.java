package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            JSONObject payload = event.getJSONObject("payload");
            JSONArray root = payload.getJSONArray("commits");
            if(root.isNull(0)) continue;
            JSONObject commit = root.getJSONObject(0);

            // Get SHA hash
            String SHA = commit.getString("sha");
            // Get commit message
            String message = commit.getString("message");
            // Get event type
            String type = event.getString("type");
            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Add type of event as header
            sb.append("<div class=\"container\">");
            sb.append("<div class=\"row\">");
            sb.append("<div class=\"col-md-4\">");
            sb.append("<h2>" + type + "</h2>");
            sb.append("</div>");
            sb.append("<div class=\"col-md-4\">");
            sb.append("<h3 id=\"date\">" + formatted + "</h3>"); // Add formatted date
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("<br />");

            // Add SHA
            sb.append("<div class=\"container\">");
            sb.append("<table id=\"table\" class=\"table table-bordered\""); // begin table
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th>SHA Hash</th>"); // Add SHA
            sb.append("<th>Commit Message</th>"); // Add commit message
            sb.append("<tr>");
            sb.append("</thead>");
            sb.append("<tbody>"); // begin tbody
            sb.append("<tr>");
            sb.append("<td>" + SHA.substring(0,8) + "</td>"); // SHA substring
            sb.append("<td>" + message + "</td>"); // commit message
            sb.append("</tr>");
            sb.append("</tbody>"); // end tbody
            sb.append("</table>"); // end table
            sb.append("</div>");
            sb.append("<br />");

            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<b> <a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a> </b>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\">");
            sb.append("<pre>" + event.toString() + "</div>");

        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        String access_token = "?access_token=7d316fab88881a06cf614ad20db93a8f2d5ba8fc";
        String url = BASE_URL + user + "/events" + access_token;
        System.out.println(url);
        // Go through up to 50 pages
        for (int page = 1; page < 50; page++) {
            String newUrl = url + "&page=" + page;
            JSONObject json = Util.queryAPI(new URL(newUrl));
            JSONArray events = json.getJSONArray("root");
            for (int i = 0; i < events.length() && eventList.size() < 10; i++) {
                JSONObject event = events.getJSONObject(i);
                String type = event.getString("type");
                if (type.equals("PushEvent")) {
                    eventList.add(events.getJSONObject(i));
                }
            }
            if(eventList.size() == 10) break;
        }
        return eventList;
    }
}