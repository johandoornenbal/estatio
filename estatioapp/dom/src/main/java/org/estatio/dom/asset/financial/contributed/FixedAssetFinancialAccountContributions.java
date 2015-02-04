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

package org.estatio.dom.asset.financial.contributed;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.dom.EstatioService;
import org.estatio.dom.asset.FixedAsset;
import org.estatio.dom.asset.FixedAssetRole;
import org.estatio.dom.asset.FixedAssetRoleType;
import org.estatio.dom.asset.FixedAssetRoles;
import org.estatio.dom.asset.financial.FixedAssetFinancialAccount;
import org.estatio.dom.asset.financial.FixedAssetFinancialAccounts;
import org.estatio.dom.financial.FinancialAccount;
import org.estatio.dom.financial.FinancialAccounts;

@DomainServiceLayout(menuOrder = "10")
@DomainService(nature = NatureOfService.VIEW_CONTRIBUTIONS_ONLY)
public class FixedAssetFinancialAccountContributions extends EstatioService<FixedAssetFinancialAccountContributions> {

    public FixedAssetFinancialAccountContributions() {
        super(FixedAssetFinancialAccountContributions.class);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @MemberOrder(name = "Accounts", sequence = "13")
    public FixedAssetFinancialAccount newAccount(
            final FixedAsset fixedAsset,
            final FinancialAccount financialAccount) {
        return fixedAssetFinancialAccounts.newFixedAssetFinancialAccount(fixedAsset, financialAccount);
    }

    public List<FinancialAccount> choices1NewAccount(
            final FixedAsset fixedAsset,
            final FinancialAccount financialAccount) {
        final FixedAssetRole role = fixedAssetRoles.findRole(fixedAsset, FixedAssetRoleType.PROPERTY_OWNER);
        if (role != null) {
            return financialAccounts.findAccountsByOwner(role.getParty());
        }
        return null;
    }

    // //////////////////////////////////////

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    @MemberOrder(name = "Accounts", sequence = "13.5")
    public List<FixedAssetFinancialAccount> accounts(final FixedAsset fixedAsset) {
        return fixedAssetFinancialAccounts.findByFixedAsset(fixedAsset);
    }

    // //////////////////////////////////////

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    @MemberOrder(name = "FinancialAccounts", sequence = "13.5")
    public List<FixedAssetFinancialAccount> fixedAssets(final FinancialAccount fixedAsset) {
        return fixedAssetFinancialAccounts.findByFinancialAccount(fixedAsset);
    }

    // //////////////////////////////////////

    private FixedAssetFinancialAccounts fixedAssetFinancialAccounts;

    public void injectFixedAssetFinancialAccounts(final FixedAssetFinancialAccounts fixedAssetFinancialAccounts) {
        this.fixedAssetFinancialAccounts = fixedAssetFinancialAccounts;
    }

    private FinancialAccounts financialAccounts;

    public final void injectFinancialAccounts(final FinancialAccounts financialAccounts) {
        this.financialAccounts = financialAccounts;
    }

    private FixedAssetRoles fixedAssetRoles;

    public final void setFixedAssetRoles(final FixedAssetRoles fixedAssetRoles) {
        this.fixedAssetRoles = fixedAssetRoles;
    }

}
