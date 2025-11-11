package com.djeno.lab1.camunda;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.*;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CamundaIdentitySyncService {

    private final IdentityService identityService;
    private final AuthorizationService authorizationService;
    public static final String ROLE_USER = "USER";
    public static final String ROLE_DEVELOPER = "DEVELOPER";
    public static final String ROLE_ADMIN = "camunda-admin";

    @PostConstruct
    @Transactional
    public void initCamundaGroups() {
        ensureGroup(ROLE_USER, "Users");
        ensureGroup(ROLE_DEVELOPER, "Developers");
        ensureGroup(ROLE_ADMIN, "Administrators");

        grantAppAccess(ROLE_USER, "tasklist");
        grantAppAccess(ROLE_DEVELOPER, "tasklist");

        grantIfAbsent(AUTH_TYPE_GRANT, null, ROLE_USER,
                PROCESS_DEFINITION, ANY, READ, READ_HISTORY, CREATE_INSTANCE);
        grantIfAbsent(AUTH_TYPE_GRANT, null, ROLE_DEVELOPER,
                PROCESS_DEFINITION, ANY, READ, READ_HISTORY, CREATE_INSTANCE);

        grantIfAbsent(AUTH_TYPE_GRANT, null, ROLE_USER,
                PROCESS_INSTANCE, ANY, CREATE);
        grantIfAbsent(AUTH_TYPE_GRANT, null, ROLE_DEVELOPER,
                PROCESS_INSTANCE, ANY, CREATE);

    }

    @Transactional
    public void createOrUpdateCamundaUser(String username, String email, String password, String role) {
        var cu = identityService.createUserQuery().userId(username).singleResult();
        if (cu == null) cu = identityService.newUser(username);
        cu.setFirstName(username);
        cu.setEmail(email);
        cu.setPassword(password);
        identityService.saveUser(cu);

        if (role.substring(5).equals("ADMIN")) {
            identityService.createMembership(username, Groups.CAMUNDA_ADMIN);
        } else {
            identityService.createMembership(username, role.substring(5));
        }
    }

    private void ensureGroup(String id, String name) {
        if (identityService.createGroupQuery().groupId(id).singleResult() == null) {
            var g = identityService.newGroup(id);
            g.setName(name);
            g.setType("WORKFLOW");
            identityService.saveGroup(g);
        }
    }

    private void grantAppAccess(String groupId, String app) {
        if (authorizationService.createAuthorizationQuery().groupIdIn(groupId).resourceType(Resources.APPLICATION).resourceId(app).count() == 0) {
            Authorization a = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            a.setGroupId(groupId);
            a.setResource(Resources.APPLICATION);
            a.setResourceId(app);
            a.addPermission(Permissions.ACCESS);
            authorizationService.saveAuthorization(a);
        }
    }

    private void grantIfAbsent(int type, String userId, String groupId,
                               Resource res, String resId, Permission... perms) {
        AuthorizationQuery q = authorizationService.createAuthorizationQuery().resourceType(res).resourceId(resId);
        if (userId != null) q = q.userIdIn(userId);
        if (groupId != null) q = q.groupIdIn(groupId);
        if (q.count() == 0) {
            Authorization a = authorizationService.createNewAuthorization(type);
            a.setResource(res);
            a.setResourceId(resId);
            if (userId != null) a.setUserId(userId);
            if (groupId != null) a.setGroupId(groupId);
            for (Permission p : perms) a.addPermission(p);
            authorizationService.saveAuthorization(a);
        }
    }
}
