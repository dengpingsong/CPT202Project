package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端用户服务实现类。
 * 用于承接用户列表查询与账号状态修改相关业务。
 */
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    /**
     * 查询用户列表。
     */
    @Override
    public List<UserVO> listUsers(User.UserRole role, String accountStatus) {
        String normalizedAccountStatus = accountStatus == null ? null : accountStatus.trim();
        return userRepository.findUserVos(role, normalizedAccountStatus);
    }

    /**
     * 修改用户状态。
     */
    @Override
    public void updateStatus(Long userId, String accountStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));
        user.setAccountStatus(accountStatus);
        userRepository.save(user);
    }
}
