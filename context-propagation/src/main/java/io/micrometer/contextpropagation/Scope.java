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

import java.util.List;

/**
 * A contract for objects that can be scoped (e.g. in a new thread).
 *
 * @see PropagationContext
 * @since 1.0.0
 */
public interface Scope extends AutoCloseable {
    /**
     * Opens the scope and makes the propagation context current.
     *
     * @param propagationContext propagation context to make current
     * @return itself
     */
    Scope open(PropagationContext propagationContext);

    @Override
    void close();

    /**
     * Scope that contains a list of scopes.
     */
    class CompositeScope implements Scope {
        private final List<Scope> scopes;

        public CompositeScope(List<Scope> scopes) {
            this.scopes = scopes;
        }

        @Override
        public Scope open(PropagationContext propagationContext) {
            this.scopes.forEach(scope -> scope.open(propagationContext));
            return this;
        }

        @Override
        public void close() {
            this.scopes.forEach(Scope::close);
        }
    }
}
