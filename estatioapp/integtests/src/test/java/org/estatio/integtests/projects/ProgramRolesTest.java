/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
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
package org.estatio.integtests.projects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.estatio.dom.party.Parties;
import org.estatio.dom.party.Party;
import org.estatio.dom.project.Program;
import org.estatio.dom.project.ProgramRole;
import org.estatio.dom.project.ProgramRoleType;
import org.estatio.dom.project.ProgramRoles;
import org.estatio.dom.project.Programs;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.party.PersonForJohnDoe;
import org.estatio.fixture.project.ProgramForGra;
import org.estatio.fixture.project.ProgramForKal;
import org.estatio.integtests.EstatioIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgramRolesTest extends EstatioIntegrationTest {

    public static class FindRole extends ProgramRolesTest {

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new ProgramForKal());
                    executionContext.executeChild(this, new ProgramForGra());
                }
            });
        }

        @Inject
        private Programs programs;
        @Inject
        private Parties parties;
        @Inject
        private ProgramRoles programRoles;

        @Test
        public void withExistingProgramPartyAndRole() throws Exception {

            // given
            Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);
            Program program = programs.findProgram(ProgramForKal.PROGRAM_REFERENCE).get(0);

            // when
            ProgramRole programActor = programRoles.findRole(program, party, ProgramRoleType.PROGRAM_OWNER);

            // then
            Assert.assertNotNull(programActor);
        }
        
        @Test
        public void withExistingParty() throws Exception {

            // given
            Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);

            // when
            Collection<ProgramRole> programActors = programRoles.findRole(party);

            // then
            assertThat(programActors.size(), is(2));
        }

    }
}