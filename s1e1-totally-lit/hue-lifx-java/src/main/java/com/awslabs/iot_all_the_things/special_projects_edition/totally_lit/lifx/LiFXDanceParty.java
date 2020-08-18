package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.lifx;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import com.github.besherman.lifx.LFXClient;
import com.github.besherman.lifx.LFXHSBKColor;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class LiFXDanceParty {
    private static final Logger log = LoggerFactory.getLogger(LiFXDanceParty.class);
    private static final Random random = new Random();
    private static final LFXClient client = new LFXClient();

    public static void main(String[] args) {
        Try.run(LiFXDanceParty::run)
                // Make sure we close the client no matter what happens, ignore this if it fails
                .andFinallyTry(client::close);
    }

    private static void run() {
        // Initialize the LiFX client
        LiFXShared.initializeLiFXClient();

        // Require at least one light
        LiFXShared.requireAtLeastOneLight();

        // Loop forever so the dance party doesn't end
        while (true) {
            // For each light set a random color
            LiFXShared.getLightStream()
                    .forEach(lfxLight -> lfxLight.setColor(getRandomColor()));
            // Sleep a little bit to give the lights a rest
            Shared.sleep(250);
        }
    }

    private static LFXHSBKColor getRandomColor() {
        // Hue range 0.0 - 360.0
        float hue = (float) (random.nextFloat() * 360.0);
        // Saturation range 0.0 - 1.0
        float saturation = random.nextFloat();
        // Brightness range 0.0 - 1.0
        float brightness = random.nextFloat();
        // Kelvin range 0 - 10000
        int kelvin = random.nextInt(10000);

        log.info(" - " + hue + ", " + saturation + ", " + brightness + ", " + kelvin);
        return new LFXHSBKColor(hue, saturation, brightness, kelvin);
    }
}
