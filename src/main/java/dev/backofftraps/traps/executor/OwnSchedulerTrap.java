package dev.backofftraps.traps.executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * TRAP. A scheduler for a couple of {@code @Scheduled} jobs.
 * <p>
 * {@link ThreadPoolTaskScheduler} is also an {@link java.util.concurrent.Executor}. Boot creates
 * {@code applicationTaskExecutor} only while no Executor bean exists, so it is skipped:
 * {@code spring.task.execution.*} stops working and {@code @Async} lands on this scheduler.
 * <p>
 * SAFE alternative needs no code: {@code spring.task.execution.mode=force} keeps Boot's executor.
 */
@Configuration(proxyBeanMethods = false)
public class OwnSchedulerTrap {

    public static final String THREAD_PREFIX = "jobs-";

    @Bean
    ThreadPoolTaskScheduler jobsScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix(THREAD_PREFIX);
        return scheduler;
    }
}
