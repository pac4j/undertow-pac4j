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
package org.pac4j.undertow;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import io.undertow.server.handlers.BlockingHandler;
import org.pac4j.core.authorization.AuthorizationChecker;
import org.pac4j.core.authorization.DefaultAuthorizationChecker;
import org.pac4j.core.client.*;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.undertow.security.Pac4jAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * <p>This security mechanism protects a resource (authentication + authorization).</p>
 * <ul>
 *  <li>If a stateful / indirect client is used, it relies on the session to get the user profile (after the {@link org.pac4j.undertow.handlers.CallbackHandler} has terminated the authentication process)</li>
 *  <li>If a stateless / direct client is used, it validates the provided credentials from the request and retrieves the user profile if the authentication succeeds.</li>
 * </ul>
 * <p>Then, authorizations are checked before accessing the resource.</p>
 * <p>Forbidden or unauthorized errors can be returned. An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration, clients and authorizers are provided using the static <code>build</code> methods.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class SecurityMechanism implements AuthenticationMechanism {

    private final static String NAME = "PAC4J_ACCOUNT";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    protected Config config;

    protected String clients;

    protected String authorizers;

    public static HttpHandler build(final HttpHandler toWrap, Config config) {
        return build(toWrap, new SecurityMechanism(config, null, null));
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients) {
        return build(toWrap, new SecurityMechanism(config, clients, null));
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers) {
        return build(toWrap, new SecurityMechanism(config, clients, authorizers));
    }

    private static HttpHandler build(final HttpHandler toWrap, final SecurityMechanism mechanism) {
        HttpHandler handler = toWrap;
        // protect resource
        handler = new AuthenticationCallHandler(handler);
        // set authentication required
        handler = new AuthenticationConstraintHandler(handler);
        List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism> singletonList(mechanism);
        // use pac4j as authentication mechanism
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        // put security context in exchange
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, null, handler);
        return new BlockingHandler(handler);
    }

    private SecurityMechanism(final Config config, final String clients, final String authorizers) {
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
    }

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        final UndertowWebContext context = new UndertowWebContext(exchange);

        final Clients configClients = config.getClients();
        CommonHelper.assertNotNull("configClients", configClients);
        logger.debug("clients: {}", clients);
        final List<Client> currentClients = clientFinder.find(configClients, context, this.clients);
        logger.debug("currentClients: {}", currentClients);

        final boolean useSession = useSession(context, currentClients);
        logger.debug("useSession: {}", useSession);
        final ProfileManager manager = new ProfileManager(context);
        UserProfile profile = manager.get(useSession);
        logger.debug("profile: {}", profile);

        // no profile and some current clients
        if (profile == null && currentClients != null && currentClients.size() > 0) {
            // loop on all clients searching direct ones to perform authentication
            for (final Client currentClient : currentClients) {
                if (currentClient instanceof DirectClient) {
                    logger.debug("Performing authentication for client: {}", currentClient);
                    final Credentials credentials;
                    try {
                        credentials = currentClient.getCredentials(context);
                        logger.debug("credentials: {}", credentials);
                    } catch (final RequiresHttpAction e) {
                        throw new TechnicalException("Unexpected HTTP action", e);
                    }
                    profile = currentClient.getUserProfile(credentials, context);
                    logger.debug("profile: {}", profile);
                    if (profile != null) {
                        manager.save(useSession, profile);
                        break;
                    }
                }
            }
        }

        if (profile != null) {
            logger.debug("authorizers: {}", authorizers);
            securityContext.authenticationComplete(new Pac4jAccount(profile), NAME, false);
            if (authorizationChecker.isAuthorized(context, profile, authorizers, config.getAuthorizers())) {
                return AuthenticationMechanismOutcome.AUTHENTICATED;
            } else {
                return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
            }
        } else {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        final UndertowWebContext context = new UndertowWebContext(exchange);
        final Pac4jAccount account = (Pac4jAccount) securityContext.getAuthenticatedAccount();
        final Clients configClients = config.getClients();
        final List<Client> currentClients = clientFinder.find(configClients, context, this.clients);
        // forbidden: we have an account, though we are not authenticated
        if (account != null) {
            logger.debug("forbidden");
            forbidden(context, currentClients, account.getProfile());
        } else {
            if (startAuthentication(context, currentClients)) {
                logger.debug("Starting authentication");
                saveRequestedUrl(context, currentClients);
                redirectToIdentityProvider(context, currentClients);
            } else {
                logger.debug("unauthorized");
                unauthorized(context, currentClients);
            }
        }
        return new ChallengeResult(true);
    }

    protected boolean useSession(final WebContext context, final List<Client> currentClients) {
        return currentClients == null || currentClients.size() == 0 || currentClients.get(0) instanceof IndirectClient;
    }

    protected void forbidden(final WebContext context, final List<Client> currentClients, final UserProfile profile) {
        context.setResponseStatus(HttpConstants.FORBIDDEN);
    }

    protected boolean startAuthentication(final WebContext context, final List<Client> currentClients) {
        return currentClients != null && currentClients.size() > 0 && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected void redirectToIdentityProvider(final WebContext context, final List<Client> currentClients) {
        try {
            final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
            currentClient.redirect(context, true);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
        }
    }

    protected void unauthorized(final WebContext context, final List<Client> currentClients) {
        context.setResponseStatus(HttpConstants.UNAUTHORIZED);
    }
}
