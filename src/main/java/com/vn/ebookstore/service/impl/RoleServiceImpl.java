package com.vn.ebookstore.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vn.ebookstore.model.Role;
import com.vn.ebookstore.repository.RoleRepository;
import com.vn.ebookstore.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(int id, Role role) {
        Role existingRole = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        return roleRepository.save(existingRole);
    }

    @Override
    public void deleteRole(int id) {
        roleRepository.deleteById(id);
    }

    @Override
    public Role getRoleById(int id) {
        return roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Override
    public List<Role> getRolesByIds(List<Integer> ids) {
        return roleRepository.findAllById(ids);
    }
}