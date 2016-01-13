/*
  Copyright 2014 - 2016 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.undertow.utils;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import org.pac4j.core.context.HttpConstants;

/**
 * Helper class to generate some basic http responses.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class ExchangeHelper {

    public static void ok(HttpServerExchange exchange, String content) {
        exchange.setStatusCode(HttpConstants.OK);
        exchange.getResponseSender().send(content);
        exchange.endExchange();
    }

    public static void redirect(HttpServerExchange exchange, String location) {
        exchange.setStatusCode(HttpConstants.TEMP_REDIRECT);
        if (location != null) {
            exchange.getResponseHeaders().put(HttpString.tryFromString(HttpConstants.LOCATION_HEADER), location);
        }
        exchange.endExchange();
    }

}
