package com.djeno.lab1.jobs;
import com.djeno.lab1.services.AppService;
import com.djeno.lab1.services.PaymentMethodService;
import com.djeno.lab1.services.UserCleanupService;
import com.djeno.lab1.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class DeleteInactiveUserJob implements Job {
    private final int DAYS_TO_KEEP = 1;
    private final UserCleanupService userCleanupService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        int deleted = userCleanupService.deleteInactiveUsers(DAYS_TO_KEEP);
        log.info("DeleteInactiveUsersJob: removed {} users", deleted);
    }
}
