package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.*;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;

/**
 * The webcontext implementation for Undertow.
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @author Igor Lobanov
 * @since 1.0.0
 */
public class UndertowWebContext implements WebContext {

    private final HttpServerExchange exchange;

    public UndertowWebContext(final HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public HttpServerExchange getExchange() {
        return exchange;
    }

    /**
     * {@inheritDoc}
     *
     * Like in the servlet API, the query string parameters come before the form parameters.
     */
    @Override
    public Optional<String> getRequestParameter(final String name) {
        final Deque<String> values = getExchange().getQueryParameters().get(name);
        if (values != null && !values.isEmpty()) {
            return Optional.ofNullable(values.peek());
        }
        final FormData data = getExchange().getAttachment(FormDataParser.FORM_DATA);
        if (data != null && data.contains(name)) {
            for (final FormData.FormValue value : data.get(name)) {
                if (!value.isFileItem()) {
                    return Optional.ofNullable(value.getValue());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * Like in the servlet API, the values of the query string and of the form are merged (query string first).
     */
    @Override
    public Map<String, String[]> getRequestParameters() {
        final Map<String, List<String>> values = new LinkedHashMap<>();
        for (final var entry : getExchange().getQueryParameters().entrySet()) {
            values.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
        }
        final FormData data = getExchange().getAttachment(FormDataParser.FORM_DATA);
        if (data != null) {
            for (final String key : data) {
                for (final FormData.FormValue value : data.get(key)) {
                    if (!value.isFileItem()) {
                        values.computeIfAbsent(key, k -> new ArrayList<>()).add(value.getValue());
                    }
                }
            }
        }
        final Map<String, String[]> map = new HashMap<>();
        values.forEach((key, list) -> map.put(key, list.toArray(new String[0])));
        return map;
    }

    @Override
    public Optional<String> getRequestHeader(final String name) {
        return Optional.ofNullable(getExchange().getRequestHeaders().get(name, 0));
    }

    @Override
    public String getRequestMethod() {
        return exchange.getRequestMethod().toString();
    }

    @Override
    public Optional<String> getResponseHeader(final String name) {
        return Optional.ofNullable(getExchange().getResponseHeaders().getFirst(name));
    }

    @Override
    public void setResponseHeader(final String name, final String value) {
        getExchange().getResponseHeaders().put(HttpString.tryFromString(name), value);
    }

    @Override
    public String getServerName() {
        return getExchange().getHostName();
    }

    @Override
    public int getServerPort() {
        return getExchange().getHostPort();
    }

    @Override
    public String getScheme() {
        return getExchange().getRequestScheme();
    }

    @Override
    public String getFullRequestURL() {
        final String url = getExchange().getRequestURL();
        final String queryString = getExchange().getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            return url + "?" + queryString;
        }
        return url;
    }

    @Override
    public String getRemoteAddr() {
        return getExchange().getSourceAddress().getAddress().getHostAddress();
    }

    @Override
    public void addResponseCookie(final Cookie cookie) {
        final CookieImpl newCookie = new CookieImpl(cookie.getName(), cookie.getValue());
        newCookie.setComment(cookie.getComment());
        newCookie.setDomain(cookie.getDomain());
        newCookie.setPath(cookie.getPath());
        // a negative max age means a session cookie (no Max-Age attribute): setMaxAge(null) fails with Undertow 2.4
        if (cookie.getMaxAge() >= 0) {
            newCookie.setMaxAge(cookie.getMaxAge());
        }
        newCookie.setSecure(cookie.isSecure());
        newCookie.setHttpOnly(cookie.isHttpOnly());
        final String sameSitePolicy = cookie.getSameSitePolicy();
        if (sameSitePolicy != null && !sameSitePolicy.isBlank()) {
            newCookie.setSameSiteMode(sameSitePolicy);
        }
        getExchange().setResponseCookie(newCookie);
    }

    @Override
    public void setRequestAttribute(final String name, final Object value) {
        RequestAttributesMap.getOrInitialize(getExchange()).put(name, value);
    }

    @Override
    public String getPath() {
        return getExchange().getRequestPath();
    }

    @Override
    public void setResponseContentType(final String content) {
        getExchange().getResponseHeaders().put(Headers.CONTENT_TYPE, content);
    }

    /**
     * {@inheritDoc}
     *
     * It relies on the deprecated {@link HttpServerExchange#getRequestCookies()} method as its replacement
     * ({@code HttpServerExchange.requestCookies()}) is only available since Undertow 2.2.
     */
    @Override
    @SuppressWarnings({"deprecation", "removal"})
    public Collection<Cookie> getRequestCookies() {
        final Collection<io.undertow.server.handlers.Cookie> uCookies = getExchange().getRequestCookies().values();
        final List<Cookie> cookies = new ArrayList<>(uCookies.size());
        for (final io.undertow.server.handlers.Cookie uCookie : uCookies) {
            final Cookie cookie = new Cookie(uCookie.getName(), uCookie.getValue());
            cookie.setComment(uCookie.getComment());
            cookie.setDomain(uCookie.getDomain());
            cookie.setPath(uCookie.getPath());
            cookie.setMaxAge(uCookie.getMaxAge() == null ? -1 : uCookie.getMaxAge());
            cookie.setSecure(uCookie.isSecure());
            cookie.setHttpOnly(uCookie.isHttpOnly());
            cookies.add(cookie);
        }
        return cookies;
    }

    @Override
    public Optional<Object> getRequestAttribute(final String name) {
        return Optional.ofNullable(RequestAttributesMap.getOrInitialize(getExchange()).get(name));
    }

    @Override
    public boolean isSecure() {
        return getExchange().isSecure();
    }
}
