package org.pac4j.undertow.context;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

/**
 * Specific session store for Undertow relying on {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore implements SessionStore {

    private final SessionManager sessionManager;
    private final SessionConfig sessionConfig;

    public UndertowSessionStore(final SessionManager sessionManager, final SessionConfig sessionConfig) {
        this.sessionManager = sessionManager;
        this.sessionConfig = sessionConfig;
    }

    protected Session getSession(final WebContext context) {
        final UndertowWebContext webContext = (UndertowWebContext) context;
        final HttpServerExchange exchange = webContext.getExchange();
        Session session = this.sessionManager.getSession(exchange, this.sessionConfig);
        if (session == null) {
            exchange.getAttachment(SessionManager.ATTACHMENT_KEY).createSession(exchange, exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
            session = this.sessionManager.getSession(exchange, this.sessionConfig);
        }
        return session;
    }

    @Override
    public String getOrCreateSessionId(final WebContext context) {
        return getSession(context).getId();
    }

    @Override
    public Object get(final WebContext context, final String key) {
        return getSession(context).getAttribute(key);
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        final Session session = getSession(context);
        if (value == null) {
            session.removeAttribute(key);
        } else {
            session.setAttribute(key, value);
        }
    }

    /**
     * Add a default session handler (sessions in memory and cookie based).
     *
     * @param toWrap the handler to wrap
     * @return the session handler
     */
    public static HttpHandler addDefaultSessionHandler(final HttpHandler toWrap) {
        return new SessionAttachmentHandler(toWrap, new InMemorySessionManager("SessionManager"), new SessionCookieConfig());
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }
}
