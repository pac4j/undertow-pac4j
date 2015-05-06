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
package org.pac4j.undertow.utils;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.undertow.ProfileWrapper;

/**
 * Helper class to save and retrieve information like session attribute or saved request url during
 * the authentication process.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class StorageHelper {

    private static Session getSession(HttpServerExchange exchange) {
        return exchange.getAttachment(SessionManager.ATTACHMENT_KEY).getSession(exchange,
                exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
    }

    private static void save(HttpServerExchange exchange, String name, Object value) {
        Session session = getSession(exchange);
        if (session != null) {
            session.setAttribute(name, value);
        }
    }

    private static void remove(HttpServerExchange exchange, String name) {
        Session session = getSession(exchange);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    private static Object get(HttpServerExchange exchange, String name) {
        Session session = getSession(exchange);
        if (session != null) {
            return session.getAttribute(name);
        } else {
            return null;
        }
    }

    public static void saveProfile(HttpServerExchange exchange, ProfileWrapper profileWrapper) {
        save(exchange, HttpConstants.USER_PROFILE, profileWrapper);
    }

    public static void removeProfile(HttpServerExchange exchange) {
        remove(exchange, HttpConstants.USER_PROFILE);
    }

    public static ProfileWrapper getProfile(HttpServerExchange exchange) {
        return (ProfileWrapper) get(exchange, HttpConstants.USER_PROFILE);
    }

    public static String getRequestedUrl(HttpServerExchange exchange) {
        return (String) get(exchange, HttpConstants.REQUESTED_URL);
    }

    public static void saveRequestedUrl(HttpServerExchange exchange, String requestedUrlToSave) {
        save(exchange, HttpConstants.REQUESTED_URL, requestedUrlToSave);
    }

    public static void removeRequestedUrl(HttpServerExchange exchange) {
        remove(exchange, HttpConstants.REQUESTED_URL);
    }

    public static void createSession(HttpServerExchange exchange) {
        Session session = getSession(exchange);
        if (session == null) {
            exchange.getAttachment(SessionManager.ATTACHMENT_KEY).createSession(exchange,
                    exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
        }
    }

}
