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
package org.estatio.dom.lease;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.InvokeOn;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;

import org.estatio.dom.EstatioUserRoles;
import org.estatio.dom.JdoColumnLength;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.agreement.Agreement;
import org.estatio.dom.agreement.AgreementRole;
import org.estatio.dom.agreement.AgreementRoleCommunicationChannel;
import org.estatio.dom.agreement.AgreementRoleCommunicationChannelTypes;
import org.estatio.dom.agreement.AgreementRoleType;
import org.estatio.dom.agreement.AgreementType;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.asset.Units;
import org.estatio.dom.bankmandate.BankMandate;
import org.estatio.dom.bankmandate.BankMandateConstants;
import org.estatio.dom.bankmandate.BankMandates;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.communicationchannel.CommunicationChannels;
import org.estatio.dom.financial.FinancialAccount;
import org.estatio.dom.financial.bankaccount.BankAccount;
import org.estatio.dom.financial.bankaccount.BankAccounts;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.lease.breaks.BreakOption;
import org.estatio.dom.lease.breaks.BreakOptions;
import org.estatio.dom.party.Party;
import org.estatio.dom.utils.JodaPeriodUtils;
import org.estatio.dom.valuetypes.LocalDateInterval;
import org.estatio.services.clock.ClockService;

@javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE)
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
// no @DatastoreIdentity nor @Version, since inherited from supertype
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByReference", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.Lease "
                        + "WHERE reference == :reference"),
        @javax.jdo.annotations.Query(
                name = "matchByReferenceOrName", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.Lease "
                        + "WHERE (reference.matches(:referenceOrName)"
                        + "|| name.matches(:referenceOrName))"
                        + "&& (:includeTerminated || tenancyEndDate == null || tenancyEndDate >= :date)"),
        @javax.jdo.annotations.Query(
                name = "findByProperty", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.Lease "
                        + "WHERE occupancies.contains(occ) "
                        + "&& (occ.unit.property == :property) "
                        + "VARIABLES "
                        + "org.estatio.dom.lease.Occupancy occ"),
        @javax.jdo.annotations.Query(
                name = "findByBrand", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.Lease "
                        + "WHERE (occupancies.contains(occ) "
                        + "&& (occ.brand == :brand)) "
                        + "&& (:includeTerminated || tenancyEndDate == null || tenancyEndDate >= :date) "
                        + "VARIABLES "
                        + "org.estatio.dom.lease.Occupancy occ"),
        @javax.jdo.annotations.Query(
                name = "findByAssetAndActiveOnDate", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.Lease "
                        + "WHERE occupancies.contains(occ) "
                        + "&& (tenancyStartDate == null || tenancyStartDate <= :activeOnDate) "
                        + "&& (tenancyEndDate == null || tenancyEndDate >= :activeOnDate) "
                        + "&& (occ.unit.property == :asset) "
                        + "VARIABLES "
                        + "org.estatio.dom.lease.Occupancy occ"),
        @javax.jdo.annotations.Query(
                name = "findExpireInDateRange", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.lease.Lease " +
                        "WHERE " +
                        "endDate != null && (endDate >= :rangeStartDate && endDate < :rangeEndDate) " +
                        "ORDER BY endDate")
})
@DomainObject(autoCompleteRepository = Leases.class, autoCompleteAction = "autoComplete")
@DomainObjectLayout(bookmarking = BookmarkPolicy.AS_ROOT)
public class Lease
        extends Agreement {

    // //////////////////////////////////////

    public void created() {
        setStatus(LeaseStatus.ACTIVE);
    }

    // //////////////////////////////////////

    private LeaseStatus status;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.STATUS_ENUM)
    @org.apache.isis.applib.annotation.Property(editing = Editing.DISABLED)
    public LeaseStatus getStatus() {
        return status;
    }

    public void setStatus(final LeaseStatus status) {
        this.status = status;
    }

    @Programmatic
    public void resolveStatus(final String reason) {
        final LeaseStatus effectiveStatus = getEffectiveStatus();
        if (effectiveStatus != null && !effectiveStatus.equals(getStatus())) {
            setStatus(effectiveStatus);
        }
    }

    @Programmatic
    public LeaseStatus getEffectiveStatus() {
        ArrayList<LeaseItem> all = new ArrayList<LeaseItem>(getItems());
        int itemCount = getItems().size();
        List<LeaseItemStatus> statusList = Lists.transform(all, LeaseItem.Functions.GET_STATUS);
        int suspensionCount = Collections.frequency(statusList, LeaseItemStatus.SUSPENDED);
        if (suspensionCount > 0) {
            if (itemCount == suspensionCount) {
                return LeaseStatus.SUSPENDED;
            } else {
                return LeaseStatus.SUSPENDED_PARTIALLY;
            }
        }
        return null;
    }

    // //////////////////////////////////////

    @Override
    @org.apache.isis.applib.annotation.Property(hidden = Where.PARENTED_TABLES, notPersisted = true)
    public Party getPrimaryParty() {
        final AgreementRole ar = getPrimaryAgreementRole();
        return partyOf(ar);
    }

    @Override
    @org.apache.isis.applib.annotation.Property(notPersisted = true)
    public Party getSecondaryParty() {
        final AgreementRole ar = getSecondaryAgreementRole();
        return partyOf(ar);
    }

    @Programmatic
    protected AgreementRole getPrimaryAgreementRole() {
        return findCurrentOrMostRecentAgreementRole(LeaseConstants.ART_LANDLORD);
    }

    @Programmatic
    protected AgreementRole getSecondaryAgreementRole() {
        return findCurrentOrMostRecentAgreementRole(LeaseConstants.ART_TENANT);
    }

    // //////////////////////////////////////

    /**
     * The {@link Property} of the (first of the) {@link #getOccupancies()
     * LeaseUnit}s.
     * 
     * <p>
     * It is not possible for the {@link Occupancy}s to belong to different
     * {@link Property properties}, and so it is sufficient to obtain the
     * {@link Property} of the first such {@link Occupancy occupancy}.
     */

    @org.apache.isis.applib.annotation.Property(hidden = Where.PARENTED_TABLES)
    public Property getProperty() {
        if (getOccupancies().isEmpty()) {
            return null;
        }
        return getOccupancies().first().getUnit().getProperty();
    }

    // //////////////////////////////////////

    private LeaseType leaseType;

    @javax.jdo.annotations.Column(name = "leaseTypeId", allowsNull = "true")
    public LeaseType getLeaseType() {
        return leaseType;
    }

    public void setLeaseType(final LeaseType leaseType) {
        this.leaseType = leaseType;
    }

    public Lease change(
            final @ParameterLayout(named = "Name") String name,
            final @ParameterLayout(named = "Lease Type") @Parameter(optionality = Optionality.OPTIONAL) LeaseType leaseType) {
        setName(name);
        setLeaseType(leaseType);
        return this;
    }

    public String default0Change() {
        return getName();
    }

    public LeaseType default1Change() {
        return getLeaseType();
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    public LocalDate tenancyStartDate;

    @org.apache.isis.applib.annotation.Property(editing = Editing.DISABLED, optionality = Optionality.OPTIONAL, hidden = Where.ALL_TABLES)
    public LocalDate getTenancyStartDate() {
        return tenancyStartDate;
    }

    public void setTenancyStartDate(final LocalDate tenancyStartDate) {
        this.tenancyStartDate = tenancyStartDate;
    }

    @javax.jdo.annotations.Persistent
    public LocalDate tenancyEndDate;

    @org.apache.isis.applib.annotation.Property(editing = Editing.DISABLED, optionality = Optionality.OPTIONAL, hidden = Where.ALL_TABLES)
    public LocalDate getTenancyEndDate() {
        return tenancyEndDate;
    }

    public void setTenancyEndDate(final LocalDate tenancyEndDate) {
        this.tenancyEndDate = tenancyEndDate;
    }

    @org.apache.isis.applib.annotation.Property(editing = Editing.DISABLED, optionality = Optionality.OPTIONAL)
    public String getTenancyDuration() {
        LocalDateInterval ldi;
        if (getTenancyStartDate() != null && getTenancyEndDate() != null) {
            ldi = getEffectiveInterval();
        } else if (getTenancyStartDate() == null && getTenancyEndDate() != null && getStartDate() != null) {
            ldi = new LocalDateInterval(getStartDate(), getTenancyEndDate());
        } else if (getTenancyStartDate() != null && getTenancyEndDate() == null && getEndDate() != null) {
            ldi = new LocalDateInterval(getTenancyStartDate(), getEndDate());
        } else {
            return null;
        }

        if (ldi.isValid()) {
            return JodaPeriodUtils.asSimpleString(new Period(ldi.asInterval(), PeriodType.yearMonthDay()));
        }

        return null;
    }

    // //////////////////////////////////////

    @Action(domainEvent = ChangeDatesEvent.class)
    public Lease changeTenancyDates(
            final @ParameterLayout(named = "Start Date") LocalDate startDate,
            final @ParameterLayout(named = "End Date") @Parameter(optionality = Optionality.OPTIONAL) LocalDate endDate
            ) {
        setTenancyStartDate(startDate);
        setTenancyEndDate(endDate);
        verifyAllOccupancies();

        return this;
    }

    private void verifyAllOccupancies() {
        for (Occupancy occupancy : occupancies) {
            occupancy.verify();
        }
    }

    public LocalDate default0ChangeTenancyDates() {
        return getTenancyStartDate();
    }

    public LocalDate default1ChangeTenancyDates() {
        return getTenancyEndDate();
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public LocalDateInterval getEffectiveInterval() {
        return new LocalDateInterval(
                getTenancyStartDate() == null ? getStartDate() : getTenancyStartDate(),
                getTenancyEndDate());
    }

    // //////////////////////////////////////

    @Programmatic
    public LocalDate getTerminationDate() {
        return getTenancyEndDate();
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "lease")
    private SortedSet<Occupancy> occupancies = new TreeSet<Occupancy>();

    @CollectionLayout(render = RenderType.EAGERLY)
    public SortedSet<Occupancy> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(final SortedSet<Occupancy> occupancies) {
        this.occupancies = occupancies;
    }

    /**
     * The action to relate a lease to a unit. A lease can occupy unlimited
     * units.
     * 
     * @param unit
     * @param startDate
     * @return
     */
    public Occupancy newOccupancy(
            final @ParameterLayout(named = "Start date") @Parameter(optionality = Optionality.OPTIONAL) LocalDate startDate,
            final @ParameterLayout(named = "Unit") Unit unit) {
        Occupancy occupancy = occupanciesRepo.newOccupancy(this, unit, startDate);
        occupancies.add(occupancy);
        return occupancy;
    }

    public LocalDate default0NewOccupancy() {
        return getTenancyStartDate();
    }

    public List<Unit> choices1NewOccupancy(final LocalDate startDate) {
        return units.findActiveByProperty(getProperty());
    }

    // //////////////////////////////////////

    private SortedSet<LeaseItem> items = new TreeSet<LeaseItem>();

    /**
     * Added to the default fetch group in an attempt to resolve pre-prod error,
     * EST-233.
     */
    @javax.jdo.annotations.Persistent(mappedBy = "lease", defaultFetchGroup = "true")
    @CollectionLayout(render = RenderType.EAGERLY)
    public SortedSet<LeaseItem> getItems() {
        return items;
    }

    public void setItems(final SortedSet<LeaseItem> items) {
        this.items = items;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public LeaseItem newItem(
            final LeaseItemType type,
            final Charge charge,
            final InvoicingFrequency invoicingFrequency,
            final PaymentMethod paymentMethod,
            final @ParameterLayout(named = "Start date") LocalDate startDate) {
        LeaseItem leaseItem = leaseItems.newLeaseItem(this, type, charge, invoicingFrequency, paymentMethod, startDate);
        return leaseItem;
    }

    public LocalDate default4NewItem() {
        return getStartDate();
    }

    @Action(hidden = Where.EVERYWHERE)
    public LeaseItem findItem(
            final LeaseItemType itemType,
            final LocalDate itemStartDate,
            final BigInteger sequence) {
        return leaseItems.findLeaseItem(this, itemType, itemStartDate, sequence);
    }

    @Programmatic
    public LeaseItem findFirstItemOfType(final LeaseItemType type) {
        for (LeaseItem item : getItems()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    @Programmatic
    public List<LeaseItem> findItemsOfType(final LeaseItemType type) {
        List<LeaseItem> items = new ArrayList<LeaseItem>();
        for (LeaseItem item : getItems()) {
            if (item.getType().equals(type)) {
                items.add(item);
            }
        }
        return items;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "lease")
    private SortedSet<BreakOption> breakOptions = new TreeSet<BreakOption>();

    @CollectionLayout(render = RenderType.EAGERLY)
    public SortedSet<BreakOption> getBreakOptions() {
        return breakOptions;
    }

    public void setBreakOptions(final SortedSet<BreakOption> breakOptions) {
        this.breakOptions = breakOptions;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Column(name = "paidByBankMandateId")
    private BankMandate paidBy;

    @org.apache.isis.applib.annotation.Property(hidden = Where.ALL_TABLES, editing = Editing.DISABLED, optionality = Optionality.OPTIONAL)
    public BankMandate getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(final BankMandate paidBy) {
        this.paidBy = paidBy;
    }

    // //////////////////////////////////////

    public Lease paidBy(final BankMandate bankMandate) {
        setPaidBy(bankMandate);
        return this;
    }

    public String disablePaidBy(final BankMandate bankMandate) {
        final List<BankMandate> validMandates = existingBankMandatesForTenant();
        if (validMandates.isEmpty()) {
            return "There are no valid mandates; set one up using 'New Mandate'";
        }
        return null;
    }

    public List<BankMandate> choices0PaidBy() {
        return existingBankMandatesForTenant();
    }

    public BankMandate default0PaidBy() {
        final List<BankMandate> choices = existingBankMandatesForTenant();
        return !choices.isEmpty() ? choices.get(0) : null;
    }

    public String validatePaidBy(final BankMandate bankMandate) {
        final List<BankMandate> validMandates = existingBankMandatesForTenant();
        if (validMandates.contains(bankMandate)) {
            return null;
        } else {
            return "Invalid mandate; the mandate's debtor must be this lease's tenant";
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<BankMandate> existingBankMandatesForTenant() {
        final AgreementRole tenantRole = getSecondaryAgreementRole();
        if (tenantRole == null || !tenantRole.isCurrent()) {
            return Collections.emptyList();
        }
        final Party tenant = partyOf(tenantRole);
        final AgreementType bankMandateAgreementType = bankMandateAgreementType();
        final AgreementRoleType debtorRoleType = debtorRoleType();

        return (List) agreements.findByAgreementTypeAndRoleTypeAndParty(
                bankMandateAgreementType, debtorRoleType, tenant);
    }

    // //////////////////////////////////////

    public Lease newMandate(
            final BankAccount bankAccount,
            final @ParameterLayout(named = "Reference") @Parameter(regexPattern = RegexValidation.REFERENCE) String reference,
            final @ParameterLayout(named = "Start Date") LocalDate startDate,
            final @ParameterLayout(named = "End Date") @Parameter(optionality = Optionality.OPTIONAL) LocalDate endDate) {

        final Party creditor = getPrimaryParty();
        final Party debtor = getSecondaryParty();

        final BankMandate bankMandate =
                bankMandates.newBankMandate(reference, reference, startDate, endDate, debtor, creditor, bankAccount);
        paidBy(bankMandate);
        return this;
    }

    public String disableNewMandate(
            final BankAccount bankAccount,
            final String reference,
            final LocalDate startDate,
            final LocalDate endDate) {
        final AgreementRole tenantRole = getSecondaryAgreementRole();
        if (tenantRole == null || !tenantRole.isCurrent()) {
            return "Could not determine the tenant (secondary party) of this lease";
        }
        final List<? extends FinancialAccount> validBankAccounts = existingBankAccountsForTenant();
        if (validBankAccounts.isEmpty()) {
            return "There are no bank accounts available for this tenant";
        }
        return null;
    }

    public List<BankAccount> choices0NewMandate() {
        return existingBankAccountsForTenant();
    }

    public BankAccount default0NewMandate() {
        final List<BankAccount> choices = existingBankAccountsForTenant();
        return !choices.isEmpty() ? choices.get(0) : null;
    }

    public LocalDate default2NewMandate() {
        return getClockService().now();
    }

    public LocalDate default3NewMandate() {
        return getClockService().now().plusYears(1);
    }

    public String validateNewMandate(
            final BankAccount bankAccount,
            final String reference,
            final LocalDate startDate,
            final LocalDate endDate) {
        final List<? extends FinancialAccount> validBankAccounts = existingBankAccountsForTenant();
        if (!validBankAccounts.contains(bankAccount)) {
            return "Bank account is not owned by this lease's tenant";
        }
        if (agreements.findAgreementByReference(reference) != null) {
            return "Reference already exists";
        }
        return null;
    }

    private List<BankAccount> existingBankAccountsForTenant() {
        final Party tenant = getSecondaryParty();
        if (tenant != null) {
            return financialAccounts.findBankAccountsByOwner(tenant);
        } else {
            return Collections.emptyList();
        }
    }

    private AgreementRoleType debtorRoleType() {
        return agreementRoleTypes.findByTitle(BankMandateConstants.ART_DEBTOR);
    }

    private AgreementType bankMandateAgreementType() {
        return agreementTypes.find(BankMandateConstants.AT_MANDATE);
    }

    // //////////////////////////////////////

    @Action(restrictTo = RestrictTo.PROTOTYPING, invokeOn = InvokeOn.OBJECT_AND_COLLECTION)
    public Lease approveAllTermsOfThisLease() {
        for (LeaseItem item : getItems()) {
            for (LeaseTerm term : item.getTerms()) {
                term.approve();
            }
        }
        return this;
    }

    // //////////////////////////////////////

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public Lease verify() {
        verifyUntil(ObjectUtils.min(getEffectiveInterval().endDateExcluding(), getClockService().now()));
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public Lease verifyUntil(final LocalDate date) {
        for (LeaseItem item : getItems()) {
            LocalDateInterval effectiveInterval = item.getEffectiveInterval();
            item.verifyUntil(ObjectUtils.min(effectiveInterval == null ? null : effectiveInterval.endDateExcluding(), date));
        }
        return this;
    }

    // //////////////////////////////////////

    @Action(domainEvent = Lease.TerminateEvent.class)
    public Lease terminate(
            final @ParameterLayout(named = "Termination Date") LocalDate terminationDate,
            final @ParameterLayout(named = "Are you sure?") Boolean confirm) {
        doTerminate(terminationDate);
        return this;
    }

    public LocalDate default0Terminate() {
        return getClockService().now();
    }

    public Boolean default1Terminate() {
        return false;
    }

    public String validateTerminate(
            final LocalDate terminationDate,
            final Boolean confirm) {
        if (terminationDate.isBefore(getStartDate())) {
            return "Termination date can't be before start date";
        }
        return confirm ? null : "Make sure you confirm this action";
    }

    public boolean hideTerminate() {
        return !getStatus().equals(LeaseStatus.ACTIVE);
    }

    @Programmatic
    public void doTerminate(final LocalDate terminationDate) {
        for (Occupancy occupancy : getOccupancies()) {
            if (occupancy.getInterval().contains(terminationDate)) {
                occupancy.terminate(terminationDate);
            }
            // TODO: remove occupancies after the termination date
        }
        // TODO: break options
        setTenancyEndDate(terminationDate);
    }

    // //////////////////////////////////////

    @Action(domainEvent = Lease.SuspendAllEvent.class)
    public Lease suspendAll(final @ParameterLayout(named = "Reason") String reason) {
        for (LeaseItem item : getItems()) {
            item.suspend(reason);
        }
        setStatus(LeaseStatus.SUSPENDED);
        return this;
    }

    public boolean hideSuspendAll() {
        return !getStatus().equals(LeaseStatus.ACTIVE) && !getStatus().equals(LeaseStatus.SUSPENDED_PARTIALLY);
    }

    // //////////////////////////////////////

    @Action(domainEvent = ResumeAllEvent.class)
    public Lease resumeAll() {
        for (LeaseItem item : getItems()) {
            item.doResume();
        }
        return this;
    }

    public boolean hideResumeAll() {
        return !getStatus().equals(LeaseStatus.SUSPENDED) && !getStatus().equals(LeaseStatus.SUSPENDED_PARTIALLY);
    }

    // //////////////////////////////////////

    public Lease assign(
            @ParameterLayout(named = "Reference") @Parameter(regexPattern = RegexValidation.REFERENCE) final String reference,
            @ParameterLayout(named = "Name") final String name,
            @ParameterLayout(named = "Tenant") final Party tenant,
            @ParameterLayout(named = "Tenancy start date") final LocalDate tenancyStartDate,
            @ParameterLayout(named = "Are you sure?") final Boolean confirm
            ) {
        Lease newLease = copyToNewLease(reference, name, tenant, getStartDate(), getEndDate(), tenancyStartDate, getEndDate());
        this.doTerminate(new LocalDateInterval(tenancyStartDate, null).endDateFromStartDate());
        return newLease;
    }

    public LocalDate default3Assign() {
        return getClockService().now();
    }

    public String validateAssign(
            final String reference,
            final String name,
            final Party tenant,
            final LocalDate startDate,
            final Boolean confirm
            ) {
        return leases.findLeaseByReferenceElseNull(reference) == null ? null : "Lease reference already exists,";
    }

    // //////////////////////////////////////

    @Programmatic
    Lease copyToNewLease(
            final String reference,
            final String name,
            final Party tenant,
            final LocalDate startDate,
            final LocalDate endDate,
            final LocalDate tenancyStartDate,
            final LocalDate tenancyEndDate) {
        Lease newLease = leases.newLease(
                reference,
                name,
                this.getLeaseType(),
                startDate,
                endDate,
                tenancyStartDate,
                tenancyEndDate,
                this.getPrimaryParty(),
                tenant);

        copyItemsAndTerms(newLease, tenancyStartDate);
        copyOccupancies(newLease, tenancyStartDate);
        breakOptionsService.copyBreakOptions(this, newLease, tenancyStartDate);
        copyAgreementRoleCommunicationChannels(newLease, tenancyStartDate);
        this.setNext(newLease);
        return newLease;
    }

    private void copyItemsAndTerms(final Lease newLease, final LocalDate startDate) {
        for (LeaseItem item : getItems()) {
            LeaseItem newItem = newLease.newItem(
                    item.getType(),
                    item.getCharge(),
                    item.getInvoicingFrequency(),
                    item.getPaymentMethod(),
                    item.getStartDate());
            item.copyTerms(startDate, newItem);
        }
    }

    private void copyOccupancies(final Lease newLease, final LocalDate startDate) {
        for (Occupancy occupancy : getOccupancies()) {
            if (occupancy.getInterval().contains(startDate)) {
                Occupancy newOccupancy = newLease.newOccupancy(startDate, occupancy.getUnit());
                newOccupancy.setActivity(occupancy.getActivity());
                newOccupancy.setBrand(occupancy.getBrand());
                newOccupancy.setSector(occupancy.getSector());
                newOccupancy.setUnitSize(occupancy.getUnitSize());
                newOccupancy.setReportOCR(occupancy.getReportOCR());
                newOccupancy.setReportRent(occupancy.getReportRent());
                newOccupancy.setReportTurnover(occupancy.getReportTurnover());
            }
        }
    }

    private void copyAgreementRoleCommunicationChannels(final Lease newLease, final LocalDate startDate) {
        if (getSecondaryParty() == newLease.getSecondaryParty()) {
            // renew
            for (AgreementRole role : getRoles()) {
                AgreementRole newRole = agreementRoles.findByAgreementAndPartyAndTypeAndContainsDate(newLease, role.getParty(), role.getType(), startDate);
                if (newRole != null) {
                    for (AgreementRoleCommunicationChannel agreementRoleCommunicationChannel : role.getCommunicationChannels()) {
                        newRole.addCommunicationChannel(agreementRoleCommunicationChannel.getType(), agreementRoleCommunicationChannel.getCommunicationChannel());
                    }
                }
            }
        }
    }

    // //////////////////////////////////////

    public Lease renew(
            @ParameterLayout(named = "Reference") @Parameter(regexPattern = RegexValidation.Lease.REFERENCE) final String reference,
            @ParameterLayout(named = "Name") final String name,
            @ParameterLayout(named = "Start date") final LocalDate startDate,
            @ParameterLayout(named = "End date") final LocalDate endDate,
            @ParameterLayout(named = "Are you sure?") final Boolean confirm
            ) {
        return copyToNewLease(reference, name, getSecondaryParty(), startDate, endDate, startDate, endDate);

    }

    public String default0Renew() {
        return getReference();
    }

    public String default1Renew() {
        return getName();
    }

    public LocalDate default2Renew() {
        return getInterval().endDateExcluding();
    }

    public String validateRenew(
            final String reference,
            final String name,
            final LocalDate startDate,
            final LocalDate endDate,
            final Boolean confirm
            ) {
        if (endDate.isBefore(startDate)) {
            return "End date can not be before start date.";
        }
        return leases.findLeaseByReferenceElseNull(reference) == null ? null : "Lease reference already exists.";
    }

    // //////////////////////////////////////

    public void remove(@ParameterLayout(named = "Are you sure?") Boolean confirm) {
        if (confirm) {
            doRemove();
        }
    }

    public boolean hideRemove() {
        return !getUser().hasRole(EstatioUserRoles.ADMIN_ROLE);
    }

    @Programmatic
    public boolean doRemove() {
        boolean success = true;

        for (LeaseItem item : getItems()) {
            success = !item.doRemove() ? false : success;
        }
        if (success) {
            getContainer().remove(this);
        }
        return success;
    }

    // //////////////////////////////////////

    public static class TerminateEvent extends ActionDomainEvent<Lease> {
        private static final long serialVersionUID = 1L;

        public TerminateEvent(
                final Lease source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    public static class SuspendAllEvent extends ActionDomainEvent<Lease> {
        private static final long serialVersionUID = 1L;

        public SuspendAllEvent(
                final Lease source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    public static class ResumeAllEvent extends ActionDomainEvent<Lease> {
        private static final long serialVersionUID = 1L;

        public ResumeAllEvent(
                final Lease source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    public static class ChangeDatesEvent extends ActionDomainEvent<Lease> {
        private static final long serialVersionUID = 1L;

        public ChangeDatesEvent(
                final Lease source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    // //////////////////////////////////////

    @Inject
    LeaseItems leaseItems;

    @Inject
    Occupancies occupanciesRepo;

    @Inject
    BankAccounts financialAccounts;

    @Inject
    BankMandates bankMandates;

    @Inject
    Leases leases;

    @Inject
    Units units;

    @Inject
    AgreementRoleCommunicationChannelTypes agreementRoleCommunicationChannelTypes;

    @Inject
    CommunicationChannels communicationChannels;

    @Inject
    BreakOptions breakOptionsService;

    @Inject
    ClockService clockService;

}
