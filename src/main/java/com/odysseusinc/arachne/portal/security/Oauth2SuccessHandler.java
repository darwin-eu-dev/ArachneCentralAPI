package com.odysseusinc.arachne.portal.security;

import com.google.common.collect.ImmutableMap;
import com.odysseusinc.arachne.portal.model.ExternalLogin;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.ExternalLoginService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.authenticator.service.authentication.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class Oauth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    private final static Logger log = LoggerFactory.getLogger(Oauth2SuccessHandler.class);


    private final String header;
    private final boolean enabledByDefault;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final ExternalLoginService externalLoginService;
    private final ProfessionalTypeService professionalTypeService;

    public Oauth2SuccessHandler(
            @Value("${arachne.token.header}") String header,
            @Value("${user.enabled.default:false}") boolean enabledByDefault,
            UserService userService,
            TokenProvider tokenProvider,
            ExternalLoginService externalLoginService,
            ProfessionalTypeService professionalTypeService
    ) {
        this.header = header;
        this.enabledByDefault = enabledByDefault;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.externalLoginService = externalLoginService;
        this.professionalTypeService = professionalTypeService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("Unsupported authentication type [{}]", authentication.getClass());
            super.onAuthenticationSuccess(request, response, authentication);
        } else {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User principal = oauthToken.getPrincipal();

            String method = oauthToken.getAuthorizedClientRegistrationId();
            ///additionalInfo.put("token", "Do we need this???");

            Map<String, Object> attributes = principal.getAttributes();
            String sub = (String) attributes.get(StandardClaimNames.SUB);
            String email = (String) attributes.getOrDefault(StandardClaimNames.EMAIL, "");
            Optional<URL> issuerUrl = Optional.ofNullable((URL) attributes.get(IdTokenClaimNames.ISS));
            String safeSub = EMAIL_PATTERN.matcher(sub).matches() ? sub
                    : issuerUrl.map(url -> sub + "@" + url.getHost()).orElseGet(() ->
                    sub + "@" + oauthToken.getAuthorizedClientRegistrationId()
            );
            String issuer = issuerUrl.map(URL::toString).orElseGet(() ->
                    // Fall back to use domain from sub, as some providers don't send issuer
                    Stream.of(safeSub.split("@")).reduce((a, b) -> b).orElseThrow(() -> new RuntimeException("Missing sub"))
            );

            ExternalLogin login = externalLoginService.login(
                    issuer, safeSub, email, createOrFindUser(method, attributes, safeSub, email)
            );
            IUser user = login.getUser();
            if (user.getEnabled()) {
                String username = ObjectUtils.firstNonNull(user.getUsername(), user.getEmail());
                // This is a bit ugly, however
                Map<String, Object> additionalInfo = ImmutableMap.of("method", user.getOrigin());
                Date exp = new Date(new Date().getTime() + 86400000L); // 24 hours
                String token = tokenProvider.createToken(username, additionalInfo, exp);
                // TODO Put more stuff in token???
                Cookie cookie = new Cookie(header, token);
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                super.onAuthenticationSuccess(request, response, authentication);
            } else {
                response.sendRedirect("/auth/login?message=inactive");
            }
        }
    }

    private Supplier<IUser> createOrFindUser(String method, Map<String, Object> attributes, String sub, String email) {
        return () -> Optional.ofNullable(
                userService.getByUsername(method, sub)
        ).orElseGet(() -> {
            log.info("User [{}/{}] not found in DB, creating...", method, sub);
            return createUser(attributes, sub, method, email);
        });
    }

    // TODO For some reason we are not able to get OidcUser here and use its nice getters, and only get DefaultOAuth2User ...

    private IUser createUser(Map<String, Object> attributes, String username, String origin, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username);
        user.setEnabled(enabledByDefault);
        user.setContactEmail(email);
        // Elixir currently gives nothing under StandardClaimNames.EMAIL_VERIFIED
        user.setEmailConfirmed(true);
        user.setFirstname((String) attributes.getOrDefault(StandardClaimNames.GIVEN_NAME, ""));
        user.setLastname((String) attributes.getOrDefault(StandardClaimNames.FAMILY_NAME, ""));
        user.setMiddlename((String) attributes.getOrDefault(StandardClaimNames.MIDDLE_NAME, ""));
        user.setOrganization("");

        // TODO Need to make meaningful choice here
        user.setProfessionalType(professionalTypeService.list().iterator().next());

        IUser created = userService.createExternal(user, origin);
        log.info("User [{}] successfully created with id [{}]", email, created.getId());
        return created;
    }


}
