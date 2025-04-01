package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.undertow.context.UndertowParameters;

/**
 * <p>This filter handles the (application + identity provider) logout process.</p>
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class LogoutHandler implements HttpHandler {
    
    private LogoutLogic logoutLogic;

    private final Config config;

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
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        config.getLogoutLogic().perform(
                this.config,
                this.defaultUrl,
                this.logoutUrlPattern,
                this.localLogout,
                this.destroySession,
                this.centralLogout,
                new UndertowParameters(exchange)
        );
    }

    public LogoutLogic getLogoutLogic() {
        return logoutLogic;
    }

    public void setLogoutLogic(final LogoutLogic logoutLogic) {
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
