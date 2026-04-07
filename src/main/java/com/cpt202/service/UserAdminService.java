package com.cpt202.service;

import com.cpt202.model.entity.User;
import com.cpt202.vo.UserVO;

import java.util.List;

/**
 * 管理端用户服务接口。
 * <p>
 * 负责用户列表查询及账号状态维护。
 */
public interface UserAdminService {

    /**
     * 按角色和账号状态查询用户列表。
     *
     * @param role 用户角色
     * @param accountStatus 账号状态
     * @return 用户展示对象列表
     */
    List<UserVO> listUsers(User.UserRole role, String accountStatus);

    /**
     * 修改指定用户的账号状态。
     *
     * @param userId 用户主键
     * @param accountStatus 账号状态
     */
    void updateStatus(Long userId, String accountStatus);
}
