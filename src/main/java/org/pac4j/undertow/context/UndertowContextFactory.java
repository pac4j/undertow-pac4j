package org.pac4j.undertow.context;

import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.WebContextFactory;
import org.pac4j.core.exception.TechnicalException;

/**
 * Specific web context factory.
 *
 * @author Jerome LELEU
 * @since 5.0.0
 */
public class UndertowContextFactory implements WebContextFactory {

    public static final UndertowContextFactory INSTANCE = new UndertowContextFactory();

    @Override
    public WebContext newContext(FrameworkParameters parameters) {
        if (parameters instanceof UndertowParameters undertowParameters) {
            return new UndertowWebContext(undertowParameters.exchange());
        }
        throw new TechnicalException("Bad parameters type");
    }
}
