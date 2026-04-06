package com.cpt202.service;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.vo.LoginVO;

/**
 * 认证服务接口。
 * <p>
 * 负责处理用户注册与登录逻辑，并返回前端登录态所需的展示信息。
 */
public interface AuthService {

    /**
     * 注册新用户。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    LoginVO register(RegisterUserDTO registerUserDTO);

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录展示对象
     */
    LoginVO login(LoginDTO loginDTO);
}
