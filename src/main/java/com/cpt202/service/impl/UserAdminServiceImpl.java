package com.cpt202.service.impl;

import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * 管理端用户服务实现类。
 * <p>
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
        List<User> users = userRepository.findAll(buildUserSpecification(role, accountStatus));
        List<UserVO> userVos = new ArrayList<>(users.size());
        for (User user : users) {
            userVos.add(UserVO.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .accountStatus(user.getAccountStatus())
                    .build());
        }
        return userVos;
    }

    /**
     * 修改用户状态。
     */
    @Override
    public void updateStatus(Long userId, String accountStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在。"));
        user.setAccountStatus(accountStatus);
        userRepository.save(user);
    }

    private Specification<User> buildUserSpecification(User.UserRole role, String accountStatus) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (accountStatus != null && !accountStatus.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("accountStatus"), accountStatus));
            }
            query.orderBy(criteriaBuilder.asc(root.get("userId")));
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
