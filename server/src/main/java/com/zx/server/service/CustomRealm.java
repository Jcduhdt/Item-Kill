package com.zx.server.service;

import com.zx.model.entity.User;
import com.zx.model.mapper.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-12
 * 用户自定义的realm-用于shiro的认证、授权
 */

public class CustomRealm extends AuthorizingRealm {

    private static final Logger log = LoggerFactory.getLogger(CustomRealm.class);

    private static final Long sessionKeyTimeOut = 3600_000L;

    @Autowired(required = false)
    private UserMapper userMapper;


    /**
     * 授权
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     * 认证-登录
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String userName = token.getUsername();
        String password = String.valueOf(token.getPassword());
        log.info("当前登录的用户名={} 密码={} ", userName, password);

        User user = userMapper.selectByUserName(userName);
        if (user == null) {
            throw new UnknownAccountException("用户名不存在!");
        }
        if (!Objects.equals(1, user.getIsActive().intValue())) {
            throw new DisabledAccountException("当前用户已被禁用!");
        }
        if (!user.getPassword().equals(password)) {
            throw new IncorrectCredentialsException("用户名密码不匹配!");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user.getUserName(), password, getName());
        setSession("uid", user.getId());
        return info;
    }

    /**
     * 将key与对应的value塞入shiro的session中-最终交给HttpSession进行管理(如果是分布式session配置，那么就是交给redis管理)
     * @param key
     * @param value
     */
    private void setSession(String key, Object value) {
        Session session = SecurityUtils.getSubject().getSession();
        if (session != null) {
            session.setAttribute(key, value);
            session.setTimeout(sessionKeyTimeOut);
        }
    }
}

