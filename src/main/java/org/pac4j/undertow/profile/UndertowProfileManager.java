package org.pac4j.undertow.profile;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.util.UndertowHelper;

import java.util.LinkedHashMap;

/**
 * Specific profile manager for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.1
 */
public class UndertowProfileManager extends ProfileManager<CommonProfile> {

    public UndertowProfileManager(final WebContext context) {
        super(context);
    }

    protected LinkedHashMap<String, CommonProfile> retrieveAll(boolean readFromSession) {

        final LinkedHashMap<String, CommonProfile> profiles = super.retrieveAll(readFromSession);
        UndertowHelper.populateContext((UndertowWebContext) context, profiles);
        return profiles;
    }
}
