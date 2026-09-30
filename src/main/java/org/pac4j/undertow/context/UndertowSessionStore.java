package org.pac4j.undertow.context;

import java.util.Optional;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import io.undertow.util.AttachmentKey;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.PrefixedSessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.TechnicalException;

/**
 * Specific session store for Undertow relying on the {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore extends PrefixedSessionStore {

    /** The session renewed during the current request: its new identifier cannot be found from the request anymore. */
    private static final AttachmentKey<Session> RENEWED_SESSION = AttachmentKey.create(Session.class);

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
        final Session renewedSession = context.getExchange().getAttachment(RENEWED_SESSION);
        if (renewedSession != null) {
            return Optional.of(renewedSession);
        }
        if (sessionManager == null || sessionConfig == null) {
            if (createSession) {
                throw new TechnicalException("No Undertow session manager or session config found in the exchange: "
                        + "the handlers must be wrapped by a SessionAttachmentHandler");
            }
            return Optional.empty();
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
        return session.map(value -> value.getAttribute(computePrefixedKey(key)));
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        // no need to create a session to remove an attribute
        final var session = getSession(context, value != null);
        session.ifPresent(s -> s.setAttribute(computePrefixedKey(key), value));
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    @Override
    public boolean destroySession(final WebContext context) {
        final HttpServerExchange exchange = ((UndertowWebContext) context).getExchange();
        final Optional<Session> session = getSession(context, false);
        session.ifPresent(value -> {
            if (exchange.getAttachment(RENEWED_SESSION) == value) {
                exchange.removeAttachment(RENEWED_SESSION);
            }
            value.invalidate(exchange);
        });
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        return getSession(context, false).map(Object.class::cast);
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
        // the session manager generates a new identifier (keeping the attributes) and sends it via the session config
        if (session.changeSessionId(exchange, sessionConfig) == null) {
            return false;
        }
        if (this.session == null) {
            exchange.putAttachment(RENEWED_SESSION, session);
        }
        return true;
    }

    /**
     * @return the session cookie name
     * @deprecated no longer used: the session renewal relies on {@link Session#changeSessionId(HttpServerExchange, SessionConfig)}
     */
    @Deprecated(since = "6.1.0", forRemoval = true)
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    /**
     * @param sessionCookieName the session cookie name
     * @deprecated no longer used: the session renewal relies on {@link Session#changeSessionId(HttpServerExchange, SessionConfig)}
     */
    @Deprecated(since = "6.1.0", forRemoval = true)
    public void setSessionCookieName(final String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }
}
