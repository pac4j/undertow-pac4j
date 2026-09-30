package org.pac4j.undertow.context;

import io.undertow.Undertow;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionCookieConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.logout.handler.DefaultSessionLogoutHandler;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.store.Store;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link UndertowSessionStore} against a real Undertow server using a non-default session cookie name.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
class UndertowSessionStoreTests {

    private static final String COOKIE_NAME = "MYSESSION";

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private static final String NONE = "none";

    @FunctionalInterface
    private interface Step {
        String apply(UndertowWebContext context, UndertowSessionStore store) throws Exception;
    }

    private record Response(String body, Optional<String> sessionId) {}

    private final HttpClient client = HttpClient.newHttpClient();

    private InMemorySessionManager sessionManager;

    private Undertow server;

    private URI uri;

    private volatile Step step;

    @BeforeEach
    void startServer() {
        sessionManager = new InMemorySessionManager("test");
        final SessionCookieConfig sessionConfig = new SessionCookieConfig().setCookieName(COOKIE_NAME);
        server = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(new SessionAttachmentHandler(exchange -> {
                    String body;
                    try {
                        body = step.apply(new UndertowWebContext(exchange), new UndertowSessionStore(exchange));
                    } catch (final Exception e) {
                        exchange.setStatusCode(500);
                        body = e.toString();
                    }
                    exchange.getResponseSender().send(body);
                }, sessionManager, sessionConfig))
                .build();
        server.start();
        final InetSocketAddress address = (InetSocketAddress) server.getListenerInfo().get(0).getAddress();
        uri = URI.create("http://localhost:" + address.getPort() + "/");
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    private Response call(final String sessionId, final Step step) throws Exception {
        this.step = step;
        final HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
        if (sessionId != null) {
            builder.header("Cookie", COOKIE_NAME + "=" + sessionId);
        }
        final HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        final Optional<String> newSessionId = response.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith(COOKIE_NAME + "=") && !c.contains("Max-Age=0"))
                .map(c -> c.substring(COOKIE_NAME.length() + 1).split(";")[0])
                .findFirst();
        assertEquals(200, response.statusCode(), response.body());
        return new Response(response.body(), newSessionId);
    }

    private static String createSessionWithValue(final UndertowWebContext context, final UndertowSessionStore store) {
        store.set(context, KEY, VALUE);
        return store.getSessionId(context, false).orElseThrow();
    }

    @Test
    void trackableSessionIsTheUndertowSession() throws Exception {
        final AtomicReference<Object> trackableSession = new AtomicReference<>();
        call(null, (context, store) -> {
            store.set(context, KEY, VALUE);
            trackableSession.set(store.getTrackableSession(context).orElseThrow());
            return "ok";
        });
        assertInstanceOf(Session.class, trackableSession.get());

        final Response response = call(null, (context, store) -> {
            final var trackedStore = store.buildFromTrackableSession(context, trackableSession.get()).orElseThrow();
            return trackedStore.get(context, KEY).orElse(NONE).toString();
        });
        assertEquals(VALUE, response.body());
    }

    @Test
    void trackedStoreAfterDestruction() throws Exception {
        final AtomicReference<Object> trackableSession = new AtomicReference<>();
        call(null, (context, store) -> {
            store.set(context, KEY, VALUE);
            trackableSession.set(store.getTrackableSession(context).orElseThrow());
            return "ok";
        });

        final Response response = call(null, (context, store) -> {
            final var trackedStore = store.buildFromTrackableSession(context, trackableSession.get()).orElseThrow();
            assertTrue(trackedStore.destroySession(context));
            return trackedStore.get(context, KEY).orElse(NONE).toString();
        });
        assertEquals(NONE, response.body());
    }

    @Test
    void backChannelLogoutDestroysTheTrackedSession() throws Exception {
        final DefaultSessionLogoutHandler logoutHandler = new DefaultSessionLogoutHandler(new MapStore());
        logoutHandler.setDestroySession(true);

        final String sessionId = call(null, (context, store) -> {
            final String id = createSessionWithValue(context, store);
            logoutHandler.recordSession(new CallContext(context, store, ProfileManager::new), "ticket");
            return id;
        }).body();

        call(null, (context, store) -> {
            logoutHandler.destroySession(new CallContext(context, store, ProfileManager::new), "ticket");
            return "ok";
        });

        assertNull(sessionManager.getSession(sessionId));
        assertEquals(NONE, call(sessionId, (context, store) -> store.get(context, KEY).orElse(NONE).toString()).body());
    }

