package com.cpt202.repository;

import com.cpt202.model.entity.User;
import com.cpt202.repository.specification.UserSpecifications;
import com.cpt202.security.UserAuthState;
import com.cpt202.vo.UserVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Long userId);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Long userId);

    @Query("""
            select new com.cpt202.security.UserAuthState(
                u.userId,
                u.role,
                u.accountStatus
            )
            from User u
            where u.userId = :userId
            """)
    Optional<UserAuthState> findAuthStateByUserId(@Param("userId") Long userId);

    default List<User> findUsers(User.UserRole role, String accountStatus) {
        return findAll(UserSpecifications.byRoleAndStatus(role, accountStatus));
    }

    @Query("""
            select new com.cpt202.vo.UserVO(
                u.userId,
                u.username,
                u.email,
                u.fullName,
                u.role,
                u.accountStatus
            )
            from User u
            where (:role is null or u.role = :role)
              and (:accountStatus is null or :accountStatus = '' or u.accountStatus = :accountStatus)
            order by u.userId asc
            """)
    List<UserVO> findUserVos(@Param("role") User.UserRole role, @Param("accountStatus") String accountStatus);
}
