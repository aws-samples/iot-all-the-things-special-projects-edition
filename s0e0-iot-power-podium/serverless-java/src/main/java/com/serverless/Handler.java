package com.serverless;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.vavr.control.Try;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Handler implements RequestStreamHandler {
    private static SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        // Initialization at the start of the container

        // Get the proxy handler
        handler = Try.of(SparkLambdaContainerHandler::getAwsProxyHandler)
                // Print a stack trace if a container initialization exception is thrown
                .onFailure(ContainerInitializationException.class, Throwable::printStackTrace)
                // Fail on all exceptions
                .get();

        // Define the Spark resources
        SparkResources.defineResources();

        // Wait for Spark to be fully initialized
        Spark.awaitInitialization();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        // Proxy all requests to Spark
        handler.proxyStream(inputStream, outputStream, context);
    }
}
