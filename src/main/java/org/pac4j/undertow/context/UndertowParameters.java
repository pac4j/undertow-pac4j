package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import org.pac4j.core.context.FrameworkParameters;

/**
 * Parameters for Undertow.
 *
 * @author Sakib Hadziavdic
 * @since 6.0.0
 */
public record UndertowParameters(HttpServerExchange exchange) implements FrameworkParameters {
}
