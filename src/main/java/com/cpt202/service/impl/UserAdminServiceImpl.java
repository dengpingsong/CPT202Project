package com.cpt202.service.impl;

import com.cpt202.model.entity.User;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端用户服务实现类。
 * <p>
 * 用于承接用户列表查询与账号状态修改相关业务。
 */
@Service
public class UserAdminServiceImpl implements UserAdminService {

    /**
     * 查询用户列表。
     */
    @Override
    public List<UserVO> listUsers(User.UserRole role, String accountStatus) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 修改用户状态。
     */
    @Override
    public void updateStatus(Long userId, String accountStatus) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
