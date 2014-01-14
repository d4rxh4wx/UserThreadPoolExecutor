/*
 * Copyright (c) 2014 d4rxh4wx
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.d4rxh4wx.thread.executor;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


/**
 * Managing a ThreadPoolExecutor per User.<br>
 * Each user is given a maximum number of threads from a shared pool. <br>
 * Note: All implementations should not be managed as singleton but as a prototype.
 * Each user must manage its own limitation threads.
 * Note: Implementations of this interface are not necessarily thread-safe.
 * @author d4rxh4wx
 *
 */
public class CustomUserThreadPoolExecutor implements UserThreadPoolExecutor {

    
    // Default number of tokens allowed for a user
    private static final int DEFAULT_USER_THREAD_POOL_MAX_SIZE = 5;
    
    // Number of tokens allowed for a user
    private int userThreadPoolMaxSize;

    // Object used as token
    private static Object TOKEN = new Object();
    
    // Each user manages its own fixed-sized blockingQueue
    // Each user has a maximum number of tokens
    private BlockingQueue<Object> bq = null;
    
    // ThreadPoolExecutor shared between users
    private ListeningThreadPoolTaskExecutor service;

    // log listener
    private LogListener logListener;
    
    // log listener activation
    private boolean logListenerEnabled = false;
    
    // number of tasks submitted to the shared threadpool
    private AtomicInteger nbSubmittedTasks = new AtomicInteger(0);
    
    // list of user tokens (success or error) returned by the thread pool;
    private BlockingQueue<Object> doneTasks = new LinkedBlockingQueue<Object>();
        
    private static final String EXCEPTION_WHILE_WAITING_FOR_REMAINING_TASKS = "Exception while waiting for remaining tasks";
    
    
    /**
     * Constructor
     * @param userThreadPoolMaxSize maximal number of tokens allowed for user (ie for this implementation)
     * @param service a thread pool executor
     */
    public CustomUserThreadPoolExecutor(int userThreadPoolMaxSize, ListeningThreadPoolTaskExecutor service) {
        this.service = service;
        this.userThreadPoolMaxSize = userThreadPoolMaxSize > 0 ? userThreadPoolMaxSize : DEFAULT_USER_THREAD_POOL_MAX_SIZE;
        this.bq = new LinkedBlockingQueue<Object>(this.userThreadPoolMaxSize);
    }
    
    /* (non-Javadoc)
     * @see org.d4rxh4wx.thread.UserThreadPoolExecutor
     * #submit(java.util.concurrent.Callable, com.google.common.util.concurrent.FutureCallback)
     */
    public <T> void submit(final Callable<T> task, final FutureCallback<T> future) {
        // Increment submitted tasks
        nbSubmittedTasks.incrementAndGet();
        
        // Verifing quota is reached (blocking waiting, non-consumptive for CPU, thanks to use of BlockingQueue.put())
        try {
            
            logBeforePuttingToken();
                        
            long start = System.currentTimeMillis();
          
            bq.put(TOKEN);
            
            logAfterPuttingToken(start);           
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Error while trying to put in blockingqueue", e);
        }
        
        logBeforeSubmitToSharedThreadPool();
        
        // Submit task to shared ThreadPoolExecutor
        // This is a ListeningThreadPoolTaskExecutor: it returns a ListenableFuture
        ListenableFuture<T> futureTask = (ListenableFuture<T>) service.submit(task);
        
        
        // Adding a callback to this future
        Futures.addCallback(futureTask, new FutureCallback<T>() {
            
            public void onSuccess(T task) {
                
                logSuccessBeforePolling();
                
                // releasing token and add to completed tasks
                bq.poll();
                doneTasks.add(TOKEN);
                
                logSuccessAfterPolling();
                
                
                if (future != null) {
                    future.onSuccess(task);
                }
            }
                        

            public void onFailure(Throwable thrown) {
                
                logFailureBeforePolling();
                
             // releasing token and add to completed tasks
                bq.poll();
                doneTasks.add(TOKEN);
                
                logFailureAfterPolling();
                
                if (future != null) {
                    future.onFailure(thrown);
                }
            }
        });
    }
    
    
    /* (non-Javadoc)
     * @see org.d4rxh4wx.thread.UserThreadPoolExecutor
     * #submit(java.util.concurrent.Callable)
     */
    public <T> void submit(Callable<T> task) {
        submit(task, null);        
    }
    
    
    /* (non-Javadoc)
     * @see org.d4rxh4wx.thread.UserThreadPoolExecutor
     * #waitForRemainingTasks()
     */
    public void waitForRemainingTasks() {
        int terminatedTasks = doneTasks.size(); // NB: calculation done only once (reminder: multithread + lock)
        int submittedTasks = nbSubmittedTasks.get();
        int remainingTasks = submittedTasks - terminatedTasks;
        
        logWaitingForRemainingTasks(submittedTasks, terminatedTasks);
        
        // if remaining tasks
        if(remainingTasks > 0) {
            // We empty the number of previously completed tasks (note: this may have changed since the calculation of the size of completed tasks)
            doneTasks.drainTo(new LinkedList<Object>(), terminatedTasks);
            // We wait while we retrieve the last missing
            do {
                try {
                    logStillWaitingForRemainingTasks(remainingTasks);
                    doneTasks.take(); // blocking waiting
                    remainingTasks--; // one less waiting
                    logTaskReceivedWhileWaitingForRemainingTasks();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(EXCEPTION_WHILE_WAITING_FOR_REMAINING_TASKS, e);
                }
            } while (remainingTasks > 0);
            
        }
        
        logNoMoreRemainingTasks();
        
    }
    
