package org.pac4j.undertow.account;

import io.undertow.security.idm.Account;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.UserProfile;

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

    private final List<UserProfile> profiles;

    private Set<String> roles;
    private Principal principal;

    public Pac4jAccount(final LinkedHashMap<String, UserProfile> profiles) {
        this.roles = new HashSet<>();
        this.profiles = ProfileHelper.flatIntoAProfileList(profiles);
        for (final UserProfile profile : this.profiles) {
            final Set<String> roles = profile.getRoles();
            for (final String role : roles) {
                this.roles.add(role);
            }
        }
        final UserProfile profile = ProfileHelper.flatIntoOneProfile(this.profiles).get();
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
    public UserProfile getProfile() {
        return ProfileHelper.flatIntoOneProfile(this.profiles).get();
    }

    /**
     * Get all the profiles of the authenticated user.
     *
     * @return the list of profiles
     */
    public List<UserProfile> getProfiles() {
        return this.profiles;
    }
}
