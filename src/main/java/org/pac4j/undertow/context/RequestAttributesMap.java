package org.pac4j.undertow.context;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

import java.util.HashMap;

/**
 * A map of request attributes stored in Undertow's HttpServerExchange as an attachment.
 *
 * This is a simple extension of a HashMap that adds no custom logic, but it must be
 * a separate class, because Undertow uses class-based AttachmentKey to distinguish
 * attachment types.
 *
 * @author Igor Lobanov
 * @since 4.1.0
 */
public class RequestAttributesMap extends HashMap<String, Object> {

    /** Actual singleton attachment key instance */
    private static final AttachmentKey<RequestAttributesMap> ATTACHMENT_KEY =
            AttachmentKey.create(RequestAttributesMap.class);

    /**
     * Returns an instance of RequestAttributesMap stored in a given Undertow's HttpServerExchange
     * object as an attachment. If there had not been such attachment in the exchange, it is created.
     *
     * @param exchange the Undertow exchange
     * @return the request attributes map
     */
    public static RequestAttributesMap getOrInitialize(HttpServerExchange exchange) {
        RequestAttributesMap attributesMap = exchange.getAttachment(ATTACHMENT_KEY);
        if (attributesMap == null) {
            attributesMap = new RequestAttributesMap();
            exchange.putAttachment(ATTACHMENT_KEY, attributesMap);
        }
        return attributesMap;
    }
}
