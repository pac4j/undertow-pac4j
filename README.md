## What is the undertow-pac4j library ? [![Build Status](https://travis-ci.org/pac4j/undertow-pac4j.png?branch=master)](https://travis-ci.org/pac4j/undertow-pac4j)

The **undertow-pac4j** library is an authentication multi-protocols client for JBoss Undertow.

It supports these 7 authentication mechanisms on client side:

1. OAuth (1.0 & 2.0)
2. CAS (1.0, 2.0, SAML, logout & proxy)
3. HTTP (form & basic auth authentications)
4. OpenID
5. SAML (2.0)
6. GAE UserService
7. OpenID Connect (1.0).

It's available under the Apache 2 license and based on the [pac4j](https://github.com/pac4j/pac4j) library.


## Providers supported

<table>
<tr><th>Provider</th><th>Protocol</th><th>Maven dependency</th><th>Client class</th><th>Profile class</th></tr>
<tr><td>CAS server</td><td>CAS</td><td>pac4j-cas</td><td>CasClient & CasProxyReceptor</td><td>CasProfile</td></tr>
<tr><td>CAS server using OAuth Wrapper</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>CasOAuthWrapperClient</td><td>CasOAuthWrapperProfile</td></tr>
<tr><td>DropBox</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>DropBoxClient</td><td>DropBoxProfile</td></tr>
<tr><td>Facebook</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FacebookClient</td><td>FacebookProfile</td></tr>
<tr><td>GitHub</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>GitHubClient</td><td>GitHubProfile</td></tr>
<tr><td>Google</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>Google2Client</td><td>Google2Profile</td></tr>
<tr><td>LinkedIn</td><td>OAuth 1.0 & 2.0</td><td>pac4j-oauth</td><td>LinkedInClient & LinkedIn2Client</td><td>LinkedInProfile & LinkedIn2Profile</td></tr>
<tr><td>Twitter</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>TwitterClient</td><td>TwitterProfile</td></tr>
<tr><td>Windows Live</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WindowsLiveClient</td><td>WindowsLiveProfile</td></tr>
<tr><td>WordPress</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WordPressClient</td><td>WordPressProfile</td></tr>
<tr><td>Yahoo</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>YahooClient</td><td>YahooProfile</td></tr>
<tr><td>PayPal</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>PayPalClient</td><td>PayPalProfile</td></tr>
<tr><td>Vk</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>VkClient</td><td>VkProfile</td></tr>
<tr><td>Foursquare</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FoursquareClient</td><td>FoursquareProfile</td></tr>
<tr><td>Bitbucket</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>BitbucketClient</td><td>BitbucketProfile</td></tr>
<tr><td>ORCiD</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>OrcidClient</td><td>OrcidProfile</td></tr>
<tr><td>Strava</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>StravaClient</td><td>StravaProfile</td></tr>
<tr><td>Web sites with basic auth authentication</td><td>HTTP</td><td>pac4j-http</td><td>BasicAuthClient</td><td>HttpProfile</td></tr>
<tr><td>Web sites with form authentication</td><td>HTTP</td><td>pac4j-http</td><td>FormClient</td><td>HttpProfile</td></tr>
<tr><td>Yahoo</td><td>OpenID</td><td>pac4j-openid</td><td>YahooOpenIdClient</td><td>YahooOpenIdProfile</td></tr>
<tr><td>SAML Identity Provider</td><td>SAML 2.0</td><td>pac4j-saml</td><td>Saml2Client</td><td>Saml2Profile</td></tr>
<tr><td>Google App Engine User Service</td><td>Gae User Service Mechanism</td><td>pac4j-gae</td><td>GaeUserServiceClient</td><td>GaeUserServiceProfile</td></tr>
<tr><td>OpenID Connect Provider</td><td>OpenID Connect 1.0</td><td>pac4j-oidc</td><td>OidcClient</td><td>OidcProfile</td></tr>
</table>


## Technical description

This library consists of the following main classes :

1. the **ClientAuthenticationMechanism** is a new Undertow authentication mechanism delegating to the pac4j clients
2. the **Config** is a configuration holder; important attributes are the Undertow SessionManager and SessionConfig
3. the **HandlerHelper** contains utility methods for enhancing Undertow handlers with additional functionality like security, form data management and session
4. the **CallbackHandler** is an Undertow handler to handle the callback of the provider after authentication to finish the authentication process
5. the **LogoutHandler** is an Undertow handler to handle the logout of the user

and is based on the <i>pac4j-*</i> libraries.

