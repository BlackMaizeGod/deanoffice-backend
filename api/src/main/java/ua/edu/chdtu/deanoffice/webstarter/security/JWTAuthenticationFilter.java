package ua.edu.chdtu.deanoffice.webstarter.security;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ua.edu.chdtu.deanoffice.webstarter.security.SecurityConstants.EXPIRATION_TIME;
import static ua.edu.chdtu.deanoffice.webstarter.security.SecurityConstants.TOKEN_PREFIX;
import static ua.edu.chdtu.deanoffice.webstarter.security.SecurityConstants.SECRET;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            ApplicationUser creds = new ObjectMapper().readValue(req.getInputStream(), ApplicationUser.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException {
        List<String> roles = auth.getAuthorities().stream().map(s -> s.toString()).collect(Collectors.toList());

        String token = generateToken(auth, roles);

        res.getWriter().write(getUserInfoJson(token, roles));
        res.addHeader(SecurityConstants.HEADER_STRING, TOKEN_PREFIX + token);
    }

    private String generateToken(Authentication auth, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(((User) auth.getPrincipal()).getUsername());
        claims.put("scopes", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    private String getUserInfoJson(String token, List<String> roles) {
        JsonObject userInfo = new JsonObject();
        String rolesJson = new GsonBuilder().create().toJson(roles);

        userInfo.addProperty("token", token);
        userInfo.addProperty("roles", rolesJson);
        return userInfo.toString();
    }
}
