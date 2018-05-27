package com.litemall.distributed_two.annotation.support;

import com.litemall.distributed_two.annotation.LoginAdmin;
import com.litemall.distributed_two.service.AdminTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginAdminHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private AdminTokenManager adminTokenManager;

    public static final String LOGIN_TOKEN_KEY = "X-Token";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Integer.class)&&parameter.hasParameterAnnotation(LoginAdmin.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) throws Exception {

//        return new Integer(1);
        String token = request.getHeader(LOGIN_TOKEN_KEY);
        if(token == null || token.isEmpty()){
            return null;
        }

        return adminTokenManager.getUserId(token);
    }
}
