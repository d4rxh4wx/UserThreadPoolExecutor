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

import org.d4rxh4wx.thread.executor.CustomUserThreadPoolExecutor.LogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUserThreadPoolLogListener implements LogListener {
    
    private static final String THREAD_ID = "Thread {} - ";
       
    private static final String BLOCKING_QUEUE_SIZE = "Blocking Queue: Size {}";
    
    private static final String BEFORE_PUTTING_TOKEN = THREAD_ID + "Before Put Token - " + BLOCKING_QUEUE_SIZE;
    private static final String AFTER_PUTTING_TOKEN = THREAD_ID + "After Put Token  - " + BLOCKING_QUEUE_SIZE + " - Waiting Time {} ms";
    private static final String BEFORE_SUBMIT = THREAD_ID + "Before Submit To ThreadPool - Current Pool Size: {} - Active threads: {}";
   
    private static final String ON_SUCCESS_BEFORE_POLLING_TOKEN = THREAD_ID + "On Success Before Polling Token " + BLOCKING_QUEUE_SIZE;
    private static final String ON_SUCCESS_AFTER_POLLING_TOKEN = THREAD_ID + "On Success After Polling Token  " + BLOCKING_QUEUE_SIZE;
    private static final String ON_FAILURE_BEFORE_POLLING_TOKEN = THREAD_ID + "On Failure Before Polling Token " + BLOCKING_QUEUE_SIZE;
    private static final String ON_FAILURE_AFTER_POLLING_TOKEN = THREAD_ID + "On Failure After Polling Token  " + BLOCKING_QUEUE_SIZE;
    
    private static final String WAITING_FOR_REMAINING_TASKS = THREAD_ID + "{} tasks submitted. {} tasks terminated. Waiting for {} remaining tasks...";
    private static final String STILL_WAITING_FOR_REMAINING_TASKS = THREAD_ID + "Still waiting for {} remaining tasks...";
    private static final String TASK_RECEIVED_WHILE_WAITING_FOR_REMAINING_TASKS =  THREAD_ID + "Task received while waiting for remaining task";
    private static final String NO_MORE_REMAINING_TASKS = THREAD_ID + "No more tasks. Ending";

    private final Logger logger = LoggerFactory.getLogger(DefaultUserThreadPoolLogListener.class);

    public void beforePuttingToken(int bqSize) {
        if (logger.isDebugEnabled()) {
            logger.debug(BEFORE_PUTTING_TOKEN, Thread.currentThread().getId(), bqSize);
        }        
    }

    
    public void afterPuttingToken(int bqSize, long startTimeInMilliseconds) {
       if (logger.isDebugEnabled()) {
           logger.debug(AFTER_PUTTING_TOKEN, 
                   new Object[] {Thread.currentThread().getId(), bqSize, System.currentTimeMillis() - startTimeInMilliseconds});
       }
        
    }

    
    public void beforeSubmit(int poolSize, int activeCount) {
        if (logger.isDebugEnabled()) {
            logger.debug(BEFORE_SUBMIT, new Object[] {Thread.currentThread().getId(), poolSize, activeCount});
        }        
    }

    
    public void onSuccessBeforePollingToken(int bqSize) {
        if (logger.isDebugEnabled()) {
            logger.debug(ON_SUCCESS_BEFORE_POLLING_TOKEN, Thread.currentThread().getId(), bqSize);
        }        
    }

    
    public void onSuccessAfterPollingToken(int bqSize) {
        if (logger.isDebugEnabled()) {
            logger.debug(ON_SUCCESS_AFTER_POLLING_TOKEN, Thread.currentThread().getId(), bqSize);
        }        
    }

    
    public void onFailureBeforePollingToken(int bqSize) {
        if (logger.isDebugEnabled()) {
            logger.debug(ON_FAILURE_BEFORE_POLLING_TOKEN, Thread.currentThread().getId(), bqSize);
        }        
    }

    
    public void onFailureAfterPollingToken(int bqSize) {
        if (logger.isDebugEnabled()) {
            logger.debug(ON_FAILURE_AFTER_POLLING_TOKEN, Thread.currentThread().getId(), bqSize);
        }
    }

    
    public void waitForRemainingTasks(int submitted, int finished) {
        if (logger.isDebugEnabled()) {
            logger.debug(WAITING_FOR_REMAINING_TASKS, 
                    new Object[] {Thread.currentThread().getId(), submitted, finished, submitted - finished});
        }        
    }

    
    public void stillWaitForRemainingTasks(int remaining) {
        if (logger.isDebugEnabled()) {
            logger.debug(STILL_WAITING_FOR_REMAINING_TASKS, Thread.currentThread().getId(), remaining);
        } 
    }

    
    public void taskReceivedWhileWaitingForRemainingTasks() {
        if (logger.isDebugEnabled()) {
            logger.debug(TASK_RECEIVED_WHILE_WAITING_FOR_REMAINING_TASKS, Thread.currentThread().getId());
        }        
    }

    
    public void noMoreRemainingTasks() {
        if (logger.isDebugEnabled()) {
            logger.debug(NO_MORE_REMAINING_TASKS, Thread.currentThread().getId());
        } 
    }
}

