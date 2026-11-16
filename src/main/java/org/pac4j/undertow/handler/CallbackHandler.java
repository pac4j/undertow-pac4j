package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.undertow.context.UndertowParameters;
import org.pac4j.undertow.util.UndertowHelper;

/**
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @author Michaël Remond
 * @since 1.0.0
 */
public class CallbackHandler implements HttpHandler {

    private CallbackLogic callbackLogic;

    private Config config;

    private String defaultUrl;

    private Boolean renewSession;

    private String defaultClient;

    /**
     * Builds the callback handler.
     *
     * @param config the security configuration
     * @param defaultUrl the default url
     */
    protected CallbackHandler(final Config config, final String defaultUrl)  {
        this.config = config;
        this.defaultUrl = defaultUrl;
    }

    /**
     * Builds the callback handler.
     *
     * @param config the security configuration
     * @return the handler
     */
    public static HttpHandler build(final Config config) {
        return build(config, null);
    }

    /**
     * Builds the callback handler.
     *
     * @param config the security configuration
     * @param defaultUrl the default url
     * @return the handler
     */
    public static HttpHandler build(final Config config, final String defaultUrl) {
        return build(config, defaultUrl, null);
    }

    /**
     * Builds the callback handler.
     *
     * @param config the security configuration
     * @param defaultUrl the default url
     * @param callbackLogic the callback logic
     * @return the handler
     */
    public static HttpHandler build(final Config config, final String defaultUrl, final CallbackLogic callbackLogic) {
        return build(config, defaultUrl, null, null, callbackLogic);
    }

    /**
     * Builds the callback handler.
     *
     * @param config the security configuration
     * @param defaultUrl the default url
     * @param renewSession whether the session must be renewed after login
     * @param defaultClient the name of the default client
     * @param callbackLogic the callback logic
     * @return the handler
     */
    public static HttpHandler build(final Config config, final String defaultUrl, final Boolean renewSession,
                                    final String defaultClient, final CallbackLogic callbackLogic) {
        final CallbackHandler callbackHandler = new CallbackHandler(config, defaultUrl);
        callbackHandler.setRenewSession(renewSession);
        callbackHandler.setDefaultClient(defaultClient);
        if (callbackLogic != null) {
            callbackHandler.setCallbackLogic(callbackLogic);
        }
        return new BlockingHandler(UndertowHelper.buildFormParsingHandler(callbackHandler));
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        final CallbackLogic logic = callbackLogic != null ? callbackLogic : config.getCallbackLogic();
        logic.perform(
                this.config,
                this.defaultUrl,
                this.renewSession,
                this.defaultClient,
                new UndertowParameters(exchange)
        );
    }

    /**
     * Gets the callback logic.
     *
     * @return the callback logic
     */
    protected CallbackLogic getCallbackLogic() {
        return callbackLogic;
    }

    /**
     * Sets the callback logic.
     *
     * @param callbackLogic the callback logic
     */
    protected void setCallbackLogic(final CallbackLogic callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    /**
     * Gets the default url.
     *
     * @return the default url
     */
    public String getDefaultUrl() {
        return defaultUrl;
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
     * Gets whether the session must be renewed after login.
     *
     * @return whether the session must be renewed after login
     */
    public Boolean getRenewSession() {
        return renewSession;
    }

    /**
     * Sets whether the session must be renewed after login.
     *
     * @param renewSession whether the session must be renewed after login
     */
    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    /**
     * Gets the name of the default client.
     *
     * @return the name of the default client
     */
    public String getDefaultClient() {
        return defaultClient;
    }

    /**
     * Sets the name of the default client.
     *
     * @param defaultClient the name of the default client
     */
    public void setDefaultClient(final String defaultClient) {
        this.defaultClient = defaultClient;
    }
}
