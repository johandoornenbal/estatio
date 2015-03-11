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
package org.estatio.integtests.guarantee;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.estatio.dom.financial.FinancialAccount;
import org.estatio.dom.financial.FinancialAccountType;
import org.estatio.dom.guarantee.Guarantee;
import org.estatio.dom.guarantee.GuaranteeType;
import org.estatio.dom.guarantee.Guarantees;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.Leases;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.guarantee.GuaranteeForOxfTopModel001;
import org.estatio.fixture.lease.LeaseForOxfTopModel001;
import org.estatio.integtests.EstatioIntegrationTest;
import org.estatio.integtests.VT;

public class GuaranteesTest extends EstatioIntegrationTest {

    @Inject
    Leases leases;

    @Inject
    Guarantees guarantees;

    public static class NewGuarantee extends GuaranteesTest {

        private Lease lease;
        private Guarantee guarantee;

        private String reference;
        private String name;
        private GuaranteeType guaranteeType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private BigDecimal maximumAmount;

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());
                    executionContext.executeChild(this, new LeaseForOxfTopModel001());
                }
            }.withTracing());

            lease = leases.findLeaseByReference(LeaseForOxfTopModel001.LEASE_REFERENCE);

            assertThat(lease.getPrimaryParty(), is(not(nullValue())));
            assertThat(lease.getSecondaryParty(), is(not(nullValue())));

            reference = "some reference";
            name = "some name";
            startDate = lease.getStartDate();
            endDate = startDate.plusYears(1);
            description = "some description";
            maximumAmount = VT.bd(12300.12);
        }

        @Test
        public void whenBankGuarantee() throws Exception {

            guaranteeType = GuaranteeType.BANK_GUARANTEE;

            // when
            guarantee = guarantees.newGuarantee(lease, reference, name, guaranteeType, startDate, endDate, description, maximumAmount, null);

            // then
            assertThat(guarantee.getLease(), is(lease));
            assertThat(guarantee.getName(), is(name));
            assertThat(guarantee.getDescription(), is(description));

            assertThat(guarantee.getStartDate(), is(startDate));
            assertThat(guarantee.getEndDate(), is(endDate));
            assertThat(guarantee.getTerminationDate(), is(nullValue()));

            assertThat(guarantee.getPrimaryParty(), is(lease.getPrimaryParty()));
            assertThat(guarantee.getSecondaryParty(), is(lease.getSecondaryParty()));

            assertThat(guarantee.getContractualAmount(), is(maximumAmount));

            FinancialAccount financialAccount = guarantee.getFinancialAccount();
            assertThat(financialAccount, is(not(nullValue())));

            // and then
            assertThat(financialAccount.getType(), is(FinancialAccountType.BANK_GUARANTEE));
            assertThat(financialAccount.getOwner(), is(lease.getSecondaryParty()));
            assertThat(financialAccount.getName(), is(name));
            assertThat(financialAccount.getReference(), is(reference));
            assertThat(financialAccount.getExternalReference(), is(nullValue()));
        }

        @Test
        public void whenDeposit() throws Exception {

            guaranteeType = GuaranteeType.DEPOSIT;

            // when
            guarantee = guarantees.newGuarantee(lease, reference, name, guaranteeType, startDate, endDate, description, maximumAmount, null);

            // then
            FinancialAccount financialAccount = guarantee.getFinancialAccount();
            assertThat(financialAccount, is(not(nullValue())));

            // and then
            assertThat(financialAccount.getType(), is(FinancialAccountType.GUARANTEE_DEPOSIT));
        }

        @Test
        public void whenCompanyGuarantee() throws Exception {

            guaranteeType = GuaranteeType.COMPANY_GUARANTEE;

            // when
            guarantee = guarantees.newGuarantee(lease, reference, name, guaranteeType, startDate, endDate, description, maximumAmount, null);

            // then
            FinancialAccount financialAccount = guarantee.getFinancialAccount();
            assertThat(financialAccount, is(nullValue()));
        }

        @Test
        public void whenNone() throws Exception {

            guaranteeType = GuaranteeType.NONE;

            // when
            guarantee = guarantees.newGuarantee(lease, reference, name, guaranteeType, startDate, endDate, description, maximumAmount, null);

            // then
            FinancialAccount financialAccount = guarantee.getFinancialAccount();
            assertThat(financialAccount, is(nullValue()));
        }

        @Test
        public void whenUnknown() throws Exception {

            guaranteeType = GuaranteeType.UNKNOWN;

            // when
            guarantee = guarantees.newGuarantee(lease, reference, name, guaranteeType, startDate, endDate, description, maximumAmount, null);

            // then
            FinancialAccount financialAccount = guarantee.getFinancialAccount();
            assertThat(financialAccount, is(nullValue()));
        }
    }

    public static class FindGuarantee extends GuaranteesTest {

        private Lease lease;

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());
                    executionContext.executeChild(this, new LeaseForOxfTopModel001());
                    executionContext.executeChild(this, new GuaranteeForOxfTopModel001());
                }
            }.withTracing());

            lease = leases.findLeaseByReference(LeaseForOxfTopModel001.LEASE_REFERENCE);
        }

        @Test
        public void happyCase1() throws Exception {
            // when
            Guarantee guarantee = guarantees.findByReference(LeaseForOxfTopModel001.LEASE_REFERENCE + "-D");

            // then
            assertThat(guarantee.getReference(), is(LeaseForOxfTopModel001.LEASE_REFERENCE + "-D"));
        }

        @Test
        public void happyCase2() throws Exception {
            // when
            Guarantee guarantee = guarantees.findByReference(LeaseForOxfTopModel001.LEASE_REFERENCE + "-E");

            // then
            assertThat(guarantee.getReference(), is(LeaseForOxfTopModel001.LEASE_REFERENCE + "-E"));
        }
    }
}