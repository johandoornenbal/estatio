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
import org.estatio.dom.project.Project;
import org.estatio.dom.project.ProjectRole;
import org.estatio.dom.project.ProjectRoleType;
import org.estatio.dom.project.ProjectRoles;
import org.estatio.dom.project.Projects;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.party.OrganisationForTopModel;
import org.estatio.fixture.party.PersonForJohnDoe;
import org.estatio.fixture.project.ProjectsForGra;
import org.estatio.fixture.project.ProjectsForKal;
import org.estatio.integtests.EstatioIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProjectRolesTest extends EstatioIntegrationTest {

    public static class FindRole extends ProjectRolesTest {

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new OrganisationForTopModel());
                    executionContext.executeChild(this, new ProjectsForKal());
                    executionContext.executeChild(this, new ProjectsForGra());
                }
            });
        }

        @Inject
        private Projects projects;
        @Inject
        private Parties parties;
        @Inject
        private ProjectRoles projectRoles;

        @Test
        public void withExistingProjectPartyAndRole() throws Exception {

            // given
            Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);
            Project project = projects.findProject(ProjectsForKal.PROJECT_REFERENCE).get(0);

            // when
            ProjectRole projectActor = projectRoles.findRole(project, party, ProjectRoleType.PROJECT_EXECUTIVE);

            // then
            Assert.assertNotNull(projectActor);
        }
        
        @Test
        public void withExistingParty() throws Exception {

            // given
            Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);

            // when
            Collection<ProjectRole> projectActors = projectRoles.findRole(party);

            // then
            assertThat(projectActors.size(), is(3));
        }

    }
}