package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

import java.util.Optional;

/**
 * Specific session store for Undertow relying on the {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore implements SessionStore {

    private SessionManager sessionManager;
    private SessionConfig sessionConfig;
    private Session session;

    private String sessionCookieName = "JSESSIONID";

    public UndertowSessionStore(final HttpServerExchange exchange) {
        this.sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        this.sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
    }

    protected UndertowSessionStore(final HttpServerExchange exchange, final Session session) {
        this(exchange);
        this.session = session;
    }

    private Optional<Session> getSession(final WebContext webContext, final boolean createSession) {
        final UndertowWebContext context = (UndertowWebContext) webContext;
        if (session != null) {
            return Optional.of(session);
        }
        final Session session = sessionManager.getSession(context.getExchange(), sessionConfig);
        if (session != null) {
            return Optional.of(session);
        }
        if (createSession) {
            return Optional.of(sessionManager.createSession(context.getExchange(), sessionConfig));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getSessionId(final WebContext context, final boolean createSession) {
        final Optional<Session> session = getSession(context, createSession);
        if (session.isPresent()) {
            return Optional.of(session.get().getId());
        } else {
            return Optional.empty();
        }
    }


    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        final Optional<Session> session = getSession(context, false);
        if (session.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(session.get().getAttribute(key));
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        final Optional<Session> session = getSession(context, true);
        session.get().setAttribute(key, value);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    @Override
    public boolean destroySession(final WebContext context) {
        final Optional<Session> session = getSession(context, false);
        if (session.isPresent()) {
            session.get().invalidate(((UndertowWebContext) context).getExchange());
        }
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        return Optional.ofNullable(getSession(context, false));
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        if (trackableSession != null) {
            return Optional.of(new UndertowSessionStore(((UndertowWebContext) context).getExchange(), (Session) trackableSession));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean renewSession(final WebContext webContext) {
        final UndertowWebContext context = (UndertowWebContext) webContext;
        final HttpServerExchange exchange = context.getExchange();
        final Optional<Session> session = getSession(context, false);
        if (session.isEmpty()) {
            sessionManager.createSession(exchange, sessionConfig);
            return true;
        }
        final String[] attributeNames = session.get().getAttributeNames().toArray(new String[0]);
        final Object[] attributeValues = new Object[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            attributeValues[i] = session.get().getAttribute(attributeNames[i]);
        }

        context.getExchange().getRequestCookies().remove(sessionCookieName);
        session.get().invalidate(exchange);

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
