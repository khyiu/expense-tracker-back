package be.kuritsu.gt.config;

import be.kuritsu.gt.filter.AwsCognitoJwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AwsCognitoJwtAuthFilter jwtAuthFilter;

    @Autowired
    public OAuth2SecurityConfig(AwsCognitoJwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/actuator/info")
                .permitAll()
                // todo kyiu: restrict access to other actuator endpoints to admins user
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(@Value("${oauth.client.name}") String oauthClientName,
                                                                     @Value("${oauth.client.scope}") String oauthClientScope,
                                                                     @Value("${oauth.login.redirect.url}") String oauthLoginRedirectURL,
                                                                     @Value("${oauth.endpoint.authorization.url}") String oauthEndpointAuthorizationURL,
                                                                     @Value("${oauth.endpoint.token.url}") String oauthEndpointTokenURL,
                                                                     @Value("${oauth.endpoint.userInfo.url}") String oauthEndpointUserInfoURL,
                                                                     @Value("${oauth.endpoint.jwks.url}") String oauthEndpointJwksURL,
                                                                     @Value("${oauth.userName.attributeName}") String oauthUserNameAttributeName,
                                                                     @Value("${oauth.client.id}") String oauthClientId,
                                                                     @Value("${oauth.client.secret}") String oauthClientSecret) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(oauthClientName)
                .clientName(oauthClientName)
                .scope(oauthClientScope)
                .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate(oauthLoginRedirectURL)
                .authorizationUri(oauthEndpointAuthorizationURL)
                .tokenUri(oauthEndpointTokenURL)
                .userInfoUri(oauthEndpointUserInfoURL)
                .userNameAttributeName(oauthUserNameAttributeName)
                .jwkSetUri(oauthEndpointJwksURL)
                .clientId(oauthClientId)
                .clientSecret(oauthClientSecret)
                .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }
}
