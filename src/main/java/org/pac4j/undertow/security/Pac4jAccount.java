/*
  Copyright 2014 - 2016 pac4j organization

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
package org.pac4j.undertow.security;

import io.undertow.security.idm.Account;
import org.pac4j.core.profile.UserProfile;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * Specific account for Undertow based on the pac4j profile.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class Pac4jAccount implements Account {

    private UserProfile profile;
    private Principal principal;
    private Set<String> roles;

    public Pac4jAccount(final UserProfile profile) {
        this.profile = profile;
        this.roles = new HashSet<String>(profile.getRoles());
        this.principal = new Principal() {
            @Override
            public String getName() {
                return profile.getId();
            }
        };
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    public UserProfile getProfile() {
        return profile;
    }
}
