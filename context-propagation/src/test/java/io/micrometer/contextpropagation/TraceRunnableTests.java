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

import static org.assertj.core.api.Assertions.assertThat;

class TraceRunnableTests {

    @Test
    void noOuterScopeShouldWork() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        PropagationContext context = new MapPropagationContext(new ConcurrentHashMap<>(), new ThreadLocalScope());
        context.put(String.class, "HELLO");

        try {
            printThreadLocal();
            assertThat(ThreadLocalScope.threadLocal.get()).isNull();

            executorService.submit(new TraceRunnable(() -> context, () -> {
                printThreadLocal();
                assertThat(ThreadLocalScope.threadLocal.get()).isEqualTo("HELLO");
            })).get();

            printThreadLocal();
            assertThat(ThreadLocalScope.threadLocal.get()).isNull();
        }
        finally {
            executorService.shutdown();
        }
    }

    @Test
    void nestedScopeShouldWork() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        PropagationContext context = new MapPropagationContext(new ConcurrentHashMap<>(), new ThreadLocalScope());
        context.put(String.class, "INITIAL_VALUE");

        try (Scope scope = context.makeCurrent()) {
            printThreadLocal();
            assertThat(ThreadLocalScope.threadLocal.get()).isEqualTo("INITIAL_VALUE");
            context.put(String.class, "HELLO");

            executorService.submit(new TraceRunnable(() -> context, () -> {
                printThreadLocal();
                assertThat(ThreadLocalScope.threadLocal.get()).isEqualTo("HELLO");
            })).get();

            printThreadLocal();
            assertThat(ThreadLocalScope.threadLocal.get()).isEqualTo("INITIAL_VALUE");
        }
        finally {
            executorService.shutdown();
        }
    }

    private void printThreadLocal() {
        System.out.println("Thread local <" + ThreadLocalScope.threadLocal.get() + ">");
    }

    static class ThreadLocalScope implements Scope {

        static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

        @Override
        public Scope open(PropagationContext propagationContext) {
            threadLocal.set(propagationContext.get(String.class));
            return this;
        }

        @Override
        public void close() {
            threadLocal.remove();
        }
    }
}
