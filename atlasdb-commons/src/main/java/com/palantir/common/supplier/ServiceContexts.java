/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.common.supplier;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * This class contains utility methods to make it easier to work with classes that consume the {@link ServiceContext}
 * interface.
 *
 * @author carrino
 */
public class ServiceContexts {
    private ServiceContexts() { /* */ }

    public static <T> ServiceContext<T> fromSupplier(final Supplier<? extends T> supplier) {
        return new ServiceContext<T>() {
            @Override
            public T get() {
                return supplier.get();
            }

            @Override
            public <R> R callWithContext(T context, java.util.concurrent.Callable<R> callable) throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> ServiceContext<T> ofInstance(final T t) {
        return fromSupplier(Suppliers.ofInstance(t));
    }


}
