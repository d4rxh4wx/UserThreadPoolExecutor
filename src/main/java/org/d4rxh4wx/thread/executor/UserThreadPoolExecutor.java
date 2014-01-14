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
import java.util.concurrent.Callable;

import com.google.common.util.concurrent.FutureCallback;


/**
 * Managing a ThreadPoolExecutor per User.<br>
 * Each user is given a maximum number of threads from a shared pool. <br>
 * Note: All implementations should not be managed as singleton but as a prototype.
 * Each user must manage its own limitation threads.
 * Note: Implementations of this interface are not necessarily thread-safe.
 * @author d4rxh4wx
 *
 */
public interface UserThreadPoolExecutor {

    /**
     * Submits a job to the thread pool executor if the user quota is not reached.<br>
     * If the quota is reached, the job is queued (non-consumptive blocking waiting for CPU). <br>
     * Once the task is completed, the quota is decremented, allowing another task waiting to be submitted to the ThreadPoolExecutor. <br>
     * Note: This task is under the supervision of ThreadPoolExecutor and respects its policy (put in the pool, waiting in the queue or rejected)
     * @param task the task to delegate
     * @param future the future once the delegated task completion or error
     */
    <T> void submit(Callable<T> task, FutureCallback<T> future);

    /**
     * Submits a job to the thread pool executor if the user quota is not reached.<br>
     * If the quota is reached, the job is queued (non-consumptive blocking waiting for CPU). <br>
     * Once the task is completed, the quota is decremented, allowing another task waiting to be submitted to the ThreadPoolExecutor. <br>
     * Note: This task is under the supervision of ThreadPoolExecutor and respects its policy (put in the pool, waiting in the queue or rejected)
     * @param task the task to delegate
     */
    <T> void submit(Callable<T> task);
    
    /**
     * Waiting blocking (not CPU intensive).<br>
     * Returns when all tasks are being completed.
     */
    void waitForRemainingTasks();
}