    @Test
    void prefixIsAppliedToSessionAttributes() throws Exception {
        final Response response = call(null, (context, store) -> {
            store.setPrefix("prefix.");
            store.set(context, KEY, VALUE);
            final Session session = sessionManager.getSession(store.getSessionId(context, false).orElseThrow());
            final UndertowSessionStore otherStore = new UndertowSessionStore(context.getExchange());
            otherStore.setPrefix("other.");
            return session.getAttribute("prefix." + KEY) + "|" + session.getAttribute(KEY) + "|"
                    + store.get(context, KEY).orElse(NONE) + "|" + otherStore.get(context, KEY).orElse(NONE);
        });
        assertEquals(VALUE + "|null|" + VALUE + "|" + NONE, response.body());
    }

    @Test
    void renewSessionChangesTheSessionIdWithCustomCookieName() throws Exception {
        final Response creation = call(null, UndertowSessionStoreTests::createSessionWithValue);
        final String oldSessionId = creation.body();
        assertEquals(Optional.of(oldSessionId), creation.sessionId());

        final Response renewal = call(oldSessionId, (context, store) -> {
            assertTrue(store.renewSession(context));
            // the renewed session must still be available during the current request, even for another store
            final UndertowSessionStore otherStore = new UndertowSessionStore(context.getExchange());
            return store.getSessionId(context, true).orElseThrow() + "|" + otherStore.getSessionId(context, false).orElseThrow()
                    + "|" + otherStore.get(context, KEY).orElse(NONE);
        });
        final String[] renewalResults = renewal.body().split("\\|");
        final String newSessionId = renewalResults[0];
        assertEquals(newSessionId, renewalResults[1]);
        assertEquals(VALUE, renewalResults[2]);
        assertNotEquals(oldSessionId, newSessionId);
        assertEquals(Optional.of(newSessionId), renewal.sessionId());

        assertNull(sessionManager.getSession(oldSessionId));
        assertEquals(VALUE, call(newSessionId, (context, store) -> store.get(context, KEY).orElse(NONE).toString()).body());
        assertEquals(NONE, call(oldSessionId, (context, store) -> store.get(context, KEY).orElse(NONE).toString()).body());
    }

    @Test
    void removingAnAttributeDoesNotCreateASession() throws Exception {
        final Response response = call(null, (context, store) -> {
            store.set(context, KEY, null);
            return store.getSessionId(context, false).orElse(NONE);
        });
        assertEquals(NONE, response.body());
        assertEquals(Optional.empty(), response.sessionId());
    }

    @Test
    void withoutSessionManager() throws Exception {
        final Response response = call(null, (context, store) -> {
            // simulate the absence of SessionAttachmentHandler
            context.getExchange().removeAttachment(SessionManager.ATTACHMENT_KEY);
            context.getExchange().removeAttachment(SessionConfig.ATTACHMENT_KEY);
            final UndertowSessionStore noSessionStore = new UndertowSessionStore(context.getExchange());
            noSessionStore.set(context, KEY, null);
            final TechnicalException e = assertThrows(TechnicalException.class, () -> noSessionStore.set(context, KEY, VALUE));
            return noSessionStore.get(context, KEY).orElse(NONE) + "|" + noSessionStore.getSessionId(context, false).orElse(NONE)
                    + "|" + noSessionStore.destroySession(context) + "|" + e.getMessage().contains("SessionAttachmentHandler");
        });
        assertEquals(NONE + "|" + NONE + "|true|true", response.body());
    }

    private static final class MapStore implements Store<String, Object> {

        private final Map<String, Object> map = new ConcurrentHashMap<>();

        @Override
        public Optional<Object> get(final String key) {
            return Optional.ofNullable(map.get(key));
        }

        @Override
        public void set(final String key, final Object value) {
            map.put(key, value);
        }

        @Override
        public void remove(final String key) {
            map.remove(key);
        }
    }
}
