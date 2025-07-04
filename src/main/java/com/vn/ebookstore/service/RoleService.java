package com.vn.ebookstore.service;

import java.util.List;

import com.vn.ebookstore.model.Role;

public interface RoleService {
    Role createRole(Role role);
    Role updateRole(int id, Role role);
    void deleteRole(int id);
    Role getRoleById(int id);
    List<Role> getAllRoles();
    Role getRoleByName(String name);
    List<Role> getRolesByIds(List<Integer> ids);
}