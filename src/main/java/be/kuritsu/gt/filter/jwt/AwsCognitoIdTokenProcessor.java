package be.kuritsu.gt.filter.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWKSecurityContext;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Component
public class AwsCognitoIdTokenProcessor {

    private static final int JKWS_ENDPOINT_CONNECTION_READ_TIMEOUT = 2000;

    private final ConfigurableJWTProcessor<JWKSecurityContext> configurableJWTProcessor;
    private final String authorizedClientRegistrationId;
    private final String oauthAccessTokenUserNameAttributeName;
    private final String oauthAccessTokenGroupsAttributeName;

    public AwsCognitoIdTokenProcessor(@Value("${oauth.endpoint.jwks.url}") String oauthEndpointJwksURL,
                                      @Value("${oauth.client.name}") String authorizedClientRegistrationId,
                                      @Value("${oauth.accessToken.userName.attributeName}") String oauthAccessTokenUserNameAttributeName,
                                      @Value("${oauth.accessToken.groups.attributeName}") String oauthAccessTokenGroupsAttributeName) throws MalformedURLException {
        ResourceRetriever resourceRetriever = new DefaultResourceRetriever(JKWS_ENDPOINT_CONNECTION_READ_TIMEOUT, JKWS_ENDPOINT_CONNECTION_READ_TIMEOUT);
        URL jwkSetURL = new URL(oauthEndpointJwksURL);
        JWKSource<JWKSecurityContext> keySource = new RemoteJWKSet<>(jwkSetURL, resourceRetriever);
        ConfigurableJWTProcessor<JWKSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<JWKSecurityContext> keySelector = new JWSVerificationKeySelector<>(RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        this.configurableJWTProcessor = jwtProcessor;

        this.authorizedClientRegistrationId = authorizedClientRegistrationId;
        this.oauthAccessTokenUserNameAttributeName = oauthAccessTokenUserNameAttributeName;
        this.oauthAccessTokenGroupsAttributeName = oauthAccessTokenGroupsAttributeName;
    }

    public Authentication authenticate(HttpServletRequest request) throws ParseException, JOSEException, BadJOSEException {
        String idToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (idToken != null) {
            JWTClaimsSet claims = this.configurableJWTProcessor.process(this.getBearerToken(idToken), null);

            if (!isAccessJWT(claims)) {
                throw new BadJWTException("Provided token is not an access token");
            }

            String username = getUserNameFrom(claims);

            if (username != null) {
                List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(claims);
                OAuth2User oAuth2User = new DefaultOAuth2User(grantedAuthorities, claims.getClaims(), oauthAccessTokenUserNameAttributeName);
                return new OAuth2AuthenticationToken(oAuth2User, grantedAuthorities, authorizedClientRegistrationId);
            }
        }
        return null;
    }

    private boolean isAccessJWT(JWTClaimsSet claims) {
        return claims.getClaim("token_use").equals("access");
    }

    private String getUserNameFrom(JWTClaimsSet claims) {
        return claims.getClaims()
                .get(this.oauthAccessTokenUserNameAttributeName)
                .toString();
    }

    private List<GrantedAuthority> getGrantedAuthorities(JWTClaimsSet claims) {
        JSONArray userGroups = (JSONArray) claims.getClaim(oauthAccessTokenGroupsAttributeName);
        return userGroups.stream()
                .map(userGroup -> String.format("ROLE_%s", userGroup.toString().toUpperCase()))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String getBearerToken(String token) {
        return token.startsWith("Bearer ") ? token.substring("Bearer ".length()) : token;
    }

}
