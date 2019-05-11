package pdm.networkservicesmonitor.agent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = "pdm.networkservicesmonitor.agent")
public class AppConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

}