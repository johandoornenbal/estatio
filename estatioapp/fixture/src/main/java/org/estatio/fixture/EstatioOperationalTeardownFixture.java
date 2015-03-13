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

import javax.inject.Inject;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;

public class EstatioOperationalTeardownFixture extends FixtureScript {

    @Override
    protected void execute(ExecutionContext fixtureResults) {
        deleteAllDirect();
    }

    private void deleteAllDirect() {
        
        deleteFrom("Numerator");

        deleteFrom("InvoiceItem");
        deleteFrom("Invoice");
        
        deleteFrom("Tag");
        
        deleteFrom("Event");
        deleteFrom("BreakOption");
        deleteFrom("LeaseTerm");
        deleteFrom("LeaseItem");
        deleteFrom("Occupancy");

        deleteFrom("AgreementRoleCommunicationChannel");
        deleteFrom("AgreementRole");

        deleteFrom("Guarantee");
        deleteFrom("BankMandate");
        deleteFrom("Lease");

        deleteFrom("FinancialAccountTransaction");
        deleteFrom("BankAccount");
        deleteFrom("FixedAssetFinancialAccount");
        deleteFrom("FinancialAccount");
        
        deleteFrom("Agreement");
        
        deleteFrom("CommunicationChannelOwnerLinkForFixedAsset");
        deleteFrom("CommunicationChannelOwnerLinkForParty");
        deleteFrom("CommunicationChannelOwnerLink");
        deleteFrom("CommunicationChannel");

        deleteFrom("Unit");
        deleteFrom("Property");
        deleteFrom("FixedAssetRole");
        deleteFrom("LandRegister");
        deleteFrom("FixedAssetRegistration");
        deleteFrom("FixedAsset");
        
        deleteFrom("PartyRegistration");
        deleteFrom("PartyRelationship");
        deleteFrom("Organisation");
        deleteFrom("Person");
        deleteFrom("Party");
    }
 
    private void deleteFrom(final String table) {
        isisJdoSupport.executeUpdate("DELETE FROM " + "\"" + table + "\"");
    }

    @Inject
    private IsisJdoSupport isisJdoSupport;

}
