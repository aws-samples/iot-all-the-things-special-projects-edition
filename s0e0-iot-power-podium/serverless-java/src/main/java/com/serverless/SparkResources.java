package com.serverless;

import io.vavr.control.Try;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient;
import software.amazon.awssdk.services.iotdataplane.model.PublishRequest;
import software.amazon.awssdk.services.iotdataplane.model.PublishResponse;
import spark.Request;
import spark.Response;

import java.net.URI;

import static spark.Spark.*;

public class SparkResources {
    public static final String POWER_PODIUM = "power-podium";
    public static final String REQUEST = "request";
    public static final String DELIMITER = "/";
    private static final PublishRequest emptyRequest = PublishRequest.builder().qos(0).payload(SdkBytes.fromUtf8String("{}")).build();
    private static final String pingTopic = String.join(DELIMITER, POWER_PODIUM, "ping");
    private static final PublishRequest pingRequest = emptyRequest.toBuilder().topic(pingTopic).build();
    private static final String playNumberTopic = String.join(DELIMITER, POWER_PODIUM, REQUEST, "play_number");
    private static final PublishRequest.Builder playNumberBuilder = PublishRequest.builder().topic(playNumberTopic).qos(0);
    private static final String playRandomTopic = String.join(DELIMITER, POWER_PODIUM, REQUEST, "play_random");
    private static final PublishRequest playRandomRequest = emptyRequest.toBuilder().topic(playRandomTopic).build();
    public static final String ID_VARIABLE = ":id";

    public static void defineResources() {
        staticFiles.location("/static");

        get("/ping", (request, response) -> {
            setTextHtml(response);

            // Try to ping but ignore any failures
            Try.of(() -> getClient().publish(pingRequest));

            return "OK";
        });

        post(String.join("/", "/say", ID_VARIABLE), (request, response) -> {
            setTextHtml(response);

            return redirectOrReportError(response, Try.of(() -> getClient().publish(playNumberBuilder.payload(idToPayload(request)).build())));
        });

        post("/say_random", (request, response) -> {
            setTextHtml(response);

            return redirectOrReportError(response, Try.of(() -> getClient().publish(playRandomRequest)));
        });
    }

    private static void setTextHtml(Response response) {
        response.type("text/html");
    }

    private static Object redirectOrReportError(Response response, Try<PublishResponse> tryPublish) {
        if (tryPublish.isSuccess()) {
            response.redirect("..");
            return "<meta http-equiv=\\\"Refresh\\\" content=\\\"0; url='..'\\\" />";
        }

        return "Operation failed [" + tryPublish.getCause().getMessage() + "]";
    }

    private static IotDataPlaneClient getClient() {
        String endpoint = IotClient.create().describeEndpoint(r -> r.endpointType("iot:Data-ATS")).endpointAddress();

        return IotDataPlaneClient.builder()
                .endpointOverride(URI.create("https://" + endpoint))
                .build();
    }

    private static SdkBytes idToPayload(Request request) {
        return SdkBytes.fromUtf8String(request.params(ID_VARIABLE));
    }
}