package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.Shared;
import io.github.zeroone3010.yahueapi.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ListHueRooms {
    private static final Logger log = LoggerFactory.getLogger(ListHueRooms.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        if (args.length < 1) {
            log.error("You must specify the following parameters (in order):");
            log.error("  - The Hue bridge's username/API key");
            log.error("");
            log.error("Example: ./list-hue-rooms.sh USERNAME");
            log.error("");
            log.error("NOTE: The Hue bridge's IP address is automatically discovered");

            System.exit(1);
        }

        HueShared.setUsername(args[0]);

        // Get the light name and log it
        List<String> roomNameList = HueShared.getRooms().stream()
                // Get the light name and log it
                .map(Room::getName)
                .collect(Collectors.toList());

        Shared.logObjectAsJson(roomNameList);
    }
}
