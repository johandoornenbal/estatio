/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.fixture;

/**
 * Required for jacoco-site. 
 *
 */

import java.util.Random;
import com.github.javafaker.service.RandomService;

public class Faker2 {

    private final RandomService randomService;
    private final Values values;

    public Faker2() {
        this(null);
    }
    public Faker2(final Random random) {
        this.randomService = new RandomService(random);
        this.values = new Values(randomService);
    }

    public Values values() { return values; }

    public static class Values {

        private final RandomService randomService;

        public Values(final RandomService randomService) {
            this.randomService = randomService;
        }

        public <E extends Enum<E>> E anEnum(final Class<E> enumType) {
            final E[] enumConstants = enumType.getEnumConstants();
            return enumConstants[anInt(enumConstants.length)];
        }

        public int anInt(final int upTo) {
            return randomService.nextInt(upTo);
        }

        public int aDouble(final int upTo) {
            return randomService.nextInt(upTo);
        }

    }


}
