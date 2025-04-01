package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import org.pac4j.core.context.FrameworkParameters;

public record UndertowParameters(HttpServerExchange exchange) implements FrameworkParameters {
}
