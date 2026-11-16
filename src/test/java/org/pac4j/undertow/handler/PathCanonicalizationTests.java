package org.pac4j.undertow.handler;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.PathHandler;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.matching.matcher.PathMatcher;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the path used for the security decisions is the path used by Undertow to route the request:
 * a non canonical path must not bypass the path matchers.
 *
 * @author Jerome Leleu
 * @since 6.1.1
 */
class PathCanonicalizationTests {

    private static final String ADMIN_SECRET = "ADMIN_SECRET";

    private static final String PUBLIC = "public";

    private static final List<String> ATTACKS = List.of(
            "/api/../admin",
            "/api/%2e%2e/admin",
            "/api/%2E%2E/admin",
            "/api/.%2e/admin",
            "/api/%252e%252e/admin",
            "/api/..;/admin",
            "/api/..;x=y/admin",
            "/api;x=y/../admin",
            "/api/hello/../../admin",
            "/api/./../admin",
            "/api//../admin",
            "/api/..//admin",
            "/api/../admin/",
            "/api/../admin/x",
            "/api/..%2fadmin",
            "/api/..%5cadmin",
            "/api/..\\admin",
            "/api\\..\\admin",
            "/admin/../api/hello",
            "/admin/%2e%2e/api/hello",
            "/admin/%252e%252e/api/hello",
            "/admin/x/../../api/hello",
            "/admin//../api/hello",
            "/admin/.//../api/hello",
            "/admin/..//api/hello",
            "/admin/..;/api/hello",
            "/admin;x=y",
            "/admin;x=y/",
            "/admin;/../api/hello",
            "//admin",
            "/./admin",
            "/../admin",
            "/%61dmin",
            "/admin/",
            "/admin/x",
            "/admin%2f..%2fapi/hello",
            "/api/../admin?x=/api/hello",
            "/admin#/../api/hello",
            "http://localhost/api/../admin",
            "http://localhost/admin/../api/hello");

    private static Config config() {
        final DirectClient client = new DirectClient() {
            @Override
            protected void internalInit(final boolean forceReinit) {
                setCredentialsExtractorIfUndefined(ctx -> Optional.empty());
                setAuthenticatorIfUndefined((ctx, credentials) -> Optional.empty());
            }
        };
        client.setName("direct");
        final Config config = new Config(client);
        final PathMatcher pathMatcher = new PathMatcher();
        pathMatcher.excludeBranch("/api");
        config.addMatcher("excludedApi", pathMatcher);
        return config;
    }

    private static HttpHandler secured(final HttpHandler toWrap) {
        return SecurityHandler.build(toWrap, config(), "direct", null, "excludedApi");
    }

    private static PathHandler routes() {
        final HttpHandler admin = exchange -> exchange.getResponseSender().send(ADMIN_SECRET);
        final HttpHandler pub = exchange -> exchange.getResponseSender().send(PUBLIC);
        return new PathHandler().addPrefixPath("/admin", admin).addPrefixPath("/api", pub);
    }

    private static String request(final int port, final String target) throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            final OutputStream out = socket.getOutputStream();
            out.write(("GET " + target + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
            final InputStream in = socket.getInputStream();
            final String response = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            final String status = response.isEmpty() ? "no response" : response.substring(0, response.indexOf("\r\n")).replace("HTTP/1.1 ", "");
            final int body = response.indexOf("\r\n\r\n");
            return status.substring(0, Math.min(3, status.length())) + " " + (body < 0 ? "" : response.substring(body + 4));
        }
    }

    private Map<String, String> run(final Function<PathHandler, HttpHandler> topology, final List<String> targets) throws Exception {
        final Undertow server = Undertow.builder().addHttpListener(0, "localhost").setHandler(topology.apply(routes())).build();
        server.start();
        try {
            final int port = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
            final Map<String, String> results = new LinkedHashMap<>();
            for (final String target : targets) {
                results.put(target, request(port, target));
            }
            return results;
        } finally {
            server.stop();
        }
    }

    private void check(final Function<PathHandler, HttpHandler> topology) throws Exception {
        final Map<String, String> sanity = run(topology, List.of("/api/hello", "/api", "/admin", "/admin/x"));
        assertEquals("200 " + PUBLIC, sanity.get("/api/hello"));
        assertEquals("200 " + PUBLIC, sanity.get("/api"));
        assertTrue(sanity.get("/admin").startsWith("401"), sanity.get("/admin"));
        assertTrue(sanity.get("/admin/x").startsWith("401"), sanity.get("/admin/x"));

        final Map<String, String> results = run(topology, ATTACKS);
        final StringBuilder leaks = new StringBuilder();
        results.forEach((target, result) -> {
            if (result.contains(ADMIN_SECRET)) {
                leaks.append('\n').append(target).append(" -> ").append(result);
            }
        });
        assertEquals("", leaks.toString(), "the protected handler has been reached without authentication");
    }

    @Test
    void securityHandlerBeforeCanonicalPathHandler() throws Exception {
        check(routes -> secured(new CanonicalPathHandler(routes)));
    }

    @Test
    void securityHandlerAfterCanonicalPathHandler() throws Exception {
        check(routes -> new CanonicalPathHandler(secured(routes)));
    }

    @Test
    void securityHandlerWithoutCanonicalPathHandler() throws Exception {
        check(PathCanonicalizationTests::secured);
    }

    @Test
    void securityHandlerUnderAPrefixPath() throws Exception {
        check(routes -> new PathHandler().addPrefixPath("/", secured(routes)));
    }

    @Test
    void securityHandlerOnTheProtectedPrefixOnly() throws Exception {
        final HttpHandler admin = exchange -> exchange.getResponseSender().send(ADMIN_SECRET);
        final HttpHandler pub = exchange -> exchange.getResponseSender().send(PUBLIC);
        check(routes -> new PathHandler().addPrefixPath("/admin", secured(admin)).addPrefixPath("/api", pub));
    }
}
