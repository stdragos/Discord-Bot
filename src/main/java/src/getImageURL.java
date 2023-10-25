package src;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.json.*;

public class getImageURL{
    public static ArrayList<ArrayList<String>> getURL(String query, int page) throws IOException, InterruptedException{

        query = query.trim().replaceAll(" +", " ");
        query = query.replaceAll(" ", "+");
        String URL = "https://pixabay.com/api/?key=15445879-73aeef906e52e4f5a15f6e971&q=" + query + "&image_type=photo&per_page=200&page=" + page;
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        System.out.println(URL);
        String jsonString;
        try {
            jsonString = GetRequestAPI.getReq(URL);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONArray arr;
        JSONObject obj;
        try {
            obj = new JSONObject(jsonString);
            arr = obj.getJSONArray("hits");
            if(arr.isEmpty())
                throw new JSONException("fail?");
        } catch (JSONException e) {
            --page;
            if(page==0)
                return result;
            URL = "https://pixabay.com/api/?key=15445879-73aeef906e52e4f5a15f6e971&q=" + query + "&image_type=photo&per_page=200&page=" + page;
            jsonString = GetRequestAPI.getReq(URL);

            obj = new JSONObject(jsonString);
            arr = obj.getJSONArray("hits");

        }
        System.out.println(arr.length());

        ArrayList<String> hits = new ArrayList<String>();
        ArrayList<String> url = new ArrayList<String>();
        ArrayList<String> tags = new ArrayList<String>();
        for(int i = 0; i < arr.length(); ++i) {
            String webformatURL = arr.getJSONObject(i).getString("webformatURL");
            String pageURL = arr.getJSONObject(i).getString("pageURL");
            String tag = arr.getJSONObject(i).getString("tags");
            hits.add(webformatURL);
            url.add(pageURL);
            tags.add(tag);
        }

        result.add(hits);
        result.add(url);
        result.add(tags);
        ArrayList<String> foo = new ArrayList<String>();
        foo.add(""+page);
        result.add(foo);

        return result;
    }
}
