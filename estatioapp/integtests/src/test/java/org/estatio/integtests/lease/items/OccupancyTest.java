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
package org.estatio.integtests.lease.items;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.Leases;
import org.estatio.dom.lease.Occupancy;
import org.estatio.dom.lease.tags.Brand;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.lease.LeaseForOxfTopModel001;
import org.estatio.integtests.EstatioIntegrationTest;

public class OccupancyTest extends EstatioIntegrationTest {

    public static class GetBrand extends OccupancyTest {

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new LeaseForOxfTopModel001());
                }
            });
        }

        @Inject
        private Leases leases;

        private Lease leaseTopModel;
        private Occupancy occupancy;

        @Before
        public void setup() {
            leaseTopModel = leases.findLeaseByReference(LeaseForOxfTopModel001.LEASE_REFERENCE);
            occupancy = leaseTopModel.getOccupancies().first();
        }

        @Test
        public void whenNotNull() throws Exception {

            // TODO: this seems to be merely asserting on the contents of the
            // fixture
            final Brand brand = occupancy.getBrand();
            assertThat(brand, is(not(nullValue())));
            assertThat(brand.getName(), is("Topmodel"));
        }

    }

    public static class Verify extends OccupancyTest {

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new LeaseForOxfTopModel001());
                }
            });
        }

        @Inject
        private Leases leases;

        private Lease leaseTopModel;
        private Occupancy occupancy;

        @Before
        public void setup() {
            leaseTopModel = leases.findLeaseByReference(LeaseForOxfTopModel001.LEASE_REFERENCE);
            occupancy = leaseTopModel.getOccupancies().first();
        }

        @Test
        public void verifyOnNewStartDate() throws Exception {
            // given
            assertThat(occupancy.getStartDate(), is(leaseTopModel.getTenancyStartDate()));

            // when
            LocalDate newTenancyStartDate = new LocalDate(2012, 7, 15);
            wrap(leaseTopModel).changeTenancyDates(newTenancyStartDate, leaseTopModel.getTenancyEndDate());

            // then
            assertThat(occupancy.getStartDate(), is(leaseTopModel.getTenancyStartDate()));
        }

        @Test
        public void verifyOnNewEndDate() throws Exception {
            // given
            assertThat(occupancy.getEndDate(), is(leaseTopModel.getTenancyEndDate()));

            // when
            LocalDate newTenancyEndDate = new LocalDate(2016, 7, 14);
            wrap(leaseTopModel).changeTenancyDates(leaseTopModel.getTenancyStartDate(), newTenancyEndDate);

            // then
            assertThat(occupancy.getEndDate(), is(leaseTopModel.getTenancyEndDate()));
        }

        @Test
        public void verifyOnNewStartAndEndDate() throws Exception {
            // given
            assertThat(occupancy.getStartDate(), is(leaseTopModel.getTenancyStartDate()));
            assertThat(occupancy.getEndDate(), is(leaseTopModel.getTenancyEndDate()));

            // when
            LocalDate newTenancyStartDate = new LocalDate(2012, 7, 15);
            LocalDate newTenancyEndDate = newTenancyStartDate.plusYears(1);
            wrap(leaseTopModel).changeTenancyDates(newTenancyStartDate, newTenancyEndDate);

            // then
            assertThat(occupancy.getStartDate(), is(leaseTopModel.getTenancyStartDate()));
            assertThat(occupancy.getEndDate(), is(leaseTopModel.getTenancyEndDate()));

        }

    }

    public static class EndCurrentOccupancy extends OccupancyTest {

        @Inject
        private Leases leases;

        private Lease leaseTopModel;
        private Occupancy occupancy;

        @Before
        public void setup() {
            leaseTopModel = leases.findLeaseByReference(LeaseForOxfTopModel001.LEASE_REFERENCE);
            occupancy = leaseTopModel.getOccupancies().first();
        }

        @Ignore
        @Test
        public void endCurrentAndStartNewOccupancy() throws Exception {
            // given
            assertNull(occupancy.getEndDate());
        }
    }

}