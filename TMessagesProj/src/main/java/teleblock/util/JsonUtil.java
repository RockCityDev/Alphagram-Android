package teleblock.util;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static Moshi moshi = new Moshi.Builder().addLast(new KotlinJsonAdapterFactory()).build();

    /**
     * 把一个对象变成json字符串
     *
     * @return
     */
    public static <T> String parseObjToJson(T t) {
        JsonAdapter<T> jsonAdapter = moshi.adapter((Type) Object.class);
        return jsonAdapter.toJson(t);
    }

    /**
     * 把一个json字符串变成对象
     *
     * @param json
     * @param cls
     * @return
     */
    public static <T> T parseJsonToBean(String json, Class<T> cls) {
        JsonAdapter<T> jsonAdapter = moshi.adapter(cls);
        try {
            return jsonAdapter.fromJson(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 把json字符串变成map
     *
     * @param json
     * @return
     */
    public static <K, V> Map<K, V> parseJsonToMap(String json, Class<K> keyCls, Class<V> valueCls) {
        Type type = Types.newParameterizedType(Map.class, keyCls, valueCls);
        JsonAdapter<Map<K, V>> adapter = moshi.adapter(type);
        Map<K, V> map;
        try {
            map = adapter.fromJson(json);
        } catch (Exception e) {
            map = new HashMap<>();
        }
        return map;
    }

    /**
     * 把json字符串变成集合
     *
     * @param json
     * @return
     */
    public static <T> List<T> parseJsonToList(String json, Class<T> cls) {
        Type type = Types.newParameterizedType(List.class, cls);
        JsonAdapter<List<T>> adapter = moshi.adapter(type);
        List<T> list;
        try {
            list = adapter.fromJson(json);
        } catch (Exception e) {
            list = new ArrayList<>();
        }
        return list;
    }
}