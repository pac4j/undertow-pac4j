package org.pac4j.undertow.http;

import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.undertow.context.UndertowWebContext;

/**
 * No-operation HTTP action adapter for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class UndertowNopHttpActionAdapter implements HttpActionAdapter<Object, UndertowWebContext> {

    public static final UndertowNopHttpActionAdapter INSTANCE = new UndertowNopHttpActionAdapter();

    @Override
    public Object adapt(int code, UndertowWebContext context) {
        return null;
    }
}
