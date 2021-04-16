package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import org.pac4j.core.context.session.SessionStore;

import java.util.Optional;

/**
 * Specific session store for Undertow relying on the {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore implements SessionStore<UndertowWebContext> {

    private final SessionManager sessionManager;
    private final SessionConfig sessionConfig;

    private String sessionCookieName = "JSESSIONID";

    public UndertowSessionStore(final HttpServerExchange exchange) {
        this.sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        this.sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
    }

    protected Session getExistingSession(final UndertowWebContext context) {
        return sessionManager.getSession(context.getExchange(), sessionConfig);
    }

    private Session getSession(final UndertowWebContext context) {
        final Session session = getExistingSession(context);
        if (session != null) {
            return session;
        }
        return sessionManager.createSession(context.getExchange(), sessionConfig);
    }

    @Override
    public String getOrCreateSessionId(final UndertowWebContext context) {
        return getSession(context).getId();
    }

    @Override
    public Optional<Object> get(final UndertowWebContext context, final String key) {
        Session session = getExistingSession(context);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(session.getAttribute(key));
    }

    @Override
    public void set(final UndertowWebContext context, final String key, final Object value) {
        final Session session = getSession(context);
        session.setAttribute(key, value);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    @Override
    public boolean destroySession(final UndertowWebContext context) {
        final Session session = getExistingSession(context);
        if (session != null) {
            session.invalidate(context.getExchange());
        }
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession(final UndertowWebContext context) {
        return Optional.ofNullable(getSession(context));
    }

    @Override
    public Optional<SessionStore<UndertowWebContext>> buildFromTrackableSession(final UndertowWebContext context, final Object trackableSession) {
        return Optional.of(new UndertowSessionStore(context.getExchange()) {
            @Override
            protected Session getExistingSession(UndertowWebContext context) {
                return (Session) trackableSession;
            }

        });
    }

    @Override
    public boolean renewSession(final UndertowWebContext context) {
        final HttpServerExchange exchange = context.getExchange();
        final Session session = getExistingSession(context);
        if (session == null) {
            sessionManager.createSession(exchange, sessionConfig);
            return true;
        }
        final String[] attributeNames = session.getAttributeNames().toArray(new String[0]);
        final Object[] attributeValues = new Object[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            attributeValues[i] = session.getAttribute(attributeNames[i]);
        }

        context.getExchange().getRequestCookies().remove(sessionCookieName);
        session.invalidate(exchange);

        final Session newSession = sessionManager.createSession(exchange, sessionConfig);
        for (int i = 0; i < attributeNames.length; i++) {
            newSession.setAttribute(attributeNames[i], attributeValues[i]);
        }
        return true;
    }

    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(final String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }
}
