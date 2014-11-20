/*
  Copyright 2014 - 2014 Michael Remond

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
package org.pac4j.undertow;

import io.undertow.security.idm.Account;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.pac4j.core.profile.CommonProfile;

/**
 * Wrapper containing a pac4j user profile and an Undertow account.
 * <p/>
 * This wrapper is required because we cannot have a unified class because the getRoles
 * method has a different signature in {@link CommonProfile} and in {@link Account}.
 * 
 * @author Michael Remond
 * @since 1.0.0
 *
 */
public class ProfileWrapper implements Serializable {

    private static final long serialVersionUID = 8996323477801100456L;

    final private CommonProfile profile;
    final private Account account;

    public ProfileWrapper(final CommonProfile profile) {
        this.profile = profile;
        this.account = new ClientAccount(profile);
    }

    CommonProfile getProfile() {
        return this.profile;
    }

    Account getAccount() {
        return this.account;
    }

    @Override
    public String toString() {
        return profile.toString();
    }

    private class ClientAccount implements Account {

        private Principal principal;
        private Set<String> roles;

        public ClientAccount(final CommonProfile profile) {
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
    }

}
