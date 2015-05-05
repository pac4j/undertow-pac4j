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
package org.pac4j.undertow.utils;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.session.SessionAttachmentHandler;

import java.util.Collections;
import java.util.List;

import org.pac4j.undertow.ClientAuthenticationMechanism;
import org.pac4j.undertow.Config;

/**
 * Helper class providing useful wrappers around {@link HttpHandler} like security, session and form data parsing.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class HandlerHelper {

    /**
     * Wrap the given handler with a security configuration by using the pac4j authentication mechanism.
     * 
     * @param toWrap the handler to wrap
     * @param config the pac4j configuration
     * @param clientName the client name
     * @param isAjax whether it is an AJAX request
     * @return the handler
     */
    public static HttpHandler requireAuthentication(final HttpHandler toWrap, Config config, final String clientName,
            boolean isAjax) {
        HttpHandler handler = toWrap;
        // protect resource
        handler = new AuthenticationCallHandler(handler);
        // set authentication required
        handler = new AuthenticationConstraintHandler(handler);
        List<AuthenticationMechanism> mechanisms = Collections
                .<AuthenticationMechanism> singletonList(new ClientAuthenticationMechanism(config, clientName, isAjax));
        // use pac4j as authentication mechanism
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        // put security context in exchange
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, null, handler);
        return handler;
    }

    /**
     * Wrap the given handler with the eager form parsing handler in order to read the complete form data.
     * 
     * @param toWrap the handler to wrap
     * @return the handler
     */
    public static HttpHandler addFormParsing(final HttpHandler toWrap) {
        HttpHandler handler = toWrap;
        FormParserFactory factory = FormParserFactory.builder().addParser(new FormEncodedDataDefinition()).build();
        EagerFormParsingHandler formHandler = new EagerFormParsingHandler(factory);
        formHandler.setNext(handler);
        handler = formHandler;
        return handler;
    }

    /**
     * Wrap the given handler to add session capabilities.
     * 
     * @param toWrap the handler to wrap
     * @param config the pac4j configuration
     * @return the handler
     */
    public static HttpHandler addSession(final HttpHandler toWrap, Config config) {
        return new SessionAttachmentHandler(toWrap, config.getSessionManager(), config.getSessioncookieconfig());
    }

}
