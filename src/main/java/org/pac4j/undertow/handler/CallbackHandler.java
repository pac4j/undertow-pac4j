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

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This handler finishes the login process for an indirect client, based on the {@link #callbackLogic}.</p>
 *
 * <p>The configuration can be provided via the following parameters: <code>config</code> (account configuration),
 * <code>defaultUrl</code> (default url after login if none was requested) and <code>multiProfile</code> (whether multiple profiles should be kept).</p>
 *
 * @author Jerome Leleu
 * @author Michaël Remond
 * @since 1.0.0
 */
public class CallbackHandler implements HttpHandler {

    private CallbackLogic<Object, UndertowWebContext> callbackLogic = new DefaultCallbackLogic<>();

    private Config config;

    private String defaultUrl;

    private Boolean multiProfile;

    protected CallbackHandler(final Config config, final String defaultUrl, final Boolean multiProfile)  {
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

        callbackLogic.perform(context, config, UndertowNopHttpActionAdapter.INSTANCE, this.defaultUrl, this.multiProfile, false);
    }

    protected CallbackLogic<Object, UndertowWebContext> getCallbackLogic() {
        return callbackLogic;
    }

    protected void setCallbackLogic(CallbackLogic<Object, UndertowWebContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }
}
