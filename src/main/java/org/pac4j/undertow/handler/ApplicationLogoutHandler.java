package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.ApplicationLogoutLogic;
import org.pac4j.core.engine.DefaultApplicationLogoutLogic;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.http.UndertowNopHttpActionAdapter;

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

    private ApplicationLogoutLogic<Object, UndertowWebContext> applicationLogoutLogic = new DefaultApplicationLogoutLogic<>();

    private Config config;

    private String defaultUrl;

    private String logoutUrlPattern;

    public ApplicationLogoutHandler(final Config config) {
        this(config, null);
    }

    public ApplicationLogoutHandler(final Config config, final String defaultUrl) {
        this(config, defaultUrl, null);
    }

    public ApplicationLogoutHandler(final Config config, final String defaultUrl, final String logoutUrlPattern) {
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        assertNotNull("applicationLogoutLogic", applicationLogoutLogic);
        final UndertowWebContext context = new UndertowWebContext(exchange);

        applicationLogoutLogic.perform(context, config, UndertowNopHttpActionAdapter.INSTANCE, this.defaultUrl, this.logoutUrlPattern);
    }

    public ApplicationLogoutLogic<Object, UndertowWebContext> getApplicationLogoutLogic() {
        return applicationLogoutLogic;
    }

    public void setApplicationLogoutLogic(ApplicationLogoutLogic<Object, UndertowWebContext> applicationLogoutLogic) {
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
