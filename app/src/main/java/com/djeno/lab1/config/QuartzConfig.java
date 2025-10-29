package com.djeno.lab1.config;

import com.djeno.lab1.jobs.DeleteInactiveUserJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfig {
    private static final String QUARTZ_JOB_NAME = "deleteInactiveUsersJob";
    private static final String TRIGGER_NAME = "deleteInactiveUsersTrigger";

    @Bean
    public JobDetail deleteInactiveUsersJobDetail() {
        return JobBuilder.newJob(DeleteInactiveUserJob.class)
                .withIdentity(QUARTZ_JOB_NAME)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger deleteInactiveUsersTrigger(JobDetail deleteInactiveUsersJobDetail) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                .cronSchedule("0 0 0 * * ?")
                .inTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

        return TriggerBuilder.newTrigger()
                .forJob(deleteInactiveUsersJobDetail)
                .withIdentity(TRIGGER_NAME)
                .withSchedule(cronScheduleBuilder)
                .build();
    }
}
