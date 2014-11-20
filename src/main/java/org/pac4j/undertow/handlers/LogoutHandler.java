/*
  Copyright 2014 - 2014 Michael Remond

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
package org.pac4j.undertow.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.pac4j.undertow.Config;
import org.pac4j.undertow.utils.HttpResponseHelper;
import org.pac4j.undertow.utils.StorageHelper;

/**
 * Logout handler which remove the user profile from the session.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class LogoutHandler implements HttpHandler {

    private Config config;

    private LogoutHandler(Config config) {
        this.config = config;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        StorageHelper.removeProfile(exchange);
        HttpResponseHelper.redirect(exchange, config.getDefaultLogoutUrl());
    }

    public static HttpHandler build(Config config) {
        return new LogoutHandler(config);
    }

}
