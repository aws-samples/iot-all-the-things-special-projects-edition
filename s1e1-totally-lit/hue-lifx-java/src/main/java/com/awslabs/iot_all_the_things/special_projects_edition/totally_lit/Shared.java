package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit;

import com.google.gson.GsonBuilder;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class Shared {
    private static final Logger log = LoggerFactory.getLogger(Shared.class);

    public static String toJson(Object object) {
        String temp = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
                .toJson(object);

        byte[] utf8JsonString = Try.of(() -> temp.getBytes("UTF8")).get();
        return new String(utf8JsonString, Charset.defaultCharset());
    }

    public static void logObjectAsJson(Object object) {
        logString(toJson(object));
    }

    public static void logString(String json) {
        // Leading new line makes it easier to copy and paste the JSON if necessary
        log.info("\n" + json);
    }

    public static void sleep(long millis) {
        Try.run(() -> Thread.sleep(millis)).get();
    }
}
