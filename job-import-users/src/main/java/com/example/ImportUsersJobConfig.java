package com.example;// com.example.jobs.ImportUsersJobConfig.java

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ImportUsersJobConfig {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();
    private static final Logger log = LoggerFactory.getLogger(ImportUsersJobConfig.class);

    // Auto-wired components from Spring's context
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ImportUsersJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    // --- Components ---

    @Bean
    public FlatFileItemReader<User> userReader() {
        return new FlatFileItemReaderBuilder<User>()
            .name("userReader")
            .resource(new ClassPathResource("input/users.csv")) // Assumes input/users.csv exists
            .delimited().names("firstName", "lastName", "email")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(User.class);
            }})
            .build();
    }

    @Bean
    public ItemProcessor<User, User> userProcessor() {
        // Simple processor: convert names to uppercase before writing
        log.info("processing user");
        return user -> {
            user.setFirstName(user.getFirstName().toUpperCase());
            user.setBatchNumber(ATOMIC_INTEGER.getAndIncrement());
            return user;
        };
    }

    @Bean
    public JdbcBatchItemWriter<User> userWriter(DataSource dataSource) {
        // Writes the processed User object to a database table named 'user'
        return new JdbcBatchItemWriterBuilder<User>()
            .dataSource(dataSource)
            .sql("INSERT INTO user_table (first_name, last_name, email , batch_number) VALUES (:firstName, :lastName, :email, :batchNumber)")
            .beanMapped()
            .build();
    }

    // --- Step & Job Definition ---

    @Bean
    public Step importStep() {
        return new StepBuilder("importStep", jobRepository)
            .<User, User>chunk(10, transactionManager) // Chunk size of 10
            .reader(userReader())
            .processor(userProcessor())
            .writer(userWriter(null)) // Data Source will be auto-wired by Spring
            .build();
    }

    @Bean("importUsersJob") // Define the bean name to match the JobScheduler
    public Job importUsersJob() {
        return new JobBuilder("importUsersJob", jobRepository)
            .start(importStep())
            .build();
    }
}