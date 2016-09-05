<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-undertow.png" width="300" />
</p>

The `undertow-pac4j` project is an **easy and powerful security library for Undertow** web applications which supports authentication and authorization, but also application logout and advanced features like CSRF protection.
It's based on Java 8, Undertow 1.3 and on the **[pac4j security engine](https://github.com/pac4j/pac4j)**. It's available under the Apache 2 license.

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - OpenID - Google App Engine - LDAP - SQL - JWT - MongoDB - Stormpath - IP address

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) The `SecurityHandler` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

4) The `CallbackHandler` finishes the login process for an indirect client

5) The `ApplicationLogoutHandler` logs out the user from the application.

==

Just follow these easy steps to secure your Undertow web application:

### 1) Add the required dependencies (`undertow-pac4j` + `pac4j-*` libraries)

You need to add a dependency on:
 
- the `undertow-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **1.2.0**)
- the appropriate `pac4j` [submodules](http://www.pac4j.org/docs/clients.html) (<em>groupId</em>: **org.pac4j**, *version*: **1.9.1**): `pac4j-oauth` for OAuth support (Facebook, Twitter...), `pac4j-cas` for CAS support, `pac4j-ldap` for LDAP authentication, etc.

All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).

---

### 2) Define the configuration (`Config` + `Client` + `Authorizer`)

The configuration (`org.pac4j.core.config.Config`) contains all the clients and authorizers required by the application to handle security.

It must be built via a configuration factory (`org.pac4j.core.config.ConfigFactory`):

```java
public class DemoConfigFactory implements ConfigFactory {

    public Config build() {
        final OidcClient oidcClient = new OidcClient();
        oidcClient.setClientID(clientId);
        oidcClient.setSecret(secret);
        oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
        oidcClient.setAuthorizationGenerator(profile -> profile.addRole("ROLE_ADMIN"));

        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:metadata-okta.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client");
        cfg.setServiceProviderMetadataPath("sp-metadata.xml");
        final SAML2Client saml2Client = new SAML2Client(cfg);

        final FacebookClient facebookClient = new FacebookClient(fbId, fbSecret);
        final TwitterClient twitterClient = new TwitterClient(twId, twSecret);

        final FormClient formClient = new FormClient("http://localhost:8080/loginForm.html", new SimpleTestUsernamePasswordAuthenticator());
        final IndirectBasicAuthClient indirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final CasClient casClient = new CasClient("https://casserverpac4j.herokuapp.com/login");

        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(DemoServer.JWT_SALT));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final AnonymousClient anonymousClient = new AnonymousClient();

        final Clients clients = new Clients("http://localhost:8080/callback", saml2Client, facebookClient, twitterClient,
                formClient, indirectBasicAuthClient, casClient, parameterClient, directBasicAuthClient, oidcClient, anonymousClient);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        return config;
    }
}
```

`http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients.

Notice that you can define:

1) a specific [`SessionStore`](http://www.pac4j.org/docs/session-store.html) using the `setSessionStore(sessionStore)` method (by default, it uses the `UndertowSessionStore` which relies on the Undertow session configuration)

2) specific [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.

---

### 3) Protect urls (`SecurityHandler`)

You can protect (authentication + authorizations) the urls of your Undertow application by using the `SecurityHandler` and defining the appropriate mapping. It has the following behaviour:

1) If the HTTP request matches the `matchers` configuration (or no `matchers` are defined), the security is applied. Otherwise, the user is automatically granted access.

2) First, if the user is not authenticated (no profile) and if some clients have been defined in the `clients` parameter, a login is tried for the direct clients.

3) Then, if the user has a profile, authorizations are checked according to the `authorizers` configuration. If the authorizations are valid, the user is granted access. Otherwise, a 403 error page is displayed.

4) Finally, if the user is still not authenticated (no profile), he is redirected to the appropriate identity provider if the first defined client is an indirect one in the `clients` configuration. Otherwise, a 401 error page is displayed.


The following parameters are available:

1) `config`: the previous security configuration

2) `clients` (optional): the list of client names (separated by commas) used for authentication:
- in all cases, this filter requires the user to be authenticated. Thus, if the `clients` is blank or not defined, the user must have been previously authenticated
- if the `client_name` request parameter is provided, only this client (if it exists in the `clients`) is selected.

3) `authorizers` (optional): the list of authorizer names (separated by commas) used to check authorizations:
- if the `authorizers` is blank or not defined, no authorization is checked
- the following authorizers are available by default (without defining them in the configuration):
  * `isFullyAuthenticated` to check if the user is authenticated but not remembered, `isRemembered` for a remembered user, `isAnonymous` to ensure the user is not authenticated, `isAuthenticated` to ensure the user is authenticated (not necessary by default unless you use the `AnonymousClient`)
  * `hsts` to use the `StrictTransportSecurityHeader` authorizer, `nosniff` for `XContentTypeOptionsHeader`, `noframe` for `XFrameOptionsHeader `, `xssprotection` for `XSSProtectionHeader `, `nocache` for `CacheControlHeader ` or `securityHeaders` for the five previous authorizers
  * `csrfToken` to use the `CsrfTokenGeneratorAuthorizer` with the `DefaultCsrfTokenGenerator` (it generates a CSRF token and saves it as the `pac4jCsrfToken` request attribute and in the `pac4jCsrfToken` cookie), `csrfCheck` to check that this previous token has been sent as the `pac4jCsrfToken` header or parameter in a POST request and `csrf` to use both previous authorizers.

