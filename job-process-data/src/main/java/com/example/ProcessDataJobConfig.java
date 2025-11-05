package com.example;// com.example.jobs.ProcessDataJobConfig.java

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ProcessDataJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // --- Tasklet ---

    // A Tasklet implementation to execute a single task
    public static class SimpleCleanupTasklet implements Tasklet {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            // Business logic for a single task (e.g., calling an external API, database maintenance)
            System.out.println("--- Executing Tasklet: Cleaning up old temporary files... ---");
            // Simulate work done
            System.out.println("--- Cleanup successful. ---");
            
            // Return FINISHED to indicate the tasklet is done and should not be repeated
            return RepeatStatus.FINISHED; 
        }
    }

    @Bean
    public Tasklet simpleCleanupTasklet() {
        return new SimpleCleanupTasklet();
    }

    // --- Step & Job Definition ---

    @Bean
    public Step cleanupStep() {
        return new StepBuilder("cleanupStep", jobRepository)
            .tasklet(simpleCleanupTasklet(), transactionManager) // Use .tasklet() instead of .chunk()
            .build();
    }

    @Bean("processDataJob") // Define the bean name to match the JobScheduler
    public Job processDataJob() {
        return new JobBuilder("processDataJob", jobRepository)
            .start(cleanupStep())
            .build();
    }
}