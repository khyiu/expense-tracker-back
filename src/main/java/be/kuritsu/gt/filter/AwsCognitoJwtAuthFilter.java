package be.kuritsu.gt.filter;

import be.kuritsu.gt.filter.jwt.AwsCognitoIdTokenProcessor;
import com.nimbusds.jwt.proc.BadJWTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AwsCognitoJwtAuthFilter extends GenericFilter {

    private final AwsCognitoIdTokenProcessor cognitoIdTokenProcessor;

    @Autowired
    public AwsCognitoJwtAuthFilter(AwsCognitoIdTokenProcessor cognitoIdTokenProcessor) {
        this.cognitoIdTokenProcessor = cognitoIdTokenProcessor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        Authentication authentication;

        try {
            authentication = this.cognitoIdTokenProcessor.authenticate((HttpServletRequest) request);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (BadJWTException e) {
            SecurityContextHolder.clearContext();
            ((HttpServletResponse) response).setStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}