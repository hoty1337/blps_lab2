package com.djeno.lab1.camunda;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CamundaIdentitySyncService {

    private final org.camunda.bpm.engine.IdentityService identityService;

    @Transactional
    public void createOrUpdateCamundaUser(String username, String email, String password, String role) {
        var cu = identityService.createUserQuery().userId(username).singleResult();
        if (cu == null) cu = identityService.newUser(username);
        cu.setFirstName(username);
        cu.setEmail(email);
        cu.setPassword(password);
        identityService.saveUser(cu);

        // membership
        ensureGroup("USER", "Users");
        ensureGroup("DEVELOPER", "Developers");
        ensureGroup("ADMIN", "Administrators");
        identityService.createMembership(username, role.substring(5));
    }

    private void ensureGroup(String id, String name) {
        if (identityService.createGroupQuery().groupId(id).singleResult() == null) {
            var g = identityService.newGroup(id);
            g.setName(name);
            g.setType("SYSTEM");
            identityService.saveGroup(g);
        }
    }
}
