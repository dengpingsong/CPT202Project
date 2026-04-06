package com.cpt202.service.impl;

import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.service.AuthService;
import com.cpt202.vo.LoginVO;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类。
 * <p>
 * 后续将在该类中实现注册、登录、账号校验与登录态生成等逻辑。
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 注册新用户。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    @Override
    public LoginVO register(RegisterUserDTO registerUserDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录展示对象
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
