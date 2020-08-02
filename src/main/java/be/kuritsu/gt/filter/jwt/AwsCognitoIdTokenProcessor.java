package be.kuritsu.gt.filter.jwt;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import net.minidev.json.JSONArray;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Component
public class AwsCognitoIdTokenProcessor {

    private final ConfigurableJWTProcessor configurableJWTProcessor;

    public AwsCognitoIdTokenProcessor() throws MalformedURLException {
        ResourceRetriever resourceRetriever =
                new DefaultResourceRetriever(2000, 2000);
        URL jwkSetURL = new URL("https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_loOqV5Kja/.well-known/jwks.json");
        JWKSource keySource = new RemoteJWKSet(jwkSetURL, resourceRetriever);
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        JWSKeySelector keySelector = new JWSVerificationKeySelector(RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        this.configurableJWTProcessor = jwtProcessor;
    }

    public Authentication authenticate(HttpServletRequest request) throws Exception {
        String idToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (idToken != null) {
            JWTClaimsSet claims = this.configurableJWTProcessor.process(this.getBearerToken(idToken), null);
            validateIssuer(claims);
            verifyIfIdToken(claims);
            String username = getUserNameFrom(claims);
            if (username != null) {
                List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(claims);
                User user = new User(username, "", getGrantedAuthorities(claims));
                return new JwtAuthentication(user, claims, grantedAuthorities);
            }
        }
        return null;
    }

    private String getUserNameFrom(JWTClaimsSet claims) {
        return claims.getClaims().get("username").toString();
    }

    private List<GrantedAuthority> getGrantedAuthorities(JWTClaimsSet claims) {
        JSONArray userGroups = (JSONArray) claims.getClaim("cognito:groups");
        return userGroups.stream()
                .map(userGroup -> String.format("ROLE_%s", userGroup.toString().toUpperCase()))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private void verifyIfIdToken(JWTClaimsSet claims) throws Exception {
        if (!claims.getIssuer().equals("https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_loOqV5Kja")) {
            throw new Exception("JWT Token is not an ID Token");
        }
    }

    private void validateIssuer(JWTClaimsSet claims) throws Exception {
        if (!claims.getIssuer().equals("https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_loOqV5Kja")) {
            throw new Exception(String.format("Issuer %s does not match cognito idp %s", claims.getIssuer(), "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_loOqV5Kja"));
        }
    }

    private String getBearerToken(String token) {
        return token.startsWith("Bearer ") ? token.substring("Bearer ".length()) : token;
    }

}
