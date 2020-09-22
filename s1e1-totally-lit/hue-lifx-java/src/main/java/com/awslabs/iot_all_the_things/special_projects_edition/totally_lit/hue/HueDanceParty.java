package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class HueDanceParty {
    private static final Logger log = LoggerFactory.getLogger(HueDanceParty.class);
    private static final Random random = new Random();
    public static final int MAX_COLOR_VALUE = 256;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        HueShared.exitAndShowUsageIfNoArgs(args, "hue-dance-party.sh");

        HueShared.setUsername(args[0]);

        List<Light> lights = HueShared.getLightsOrExitIfEmpty(args);

        log.info("Dance party with " + lights.size() + " light(s) starting now. Press CTRL-C to stop.");

        while (true) {
            lights.forEach(HueDanceParty::setRandomColor);
            Shared.sleep(250);
        }
    }

    private static Color getRandomColor() {
        return new Color(random.nextInt(MAX_COLOR_VALUE), random.nextInt(MAX_COLOR_VALUE), random.nextInt(MAX_COLOR_VALUE));
    }

    private static void setRandomColor(Light light) {
        light.setState(State.builder().color(getRandomColor()).keepCurrentState());
    }
}
