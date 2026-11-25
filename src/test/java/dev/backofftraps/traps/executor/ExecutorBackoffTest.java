package dev.backofftraps.traps.executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@code ThreadPoolTaskScheduler} is an {@code Executor}, so Boot skips {@code applicationTaskExecutor}.
 */
class ExecutorBackoffTest {

    private static final String BOOT_EXECUTOR = "applicationTaskExecutor";
    private static final String BOOT_THREAD_PREFIX = "task-";

    @Configuration(proxyBeanMethods = false)
    @EnableAsync
    static class AsyncConfig {

        @Bean
        AsyncWorker asyncWorker() {
            return new AsyncWorker();
        }
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
            .withUserConfiguration(AsyncConfig.class);

    @Test
    @DisplayName("Boot's executor: exists, @Async runs on task- threads")
    void bootExecutorRunsAsync() {
        runner.run(context -> {
            assertThat(context).hasBean(BOOT_EXECUTOR);
            assertThat(asyncThread(context.getBean(AsyncWorker.class))).startsWith(BOOT_THREAD_PREFIX);
        });
    }

    @Test
    @DisplayName("TRAP: own scheduler, Boot's executor is skipped, @Async moves to the scheduler")
    void ownSchedulerTakesOverAsync() {
        runner.withUserConfiguration(OwnSchedulerTrap.class).run(context -> {
            assertThat(context).doesNotHaveBean(BOOT_EXECUTOR);
            assertThat(asyncThread(context.getBean(AsyncWorker.class))).startsWith(OwnSchedulerTrap.THREAD_PREFIX);
        });
    }

    @Test
    @DisplayName("SAFE: spring.task.execution.mode=force keeps Boot's executor next to the scheduler")
    void forceModeKeepsBootExecutor() {
        runner.withUserConfiguration(OwnSchedulerTrap.class)
                .withPropertyValues("spring.task.execution.mode=force")
                .run(context -> {
                    assertThat(context).hasBean(BOOT_EXECUTOR);
                    assertThat(asyncThread(context.getBean(AsyncWorker.class))).startsWith(BOOT_THREAD_PREFIX);
                });
    }

    private static String asyncThread(AsyncWorker worker) throws Exception {
        return worker.threadName().get(5, TimeUnit.SECONDS);
    }
}
