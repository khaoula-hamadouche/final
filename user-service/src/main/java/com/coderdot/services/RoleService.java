package com.coderdot.services;
import org.springframework.transaction.annotation.Transactional;
import com.coderdot.entities.Permission;
import com.coderdot.entities.Role;
import com.coderdot.entities.User;
import com.coderdot.repository.PermissionRepository;
import com.coderdot.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public Role createRoleWithPermissions(Role roleRequest) {
        if (roleRequest.getName() == null || roleRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est obligatoire !");
        }

        if (roleRepository.findByName(roleRequest.getName()).isPresent()) {
            throw new IllegalArgumentException("Ce rôle existe déjà !");
        }

        Role role = new Role();
        role.setName(roleRequest.getName());

        // 🔹 Gestion des permissions
        Set<Permission> permissions = new HashSet<>();
        if (roleRequest.getPermissions() != null) {
            for (Permission p : roleRequest.getPermissions()) {
                Optional<Permission> existingPermission = permissionRepository.findByName(p.getName());
                if (existingPermission.isPresent()) {
                    permissions.add(existingPermission.get());
                } else {
                    Permission newPermission = new Permission(p.getName());
                    permissionRepository.save(newPermission);
                    permissions.add(newPermission);
                }
            }
        }

        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    }

    @Transactional
    public Optional<Role> updateRole(Long id, Role roleRequest) {
        Optional<Role> existingRoleOpt = roleRepository.findById(id);
        if (existingRoleOpt.isEmpty()) {
            return Optional.empty();
        }

        Role existingRole = existingRoleOpt.get();

        // ✅ Mise à jour du nom
        if (roleRequest.getName() != null && !roleRequest.getName().trim().isEmpty()) {
            existingRole.setName(roleRequest.getName());
        }

        // ✅ Mise à jour des permissions : on recharge chaque permission à partir de la BDD
        if (roleRequest.getPermissions() != null) {
            Set<Permission> updatedPermissions = new HashSet<>();
            for (Permission p : roleRequest.getPermissions()) {
                if (p.getId() != null) {
                    permissionRepository.findById(p.getId()).ifPresent(updatedPermissions::add);
                }
            }
            existingRole.setPermissions(updatedPermissions);
        }

        return Optional.of(roleRepository.save(existingRole));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé avec l'ID : " + id));

        // Protection des rôles critiques (facultatif)
        if ("SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalStateException("Le rôle SUPER_ADMIN ne peut pas être supprimé.");
        }


        // Retirer le rôle de tous les utilisateurs associés
        for (User user : role.getUsers()) {  // Assure-toi que Role a une méthode getUsers()
            user.getRoles().remove(role);
        }

        // Détacher les permissions associées (optionnel mais safe)
        role.getPermissions().clear();
        roleRepository.save(role);

        // Supprimer le rôle
        roleRepository.delete(role);
    }
}
