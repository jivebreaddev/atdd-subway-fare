package nextstep.auth.principal;

import io.jsonwebtoken.MalformedJwtException;
import nextstep.auth.AuthenticationException;
import nextstep.auth.token.JwtTokenProvider;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
    private JwtTokenProvider jwtTokenProvider;

    public AuthenticationPrincipalArgumentResolver(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        AuthenticationPrincipal principal = parameter.getParameterAnnotation(AuthenticationPrincipal.class);
        boolean required = principal.required();
        String authorization = webRequest.getHeader("Authorization");

        if(!StringUtils.hasText(authorization) && !required){
            return NonLoginUser.of();
        }

        if("bearer".equalsIgnoreCase(authorization) || (!"bearer".equalsIgnoreCase(authorization.split(" ")[0]))){
            throw new AuthenticationException("로그인 정보를 확인해주세요!");
        }

        String token = authorization.split(" ")[1];

        String username = jwtTokenProvider.getPrincipal(token);
        String role = jwtTokenProvider.getRoles(token);

        return new UserPrincipal(username, role);
    }
}
