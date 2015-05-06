/*
  Copyright 2014 - 2015 pac4j organization

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

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.undertow.Config;
import org.pac4j.undertow.ProfileWrapper;
import org.pac4j.undertow.UndertowWebContext;
import org.pac4j.undertow.utils.HandlerHelper;
import org.pac4j.undertow.utils.HttpResponseHelper;
import org.pac4j.undertow.utils.StorageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback handler for Undertow pac4j binding. This handler finishes the authentication process.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class CallbackHandler implements HttpHandler {

    protected static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private Config config;

    private CallbackHandler(Config config) {
        this.config = config;
    }

    public static HttpHandler build(Config config) {
        return HandlerHelper.addFormParsing(new CallbackHandler(config));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        // clients group from config
        final Clients clientsGroup = config.getClients();

        // web context
        final WebContext context = new UndertowWebContext(exchange);

        // get the client from its type
        final BaseClient client = (BaseClient) clientsGroup.findClient(context);
        logger.debug("client : {}", client);

        // get credentials
        Credentials credentials = null;
        try {
            credentials = client.getCredentials(context);
            logger.debug("credentials : {}", credentials);
        } catch (final RequiresHttpAction e) {
            // requires some specific HTTP action
            final int code = e.getCode();
            logger.debug("requires HTTP action : {}", code);
            exchange.endExchange();
            return;
        }

        // get user profile
        final CommonProfile profile = client.getUserProfile(credentials, context);
        logger.debug("profile : {}", profile);

        // save user profile only if it's not null
        if (profile != null) {
            StorageHelper.saveProfile(exchange, new ProfileWrapper(profile));
        }

        // get requested url
        final String requestedUrl = StorageHelper.getRequestedUrl(exchange);
        StorageHelper.removeRequestedUrl(exchange);
        final String redirectUrl = defaultUrl(requestedUrl, config.getDefaultSuccessUrl());

        // retrieve saved request and redirect
        HttpResponseHelper.redirect(exchange, redirectUrl);
    }

    /**
     * This method returns the default url from a specified url compared with a default url.
     * 
     * @param url the compared url
     * @param defaultUrl the default url
     * @return the default url
     */
    public static String defaultUrl(final String url, final String defaultUrl) {
        String redirectUrl = defaultUrl;
        if (CommonHelper.isNotBlank(url)) {
            redirectUrl = url;
        }
        logger.debug("redirectUrl : {}", redirectUrl);
        return redirectUrl;
    }

}