4) `matchers` (optional): the list of matcher names (separated by commas) that the request must satisfy to check authentication / authorizations

5) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default).

Example:

```java
PathHandler path = new PathHandler();
path.addExactPath("/facebook/index.html", SecurityHandler.build(DemoHandlers.protectedIndex, config, "FacebookClient"));
```


### 4) Define the callback endpoint only for indirect clients (`CallbackHandler`)

For indirect clients (like Facebook), the user is redirected to an external identity provider for login and then back to the application.
Thus, a callback endpoint is required in the application. It is managed by the `CallbackHandler` which has the following behaviour:

1) the credentials are extracted from the current request to fetch the user profile (from the identity provider) which is then saved in the web session

2) finally, the user is redirected back to the originally requested url (or to the `defaultUrl`).


The following parameters are available:

1) `configFactory` (optional): the factory to initialize the configuration. By default, the configuration is shared across filters so it can be specified only once, but each filter can defined its own configuration if necessary

2) `defaultUrl` (optional): it's the default url after login if no url was originally requested (`/` by default)

3) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default).

Example:

```java
path.addExactPath("/callback", CallbackHandler.build(config, null, true));
```


### 5) Get the user profile (`SecurityContext`)

Like for any Undertow web application, you can get the authenticated user via the `exchange.getSecurityContext().getAuthenticatedAccount()`.
If the user is authenticated, the appropriate account will be stored in the context as a `Pac4jAccount`,
on which you can get the main profile (`getProfile` method) or all profiles (`getProfiles` method) of the authenticated user:

```java
SecurityContext securityContext = exchange.getSecurityContext();
if (securityContext != null) {
  Account account = securityContext.getAuthenticatedAccount();
  if (account instanceof Pac4jAccount) {
    List<CommonProfile> = ((Pac4jAccount) account).getProfiles();
  }
}
```

In fact, the Undertow `SecurityContext` is populated by the `SecurityHandler`, so you may need to use the `AnonymousClient` or directly the regular pac4j `ProfileManager` for anonymous urls:

```java
UndertowWebContext context = new UndertowWebContext(exchange);
ProfileManager manager = new ProfileManager(context);
Optional<CommonProfile> profile = manager.get(true);
```

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common attributes that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:

```java
FacebookProfile facebookProfile = (FacebookProfile) commonProfile;
```


### 6) Logout (`ApplicationLogoutHandler`)

You can log out the current authenticated user using the `ApplicationLogoutHandler`. It has the following behaviour:

1) after logout, the user is redirected to the url defined by the `url` request parameter if it matches the `logoutUrlPattern`

2) or the user is redirected to the `defaultUrl` if it is defined

3) otherwise, a blank page is displayed.


The following parameters are available:

1) `defaultUrl` (optional): the default logout url if no `url` request parameter is provided or if the `url` does not match the `logoutUrlPattern` (not defined by default)

2) `logoutUrlPattern` (optional): the logout url pattern that the `url` parameter must match (only relative urls are allowed by default).

Example:

```java
path.addExactPath("/logout", new ApplicationLogoutHandler(config, "/?defaulturlafterlogout"));
```


## Migration guide

### 1.1 -> 1.2

The `SecurityMechanism` is replaced by a `SecurityHandler` (more in the spirit of the pac4j "security filter"). It populates the Undertow security context with the authenticated user as a `Pac4jaccount`

The `ApplicationLogoutHandler` behaviour has slightly changed: even without any url request parameter, the user will be redirected to the `defaultUrl` if it has been defined.

### 1.0 -> 1.1

The `ClientAuthenticationMechanism` has been replaced by the `SecurityMechanism` and now handle both authentication and authorizations.

The `HttpResponseHelper` has been renamed in `ExchangeHelper`.

The application logout process is managed with the `ApplicationLogoutHandler`.


## Demo

The demo webapp: [undertow-pac4j-demo](https://github.com/pac4j/undertow-pac4j-demo) is available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Release notes

See the [release notes](https://github.com/pac4j/undertow-pac4j/wiki/Release-Notes). Learn more by browsing the [undertow-pac4j Javadoc](http://www.javadoc.io/doc/org.pac4j/undertow-pac4j/1.2.0) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/1.9.1/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)


## Development

The version 1.2.1-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/undertow-pac4j.png?branch=master)](https://travis-ci.org/pac4j/undertow-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the Maven *pom.xml* file for example:

    <repositories>
      <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
          <enabled>false</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
      </repository>
    </repositories>
