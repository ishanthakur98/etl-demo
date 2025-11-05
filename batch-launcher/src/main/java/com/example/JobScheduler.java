//package com.example;// com.example.launcher.JobScheduler.java
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.batch.BatchProperties;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.atomic.AtomicLong;
//
//@Component
//public class JobScheduler {
//
//    private final JobLauncher jobLauncher;
//    private final Job importUsersJob;
//    private final Job processDataJob;
//    private final Job generateReportJob;
//
//    private final AtomicLong jobCounter = new AtomicLong();
//
//    /**
//     * Constructor Injection: Spring automatically wires the JobLauncher and
//     * the specific Job beans (defined in other modules) by their unique bean names.
//     */
//    public JobScheduler(
//            JobLauncher jobLauncher,
//            @Qualifier("importUsersJob") Job importUsersJob,
//            @Qualifier("processDataJob") Job processDataJob,
//            @Qualifier("generateReportJob") Job generateReportJob) {
//
//        this.jobLauncher = jobLauncher;
//        this.importUsersJob = importUsersJob;
//        this.processDataJob = processDataJob;
//        this.generateReportJob = generateReportJob;
//    }
//
//    // ... @Autowired fields: jobLauncher, importUsersJob, processDataJob, generateReportJob, jobCounter ...
//
//    // 1. Job 1: Import Users (Runs every 1 minute for TESTING)
//    // Change this to the intended daily schedule later: "0 0 1 * * *" (1:00 AM daily)
//    @Scheduled(cron = "${job.import-users.cron}") // Runs at the start of every minute
//    public void runImportUsersJob() throws Exception {
//        runJob(importUsersJob, "import_id");
//    }
//
//    // 2. Job 2: Process Data (Runs every hour on the hour)
//    @Scheduled(cron = "${job.process-data.cron}")
//    public void runProcessDataJob() throws Exception {
//        runJob(processDataJob, "process_id");
//    }
//
//    // 3. Job 3: Generate Report (Runs every Sunday at 5:00 PM)
//    @Scheduled(cron = "${job.report-gen.cron}")
//    public void runGenerateReportJob() throws Exception {
//        runJob(generateReportJob, "report_id");
//    }
//
//    // Helper method: ensures unique parameters for every run
//    private void runJob(Job job, String jobParameterName) throws Exception {
//        JobParameters params = new JobParametersBuilder()
//            .addLong(jobParameterName, jobCounter.incrementAndGet())
//            .addString("time", String.valueOf(System.currentTimeMillis()))
//            .toJobParameters();
//
//        JobExecution execution = jobLauncher.run(job, params);
//        System.out.println("✅ Launched Job: " + job.getName() +
//                           " | Status: " + execution.getStatus());
//    }
//}