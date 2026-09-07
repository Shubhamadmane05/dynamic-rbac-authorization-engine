package com.app.rbac.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.rbac.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>{

	List<UserRole> findByUserId(Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
	
	
	@Query("""
            SELECT DISTINCT p.name
            FROM UserRole urm
              JOIN urm.role r
              JOIN RolePermission rp ON rp.role.id = r.id
              JOIN rp.permission p
            WHERE urm.user.username = :username
            """)
    Set<String> findPermissionNamesByUsername(@Param("username") String username);
	
	   @Query("""
	            SELECT DISTINCT r.name
	            FROM UserRole urm
	              JOIN urm.role r
	            WHERE urm.user.username = :username
	            """)
	    Set<String> findRoleNamesByUsername(@Param("username") String username);
}
