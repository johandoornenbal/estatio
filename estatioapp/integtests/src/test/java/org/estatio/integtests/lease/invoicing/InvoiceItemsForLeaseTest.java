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
package org.estatio.integtests.lease.invoicing;

import java.util.List;
import javax.inject.Inject;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.Leases;
import org.estatio.dom.lease.invoicing.InvoiceItemForLease;
import org.estatio.dom.lease.invoicing.InvoiceItemsForLease;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.invoice.InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003;
import org.estatio.integtests.EstatioIntegrationTest;

public class InvoiceItemsForLeaseTest extends EstatioIntegrationTest {

    public static class FindByLeaseAndInvoiceStatus extends InvoiceItemsForLeaseTest {

        @Before
        public void setupData() {
            runFixtureScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003());
                }
            });
        }

        @Inject
        private Leases leases;
        @Inject
        private InvoiceItemsForLease invoiceItemsForLease;

        @Test
        public void givenValidLeaseWithNewInvoiceItems() throws Exception {
            // given
            Lease lease = leases.findLeaseByReference(InvoiceForLeaseItemTypeOfRentOneQuarterForOxfPoison003.LEASE);
            // when
            List<InvoiceItemForLease> invoiceItems = invoiceItemsForLease.findByLeaseAndInvoiceStatus(lease, InvoiceStatus.NEW);
            // then
            Assert.assertThat(invoiceItems.size(), Is.is(2));
        }

    }
}