# spring-boot-backoff-traps

Seven beans that silently switch off a Spring Boot auto-configuration.

Boot creates most infrastructure beans with `@ConditionalOnMissingBean`. The condition checks the
**type** of your beans, not your intent: one bean of the right type and Boot backs off for the whole
application. The compiler is silent, the context starts, the tests are green.

Every trap has three tests: what Boot does by itself, how your bean switches it off, and a safe variant.

| Your bean | What silently stops working | Safe variant | Test |
|---|---|---|---|
| `JwtDecoder` | signature and expiry checks of incoming tokens: a forged token gets **200** | a plain class that does not implement `JwtDecoder` | [JwtDecoderBackoffTest](src/test/java/dev/backofftraps/traps/jwt/JwtDecoderBackoffTest.java) |
| `ObjectMapper` | `spring.jackson.*` and Boot defaults: an unknown JSON field fails parsing | `Jackson2ObjectMapperBuilderCustomizer` | [ObjectMapperBackoffTest](src/test/java/dev/backofftraps/traps/jackson/ObjectMapperBackoffTest.java) |
| `SecurityFilterChain` | the default "authenticate everything" chain: paths outside your chain are **open** | an explicit catch-all chain | [SecurityFilterChainBackoffTest](src/test/java/dev/backofftraps/traps/security/SecurityFilterChainBackoffTest.java) |
| `Executor` (`ThreadPoolTaskScheduler` too) | `applicationTaskExecutor`: `@Async` moves to your scheduler | `spring.task.execution.mode=force` | [ExecutorBackoffTest](src/test/java/dev/backofftraps/traps/executor/ExecutorBackoffTest.java) |
| `CacheManager` | the Redis cache manager: every pod caches on its own | `@Bean(defaultCandidate = false)` | [CacheManagerBackoffTest](src/test/java/dev/backofftraps/traps/cache/CacheManagerBackoffTest.java) |
| `DataSource` | the primary data source and its pool settings | `@Bean(defaultCandidate = false)` | [DataSourceBackoffTest](src/test/java/dev/backofftraps/traps/datasource/DataSourceBackoffTest.java) |
| `ConsumerFactory` | `spring.kafka.*`, SASL included | `DefaultKafkaConsumerFactoryCustomizer` | [ConsumerFactoryBackoffTest](src/test/java/dev/backofftraps/traps/kafka/ConsumerFactoryBackoffTest.java) |

## Takeaways

- **Customize, do not replace.** Boot ships a customizer for most of its beans.
- **`@Bean(defaultCandidate = false)`** (Spring Framework 6.2): your bean exists and is injected by
  name, but `@ConditionalOnMissingBean` ignores it, so Boot keeps its own default.
  Checked in `OnBeanCondition` of Spring Boot 3.5.
- **A helper that "just needs the type" should not implement a framework interface.**
- **Find it before production:** run with `--debug` (or `/actuator/conditions`) and look for your
  auto-configuration under *Negative matches*.
- **Test gotcha:** AssertJ's `assertThat(context).getBean(Type.class)` demands a single bean of the
  type and counts non-default candidates too. Spring's `context.getBean(Type.class)` resolves the
  default candidate, exactly like injection does.

## Run

```
mvn test
```

Java 21, Spring Boot 3.5. No Docker, no broker, no Redis: tests use `ApplicationContextRunner`
and MockMvc and check what is wired. The application scans only `dev.backofftraps.app`,
so traps never switch on by accident.

