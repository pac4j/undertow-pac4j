package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.http.UndertowHttpActionAdapter;
import org.pac4j.undertow.profile.UndertowProfileManager;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter protects an URL.</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class SecurityHandler implements HttpHandler {

    static {
        Config.defaultProfileManagerFactory("UndertowProfileManager", ctx -> new UndertowProfileManager(ctx));
    }

    private SecurityLogic<Object, UndertowWebContext> securityLogic;

    private HttpHandler toWrap;

    private Config config;

    private String clients;

    private String authorizers;

    private String matchers;

    private Boolean multiProfile;

    protected SecurityHandler(final HttpHandler toWrap, final Config config, final String clients, final String authorizers, final String matchers, final Boolean multiProfile) {
        this.toWrap = toWrap;
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
        this.matchers = matchers;
        this.multiProfile = multiProfile;
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config) {
        return build(toWrap, config, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients) {
        return build(toWrap, config, clients, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers) {
        return build(toWrap, config, clients, authorizers, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers) {
        return build(toWrap, config, clients, authorizers, matchers, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers, final Boolean multiProfile) {
        return build(toWrap, config, clients, authorizers, matchers, multiProfile, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers, final Boolean multiProfile, final SecurityLogic<Object, UndertowWebContext> securityLogic) {
        final SecurityHandler securityHandler = new SecurityHandler(toWrap, config, clients, authorizers, matchers, multiProfile);
        if (securityLogic != null) {
            securityHandler.setSecurityLogic(securityLogic);
        }
        return new BlockingHandler(securityHandler);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        final HttpActionAdapter<Object, UndertowWebContext> bestAdapter = FindBest.httpActionAdapter(null, config, UndertowHttpActionAdapter.INSTANCE);
        final SecurityLogic<Object, UndertowWebContext> bestLogic = FindBest.securityLogic(securityLogic, config, DefaultSecurityLogic.INSTANCE);

        assertNotNull("config", config);
        final UndertowWebContext context = new UndertowWebContext(exchange, config.getSessionStore());

        bestLogic.perform(context, this.config, (ctx, profiles, parameters) -> {

            toWrap.handleRequest(exchange);
            return null;

        }, bestAdapter, this.clients, this.authorizers, this.matchers, this.multiProfile);
    }

    protected SecurityLogic<Object, UndertowWebContext> getSecurityLogic() {
        return securityLogic;
    }

    protected void setSecurityLogic(final SecurityLogic<Object, UndertowWebContext> securityLogic) {
        this.securityLogic = securityLogic;
    }

    public String getClients() {
        return clients;
    }

    public void setClients(final String clients) {
        this.clients = clients;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizers(final String authorizers) {
        this.authorizers = authorizers;
    }

    public String getMatchers() {
        return matchers;
    }

    public void setMatchers(final String matchers) {
        this.matchers = matchers;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(final Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }
}
