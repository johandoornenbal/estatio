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
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

@DomainService(nature=NatureOfService.DOMAIN, repositoryFor = ProgramRole.class)
@DomainServiceLayout(menuOrder="10")
public class ProgramRoles extends EstatioDomainService<ProgramRole> {

    public ProgramRoles() {
        super(ProgramRoles.class, ProgramRole.class);
    }

    // //////////////////////////////////////

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProgramRole findRole(
            final Program program) {
        return firstMatch("findByProgram",
                "program", program);
    }
    
    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProgramRole findRole(
            final Program program,
            final ProgramRoleType type) {
        return firstMatch("findByProgramAndType",
                "program", program,
                "type", type);
    }
    
    // //////////////////////////////////////

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public Collection<ProgramRole> findRole(
            final Party party) {
        return allMatches("findByParty",
                "party", party);
    }

    // //////////////////////////////////////

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProgramRole findRole(
            final Program program,
            final Party party,
            final ProgramRoleType type) {
        return firstMatch("findByProgramAndPartyAndType",
                "program", program,
                "party", party,
                "type", type);
    }

    @Action(semantics=SemanticsOf.SAFE)
    @ActionLayout(contributed=Contributed.AS_NEITHER)
    public ProgramRole findRole(
            final Program program,
            final Party party,
            final ProgramRoleType type,
            final LocalDate startDate,
            final LocalDate endDate) {
        return firstMatch("findByProgramAndPartyAndType",
                "program", program,
                "party", party,
                "type", type);
    }
    
	@Programmatic
	public ProgramRole createRole(
	        final Program program, 
	        final ProgramRoleType type, 
	        final Party party, 
	        final LocalDate startDate, 
	        final LocalDate endDate) {
	    final ProgramRole role = newTransientInstance(ProgramRole.class);
	    role.setStartDate(startDate);
	    role.setEndDate(endDate);
	    role.setType(type);
	    role.setParty(party);
	    role.setProgram(program);
	    persistIfNotAlready(role);
	    return role;
	}
	
	@Programmatic
    public List<ProgramRole> findByProgram(final Program program) {
        return allMatches("findByProgram", "program", program);
    }
	
	//TODO: deze code werkt niet goed (persistence issues)
	@Programmatic
    public SortedSet<ProgramRole> findByProgramSet(final Program program) {
		final SortedSet<ProgramRole> roles = new TreeSet<ProgramRole>();
		for (Iterator<ProgramRole> it = allMatches("findByProgram", "program", program).iterator(); it.hasNext();){
			roles.add(it.next());
		}
        return roles;
    }

}




