package org.pac4j.undertow.handler;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.context.UndertowParameters;
import org.pac4j.undertow.context.UndertowWebContext;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the handlers call the right logic with the right parameters.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
class HandlersTests {

    private static final String DEFAULT_URL = "/home";

    private final AtomicReference<List<Object>> performArgs = new AtomicReference<>();

    private void call(final HttpHandler handler) throws Exception {
        call(handler, null);
    }

    private void call(final HttpHandler handler, final String form) throws Exception {
        final Undertow server = Undertow.builder().addHttpListener(0, "localhost").setHandler(handler).build();
        server.start();
        try {
            final InetSocketAddress address = (InetSocketAddress) server.getListenerInfo().get(0).getAddress();
            final HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + address.getPort() + "/"));
            if (form != null) {
                request.header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form));
            }
            final HttpResponse<String> response = HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode(), response.body());
        } finally {
            server.stop();
        }
    }

    private List<Object> recordedArgs() {
        final List<Object> args = performArgs.get();
        assertNotNull(args, "the custom logic has not been called");
        assertInstanceOf(UndertowParameters.class, args.get(args.size() - 1));
        return args.subList(0, args.size() - 1);
    }

    @Test
    void callbackHandlerUsesCustomLogicAndDefaultClient() throws Exception {
        final Config config = new Config(new AnonymousClient());
        call(CallbackHandler.build(config, DEFAULT_URL, false, "MyClient",
                (cfg, defaultUrl, renewSession, defaultClient, params) -> {
                    performArgs.set(Arrays.asList(cfg, defaultUrl, renewSession, defaultClient, params));
                    return null;
                }));
        assertEquals(Arrays.asList(config, DEFAULT_URL, false, "MyClient"), recordedArgs());
    }

    @Test
    void callbackHandlerDoesNotDefaultToTheFirstClient() throws Exception {
        final Config config = new Config(new AnonymousClient());
        call(CallbackHandler.build(config, DEFAULT_URL,
                (cfg, defaultUrl, renewSession, defaultClient, params) -> {
                    performArgs.set(Arrays.asList(cfg, defaultUrl, renewSession, defaultClient, params));
                    return null;
                }));
        assertEquals(Arrays.asList(config, DEFAULT_URL, null, null), recordedArgs());
    }

    @Test
    void callbackHandlerWorksWithoutClients() throws Exception {
        final Config config = new Config();
        call(CallbackHandler.build(config, DEFAULT_URL,
                (cfg, defaultUrl, renewSession, defaultClient, params) -> {
                    performArgs.set(Arrays.asList(cfg, defaultUrl, renewSession, defaultClient, params));
                    return null;
                }));
        assertEquals(Arrays.asList(config, DEFAULT_URL, null, null), recordedArgs());
    }

    @Test
    void securityHandlerUsesCustomLogic() throws Exception {
        final Config config = new Config();
        call(SecurityHandler.build(exchange -> { }, config, "clients", "authorizers", "matchers",
                (cfg, adapter, clients, authorizers, matchers, params) -> {
                    performArgs.set(Arrays.asList(cfg, clients, authorizers, matchers, params));
                    return null;
                }));
        assertEquals(Arrays.asList(config, "clients", "authorizers", "matchers"), recordedArgs());
    }

    @Test
    void securityHandlerParsesFormParameters() throws Exception {
        call(SecurityHandler.build(exchange -> { }, new Config(), "clients", null, null,
                (cfg, adapter, clients, authorizers, matchers, params) -> {
                    final UndertowWebContext context = new UndertowWebContext(((UndertowParameters) params).exchange());
                    performArgs.set(Arrays.asList(context.getRequestParameter("username").orElse(null),
                            context.getRequestParameters().get("password")[0], params));
                    return null;
                }), "username=jerome&password=secret");
        assertEquals(Arrays.asList("jerome", "secret"), recordedArgs());
    }

    @Test
    void logoutHandlerUsesCustomLogic() throws Exception {
        final Config config = new Config();
        final LogoutHandler logoutHandler = new LogoutHandler(config, DEFAULT_URL, "/.*");
        logoutHandler.setLocalLogout(true);
        logoutHandler.setDestroySession(false);
        logoutHandler.setCentralLogout(true);
        logoutHandler.setLogoutLogic((cfg, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout, params) -> {
            performArgs.set(Arrays.asList(cfg, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout, params));
            return null;
        });
        call(logoutHandler);
        assertEquals(Arrays.asList(config, DEFAULT_URL, "/.*", true, false, true), recordedArgs());
    }

    @Test
    void logoutHandlerRunsInABlockingWorkerThread() throws Exception {
        final LogoutHandler logoutHandler = new LogoutHandler(new Config());
        logoutHandler.setLogoutLogic((cfg, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout, params) -> {
            final var exchange = ((UndertowParameters) params).exchange();
            performArgs.set(Arrays.asList(exchange.isInIoThread(), exchange.isBlocking(), params));
            return null;
        });
        call(logoutHandler);
        assertEquals(Arrays.asList(false, true), recordedArgs());
    }
}
