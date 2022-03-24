package org.pac4j.undertow.http;

import io.undertow.server.HttpServerExchange;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.undertow.context.UndertowWebContext;

/**
 * The specific {@link HttpActionAdapter} for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class UndertowHttpActionAdapter implements HttpActionAdapter {

    public static final UndertowHttpActionAdapter INSTANCE = new UndertowHttpActionAdapter();

    @Override
    public Object adapt(final HttpAction action, final WebContext context) {
        if (action != null) {
            final int code = action.getCode();
            final HttpServerExchange exchange = ((UndertowWebContext) context).getExchange();
            exchange.setStatusCode(code);

            if (action instanceof WithLocationAction) {
                final WithLocationAction withLocationAction = (WithLocationAction) action;
                context.setResponseHeader(HttpConstants.LOCATION_HEADER, withLocationAction.getLocation());
            } else if (action instanceof WithContentAction) {
                final WithContentAction withContentAction = (WithContentAction) action;
                final String content = withContentAction.getContent();
                exchange.getResponseSender().send(content);
            }

            return null;
        }

        throw new TechnicalException("No action provided");
    }
}
