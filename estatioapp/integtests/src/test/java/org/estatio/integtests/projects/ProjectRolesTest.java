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
import static org.junit.Assert.assertNull;
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
import org.estatio.dom.project.ProjectRolesContributions;
import org.estatio.dom.project.Projects;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.party.OrganisationForTopModel;
import org.estatio.fixture.party.PersonForJohnDoe;
import org.estatio.fixture.project.ProjectsForGra;
import org.estatio.fixture.project.ProjectsForKal;
import org.estatio.integtests.EstatioIntegrationTest;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProjectRolesTest extends EstatioIntegrationTest {
	
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
    Projects projects;
    
	@Inject
    Parties parties;
    
	@Inject
    ProjectRoles projectRoles;
	
	@Inject
    ProjectRolesContributions projectRolesContributions;

    public static class FindRole extends ProjectRolesTest {

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
    
    public static class newProjectRole extends ProjectRolesTest {
    	
    	Party party;
    	Project project;
    	ProjectRole pr;
    	
    	@Test
    	public void overlappingPeriod() throws Exception {
    		
    		// given
	    	Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);
	    	Project project = projects.findProject(ProjectsForKal.PROJECT_REFERENCE).get(0);
			
			// when
			pr = projectRoles.findRole(project, party, ProjectRoleType.PROJECT_EXECUTIVE);
			assertThat(pr.getEndDate(), is(new LocalDate(2000,1,1)));
			assertThat(pr.getType(), is(ProjectRoleType.PROJECT_EXECUTIVE));
			// then
			assertThat(projectRolesContributions.validateNewProjectRole(project, ProjectRoleType.PROJECT_EXECUTIVE, party, new LocalDate(2000,1,1), null), is("Same party, same role, cannot have overlapping period"));
			assertNull(projectRolesContributions.validateNewProjectRole(project, ProjectRoleType.PROJECT_EXECUTIVE, party, new LocalDate(2000,1,2), null));
    	}
    }
    
    public static class changeProjectRoleDates extends ProjectRolesTest {
    	
    	Party party;
    	Project project;
    	ProjectRole pr1;
    	ProjectRole pr2;
    	
    	@Before
    	public void setUp() {
	    	Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);
	    	Project project = projects.findProject(ProjectsForKal.PROJECT_REFERENCE).get(0);
    		pr2 = projectRoles.createRole(project, ProjectRoleType.PROJECT_EXECUTIVE, party, new LocalDate(2000,1,2), null);
    	}
    	
    	@Test
    	public void overlappingPeriod() throws Exception {
    		
    		// given
	    	Party party = parties.findPartyByReference(PersonForJohnDoe.PARTY_REFERENCE);
	    	Project project = projects.findProject(ProjectsForKal.PROJECT_REFERENCE).get(0);
			
			// when
			pr1 = projectRoles.findRole(project, party, ProjectRoleType.PROJECT_EXECUTIVE);
			assertThat(pr1.getEndDate(), is(new LocalDate(2000,1,1)));
			assertThat(pr2.getStartDate(), is(new LocalDate(2000,1,2)));
			
			// then
			assertThat(pr2.validateChangeDates(new LocalDate(2000,1,1), null), is("Same party, same role, cannot have overlapping period"));
			assertNull(pr2.validateChangeDates(new LocalDate(2000,1,3), null));
    	}
    }
}