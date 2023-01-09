package teleblock.translate.trans;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import teleblock.translate.callback.TranslateCallBack;


/**
 * Created by LSD on 2021/12/22.
 * Desc
 */
public class GoogleTranslator extends AbsTranslator {

    private static String url = "https://translate.googleapis.com/translate_a/single";

    private String[] userAgents = new String[] {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36", // 13.5%
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36", // 6.6%
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0", // 6.4%
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:95.0) Gecko/20100101 Firefox/95.0", // 6.2%
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36", // 5.2%
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.55 Safari/537.36" // 4.8%
    };

    public GoogleTranslator(Context context) {
        super(context, url);
    }

    @Override
    void setLangSupport() {

    }

    @Override
    Map<String, String> getParams(String from, String to, String content) {
        return null;
    }

    @Override
    Map<String, String> getHeaders() {
        return null;
    }

    @Override
    String parses(String text) {
        return null;
    }

    @Override
    public void run(String fromLanguage, String toLanguage, String text, TranslateCallBack tBack) {
//        if (loadingDialog != null && !loadingDialog.isShowing()) loadingDialog.show();
        new Thread() {
            @Override
            public void run() {
                String uri = "";
                HttpURLConnection connection = null;
                try {
                    uri = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=";
                    uri += Uri.encode(fromLanguage);
                    uri += "&tl=";
                    uri += Uri.encode(toLanguage);
                    uri += "&dt=t&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&kc=7&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&q=";
                    uri += Uri.encode(text.toString());
                    connection = (HttpURLConnection) new URI(uri).toURL().openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", userAgents[(int) Math.round(Math.random() * (userAgents.length - 1))]);
                    connection.setRequestProperty("Content-Type", "application/json");

                    StringBuilder textBuilder = new StringBuilder();
                    try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")))) {
                        int c = 0;
                        while ((c = reader.read()) != -1) {
                            textBuilder.append((char) c);
                        }
                    }
                    String jsonString = textBuilder.toString();

                    JSONTokener tokener = new JSONTokener(jsonString);
                    JSONArray array = new JSONArray(tokener);
                    JSONArray array1 = array.getJSONArray(0);
                    String result = "";
                    for (int i = 0; i < array1.length(); ++i) {
                        String blockText = array1.getJSONArray(i).getString(0);
                        if (blockText != null && !blockText.equals("null"))
                            result += /*(i > 0 ? "\n" : "") +*/ blockText;
                    }
                    if (text.length() > 0 && text.charAt(0) == '\n')
                        result = "\n" + result;
                    final String finalResult = result;
                    AndroidUtilities.runOnUIThread(() -> {
                        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.cancel();
                        if (tBack != null){
                            tBack.onSuccess(finalResult);
                        }
                    });
                } catch (Exception e) {
                    try {
                        Log.e("translate", "failed to translate a text " + (connection != null ? connection.getResponseCode() : null) + " " + (connection != null ? connection.getResponseMessage() : null));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                    AndroidUtilities.runOnUIThread(() -> {
                        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.cancel();
                        if (tBack != null){
                            tBack.onFail();
                        }
                    });
                }
            }
        }.start();
    }
}
