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

// E.G. THIS CODE IS IN SPRING FRAMEWORK
public class TraceRunnable implements Runnable {

    private final Runnable delegate;

    private final PropagationContext contextSnapshot;

    public TraceRunnable(SynchronousContextProvider contextProvider, Runnable delegate) {
        this.delegate = delegate;
        this.contextSnapshot = contextProvider.getContext();
    }

    // after the constructor but before run a 3 entry was added we will not see it

    @Override
    public void run() {
        // Puts in thread local and returns the context
        try (Scope scope = contextSnapshot.makeCurrent()) {
            System.out.println("Inside of scope");
            this.delegate.run();
        }
        System.out.println("Outside of scope");
    }
}