    /************** logger methods ************/
    
    private void logBeforePuttingToken() {
        if (logListenerEnabled && logListener != null) {
            logListener.beforePuttingToken(bq.size());
        }        
    }
    
    private void logAfterPuttingToken(long startTimeInMilliseconds) {        
        if (logListenerEnabled && logListener != null) {
            logListener.afterPuttingToken(bq.size(), startTimeInMilliseconds);
        }
    }
    
    private void logBeforeSubmitToSharedThreadPool() {
        if (logListenerEnabled && logListener != null) {
            logListener.beforeSubmit(service.getPoolSize(), service.getActiveCount());
        }
        
    }
    
    private void logSuccessBeforePolling() {
        if (logListenerEnabled && logListener != null) {
            logListener.onSuccessBeforePollingToken(bq.size());
        }        
    }
    
    private void logSuccessAfterPolling() {
        if (logListenerEnabled && logListener != null) {
            logListener.onSuccessAfterPollingToken(bq.size());
        }
    }
    
    private void logFailureBeforePolling() {
        if (logListenerEnabled && logListener != null) {
            logListener.onFailureBeforePollingToken(bq.size());
        }        
    }
    
    private void logFailureAfterPolling() {
        if (logListenerEnabled && logListener != null) {
            logListener.onFailureAfterPollingToken(bq.size());
        }
    }
    
    private void logWaitingForRemainingTasks(int submitted, int finished) {
        if (logListenerEnabled && logListener != null) {
            logListener.waitForRemainingTasks(submitted, finished);
        }
    }
    
    private void logStillWaitingForRemainingTasks(int remaining) {
        if (logListenerEnabled && logListener != null) {
            logListener.stillWaitForRemainingTasks(remaining);
        }
    }
    
    private void logTaskReceivedWhileWaitingForRemainingTasks() {
        if (logListenerEnabled && logListener != null) {
            logListener.taskReceivedWhileWaitingForRemainingTasks();
        }
    }
    
    private void logNoMoreRemainingTasks() {
        if (logListenerEnabled && logListener != null) {
            logListener.noMoreRemainingTasks();
        }
    }

    /**
     * Setter log listener
     * @param logListener log listener
     */
    public void setLogListener(LogListener logListener) {
        this.logListener = logListener;
    }


    public interface LogListener {
        void beforePuttingToken(int bqSize);
        void afterPuttingToken(int bqSize, long startTimeInMilliseconds);
        void beforeSubmit(int poolSize, int activeCount);
        void onSuccessBeforePollingToken(int bqSize);
        void onSuccessAfterPollingToken(int bqSize);
        void onFailureBeforePollingToken(int bqSize);
        void onFailureAfterPollingToken(int bqSize);
        void waitForRemainingTasks(int submitted, int finished);
        void stillWaitForRemainingTasks(int remaining);
        void taskReceivedWhileWaitingForRemainingTasks();
        void noMoreRemainingTasks();
    }


    
    public boolean isLogListenerEnabled() {
        return logListenerEnabled;
    }

    
    public void setLogListenerEnabled(boolean logListenerEnabled) {
        this.logListenerEnabled = logListenerEnabled;
    }
    
    

}
