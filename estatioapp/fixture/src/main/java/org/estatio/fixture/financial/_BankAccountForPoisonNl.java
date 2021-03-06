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
package org.estatio.fixture.financial;

import org.estatio.fixture.lease.LeaseForKalPoison001Nl;
import org.estatio.fixture.lease._LeaseForOxfPoison003Gb;
import org.estatio.fixture.party.OrganisationForPoisonNl;

public class _BankAccountForPoisonNl extends BankAccountAbstract {

    public static final String REF = "NL31ABNA0580744437";
    public static final String PARTY_REF = OrganisationForPoisonNl.REF;

    public _BankAccountForPoisonNl() {
        this(null, null);
    }

    public _BankAccountForPoisonNl(String friendlyName, String localName) {
        super(friendlyName, localName);
    }

    @Override
    protected void execute(ExecutionContext executionContext) {

        // prereqs
        if(isExecutePrereqs()) {
            executionContext.executeChild(this, new LeaseForKalPoison001Nl());
            executionContext.executeChild(this, new _LeaseForOxfPoison003Gb());
        }

        // exec
        createBankAccountAndOptionallyFixedAssetFinancialAsset(
                PARTY_REF,
                REF,
                null, // no property = no FAFA,
                executionContext);
    }

}
