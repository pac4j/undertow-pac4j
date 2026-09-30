package org.pac4j.framework.adapter;

import org.pac4j.core.adapter.DefaultFrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.context.UndertowContextFactory;
import org.pac4j.undertow.context.UndertowSessionStoreFactory;
import org.pac4j.undertow.http.UndertowHttpActionAdapter;
import org.pac4j.undertow.profile.UndertowProfileManager;

/**
 * This class is found on classpath by pac4j
 * @author Sakib Hadziavdic
 * @since 6.0.0
 */
public class FrameworkAdapterImpl extends DefaultFrameworkAdapter {

    @Override
    public void applyDefaultSettingsIfUndefined(final Config config) {
        // before the parent call which defines the default pac4j profile manager factory
        config.setProfileManagerFactoryIfUndefined(UndertowProfileManager::new);
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