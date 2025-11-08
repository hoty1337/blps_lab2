package com.djeno.lab1.bpmn;

import com.djeno.lab1.services.UserCleanupService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("cleanupInactiveUsersDelegate")
@RequiredArgsConstructor
public class CleanupInactiveUsersDelegate implements JavaDelegate {

    private final UserCleanupService userCleanupService;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {
        int DAYS_TO_KEEP = 1;
        int deleted = userCleanupService.deleteInactiveUsers(DAYS_TO_KEEP);
        execution.setVariable("deletedUsers", deleted);
    }
}
