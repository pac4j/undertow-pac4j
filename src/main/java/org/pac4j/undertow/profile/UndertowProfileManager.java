package org.pac4j.undertow.profile;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.util.UndertowHelper;

import java.util.LinkedHashMap;

/**
 * Specific profile manager for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.1
 */
public class UndertowProfileManager extends ProfileManager {

    public UndertowProfileManager(final WebContext context, final SessionStore sessionStore) {
        super(context, sessionStore);
    }

    protected LinkedHashMap<String, UserProfile> retrieveAll(boolean readFromSession) {

        final LinkedHashMap<String, UserProfile> profiles = super.retrieveAll(readFromSession);
        UndertowHelper.populateContext((UndertowWebContext) context, profiles);
        return profiles;
    }
}
