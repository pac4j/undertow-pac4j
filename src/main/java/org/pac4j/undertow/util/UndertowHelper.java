package org.pac4j.undertow.util;

import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.impl.SecurityContextFactoryImpl;
import io.undertow.server.HttpServerExchange;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.undertow.account.Pac4jAccount;
import org.pac4j.undertow.context.UndertowWebContext;

import java.util.LinkedHashMap;

/**
 * Helper for Undertow
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class UndertowHelper {

    /**
     * Populate the security context with the authenticated user profiles.
     *
     * @param context the web context
     * @param profiles the linked hashmap of profiles
     */
    public static void populateContext(final UndertowWebContext context, final LinkedHashMap<String, CommonProfile> profiles) {
        if (profiles != null && profiles.size() > 0) {
            final HttpServerExchange exchange = context.getExchange();
            SecurityContext securityContext = exchange.getSecurityContext();
            if (securityContext == null) {
                securityContext = SecurityContextFactoryImpl.INSTANCE.createSecurityContext(exchange, AuthenticationMode.PRO_ACTIVE, null, null);
            }
            securityContext.authenticationComplete(new Pac4jAccount(profiles), "PAC4J_ACCOUNT", false);
            exchange.setSecurityContext(securityContext);
        }
    }
}
