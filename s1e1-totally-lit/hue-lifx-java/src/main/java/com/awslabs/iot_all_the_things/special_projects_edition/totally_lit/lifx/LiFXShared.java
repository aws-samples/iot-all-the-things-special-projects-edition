package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.lifx;

import com.github.besherman.lifx.LFXClient;
import com.github.besherman.lifx.LFXLight;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LiFXShared {
    private static final Logger log = LoggerFactory.getLogger(LiFXShared.class);
    public static final LFXClient client = new LFXClient();

    public static void initializeLiFXClient() {
        if (isRunningInDocker()) {
            log.error("LiFX support requires access to the local network to send and receive broadcast payloads.");
            log.error("Currently we do not know a reliable way to do this in Docker.");
            log.error("");
            log.error("If you do please let us know via a Github issue or PR!");
            System.exit(1);
        }
        // Open a blocking LiFX client and fail immediately if this blows up
        Try.run(() -> client.open(true)).get();
    }

    public static void requireAtLeastOneLight() {
        // Make sure there are some lights. If not give the user some hints on what might be wrong.
        if (getLightCount() == 0) {
            log.error("No LiFX lights detected. If you have LiFX lights on your network try turning off your firewall and run this program again.");
            log.error("");
            log.error("On MacOS you can disable your firewall by running this command: sudo pfctl -F all");
            System.exit(1);
        }
    }

    private static long getLightCount() {
        return getLightStream().count();
    }

    public static Stream<LFXLight> getLightStream() {
        Iterator<LFXLight> lightIterator = client.getLights().iterator();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(lightIterator, Spliterator.ORDERED), false);
    }

    public static boolean isRunningInDocker() {
        String proc1CgroupContents = Try.of(() -> readFileAsString(new File("/proc/1/cgroup"))).getOrElse("");

        return proc1CgroupContents.contains(":/docker/");
    }

    public static String readFileAsString(File file) {
        return new String(Try.of(() -> Files.readAllBytes(file.toPath())).get());
    }
}