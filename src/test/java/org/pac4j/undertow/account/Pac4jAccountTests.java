package org.pac4j.undertow.account;

import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link Pac4jAccount}.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
class Pac4jAccountTests {

    private static final String ID = "jerome";

    private static Pac4jAccount buildAccount() {
        final CommonProfile profile = new CommonProfile();
        profile.setId(ID);
        profile.setClientName("MyClient");
        profile.addRole("admin");
        final LinkedHashMap<String, UserProfile> profiles = new LinkedHashMap<>();
        profiles.put("MyClient", profile);
        return new Pac4jAccount(profiles);
    }

    @Test
    void principal() {
        final Pac4jAccount account = buildAccount();
        assertEquals(ID, account.getPrincipal().getName());
        assertEquals(buildAccount().getPrincipal(), account.getPrincipal());
        assertTrue(account.getPrincipal().toString().contains(ID));
        assertEquals(Set.of("admin"), account.getRoles());
    }

    @Test
    void serialization() throws Exception {
        final Pac4jAccount account = buildAccount();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(account);
        }
        final Pac4jAccount deserialized;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            deserialized = (Pac4jAccount) in.readObject();
        }
        assertEquals(account.getPrincipal(), deserialized.getPrincipal());
        assertEquals(account.getRoles(), deserialized.getRoles());
        assertEquals(ID, deserialized.getProfile().getId());
    }
}
