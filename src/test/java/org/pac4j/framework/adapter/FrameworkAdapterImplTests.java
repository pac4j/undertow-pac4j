package org.pac4j.framework.adapter;

import org.junit.jupiter.api.Test;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.test.context.session.MockSessionStore;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.handler.SecurityHandler;
import org.pac4j.undertow.profile.UndertowProfileManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link FrameworkAdapterImpl}.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
class FrameworkAdapterImplTests {

    @Test
    void defaultProfileManagerIsTheUndertowOne() {
        final Config config = new Config();
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        final ProfileManager manager = config.getProfileManagerFactory().apply(new UndertowWebContext(null), new MockSessionStore());
        assertInstanceOf(UndertowProfileManager.class, manager);
    }

    @Test
    void customProfileManagerFactoryIsKept() {
        final ProfileManagerFactory factory = ProfileManager::new;
        final Config config = new Config();
        config.setProfileManagerFactory(factory);
        SecurityHandler.build(exchange -> { }, config);
        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);
        assertSame(factory, config.getProfileManagerFactory());
    }
}
