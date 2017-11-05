package com.wally.bhegde;

import com.sun.jna.platform.win32.WinDef;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Bhargav on 30-06-2017.
 */
public class Wally {
    public static void changeWallpaper(String image){
        SPI.INSTANCE.SystemParametersInfo(
                new WinDef.UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
                new WinDef.UINT_PTR(0),
                image,
                new WinDef.UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
    }

    public static String getJsonData(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        String data = "";
        while ((line = rd.readLine()) != null) {
            data = data + line;
        }

        return data;
    }

    public static List<String> filterURLs(List<String> imgURLs){

        List<String> filteredList = new ArrayList<>();

        for(String imgURL : imgURLs){
            //imgURL.substring(imgURL.length() - 4)

            /*if(imgURL.contains("i.imgur.com") || imgURL.contains("imgur.com") || imgURL.contains("i.redd.it")){
                filteredList.add(imgURL);
            }*/

            if(".jpg".equals(imgURL.substring(imgURL.length() - 4)) || ".png".equals(imgURL.substring(imgURL.length() - 4))){
                filteredList.add(imgURL);
            }
        }

        return filteredList;
    }

    public static void getChildrenArray(JSONObject jsonObject, List<String> imgURLs){

        if(jsonObject.names()!=null)
            for(int i = 0; i<jsonObject.names().length(); i++){

                String key = jsonObject.names().getString(i);
                Object value = jsonObject.get(jsonObject.names().getString(i));
                if("url".equals(key)){
                    imgURLs.add((String) value);
                }
                else if("children".equals(key)){//TODO check for json arrray instead of the key value

                    JSONArray childrenArray =(JSONArray)value;

                    for(int j = 0; j< childrenArray.length(); j++){
                        JSONObject childrenObject = childrenArray.getJSONObject(j);

                        //call this method for each and every json object in this array
                        if(childrenObject!=null) {
                            getChildrenArray(childrenObject, imgURLs);
                        }
                    }
                }//'children' array handler
                else if (value instanceof JSONObject) {
                    if(value!=null) {
                        getChildrenArray((JSONObject) value, imgURLs);
                    }
                }
            }//loop through the json object
    }

    public static String selectRandomImg(List<String> imgURLs){
        String url = "https://i.redd.it/l17clu4t2n6z.png";//default url set

        Random randomizer = new Random();
        String randomURL = imgURLs.get(randomizer.nextInt(imgURLs.size()));

        if(!randomURL.isEmpty() && randomURL != null){
            url = randomURL;
        }

        return url;
    }

    private static void getImages(String src,String name) throws IOException {

        //Open a URL Stream
        URL url = new URL(src);
        InputStream in = url.openStream();

        OutputStream out = new BufferedOutputStream(new FileOutputStream( "C:\\Users\\Bhargav\\Pictures\\reddit_images\\"+ name));

        for (int b; (b = in.read()) != -1;) {
            out.write(b);
        }
        out.close();
        in.close();

    }

    public static void main(String[] args) throws IOException {
        //changeWallpaper("C:\\Users\\Bhargav\\Pictures\\image.png", "don't get too exited yet");

        //JSONObject jsonObject = new JSONObject(getJsonData("https://www.reddit.com/r/EarthPorn.json"));
        JSONObject jsonObject = new JSONObject(getJsonData("https://www.reddit.com/r/"+args[0]+".json"));
        //JSONObject jsonObject = new JSONObject(getJsonData("https://www.reddit.com/r/NSFW_Wallpapers.json"));

        List<String> imgURLs = new ArrayList<>();
        getChildrenArray(jsonObject, imgURLs);

        imgURLs = filterURLs(imgURLs);

        for(String imgURL : imgURLs){
            //System.out.println(imgURL);
            //System.out.println(imgURL.substring(imgURL.length() - 4));
        }

        //download one of the random pictures
        String imgURL = selectRandomImg(imgURLs);

        String extension = "jpg";
        if (".png".equals(imgURL.substring(imgURL.length() - 4))){
            extension = "png";
        }

        //System.out.println(imgURL);

        getImages(imgURL, "picture."+extension);

        //set it as the wallpaper
        changeWallpaper("C:\\Users\\Bhargav\\Pictures\\reddit_images\\picture."+extension);

    }
}