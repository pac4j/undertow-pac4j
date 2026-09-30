package org.pac4j.undertow.account;

import io.undertow.security.idm.Account;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.UserProfile;

import java.io.Serial;
import java.io.Serializable;
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

    @Serial
    private static final long serialVersionUID = 2092398417475516512L;

    private final List<UserProfile> profiles;
    private final Set<String> roles;
    private final Principal principal;

    public Pac4jAccount(final LinkedHashMap<String, UserProfile> profiles) {
        this.roles = new HashSet<>();
        this.profiles = ProfileHelper.flatIntoAProfileList(profiles);
        for (final UserProfile profile : this.profiles) {
            final Set<String> roles = profile.getRoles();
            this.roles.addAll(roles);
        }
        final UserProfile profile = ProfileHelper.flatIntoOneProfile(this.profiles).get();
        this.principal = new Pac4jPrincipal(profile.getId());
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

    /**
     * Serializable principal named after the identifier of the authenticated user.
     *
     * @param name the principal name
     */
    private record Pac4jPrincipal(String name) implements Principal, Serializable {

        @Override
        public String getName() {
            return name;
        }
    }
}
