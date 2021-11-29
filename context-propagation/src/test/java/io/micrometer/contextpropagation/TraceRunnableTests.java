/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micrometer.contextpropagation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class TraceRunnableTests {

    @Test
    void should_work() {
        TraceRunnable traceRunnable = new TraceRunnable(() -> new MapPropagationContext(new ConcurrentHashMap<>(), new MyThreadLocalRestorable()),
                this::printThreadLocal);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            printThreadLocal();
            executorService.submit(traceRunnable);
            printThreadLocal();
        } finally {
            executorService.shutdown();
        }
    }

    private void printThreadLocal() {
        System.out.println("Thread local <" + MyThreadLocalRestorable.threadLocal.get() + ">");
    }

    static class MyThreadLocalRestorable implements Restorable {

        static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

        @Override
        public Scope makeCurrent(PropagationContext context) {
            threadLocal.set(context.get(String.class));
            return threadLocal::remove;
        }
    }
}
