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
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A ThreadPoolTaskExecutor decorated by a listener and returning ListenableFuture
 * @author d4rxh4wx
 *
 */
public class ListeningThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Future<?> submit(Runnable task) {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(getThreadPoolExecutor());

        try {
            return executor.submit(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(getThreadPoolExecutor());

        try {
            return executor.submit(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
        }
    }
}
