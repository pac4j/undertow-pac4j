package org.pac4j.framework.adapter;

import org.pac4j.core.adapter.DefaultFrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.context.UndertowContextFactory;
import org.pac4j.undertow.context.UndertowSessionStoreFactory;
import org.pac4j.undertow.http.UndertowHttpActionAdapter;

/**
 * This class is found on classpath by pac4j
 * @author Sakib Hadziavdic
 * @since 6.0.0
 */
public class FrameworkAdapterImpl extends DefaultFrameworkAdapter {

    @Override
    public void applyDefaultSettingsIfUndefined(final Config config) {
        super.applyDefaultSettingsIfUndefined(config);
        config.setWebContextFactoryIfUndefined(UndertowContextFactory.INSTANCE);
        config.setSessionStoreFactoryIfUndefined(UndertowSessionStoreFactory.INSTANCE);
        config.setHttpActionAdapterIfUndefined(UndertowHttpActionAdapter.INSTANCE);
    }

    @Override
    public String toString() {
        return "Undertow";
    }
}