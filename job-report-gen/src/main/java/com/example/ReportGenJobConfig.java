package com.example;// com.example.jobs.ReportGenJobConfig.java

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class ReportGenJobConfig {
    
    // Auto-wired components from Spring's context
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ReportGenJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    // --- Components ---

    @Bean
    public JdbcCursorItemReader<String> reportReader(DataSource dataSource) {
        // Read simple data (e.g., just the emails) from the 'user' table
        return new JdbcCursorItemReaderBuilder<String>()
            .name("reportReader")
            .dataSource(dataSource)
            .sql("SELECT email FROM user_table")
            .rowMapper((rs, rowNum) -> rs.getString(1)) // Map the single column to a String
            .build();
    }

    @Bean
    public ItemProcessor<String, String> reportProcessor() {
        // Simple processor: prefix the email for the report
        return email -> "Report_Email: " + email;
    }

    @Bean
    public FlatFileItemWriter<String> reportWriter() {
        // Write the final report lines to a flat file
        return new FlatFileItemWriterBuilder<String>()
            .name("reportWriter")
            .resource(new FileSystemResource("output/daily_report.txt")) // Output file location
            .lineAggregator(item -> item) // Writes the String item as a line
            .build();
    }

    // --- Step & Job Definition ---
    
    @Bean
    public Step reportStep() {
        return new StepBuilder("reportStep", jobRepository)
            .<String, String>chunk(5, transactionManager)
            .reader(reportReader(null))
            .processor(reportProcessor())
            .writer(reportWriter())
            .build();
    }

    @Bean("generateReportJob") // Define the bean name to match the JobScheduler
    public Job generateReportJob() {
        return new JobBuilder("generateReportJob", jobRepository)
            .start(reportStep())
            .build();
    }
}