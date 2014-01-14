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

import org.d4rxh4wx.thread.executor.UserThreadPoolExecutor;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * UserThreadPoolTest
 */
public class UserThreadPoolTest {

    @Test
    public void test() {
        System.out.println("Starting");
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("userthreadpoolexecutor-context-test.xml");
        // building a user thread pool executor (one for this thread)
        // reminder: this user thread pool is using a shared thread pool
        UserThreadPoolExecutor userThreadPoolExecutor = ctx.getBean(UserThreadPoolExecutor.class);
        
        // submitting a number of 10 tasks (max per user = 5 (cf userthreadpool-context-test.xml)
        // this thread will block of the 6th task, waiting for a finished task to proceed
        for (int i = 0; i < 10; i++) {
            userThreadPoolExecutor.submit(new SampleTask(i+1));
        }
        
        // waiting (in this thread) for remaining user tasks to finish
        userThreadPoolExecutor.waitForRemainingTasks();
        
        ctx.close();
        System.out.println("End");
        
        

    }
    
    private static class SampleTask implements Callable<Integer> {
        
        private int number;
        
        public SampleTask(final int number) {
            this.number = number;
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        public Integer call() throws Exception {
            // faking a long process of 2 sec
            Thread.sleep(2000);
            return number;
        }
        
    }

}
