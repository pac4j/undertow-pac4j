package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.ApplicationLogoutLogic;
import org.pac4j.core.engine.DefaultApplicationLogoutLogic;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.http.UndertowNopHttpActionAdapter;
import org.pac4j.undertow.profile.UndertowProfileManager;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This handler handles the application logout process, based on the {@link #applicationLogoutLogic}.</p>
 *
 * <p>The configuration can be provided via the following parameters: <code>config</code> (the account configuration),
 * <code>defaultUrl</code> (default logourl url) and <code>logoutUrlPattern</code> (pattern that logout urls must match).</p>
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class ApplicationLogoutHandler implements HttpHandler {

    private ApplicationLogoutLogic<Object, UndertowWebContext> applicationLogoutLogic;

    private Config config;

    private String defaultUrl;

    private String logoutUrlPattern;

    public ApplicationLogoutHandler() {
        applicationLogoutLogic = new DefaultApplicationLogoutLogic<>();
        ((DefaultApplicationLogoutLogic<Object, UndertowWebContext>) applicationLogoutLogic).setProfileManagerFactory(UndertowProfileManager::new);
    }

    public ApplicationLogoutHandler(final Config config) {
        this();
        this.config = config;
    }

    public ApplicationLogoutHandler(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    public ApplicationLogoutHandler(final Config config, final String defaultUrl, final String logoutUrlPattern) {
        this(config, defaultUrl);
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        assertNotNull("applicationLogoutLogic", applicationLogoutLogic);
        assertNotNull("config", config);
        final UndertowWebContext context = new UndertowWebContext(exchange, config.getSessionStore());

        applicationLogoutLogic.perform(context, config, UndertowNopHttpActionAdapter.INSTANCE, this.defaultUrl, this.logoutUrlPattern);
    }

    public ApplicationLogoutLogic<Object, UndertowWebContext> getApplicationLogoutLogic() {
        return applicationLogoutLogic;
    }

    public void setApplicationLogoutLogic(final ApplicationLogoutLogic<Object, UndertowWebContext> applicationLogoutLogic) {
        this.applicationLogoutLogic = applicationLogoutLogic;
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    public void setLogoutUrlPattern(final String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }
}
