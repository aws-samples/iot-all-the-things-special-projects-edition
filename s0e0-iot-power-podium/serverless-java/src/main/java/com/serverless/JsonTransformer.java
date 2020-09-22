/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.serverless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.apache.log4j.Logger;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {
    private ObjectMapper mapper = new ObjectMapper();
    private Logger log = Logger.getLogger(JsonTransformer.class);

    @Override
    public String render(Object model) {
        return Try.of(() -> mapper.writeValueAsString(model))
                .onFailure(JsonProcessingException.class, exception -> log.error("Cannot serialize object", exception))
                .getOrNull();
    }
}