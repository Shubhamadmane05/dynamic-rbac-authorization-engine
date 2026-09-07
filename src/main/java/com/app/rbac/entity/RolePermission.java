package com.app.rbac.entity;



import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(
        name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RolePermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	  @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "permission_id", nullable = false)
	    private Permission permission;
	
	  @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "role_id", nullable = false)
	    private Role role;
}
