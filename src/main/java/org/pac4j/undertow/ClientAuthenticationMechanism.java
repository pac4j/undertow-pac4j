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

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.undertow.utils.StorageHelper;

/**
 * <p>Undertow authentication mechanism relying on a pac4j client.</p>
 * <p>This mechanism first looks for a valid user profile in session. If it is present, the security context
 * is completed, otherwise we redirect to the client's authentication provider.</p>
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class ClientAuthenticationMechanism implements AuthenticationMechanism {

    private final String name = "PAC4J_CLIENT";

    private final Config config;

    private final String clientName;

    private final boolean isAjax;

    public ClientAuthenticationMechanism(Config config, String clientName, boolean isAjax) {
        this.config = config;
        this.clientName = clientName;
        this.isAjax = isAjax;
    }

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        ProfileWrapper profile = StorageHelper.getProfile(exchange);
        if (profile != null) {
            securityContext.authenticationComplete(profile.getAccount(), name, false);
            return AuthenticationMechanismOutcome.AUTHENTICATED;
        } else {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        WebContext webContext = new UndertowWebContext(exchange);
        final String requestedUrlToSave = webContext.getFullRequestURL();
        StorageHelper.createSession(exchange);
        StorageHelper.saveRequestedUrl(exchange, requestedUrlToSave);
        // get client
        final BaseClient client = (BaseClient) config.getClients().findClient(clientName);
        try {
            client.redirect(webContext, true, isAjax);
            return new ChallengeResult(true);
        } catch (final RequiresHttpAction e) {
            StorageHelper.removeRequestedUrl(exchange);
            // We should return a 401 but undertow returns a 403
            return new ChallengeResult(false);
        }
    }
}
