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
package org.estatio.dom.bankmandate;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.agreement.AgreementRoleCommunicationChannelTypes;
import org.estatio.dom.agreement.AgreementRoleType;
import org.estatio.dom.agreement.AgreementRoleTypes;
import org.estatio.dom.agreement.AgreementType;
import org.estatio.dom.agreement.AgreementTypes;
import org.estatio.dom.financial.bankaccount.BankAccount;
import org.estatio.dom.party.Party;

@DomainService(repositoryFor = BankMandate.class)
@DomainServiceLayout(
        named = "Accounts",
        menuBar = DomainServiceLayout.MenuBar.PRIMARY,
        menuOrder = "30.2")
public class BankMandates extends EstatioDomainService<BankMandate> {

    public BankMandates() {
        super(BankMandates.class, BankMandate.class);
    }

    // //////////////////////////////////////

    /**
     * for migration API only
     */
    @Programmatic
    public BankMandate newBankMandate(
            // CHECKSTYLE:OFF ParameterNumber - Wicket viewer does not support
            // aggregate value types
            @Parameter(regexPattern = RegexValidation.REFERENCE) final String reference,
            final String name,
            final LocalDate startDate,
            final LocalDate endDate,
            final Party debtor,
            final Party creditor,
            final BankAccount bankAccount
            // CHECKSTYLE:ON
            ) {
        BankMandate mandate = newTransientInstance();
        mandate.setType(agreementTypes.find(BankMandateConstants.AT_MANDATE));
        mandate.setReference(reference);
        mandate.setName(name);
        mandate.setStartDate(startDate);
        mandate.setEndDate(endDate);
        mandate.setBankAccount(bankAccount);
        persistIfNotAlready(mandate);

        final AgreementRoleType artCreditor = agreementRoleTypes.findByTitle(BankMandateConstants.ART_CREDITOR);
        mandate.newRole(artCreditor, creditor, null, null);
        final AgreementRoleType artDebtor = agreementRoleTypes.findByTitle(BankMandateConstants.ART_DEBTOR);
        mandate.newRole(artDebtor, debtor, null, null);
        return mandate;
    }

    // //////////////////////////////////////

    @Action(semantics = SemanticsOf.SAFE, restrictTo = RestrictTo.PROTOTYPING)
    @MemberOrder(name = "Accounts", sequence = "99")
    public List<BankMandate> allBankMandates() {
        return allInstances();
    }

    @Programmatic
    @Action(semantics = SemanticsOf.SAFE)
    public List<BankMandate> findBankMandatesFor(final BankAccount bankAccount) {
        return allMatches("findBankMandatesFor", "bankAccount", bankAccount);
    }

    // //////////////////////////////////////

    @PostConstruct
    @Programmatic
    public void init(Map<String, String> properties) {
        super.init(properties);
        AgreementType agreementType = agreementTypes.findOrCreate(BankMandateConstants.AT_MANDATE);
        agreementRoleTypes.findOrCreate(BankMandateConstants.ART_DEBTOR, agreementType);
        agreementRoleTypes.findOrCreate(BankMandateConstants.ART_CREDITOR, agreementType);
        agreementRoleTypes.findOrCreate(BankMandateConstants.ART_OWNER, agreementType);
    }

    // //////////////////////////////////////

    private AgreementTypes agreementTypes;

    public void injectAgreementTypes(final AgreementTypes agreementTypes) {
        this.agreementTypes = agreementTypes;
    }

    private AgreementRoleTypes agreementRoleTypes;

    public void injectAgreementRoleTypes(final AgreementRoleTypes agreementRoleTypes) {
        this.agreementRoleTypes = agreementRoleTypes;
    }

    @Inject
    AgreementRoleCommunicationChannelTypes agreementRoleCommunicationChannelTypes;

}
