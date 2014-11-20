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
package org.pac4j.undertow;

import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;

import org.pac4j.core.client.Clients;

/**
 * Holder for configuration values.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public final class Config {

    private final String DEFAULT_URL = "/";

    private String defaultSuccessUrl = DEFAULT_URL;

    private String defaultLogoutUrl = DEFAULT_URL;

    // all the clients
    private Clients clients;

    private SessionManager sessionManager = new InMemorySessionManager("SessionManager");

    private final SessionConfig sessionCookieConfig = new SessionCookieConfig();

    public String getDefaultSuccessUrl() {
        return defaultSuccessUrl;
    }

    public void setDefaultSuccessUrl(final String defaultSuccessUrl) {
        this.defaultSuccessUrl = defaultSuccessUrl;
    }

    public String getDefaultLogoutUrl() {
        return defaultLogoutUrl;
    }

    public void setDefaultLogoutUrl(final String defaultLogoutUrl) {
        this.defaultLogoutUrl = defaultLogoutUrl;
    }

    public Clients getClients() {
        return clients;
    }

    public void setClients(final Clients clients) {
        this.clients = clients;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public SessionConfig getSessioncookieconfig() {
        return sessionCookieConfig;
    }
}
