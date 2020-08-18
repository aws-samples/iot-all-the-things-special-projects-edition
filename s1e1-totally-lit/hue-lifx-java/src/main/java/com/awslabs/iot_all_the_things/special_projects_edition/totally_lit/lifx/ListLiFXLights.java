package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.lifx;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import com.github.besherman.lifx.LFXLight;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ListLiFXLights {
    private static final Logger log = LoggerFactory.getLogger(ListLiFXLights.class);

    public static void main(String[] args) {
        Try.run(ListLiFXLights::run)
                // Make sure we close the client no matter what happens, ignore this if it fails
                .andFinallyTry(LiFXShared.client::close);
    }

    private static void run() {
        // Initialize the LiFX client
        LiFXShared.initializeLiFXClient();

        // Require at least one light
        LiFXShared.requireAtLeastOneLight();

        // List all of the lights
        listAllLights();
    }

    public static void listAllLights() {
        LiFXShared.getLightStream()
                .forEach(ListLiFXLights::logLightInfo);
    }

    private static void logLightInfo(LFXLight lfxLight) {
        log.info("LiFX light:");
        log.info("\tId={}", lfxLight.getID());
        log.info("\tLabel={}", lfxLight.getLabel());
        log.info("\tPower={}", lfxLight.isPower());
        log.info("\tTime={}", lfxLight.getTime());
        log.info("\tColor={}", lfxLight.getColor());
    }
}