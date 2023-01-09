package teleblock.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Primitives;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


/**
 * Created by Dream on 2017/1/4.
 */

public class ParseJsonUtil {

    private static GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
    private static Gson gson = gsonBuilder.create();
    private static JsonParser jsonParser = new JsonParser();


    public static <T> T getObjectFromJson(JsonElement jsonElement, Class<T> classOfT) {
        try {
            Object object = gson.fromJson(jsonElement, classOfT);
            return Primitives.wrap(classOfT).cast(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> ArrayList<T> jsonToList(JsonElement json, Class<T> classOfT) {
        ArrayList<T> list = new ArrayList<T>();
        try {
            for (JsonElement obj : json.getAsJsonArray()) {
                list.add(getObjectFromJson(obj, classOfT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static JsonElement stringToJson(String json) {
        return jsonParser.parse(json);
    }

    public static String getJsonFromMap(Map<String, String> map) {
        JsonObject jsonObject = new JsonObject();

        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            jsonObject.addProperty(key, map.get(key));
        }

        return jsonObject.toString();
    }
}
