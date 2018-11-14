package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.http.UndertowNopHttpActionAdapter;
import org.pac4j.undertow.profile.UndertowProfileManager;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This handler finishes the login process for an indirect client, based on the {@link #callbackLogic}.</p>
 *
 * <p>The configuration can be provided via setters and constructors for the following options:</p>
 * <ul>
 *     <li><code>config</code> (the security configuration itself)</li>
 *     <li><code>defaultUrl</code> (default url after login if none was requested)</li>
 *     <li><code>saveInSession</code> (whether the profile should be saved into the session)</li>
 *     <li><code>multiProfile</code> (whether multiple profiles should be kept)</li>
 *     <li><code>renewSession</code> (whether the session must be renewed after login)</li>
 *     <li><code>defaultClient</code> (the default client if none is provided on the URL)</li>
 * </ul>
 *
 * @author Jerome Leleu
 * @author Michaël Remond
 * @since 1.0.0
 */
public class CallbackHandler implements HttpHandler {

    private CallbackLogic<Object, UndertowWebContext> callbackLogic;

    private Config config;

    private String defaultUrl;

    private Boolean saveInSession;

    private Boolean multiProfile;

    private Boolean renewSession;

    private String defaultClient;

    protected CallbackHandler(final Config config, final String defaultUrl, final Boolean multiProfile)  {
        callbackLogic = new DefaultCallbackLogic<>();
        ((DefaultCallbackLogic<Object, UndertowWebContext>) callbackLogic).setProfileManagerFactory(UndertowProfileManager::new);
        this.config = config;
        this.defaultUrl = defaultUrl;
        this.multiProfile = multiProfile;
    }

    public static HttpHandler build(final Config config) {
        return build(config, null);
    }

    public static HttpHandler build(final Config config, final String defaultUrl) {
        return build(config, defaultUrl, null);
    }

    public static HttpHandler build(final Config config, final String defaultUrl, final Boolean multiProfile) {
        return build(config, defaultUrl, multiProfile, null);
    }

    public static HttpHandler build(final Config config, final String defaultUrl, final Boolean multiProfile, final CallbackLogic<Object, UndertowWebContext> callbackLogic) {
        final FormParserFactory factory = FormParserFactory.builder().addParser(new FormEncodedDataDefinition()).build();
        final EagerFormParsingHandler formHandler = new EagerFormParsingHandler(factory);
        final CallbackHandler callbackHandler = new CallbackHandler(config, defaultUrl, multiProfile);
        if (callbackLogic != null) {
            callbackHandler.setCallbackLogic(callbackLogic);
        }
        formHandler.setNext(callbackHandler);
        return new BlockingHandler(formHandler);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) {

        assertNotNull("callbackLogic", callbackLogic);
        assertNotNull("config", config);
        final UndertowWebContext context = new UndertowWebContext(exchange, config.getSessionStore());

        callbackLogic.perform(context, config, UndertowNopHttpActionAdapter.INSTANCE, this.defaultUrl,
                this.saveInSession, this.multiProfile, this.renewSession, this.defaultClient);
    }

    protected CallbackLogic<Object, UndertowWebContext> getCallbackLogic() {
        return callbackLogic;
    }

    protected void setCallbackLogic(final CallbackLogic<Object, UndertowWebContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public Boolean getSaveInSession() {
        return saveInSession;
    }

    public void setSaveInSession(final Boolean saveInSession) {
        this.saveInSession = saveInSession;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(final Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(final String defaultClient) {
        this.defaultClient = defaultClient;
    }
}
