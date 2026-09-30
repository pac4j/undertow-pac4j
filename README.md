<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-undertow.png" width="300" alt="undertow-pac4j" />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/org.pac4j/undertow-pac4j"><img src="https://img.shields.io/maven-central/v/org.pac4j/undertow-pac4j?label=Maven%20Central" alt="Maven Central" /></a>
  <a href="https://github.com/pac4j/undertow-pac4j/actions/workflows/ci.yml"><img src="https://github.com/pac4j/undertow-pac4j/actions/workflows/ci.yml/badge.svg" alt="Build status" /></a>
  <img src="https://img.shields.io/badge/Java-17%2B-blue" alt="Java 17+" />
  <img src="https://img.shields.io/badge/Undertow-2.0%20to%202.4-blue" alt="Undertow 2.0 to 2.4" />
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="Apache 2 license" /></a>
</p>

> `undertow-pac4j` is the Undertow implementation of **[pac4j](https://github.com/pac4j/pac4j)**, the security engine for Java.
> If it is useful to you, please ⭐ **[star pac4j on GitHub](https://github.com/pac4j/pac4j)**: it helps other developers discover it!

The `undertow-pac4j` project is an **easy and powerful security library for Undertow** web applications which supports authentication and authorization, but also application logout and advanced features like CSRF protection.
It's based on Java 17, Undertow 2 (from v2.0 to v2.4) and on the **[pac4j security engine](https://github.com/pac4j/pac4j) v6**. It's available under the Apache 2 license.

[**Main concepts and components:**](https://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](https://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for web application authentication while a direct client is for web services authentication:

&#9656; OpenID Connect - SAML - CAS - OAuth - HTTP - LDAP - SQL - JWT - MongoDB - Kerberos - IP address - REST API

2) An [**authorizer**](https://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) A [**matcher**](https://www.pac4j.org/docs/matchers.html) defines whether the `SecurityHandler` must be applied and can be used for additional web processing

4) The `SecurityHandler` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

5) The `CallbackHandler` finishes the login process for an indirect client

6) The `LogoutHandler` logs out the user from the application and triggers the logout at the identity provider level.


## Quick start (OpenID Connect)

Add Undertow (from v2.0 to v2.4, it's a `provided` dependency), `undertow-pac4j` and the OpenID Connect module:

```xml
<dependency>
    <groupId>io.undertow</groupId>
    <artifactId>undertow-core</artifactId>
    <version>2.4.3.Final</version>
</dependency>
<dependency>
    <groupId>org.pac4j</groupId>
    <artifactId>undertow-pac4j</artifactId>
    <version>6.1.0</version>
</dependency>
<dependency>
    <groupId>org.pac4j</groupId>
    <artifactId>pac4j-oidc</artifactId>
    <version>6.5.9</version>
</dependency>
```

Then define your identity provider and register the handlers, wrapped by the Undertow session handler:

```java
final var oidc = new OidcConfiguration()
    .setDiscoveryURI("https://www.casserverpac4j.dev/oidc/.well-known/openid-configuration")
    .setClientId("myclient")
    .setSecret("mysecret")
    .setAllowUnsignedIdTokens(true); // only for this demo server
final var config = new Config("http://localhost:8080/callback", new OidcClient(oidc));

final var path = new PathHandler();
path.addExactPath("/protected", SecurityHandler.build(protectedHandler, config, "OidcClient"));
path.addExactPath("/callback", CallbackHandler.build(config));
path.addExactPath("/logout", new LogoutHandler(config, "/"));

Undertow.builder()
    .addHttpListener(8080, "localhost")
    .setHandler(new SessionAttachmentHandler(path, new InMemorySessionManager("sessions"), new SessionCookieConfig()))
    .build()
    .start();
```

That's it: `/protected` now requires an OpenID Connect login, and the authenticated user is available as a `Pac4jAccount` via `exchange.getSecurityContext().getAuthenticatedAccount()`.
Read the [full guide](https://www.pac4j.org/how-to-secure-an-undertow-application-with-oidc.html) for more details (session, logout, profiles...).


## Usage

### 1) [Add the required dependencies](https://github.com/pac4j/undertow-pac4j/wiki/Dependencies)

### 2) Define:

### - the [security configuration](https://github.com/pac4j/undertow-pac4j/wiki/Security-configuration)
### - the [callback configuration](https://github.com/pac4j/undertow-pac4j/wiki/Callback-configuration), only for web applications
### - the [logout configuration](https://github.com/pac4j/undertow-pac4j/wiki/Logout-configuration)

### 3) [Apply security](https://github.com/pac4j/undertow-pac4j/wiki/Apply-security)

### 4) [Get the authenticated user profiles](https://github.com/pac4j/undertow-pac4j/wiki/Get-the-authenticated-user-profiles)


## Demo

The demo webapp: [undertow-pac4j-demo](https://github.com/pac4j/undertow-pac4j-demo) is available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Versions

The latest released version is the [![Maven Central](https://img.shields.io/maven-central/v/org.pac4j/undertow-pac4j.svg)](https://repo1.maven.org/maven2/org/pac4j/undertow-pac4j). The [next version](https://github.com/pac4j/undertow-pac4j/wiki/Next-version) is under development.

See the [release notes](https://github.com/pac4j/undertow-pac4j/wiki/Release-notes).

See the [migration guide](https://github.com/pac4j/undertow-pac4j/wiki/Migration-guide) as well.


## Need help?

You can use the [mailing lists](https://www.pac4j.org/mailing-lists.html) or the [commercial support](https://www.pac4j.org/commercial-support.html).
