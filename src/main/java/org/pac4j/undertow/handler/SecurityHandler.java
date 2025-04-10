package org.pac4j.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.undertow.context.UndertowParameters;
import org.pac4j.undertow.profile.UndertowProfileManager;

/**
 * <p>This filter protects an URL.</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class SecurityHandler implements HttpHandler {

    private SecurityLogic securityLogic;

    private final HttpHandler toWrap;

    private final Config config;

    private String clients;

    private String authorizers;

    private String matchers;

    protected SecurityHandler(final HttpHandler toWrap, final Config config, final String clients, final String authorizers, final String matchers) {
        this.toWrap = toWrap;
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
        this.matchers = matchers;
        config.setProfileManagerFactory(UndertowProfileManager::new);
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

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers, final SecurityLogic securityLogic) {
        final SecurityHandler securityHandler = new SecurityHandler(toWrap, config, clients, authorizers, matchers);
        if (securityLogic != null) {
            securityHandler.setSecurityLogic(securityLogic);
        }
        return new BlockingHandler(securityHandler);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        config.getSecurityLogic().perform(
                this.config,
                (ctx, store, profiles) -> {
                    this.toWrap.handleRequest(exchange);
                    return null;
                },
                this.clients,
                this.authorizers,
                this.matchers,
                new UndertowParameters(exchange)
        );
    }

    protected SecurityLogic getSecurityLogic() {
        return securityLogic;
    }

    protected void setSecurityLogic(final SecurityLogic securityLogic) {
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
}
