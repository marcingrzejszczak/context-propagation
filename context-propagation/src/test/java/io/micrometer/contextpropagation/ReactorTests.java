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

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

class ReactorTests {

    private Scope withScope(ContextView contextView) {
        return contextView.get(ContextAndScope.class).reactiveMapPropagationContext.makeCurrent();
    }

    private void decorate(ContextView contextView, Runnable runnable) {
        try (Scope scope = withScope(contextView)) {
            runnable.run();
        }
    }

    @Test
    void should_work() {

        Mono<String> userCode = Mono.just("HELLO")
                .transformDeferredContextual((stringMono, contextView) -> {

                    // SecurityContextHolder.get();
                    // contextView.get(SecurityContext.class);


                    // If the user wants to modify the context
//                    contextView.get(ContextAndScope.class).scope = contextView.get(ContextAndScope.class)
//                            .reactiveMapPropagationContext.put(String.class, "HACKED").makeCurrent();
                    return stringMono
//                            .doOnNext(s -> Reactor.decorate(contextView, () -> log.info))
                            .doOnNext(s -> {
                                try (Scope scope = withScope(contextView)) {
                                    // SecurityContextHolder.get();
                                    // call rest template
                                        // puts stuff to MDC
                                }
                                //                                    Reactor.decorate(contextView, () -> library.call(s));
                            })
//                            .doOnNext(s -> library.call(s))
                            .doOnNext(s -> printThreadLocal());
                });

        Mono<String> ready = Mono
                .deferContextual(contextView -> userCode.doFinally(signalType -> contextView.get(ContextAndScope.class).scope.close())) // remove stuff from thread local
                .contextWrite(context -> {
                    printThreadLocal();
                    ReactiveMapPropagationContext reactiveMapPropagationContext = new ReactiveMapPropagationContext(context, new ThreadLocalScope());
                    Scope scope = reactiveMapPropagationContext.makeCurrent();
                    return context.put(ContextAndScope.class, new ContextAndScope(reactiveMapPropagationContext, scope));
                });

        String result = ready.block();
        printThreadLocal();
        BDDAssertions.then(result).isEqualTo("HELLO");
    }

    private void printThreadLocal() {
        System.out.println("Thread local <" + ThreadLocalScope.threadLocal.get() + ">");
    }

    // TODO: Add stacking mechanism
    static class ContextAndScope {
        ReactiveMapPropagationContext reactiveMapPropagationContext;
        Scope scope;

        ContextAndScope(ReactiveMapPropagationContext reactiveMapPropagationContext, Scope scope) {
            this.reactiveMapPropagationContext = reactiveMapPropagationContext;
            this.scope = scope;
        }
    }

    static class ThreadLocalScope implements Scope {

        static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

        @Override
        public Scope open(PropagationContext propagationContext) {
            if (!propagationContext.hasKey(String.class)) {
                return this;
            }
            threadLocal.set(propagationContext.get(String.class));
            return this;
        }

        @Override
        public void close() {
            threadLocal.remove();
        }
    }
}
