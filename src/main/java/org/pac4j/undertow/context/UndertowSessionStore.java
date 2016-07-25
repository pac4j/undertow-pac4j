package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import org.pac4j.core.context.session.SessionStore;

/**
 * Specific session store for Undertow relying on the {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore implements SessionStore<UndertowWebContext> {

    private final SessionManager sessionManager;
    private final SessionConfig sessionConfig;

    public UndertowSessionStore(final HttpServerExchange exchange) {
        this.sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        this.sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
    }

    protected Session getSession(final UndertowWebContext context) {
        final HttpServerExchange exchange = context.getExchange();
        Session session = this.sessionManager.getSession(exchange, this.sessionConfig);
        if (session == null) {
            exchange.getAttachment(SessionManager.ATTACHMENT_KEY).createSession(exchange, exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
            session = this.sessionManager.getSession(exchange, this.sessionConfig);
        }
        return session;
    }

    @Override
    public String getOrCreateSessionId(final UndertowWebContext context) {
        return getSession(context).getId();
    }

    @Override
    public Object get(final UndertowWebContext context, final String key) {
        return getSession(context).getAttribute(key);
    }

    @Override
    public void set(final UndertowWebContext context, final String key, final Object value) {
        final Session session = getSession(context);
        if (value == null) {
            session.removeAttribute(key);
        } else {
            session.setAttribute(key, value);
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }
}
