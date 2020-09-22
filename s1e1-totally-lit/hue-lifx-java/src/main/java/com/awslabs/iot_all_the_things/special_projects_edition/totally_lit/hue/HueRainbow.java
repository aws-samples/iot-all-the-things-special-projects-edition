package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HueRainbow {
    private static final Logger log = LoggerFactory.getLogger(HueRainbow.class);
    private static final float HUE_INCREMENT = (float) 0.001;
    private static final float FIXED_BRIGHTNESS = 1;
    private static final float FIXED_SATURATION = 1;
    private static float currentHueNumber = 0;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        HueShared.exitAndShowUsageIfNoArgs(args, "hue-rainbow.sh");

        HueShared.setUsername(args[0]);

        List<Light> lights = HueShared.getLightsOrExitIfEmpty(args);

        log.info("Rainbow with " + lights.size() + " light(s) starting now. Press CTRL-C to stop.");

        while (true) {
            Color currentColor = getNextColor();
            lights.forEach(light -> HueRainbow.setNextColor(light, currentColor));
            Shared.sleep(250);
        }
    }

    private static Color getNextColor() {
        currentHueNumber += HUE_INCREMENT;

        if (currentHueNumber > 1) {
            currentHueNumber = 0;
        }

        return Color.getHSBColor(currentHueNumber, FIXED_SATURATION, FIXED_BRIGHTNESS);
    }

    private static void setNextColor(Light light, Color color) {
        light.setState(State.builder().color(color).keepCurrentState());
    }
}
