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

import javax.annotation.concurrent.ThreadSafe;

/**
 * A marker interface for providers of {@code RequestContext}.  Sub-interfaces in higher-level modules define methods that actually
 * return instances of {@code RequestContext}, depending on whether the client is synchronous or reactive.
 *
 * Taken from https://github.com/mongodb/mongo-java-driver/blob/r4.4.0/driver-core/src/main/com/mongodb/ContextProvider.java
 *
 * @see PropagationContext
 * @since 1.0.0
 */
@ThreadSafe
public interface ContextProvider {
}
