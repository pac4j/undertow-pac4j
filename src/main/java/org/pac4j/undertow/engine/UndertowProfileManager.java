package org.pac4j.undertow.engine;

import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.impl.SecurityContextFactoryImpl;
import io.undertow.server.HttpServerExchange;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.undertow.account.Pac4jAccount;
import org.pac4j.undertow.context.UndertowWebContext;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Specific profile manager for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class UndertowProfileManager extends ProfileManager<CommonProfile> {

    public UndertowProfileManager(final UndertowWebContext context) {
        super(context);
    }

    @Override
    public List<CommonProfile> getAll(final boolean readFromSession) {
        final LinkedHashMap<String, CommonProfile> profiles = retrieveAll(readFromSession);

        if (profiles.size() > 0) {
            final UndertowWebContext webCtx = (UndertowWebContext) context;
            final HttpServerExchange exchange = webCtx.getExchange();
            SecurityContext securityContext = exchange.getSecurityContext();
            if (securityContext == null) {
                securityContext = SecurityContextFactoryImpl.INSTANCE.createSecurityContext(exchange, AuthenticationMode.PRO_ACTIVE, null, null);
            }
            securityContext.authenticationComplete(new Pac4jAccount(profiles), "PAC4J_ACCOUNT", false);
            exchange.setSecurityContext(securityContext);
        }

        return ProfileHelper.flatIntoAProfileList(profiles);
    }
}
