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
package org.estatio.dom.project;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.party.Party;
import org.estatio.dom.utils.StringUtils;
import org.joda.time.LocalDate;

@DomainServiceLayout(menuOrder="35", menuBar=MenuBar.PRIMARY, named="Projects")
@DomainService(repositoryFor = Project.class, nature=NatureOfService.VIEW)
public class Projects extends EstatioDomainService<Project> {

    public Projects() {
        super(Projects.class, Project.class);
    }

    @Action(semantics=SemanticsOf.NON_IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public Project newProject(
            final @ParameterLayout(named="Reference") String reference,
            final @ParameterLayout(named="Name") String name,
            final @ParameterLayout(named="Start date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate startDate,
            final @ParameterLayout(named="End date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate endDate) {
        // Create project instance
        Project project = getContainer().newTransientInstance(Project.class);
        // Set values
        project.setReference(reference);
        project.setName(name);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        // Persist it
        persist(project);
        // Return it
        return project;
    }

    @Action(semantics=SemanticsOf.SAFE)
    public List<Project> allProjects() {
        return allInstances();
    }

    @Action(semantics=SemanticsOf.SAFE)
    public List<Project> findProject(final @ParameterLayout(named="Name or reference") String searchStr) {
        return allMatches("findByReferenceOrName", "matcher", StringUtils.wildcardToCaseInsensitiveRegex(searchStr));
    }


//    @Programmatic
    @ActionLayout(contributed=Contributed.AS_ASSOCIATION)
    @MemberOrder(name = "Projects", sequence = "1")
    @Action(semantics=SemanticsOf.SAFE)
    public List<Project> projects(final Party party) {
        return allMatches("findByResponsible", "responsible", party);
    }

}
