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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import reactor.util.context.ContextView;

// TODO: Move this to reactor?
public class ReactiveMapPropagationContext extends MapPropagationContext {

    public ReactiveMapPropagationContext(ContextView context, List<Restorable> handlers) {
        super(new ConcurrentHashMap<>(context.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))), handlers);
    }

    public ReactiveMapPropagationContext(ContextView context, Restorable... handlers) {
        this(context, Arrays.asList(handlers));
    }

}
