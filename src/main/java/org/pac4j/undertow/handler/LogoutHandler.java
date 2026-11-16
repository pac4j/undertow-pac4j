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

    /**
     * Builds the logout handler.
     *
     * @param config the security configuration
     */
    public LogoutHandler(final Config config) {
        this.config = config;
    }

    /**
     * Builds the logout handler.
     *
     * @param config the security configuration
     * @param defaultUrl the default url
     */
    public LogoutHandler(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    /**
     * Builds the logout handler.
     *
     * @param config the security configuration
     * @param defaultUrl the default url
     * @param logoutUrlPattern the logout url pattern
     */
    public LogoutHandler(final Config config, final String defaultUrl, final String logoutUrlPattern) {
        this(config, defaultUrl);
        this.logoutUrlPattern = logoutUrlPattern;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        // like the other handlers (wrapped by a BlockingHandler): the logout logic may perform blocking operations,
        // like calling the identity provider, so it must not run in the IO thread
        if (!exchange.isBlocking()) {
            exchange.startBlocking();
        }
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        final LogoutLogic logic = logoutLogic != null ? logoutLogic : config.getLogoutLogic();
        logic.perform(
                this.config,
                this.defaultUrl,
                this.logoutUrlPattern,
                this.localLogout,
                this.destroySession,
                this.centralLogout,
                new UndertowParameters(exchange)
        );
    }

    /**
     * Gets the logout logic.
     *
     * @return the logout logic
     */
    public LogoutLogic getLogoutLogic() {
        return logoutLogic;
    }

    /**
     * Sets the logout logic.
     *
     * @param logoutLogic the logout logic
     */
    public void setLogoutLogic(final LogoutLogic logoutLogic) {
        this.logoutLogic = logoutLogic;
    }

    /**
     * Gets the default url.
     *
     * @return the default url
     */
    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    /**
     * Sets the default url.
     *
     * @param defaultUrl the default url
     */
    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    /**
     * Gets the logout url pattern.
     *
     * @return the logout url pattern
     */
    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    /**
     * Sets the logout url pattern.
     *
     * @param logoutUrlPattern the logout url pattern
     */
    public void setLogoutUrlPattern(final String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }

    /**
     * Gets whether a local logout must be performed.
     *
     * @return whether a local logout must be performed
     */
    public Boolean getLocalLogout() {
        return localLogout;
    }

    /**
     * Sets whether a local logout must be performed.
     *
     * @param localLogout whether a local logout must be performed
     */
    public void setLocalLogout(final Boolean localLogout) {
        this.localLogout = localLogout;
    }

    /**
     * Gets whether the session must be destroyed.
     *
     * @return whether the session must be destroyed
     */
    public Boolean getDestroySession() {
        return destroySession;
    }

    /**
     * Sets whether the session must be destroyed.
     *
     * @param destroySession whether the session must be destroyed
     */
    public void setDestroySession(final Boolean destroySession) {
        this.destroySession = destroySession;
    }

    /**
     * Gets whether a central logout must be performed.
     *
     * @return whether a central logout must be performed
     */
    public Boolean getCentralLogout() {
        return centralLogout;
    }

    /**
     * Sets whether a central logout must be performed.
     *
     * @param centralLogout whether a central logout must be performed
     */
    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }
}
