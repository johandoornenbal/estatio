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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.party.Party;

@DomainService(nature = NatureOfService.VIEW_CONTRIBUTIONS_ONLY)
public class ProjectsContributions extends EstatioDomainService<Project> {

	public ProjectsContributions() {
		super(ProjectsContributions.class, Project.class);
	}

	@ActionLayout(contributed = Contributed.AS_ASSOCIATION)
	@MemberOrder(name = "Projects", sequence = "1")
	@Action(semantics = SemanticsOf.SAFE)
	public List<Project> projects(final Party party) {
		return projects.findByResponsible(party);
	}

	@ActionLayout(contributed = Contributed.AS_ASSOCIATION)
	@MemberOrder(name = "Projects", sequence = "1")
	@Action(semantics = SemanticsOf.SAFE)
	public List<Project> projects(final Program program) {
		return projects.findByProgram(program);
	}

	@Inject
	Projects projects;

}