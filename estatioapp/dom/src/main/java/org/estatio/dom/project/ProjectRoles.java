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

import java.util.Collection;
import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.party.Party;
import org.joda.time.LocalDate;

@DomainService(nature=NatureOfService.DOMAIN, repositoryFor = ProjectRole.class)
@DomainServiceLayout(menuOrder="10")
public class ProjectRoles extends EstatioDomainService<ProjectRole> {

    public ProjectRoles() {
        super(ProjectRoles.class, ProjectRole.class);
    }

    // //////////////////////////////////////

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProjectRole findRole(
            final Project project) {
        return firstMatch("findByProject",
                "project", project);
    }

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProjectRole findRole(
            final Project project,
            final ProjectRoleType type) {
        return firstMatch("findByProjectAndType",
                "project", project,
                "type", type);
    }
    
    // //////////////////////////////////////

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public Collection<ProjectRole> findRole(
            final Party party) {
        return allMatches("findByParty",
                "party", party);
    }

    // //////////////////////////////////////

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProjectRole findRole(
            final Project project,
            final Party party,
            final ProjectRoleType type) {
        return firstMatch("findByProjectAndPartyAndType",
                "project", project,
                "party", party,
                "type", type);
    }

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProjectRole findRole(
            final Project project,
            final Party party,
            final ProjectRoleType type,
            final LocalDate startDate,
            final LocalDate endDate) {
        return firstMatch("findByProjectAndPartyAndType",
                "project", project,
                "party", party,
                "type", type);
    }
    
    @Programmatic
    public ProjectRole createRole(
    		final Project project,
            final ProjectRoleType type, 
            final Party party, 
            final LocalDate startDate, 
            final LocalDate endDate) {
        final ProjectRole role = newTransientInstance(ProjectRole.class);
        role.setStartDate(startDate);
        role.setEndDate(endDate);
        role.setType(type);
        role.setParty(party);
        role.setProject(project);

        persistIfNotAlready(role);

        return role;
    }

	@Programmatic
    public List<ProjectRole> findByProject(final Project project) {
        return allMatches("findByProject", "project", project);
    }
}
