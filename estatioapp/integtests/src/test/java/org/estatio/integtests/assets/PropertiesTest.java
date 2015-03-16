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
package org.estatio.integtests.assets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.asset.Properties;
import org.estatio.dom.asset.Property;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForKal;
import org.estatio.fixture.asset.PropertyForOxf;
import org.estatio.integtests.EstatioIntegrationTest;

public class PropertiesTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());

                executionContext.executeChild(this, new PropertyForOxf());
                executionContext.executeChild(this, new PropertyForKal());
            }
        });
    }

    @Inject
    Properties properties;

    public static class AllProperties extends PropertiesTest {

        @Test
        public void whenReturnsInstance_thenCanTraverseUnits() throws Exception {
            // when
            List<Property> allProperties = properties.allProperties();

            // then
            assertThat(allProperties.size(), is(2));
        }

    }

    public static class FindProperties extends PropertiesTest {

        @Test
        public void withReference() throws Exception {
            final List<Property> props = properties.findProperties("OXF");
            assertNotNull(props);
            assertThat(props.size(), is(1));
        }

        @Test
        public void withName() throws Exception {
            final List<Property> props = properties.findProperties("Oxford Super Mall");
            assertNotNull(props);
            assertThat(props.size(), is(1));
        }

        @Test
        public void withWildcard() throws Exception {
            final List<Property> props = properties.findProperties("Oxford*");
            assertNotNull(props);
            assertThat(props.size(), is(1));
        }

        @Test
        public void withWildcard_returningMultiple() throws Exception {
            final List<Property> props = properties.findProperties("*");
            assertNotNull(props);
            assertThat(props.size(), is(2));
        }
    }

    public static class FindPropertyByReference extends PropertiesTest {

        @Test
        public void withReference() throws Exception {

            // when
            final Property property = properties.findPropertyByReference(PropertyForOxf.PROPERTY_REFERENCE);

            // then
            assertThat(property.getReference(), is(PropertyForOxf.PROPERTY_REFERENCE));
        }
    }

    public static class FindPropertyByReferenceElseNull extends PropertiesTest {

        @Test
        public void withReference() throws Exception {

            // when
            final Property property = properties.findPropertyByReferenceElseNull("FAKEREF");

            // then
            assertNull(property);
        }
    }
}