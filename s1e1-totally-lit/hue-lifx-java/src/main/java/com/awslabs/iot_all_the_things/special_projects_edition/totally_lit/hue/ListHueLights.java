package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class ListHueLights {
    private static final Logger log = LoggerFactory.getLogger(ListHueLights.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        if (args.length < 1) {
            log.error("You must specify the following parameters (in order):");
            log.error("  - The Hue bridge's username/API key");
            log.error("");
            log.error("Example: ./list-hue-lights.sh USERNAME");
            log.error("");
            log.error("NOTE: The Hue bridge's IP address is automatically discovered");

            System.exit(1);
        }

        HueShared.setUsername(args[0]);

        Shared.logObjectAsJson(HueShared.getLightMap());
    }
}
