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
package org.pac4j.undertow.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.undertow.UndertowWebContext;
import org.pac4j.undertow.utils.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This handler handles the callback from the identity provider to finish the authentication process.</p>
 * <p>The default url after login (if none has originally be requested) can be defined via the {@link #build(Config, String)} method.</p>
 *
 * @author Jerome Leleu
 * @author Michaël Remond
 * @since 1.0.0
 */
public class CallbackHandler implements HttpHandler {

    protected static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private final Config config;

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    private CallbackHandler(final Config config) {
        this.config = config;
    }

    public static HttpHandler build(final Config config, final String defaultUrl) {
        final FormParserFactory factory = FormParserFactory.builder().addParser(new FormEncodedDataDefinition()).build();
        final EagerFormParsingHandler formHandler = new EagerFormParsingHandler(factory);
        final CallbackHandler callbackHandler = new CallbackHandler(config);
        if (CommonHelper.isNotBlank(defaultUrl)) {
            callbackHandler.setDefaultUrl(defaultUrl);
        }
        formHandler.setNext(callbackHandler);
        return new BlockingHandler(formHandler);
    }

    public static HttpHandler build(final Config config) {
        return build(config, null);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        CommonHelper.assertNotNull("config", config);
        final WebContext context = new UndertowWebContext(exchange);

        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        final Client client = clients.findClient(context);
        logger.debug("client: {}", client);
        CommonHelper.assertNotNull("client", client);
        CommonHelper.assertTrue(client instanceof IndirectClient, "only indirect clients are allowed on the callback url");

        final Credentials credentials;
        try {
            credentials = client.getCredentials(context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
            exchange.endExchange();
            return;
        }
        logger.debug("credentials: {}", credentials);

        final UserProfile profile = client.getUserProfile(credentials, context);
        logger.debug("profile: {}", profile);
        saveUserProfile(context, profile);
        redirectToOriginallyRequestedUrl(context);
    }

    protected void saveUserProfile(final WebContext context, final UserProfile profile) {
        final ProfileManager manager = new ProfileManager(context);
        if (profile != null) {
            manager.save(true, profile);
        }
    }

    protected void redirectToOriginallyRequestedUrl(final WebContext context) {
        final UndertowWebContext webContext = (UndertowWebContext) context;
        final HttpServerExchange exchange = webContext.getExchange();
        final String requestedUrl = (String) context.getSessionAttribute(Pac4jConstants.REQUESTED_URL);
        logger.debug("requestedUrl: {}", requestedUrl);
        if (CommonHelper.isNotBlank(requestedUrl)) {
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, null);
            ExchangeHelper.redirect(exchange, requestedUrl);
        } else {
            ExchangeHelper.redirect(exchange, this.defaultUrl);
        }
    }

    protected String getDefaultUrl() {
        return defaultUrl;
    }

    protected void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }
}
