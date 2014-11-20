/*
  Copyright 2014 - 2014 Michael Remond

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.HttpString;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.CommonHelper;

/**
 * The webcontext implementation for Undertow.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class UndertowWebContext implements WebContext {

    private final HttpServerExchange exchange;
    private final SessionManager sessionManager;
    private final SessionConfig sessionConfig;

    public UndertowWebContext(HttpServerExchange exchange) {
        this.exchange = exchange;
        this.sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        this.sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
    }

    @Override
    public String getRequestParameter(String name) {
        Deque<String> param = exchange.getQueryParameters().get(name);
        if (param != null) {
            return param.peek();
        } else {
            FormData data = exchange.getAttachment(FormDataParser.FORM_DATA);
            if (data != null && data.get(name) != null) {
                return data.get(name).peek().getValue();
            }
        }
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        Map<String, String[]> map = new HashMap<String, String[]>();
        for (Entry<String, Deque<String>> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        FormData data = exchange.getAttachment(FormDataParser.FORM_DATA);
        if (data != null) {
            for (String key : data) {
                map.put(key, data.get(key).toArray(new String[data.get(key).size()]));
            }
        }
        return map;
    }

    @Override
    public String getRequestHeader(String name) {
        return exchange.getRequestHeaders().get(name, 0);
    }

    @Override
    public void setSessionAttribute(String name, Object value) {
        Session session = this.sessionManager.getSession(exchange, this.sessionConfig);
        if (session != null) {
            if (value == null) {
                session.removeAttribute(name);
            } else {
                session.setAttribute(name, value);
            }
        }
    }

    @Override
    public Object getSessionAttribute(String name) {
        Session session = this.sessionManager.getSession(exchange, this.sessionConfig);
        return (session != null) ? session.getAttribute(name) : null;
    }

    @Override
    public String getRequestMethod() {
        return exchange.getRequestMethod().toString();
    }

    @Override
    public void writeResponseContent(String content) {
        exchange.getResponseSender().send(content);
    }

    @Override
    public void setResponseStatus(int code) {
        exchange.setResponseCode(code);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
    }

    @Override
    public String getServerName() {
        return exchange.getHostName();
    }

    @Override
    public int getServerPort() {
        return exchange.getHostPort();
    }

    @Override
    public String getScheme() {
        return exchange.getProtocol().toString();
    }

    @Override
    public String getFullRequestURL() {
        String full = exchange.getRequestURL();
        if (CommonHelper.isNotBlank(exchange.getQueryString())) {
            full = full + "?" + exchange.getQueryString();
        }
        return full;
    }

}
