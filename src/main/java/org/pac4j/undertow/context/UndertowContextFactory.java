package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.WebContextFactory;

/**
 * Specific web context factory.
 *
 * @author Jerome LELEU
 * @since 5.0.0
 */
public class UndertowContextFactory implements WebContextFactory {

    public static final UndertowContextFactory INSTANCE = new UndertowContextFactory();

    @Override
    public WebContext newContext(final Object... parameters) {
        return new UndertowWebContext((HttpServerExchange) parameters[0]);
    }
}
