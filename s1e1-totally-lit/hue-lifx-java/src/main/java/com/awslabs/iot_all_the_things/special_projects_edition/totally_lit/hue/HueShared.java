package com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue;

import io.github.zeroone3010.yahueapi.Hue;
import io.github.zeroone3010.yahueapi.HueBridge;
import io.github.zeroone3010.yahueapi.Light;
import io.github.zeroone3010.yahueapi.Room;
import io.github.zeroone3010.yahueapi.discovery.HueBridgeDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class HueShared {
    private static String username;
    private static Logger log = LoggerFactory.getLogger(HueShared.class);
    private static Optional<Hue> optionalHue = Optional.empty();

    public static void setUsername(String username) {
        HueShared.username = username;
    }

    public static Hue getHue() throws ExecutionException, InterruptedException {
        if (!optionalHue.isPresent()) {
            log.info("Discovering Hue bridge, this may take a moment...");
            optionalHue = Optional.of(new Hue(getHueBridgeIp(), username));
        }

        return optionalHue.get();
    }

    public static String getHueBridgeIp() throws ExecutionException, InterruptedException {
        Future<List<HueBridge>> bridgesFuture = new HueBridgeDiscoveryService()
                .discoverBridges(bridge -> log.info("Bridge found: " + bridge));

        final List<HueBridge> bridges = bridgesFuture.get();

        if (bridges.isEmpty()) {
            log.error("No Hue bridge found with auto-discovery");
            System.exit(1);
        } else if (bridges.size() > 1) {
            log.error("Multiple Hue bridges found with auto-discovery, this is currently unsupported!");
            System.exit(1);
        }

        return bridges.get(0).getIp();
    }

    public static List<Room> getRooms() throws ExecutionException, InterruptedException {
        return new ArrayList<>(getHue().getRooms());
    }

    public static List<Light> getLights() throws ExecutionException, InterruptedException {
        return getHue()
                // Get all of the rooms and turn it into a stream
                .getRooms().stream()
                // Get all of the lights for each room
                .map(Room::getLights)
                // Turn all of the lists of lights into a single stream of lights
                .flatMap(Collection::stream)
                // Collect them into a list
                .collect(Collectors.toList());
    }

    public static Map<String, List<String>> getLightMap() throws ExecutionException, InterruptedException {
        return getHue()
                // Get all of the rooms and turn it into a stream
                .getRooms().stream()
                // Create a map of the room name with each room's list of lights
                .collect(Collectors.toMap(Room::getName,
                        room -> new ArrayList<String>(room.getLights().stream().map(Light::getName).collect(Collectors.toList()))));
    }

    public static List<String> getOriginalLightAndRoomList(String[] args) {
        return Arrays.stream(args)
                // Skip the username argument
                .skip(1)
                .collect(Collectors.toList());
    }

    public static List<String> getCaseInsensitiveLightAndRoomList(List<String> originalLightAndRoomList) {
        return originalLightAndRoomList.stream()
                // Make all strings lowercase for case insensitive matching and return a list
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public static List<Light> getLightsByName(List<String> caseInsensitiveLightAndRoomList) throws ExecutionException, InterruptedException {
        return getLights().stream()
                // Find all the matching lights
                .filter(light -> caseInsensitiveLightAndRoomList.contains(light.getName().toLowerCase()))
                .collect(Collectors.toList());
    }

    public static List<Light> getLightsByRoomName(List<String> caseInsensitiveLightAndRoomList) throws ExecutionException, InterruptedException {
        return getRooms().stream()
                // Find all the matching rooms
                .filter(room -> caseInsensitiveLightAndRoomList.contains(room.getName().toLowerCase()))
                // Extract all of the lights from those rooms
                .map(Room::getLights)
                // Flatten them into a single stream and grab the entire list
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<Light> getFullLightListOrExitIfEmpty(List<String> originalLightAndRoomList, List<Light> lightsByName, List<Light> lightsByRoomName) {
        List<Light> lights = new ArrayList<>();
        lights.addAll(lightsByName);
        lights.addAll(lightsByRoomName);

        if (lights.size() == 0) {
            log.error("No lights or rooms found that matched the specified names [" + String.join(", ", originalLightAndRoomList) + "]");
            System.exit(1);
        }

        return lights;
    }

    public static List<Light> getLightsOrExitIfEmpty(String[] args) throws ExecutionException, InterruptedException {
        List<String> originalLightAndRoomList = getOriginalLightAndRoomList(args);

        List<String> caseInsensitiveLightAndRoomList = getCaseInsensitiveLightAndRoomList(originalLightAndRoomList);

        List<Light> lightsByName = getLightsByName(caseInsensitiveLightAndRoomList);

        List<Light> lightsByRoomName = getLightsByRoomName(caseInsensitiveLightAndRoomList);

        List<Light> lights = getFullLightListOrExitIfEmpty(originalLightAndRoomList, lightsByName, lightsByRoomName);
        return lights;
    }

    public static void exitAndShowUsageIfNoArgs(String[] args, String scriptName) {
        if (args.length < 2) {
            log.error("You must specify the following parameters (in order):");
            log.error("  - The Hue bridge's username/API key");
            log.error("  - The Hue bridge's light names and/or room names (in quotes if the name contains spaces)");
            log.error("");
            log.error("Example: ./" + scriptName + " USERNAME \"Light 1\" \"Light 2\" Basement");
            log.error("");
            log.error("You must specify at least one light but may specify any number of lights by adding more parameters");
            log.error("");
            log.error("NOTE: The Hue bridge's IP address is automatically discovered");
            log.error("NOTE: Light names and room names can be mixed");
            log.error("NOTE: Light names and room names are case-insensitive");

            System.exit(1);
        }
    }
}
