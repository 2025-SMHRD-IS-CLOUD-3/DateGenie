package com.smhrd.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;

public class Jsons {
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .disableHtmlEscaping()
            .create();

    public static String ok(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("message", "success");
        body.put("data", data);
        return GSON.toJson(body);
    }

    public static String error(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", false);
        body.put("message", message);
        return GSON.toJson(body);
    }

    public static Gson gson() { return GSON; }
}