Learn more by browsing the [undertow-pac4j Javadoc](http://www.pac4j.org/apidocs/undertow-pac4j/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/index.html).


## How to use it ?

### Add the required dependencies

If you want to use a specific client support, you need to add the appropriate Maven dependency in the *pom.xml* file :

* for OAuth support, the *pac4j-oauth* dependency is required
* for CAS support, the *pac4j-cas* dependency is required
* for HTTP support, the *pac4j-http* dependency is required
* for OpenID support, the *pac4j-openid* dependency is required
* for SAML support, the *pac4j-saml* dependency is required
* for Google App Engine support, the *pac4j-gae* dependency is required
* for OpenID Connect support, the *pac4j-oidc* dependency is required.

For example, to add OAuth support, add the following XML snippet :

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-oauth</artifactId>
      <version>1.7.0</version>
    </dependency>

As these snapshot dependencies are only available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/), the appropriate repository must be added in the *pom.xml* file also :

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

### Define the clients and the config object

All the clients used to communicate with various providers (Facebook, Twitter, a CAS server...) must be defined in your Undertow Server. For example :

    public class DemoServer {
      
      public Clients buildClients() {
        final FacebookClient facebookClient = new FacebookClient("fbkey", "fbsecret");
        final TwitterClient twitterClient = new TwitterClient("twkey", "twsecret");
        // HTTP
        final FormClient formClient = new FormClient("http://localhost:8080/theForm.jsp", new SimpleTestUsernamePasswordAuthenticator());
        final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());        
        // CAS
        final CasClient casClient = new CasClient();
        casClient.setCasLoginUrl("http://localhost:8888/cas/login");        
        // OpenID
        final GoogleOpenIdClient googleOpenIdClient = new GoogleOpenIdClient();
        final Clients clients = new Clients("http://localhost:8080/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient, googleOpenIdClient);
        return clients;
      }
      
      public static void main(final String[] args) {

        Config config = new Config();
        config.setClients(buildClients());
        
      }
    }
    
### Define the "callback filter"

To handle callback from providers, you need to define the appropriate handler :

    public static void main(final String[] args) {

        Config config = new Config();
        config.setClients(buildClients());
        PathHandler path = new PathHandler();
        path.addExactPath("/callback", CallbackHandler.build(config));
        
      }

### Protect the urls

You can protect your urls and force the user to be authenticated by a client by using the *requireAuthentication* handler helper.  
For example, for Facebook if you want to protect the facebookHandler :

    public static void main(final String[] args) {

        Config config = new Config();
        config.setClients(buildClients());
        PathHandler path = new PathHandler();
        path.addExactPath("/callback", CallbackHandler.build(config));
        path.addExactPath("/facebook/index.html",
                HandlerHelper.requireAuthentication(facebookHandler, config, "FacebookClient", false));
        
    }

### Add session capability

Finally you can finalize the configuration by adding session management and start the server:

    public static void main(final String[] args) {

        Config config = new Config();
        config.setClients(buildClients());
        PathHandler path = new PathHandler();
        path.addExactPath("/callback", CallbackHandler.build(config));
        path.addExactPath("/facebook/index.html",
                HandlerHelper.requireAuthentication(facebookHandler, config, "FacebookClient", false));
        
        Undertow server = Undertow.builder().addListener(8080, "localhost")
                .setHandler(HandlerHelper.addSession(path, config)).build();
        server.start();
        
    }

### Get redirection urls

You can also explicitely compute a redirection url to a provider for authentication by using the *getRedirectionUrl* method. For example with Facebook :

    StorageHelper.createSession(exchange);
    WebContext context = new UndertowWebContext(exchange);
    Clients client = config.getClients();
    FacebookClient fbClient = (FacebookClient) client.findClient("FacebookClient");
    String redirectionUrl = Client.getRedirectionUrl(context, false, false);

### Get the user profile

After successful authentication, you can test if the user is authenticated using ```StorageHelper.getProfile()```.

This method returns a wrapper containing an undertow account and a pac4j profile. This profile is a *CommonProfile*, from which you can retrieve the most common properties that all profiles share. 
But you can also cast the user profile to the appropriate profile according to the provider used for authentication.
For example, after a Facebook authentication :
 
    // facebook profile
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;

Or for all the OAuth 1.0/2.0 profiles, to get the access token :
    
    OAuth10Profile oauthProfile = (OAuth10Profile) commonProfile
    String accessToken = oauthProfile.getAccessToken();
    // or
    String accessToken = facebookProfile.getAccessToken();

### Demo

A demo with Facebook, Twitter, CAS, form authentication and basic auth authentication providers is available with [undertow-pac4j-demo](https://github.com/pac4j/undertow-pac4j-demo).

## Versions

The current version **1.0.1-SNAPSHOT** is under development. It's available on the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j) as a Maven dependency :

The last released version is the **1.0.0** :

    <dependency>
        <groupId>org.pac4j</groupId>
        <artifactId>undertow-pac4j</artifactId>
        <version>1.0.0</version>
    </dependency>

See the [release notes](https://github.com/pac4j/undertow-pac4j/wiki/Release-Notes).

## Contact

If you have any question, please use the following mailing lists :
- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)
