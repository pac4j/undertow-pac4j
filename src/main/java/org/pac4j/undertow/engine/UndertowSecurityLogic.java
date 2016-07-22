package org.pac4j.undertow.engine;

import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.undertow.context.UndertowWebContext;

/**
 * Specific account logic for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class UndertowSecurityLogic extends DefaultSecurityLogic<Object, UndertowWebContext> {

    @Override
    protected ProfileManager getProfileManager(final UndertowWebContext context) {
        return new UndertowProfileManager(context);
    }
}
