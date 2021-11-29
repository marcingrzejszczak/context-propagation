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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * The propagation context.
 *
 * Influenced by https://github.com/mongodb/mongo-java-driver/blob/r4.4.0/driver-core/src/main/com/mongodb/RequestContext.java
 *
 * @since 1.0.0
 */
public interface PropagationContext {
    /**
     * Resolve a value given a key that exists within the {@link PropagationContext}, or throw
     * a {@link NoSuchElementException} if the key is not present.
     *
     * @param key a lookup key to resolve the value within the context
     * @param <T> an unchecked casted generic for fluent typing convenience
     * @return the value resolved for this key (throws if key not found)
     * @throws NoSuchElementException when the given key is not present
     * @see #getOrDefault(Object, Object)
     * @see #getOrEmpty(Object)
     * @see #hasKey(Object)
     */
    <T> T get(Object key);

    /**
     * Resolve a value given a type key within the {@link PropagationContext}.
     *
     * @param key a type key to resolve the value within the context
     * @param <T> an unchecked casted generic for fluent typing convenience
     * @return the value resolved for this type key (throws if key not found)
     * @throws NoSuchElementException when the given type key is not present
     * @see #getOrDefault(Object, Object)
     * @see #getOrEmpty(Object)
     */
    default <T> T get(Class<T> key) {
        T v = get((Object) key);
        if (key.isInstance(v)) {
            return v;
        }
        throw new NoSuchElementException("Context does not contain a value of type " + key
                .getName());
    }

    /**
     * Resolve a value given a key within the {@link PropagationContext}. If unresolved return the
     * passed default value.
     *
     * @param <T> an unchecked casted generic for fluent typing convenience
     * @param key          a lookup key to resolve the value within the context
     * @param defaultValue a fallback value if key doesn't resolve
     * @return the value resolved for this key, or the given default if not present
     */
    @Nullable
    default <T> T getOrDefault(Object key, @Nullable T defaultValue) {
        if (!hasKey(key)) {
            return defaultValue;
        }
        return get(key);
    }

    /**
     * Resolve a value given a key within the {@link PropagationContext}.
     *
     * @param <T> an unchecked casted generic for fluent typing convenience
     * @param key a lookup key to resolve the value within the context
     * @return an {@link Optional} of the value for that key.
     */
    default <T> Optional<T> getOrEmpty(Object key) {
        if (hasKey(key)) {
            return Optional.of(get(key));
        }
        return Optional.empty();
    }

    /**
     * Return true if a particular key resolves to a value within the {@link PropagationContext}.
     *
     * @param key a lookup key to test for
     * @return true if this context contains the given key
     */
    boolean hasKey(Object key);

    /**
     * Return true if the {@link PropagationContext} is empty.
     *
     * @return true if the {@link PropagationContext} is empty.
     */
    boolean isEmpty();

    /**
     * Modifies this instance with the given key and value. If that key existed in the current {@link PropagationContext}, its associated
     * value is replaced.
     *
     * @param key   the key to add/update
     * @param value the value to associate to the key
     * @throws NullPointerException if either the key or value are null
     * @return this for chaining.
     */
    PropagationContext put(Object key, Object value);

    /**
     * Modifies this instance with the given key and value <strong>only if the value is not {@literal null}</strong>. If that key existed
     * in the current Context, its associated value is replaced in the resulting {@link PropagationContext}.
     *
     * @param key         the key to add/update
     * @param valueOrNull the value to associate to the key, null to ignore the operation
     * @throws NullPointerException if the key is null
     * @return this for chaining.
     */
    default PropagationContext putNonNull(Object key, @Nullable Object valueOrNull) {
        if (valueOrNull != null) {
            return put(key, valueOrNull);
        }
        return this;
    }

    /**
     * Delete the given key and its associated value from the RequestContext.
     *
     * @param key the key to remove.
     * @return this for chaining.
     */
    PropagationContext delete(Object key);

    /**
     * Return the size of this {@link PropagationContext}, the number of key/value pairs stored inside it.
     *
     * @return the size of the {@link PropagationContext}
     */
    int size();

    /**
     * Stream key/value pairs from this {@link PropagationContext}
     *
     * <p>
     * It is not specified whether modification of a {@code Map.Entry} instance in the {@code Stream} results in a modification of the
     * state of the {@code RequestContext}, or whether the {@code Map.Entry} instances are modifiable. That is considered an
     * implementation detail, so users of this method should not rely on the behavior one way or the other unless the implementing class
     * has documented it.
     * </p>
     *
     * @return a {@link Stream} of key/value pairs held by this context
     */
    Stream<Map.Entry<Object, Object>> stream();


    /**
     * Calls restore on all {@link Restorable} elements in the context.
     * @return scope.
     */
    default Restorable.Scope makeCurrent() {
        return new Restorable.CompositeScope(getRestoreHandlers().stream().map(r -> r.makeCurrent(this)).collect(Collectors.toList()));
    }

    /**
     * @return handlers to making a propagation context current
     */
    List<Restorable> getRestoreHandlers();
}
