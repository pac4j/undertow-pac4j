package org.pac4j.undertow.context;

import java.util.Optional;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.PrefixedSessionStore;
import org.pac4j.core.context.session.SessionStore;

/**
 * Specific session store for Undertow relying on the {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore extends PrefixedSessionStore {

    private final HttpServerExchange exchange;
    private final SessionManager sessionManager;
    private final SessionConfig sessionConfig;
    private Session session;

    private String sessionCookieName = "JSESSIONID";

    public UndertowSessionStore(final HttpServerExchange exchange) {
        this.exchange = exchange;
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
        final var session = getSession(context, createSession);
        return session.map(Session::getId);
    }


    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        final var session = getSession(context, false);
        return session.map(value -> value.getAttribute(key));
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        final var session = getSession(context, true);
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
        session.ifPresent(value -> value.invalidate(((UndertowWebContext) context).getExchange()));
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        return Optional.ofNullable(getSession(context, false));
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        if (trackableSession != null) {
            var undertowSession = (Session) trackableSession;
            var sessionStore = new UndertowSessionStore(exchange, undertowSession);
            sessionStore.setPrefix(this.getPrefix());
            return Optional.of(sessionStore);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean renewSession(final WebContext webContext) {
        final UndertowWebContext context = (UndertowWebContext) webContext;
        final HttpServerExchange exchange = context.getExchange();
        final Session session = getSession(context, true).get();
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
