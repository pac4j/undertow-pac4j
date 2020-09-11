package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.http.UndertowHttpActionAdapter;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter handles the (application + identity provider) logout process.</p>
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class LogoutHandler implements HttpHandler {
    
    private LogoutLogic<Object, UndertowWebContext> logoutLogic;

    private Config config;

    private String defaultUrl;

    private String logoutUrlPattern;
    
    private Boolean localLogout;

    private Boolean destroySession;

    private Boolean centralLogout;

    public LogoutHandler(final Config config) {
        this.config = config;
    }

    public LogoutHandler(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    public LogoutHandler(final Config config, final String defaultUrl, final String logoutUrlPattern) {
        this(config, defaultUrl);
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        final HttpActionAdapter<Object, UndertowWebContext> bestAdapter = FindBest.httpActionAdapter(null, config, UndertowHttpActionAdapter.INSTANCE);
        final LogoutLogic<Object, UndertowWebContext> bestLogic = FindBest.logoutLogic(logoutLogic, config, DefaultLogoutLogic.INSTANCE);

        assertNotNull("config", config);
        final UndertowWebContext context = new UndertowWebContext(exchange, config.getSessionStore());

        bestLogic.perform(context, config, bestAdapter, defaultUrl, logoutUrlPattern,
                localLogout, destroySession, centralLogout);
    }

    public LogoutLogic<Object, UndertowWebContext> getLogoutLogic() {
        return logoutLogic;
    }

    public void setLogoutLogic(final LogoutLogic<Object, UndertowWebContext> logoutLogic) {
        this.logoutLogic = logoutLogic;
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

    public Boolean getLocalLogout() {
        return localLogout;
    }

    public void setLocalLogout(final Boolean localLogout) {
        this.localLogout = localLogout;
    }

    public Boolean getDestroySession() {
        return destroySession;
    }

    public void setDestroySession(final Boolean destroySession) {
        this.destroySession = destroySession;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }
}
