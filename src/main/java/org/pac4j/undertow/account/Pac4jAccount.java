package org.pac4j.undertow.account;

import io.undertow.security.idm.Account;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;

import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Specific account for Undertow based on the pac4j profile.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class Pac4jAccount implements Account {

    private final LinkedHashMap<String, CommonProfile> profiles;

    private Set<String> roles;
    private Principal principal;

    public Pac4jAccount(final LinkedHashMap<String, CommonProfile> profiles) {
        this.profiles = profiles;
        this.roles = new HashSet<>();
        final List<CommonProfile> listProfiles = ProfileHelper.flatIntoAProfileList(this.profiles);
        for (final CommonProfile profile : listProfiles) {
            final Set<String> roles = profile.getRoles();
            for (final String role : roles) {
                this.roles.add(role);
            }
        }
        final CommonProfile profile = ProfileHelper.flatIntoOneProfile(this.profiles).get();
        this.principal = () -> profile.getId();
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    /**
     * Get the main profile of the authenticated user.
     *
     * @return the main profile
     */
    public CommonProfile getProfile() {
        return ProfileHelper.flatIntoOneProfile(this.profiles).get();
    }

    /**
     * Get all the profiles of the authenticated user.
     *
     * @return the list of profiles
     */
    public List<CommonProfile> getProfiles() {
        return ProfileHelper.flatIntoAProfileList(this.profiles);
    }
}
