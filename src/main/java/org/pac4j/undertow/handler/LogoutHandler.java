package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.http.UndertowNopHttpActionAdapter;
import org.pac4j.undertow.profile.UndertowProfileManager;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This handler handles the application logout process, based on the {@link #logoutLogic}.</p>
 *
 * <p>The configuration can be provided via the following parameters:</p>
 * <ul>
 *     <li><code>config</code> (the security configuration itself)</li>
 *     <li><code>defaultUrl</code> (default logourl url)</li>
 *     <li><code>logoutUrlPattern</code> (pattern that logout urls must match)</li>
 *     <li><code>localLogout</code> (whether the application logout must be performed)</li>
 *     <li><code>destroySession</code> (whether we must destroy the web session during the local logout)</li>
 *     <li><code>centralLogout</code> (whether the centralLogout must be performed)</li>
 * </ul>
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

    public LogoutHandler() {
        logoutLogic = new DefaultLogoutLogic<>();
        ((DefaultLogoutLogic<Object, UndertowWebContext>) logoutLogic).setProfileManagerFactory(UndertowProfileManager::new);
    }

    public LogoutHandler(final Config config) {
        this();
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

        assertNotNull("logoutLogic", logoutLogic);
        assertNotNull("config", config);
        final UndertowWebContext context = new UndertowWebContext(exchange, config.getSessionStore());

        logoutLogic.perform(context, config, UndertowNopHttpActionAdapter.INSTANCE,
                defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout);
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