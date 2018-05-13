package ua.edu.chdtu.deanoffice.webstarter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static ua.edu.chdtu.deanoffice.webstarter.security.SecurityConstants.HEADER_STRING;
import static ua.edu.chdtu.deanoffice.webstarter.security.SecurityConstants.TOKEN_PREFIX;
import static ua.edu.chdtu.deanoffice.webstarter.security.SecurityConstants.SECRET;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    public JWTAuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String token = getToken(req);
        if (token == null) {
            chain.doFilter(req, res);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String userName;
        List<GrantedAuthority> authorities;
        if (token != null) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, ""));
                userName = jwsClaims.getBody().getSubject();
                List<String> scopes = jwsClaims.getBody().get("scopes", List.class);
                authorities = scopes.stream()
                        .map(authority -> new SimpleGrantedAuthority(authority))
                        .collect(Collectors.toList());

            } catch (JwtException e) {
                return null;
            }
            if (userName != null) {
                return new UsernamePasswordAuthenticationToken(userName, null, authorities);
            }
            return null;
        }
        return null;
    }

    private String getToken(HttpServletRequest req) {
        boolean isTokenInHeader = req.getHeader(HEADER_STRING) != null;

        if (isTokenInHeader) {
            return req.getHeader(HEADER_STRING).replace(TOKEN_PREFIX, "");
        } else {
            return req.getParameter("auth-jwt-token");
        }
    }
}
