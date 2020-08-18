package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import io.github.zeroone3010.yahueapi.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class HueOff {
    private static final Logger log = LoggerFactory.getLogger(HueOff.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        HueShared.exitAndShowUsageIfNoArgs(args, "hue-off.sh");

        HueShared.setUsername(args[0]);

        List<Light> lights = HueShared.getLightsOrExitIfEmpty(args);

        log.info("Turning off " + lights.size() + " light(s)");

        lights.forEach(Light::turnOff);
    }
}
