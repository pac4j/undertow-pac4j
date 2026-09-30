package org.pac4j.undertow.context;

import io.undertow.Undertow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.Cookie;
import org.pac4j.undertow.util.UndertowHelper;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link UndertowWebContext} against a real Undertow server.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
class UndertowWebContextTests {

    private final HttpClient client = HttpClient.newHttpClient();

    private Undertow server;

    private String baseUrl;

    private volatile Function<UndertowWebContext, String> step;

    @BeforeEach
    void startServer() {
        server = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(UndertowHelper.buildFormParsingHandler(exchange -> {
                    final String body = step.apply(new UndertowWebContext(exchange));
                    exchange.getResponseSender().send(body);
                }))
                .build();
        server.start();
        final InetSocketAddress address = (InetSocketAddress) server.getListenerInfo().get(0).getAddress();
        baseUrl = "http://localhost:" + address.getPort() + "/";
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    private HttpResponse<String> call(final HttpRequest.Builder request, final Function<UndertowWebContext, String> step)
            throws Exception {
        this.step = step;
        final HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        return response;
    }

    @Test
    void queryParametersComeBeforeFormParameters() throws Exception {
        final HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + "?a=q1&b=q"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("a=f1&c=f"));
        final HttpResponse<String> response = call(request, context -> {
            final Map<String, String[]> params = context.getRequestParameters();
            return context.getRequestParameter("a").orElse(null) + "|" + String.join(",", params.get("a"))
                    + "|" + context.getRequestParameter("b").orElse(null) + "|" + String.join(",", params.get("b"))
                    + "|" + context.getRequestParameter("c").orElse(null) + "|" + String.join(",", params.get("c"))
                    + "|" + context.getRequestParameter("d").isPresent();
        });
        assertEquals("q1|q1,f1|q|q|f|f|false", response.body());
    }

    @Test
    void contentTypeIsNotDuplicated() throws Exception {
        final HttpResponse<String> response = call(HttpRequest.newBuilder(URI.create(baseUrl)), context -> {
            context.setResponseContentType("text/plain");
            context.setResponseContentType("application/json");
            return "{}";
        });
        assertEquals(List.of("application/json"), response.headers().allValues("Content-Type"));
    }

    @Test
    void requestCookies() throws Exception {
        final HttpResponse<String> response = call(HttpRequest.newBuilder(URI.create(baseUrl)).header("Cookie", "a=1; b=2"),
                context -> {
                    final Map<String, String> cookies = new TreeMap<>();
                    for (final Cookie cookie : context.getRequestCookies()) {
                        cookies.put(cookie.getName(), cookie.getValue());
                    }
                    return cookies.toString();
                });
        assertEquals("{a=1, b=2}", response.body());
    }
}
