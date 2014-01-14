UserThreadPoolExecutor
======================

Description
-----------
Each UserThreadPoolExecutor is given a maximum number of threads from a shared ThreadPoolExecutor

Each UserThreadPoolExecutor can:
- submit a task to the shared thread pool executor if its quota is not reached.
If its quota is reached, the job is queued (non-consumptive blocking waiting for CPU)
Once one of its submitted task is completed, the quota is decremented, allowing another task waiting to be submitted to the ThreadPoolExecutor
- wait for the remaining tasks to complete


Example
-------
See: UserThreadPoolTest.java:

// submitting a number of 10 tasks (max per user = 5)
// this thread will block of the 6th task, waiting for a finished task to proceed
for (int i = 0; i < 10; i++) {
    userThreadPoolExecutor.submit(new SampleTask(i+1));
}

// waiting (in this thread) for remaining user tasks to finish
userThreadPoolExecutor.waitForRemainingTasks();


Run a test
----------
mvn clean test