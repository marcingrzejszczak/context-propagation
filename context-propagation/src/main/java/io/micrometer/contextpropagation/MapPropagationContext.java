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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MapPropagationContext implements PropagationContext {

    private final Map<Object, Object> map;

    private final List<Restorable> handlers;

    public MapPropagationContext(Map<Object, Object> map, List<Restorable> handlers) {
        this.map = map;
        this.handlers = handlers;
    }

    public MapPropagationContext(Map<Object, Object> map, Restorable... handlers) {
        this.map = map;
        this.handlers = Arrays.asList(handlers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) map.get(key);
    }

    @Override
    public boolean hasKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public PropagationContext put(Object key, Object value) {
        map.put(key, value);
        return this;
    }

    @Override
    public PropagationContext delete(Object key) {
        map.remove(key);
        return this;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Stream<Map.Entry<Object, Object>> stream() {
        return map.entrySet().stream();
    }

    @Override
    public List<Restorable> getRestoreHandlers() {
        return this.handlers;
    }

}
