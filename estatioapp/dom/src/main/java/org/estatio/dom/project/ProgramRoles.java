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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.party.Party;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

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
    
    public Program newRole(
    		final Program program,
            final @ParameterLayout(named = "Type") ProgramRoleType type,
            final Party party,
            final @ParameterLayout(named = "Start date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate startDate,
            final @ParameterLayout(named = "End date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate endDate) {
        createRole(program, type, party, startDate, endDate);
        return program;
    }

    public String validateNewRole(
    		final Program program,
            final ProgramRoleType type,
            final Party party,
            final LocalDate startDate,
            final LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return "End date cannot be earlier than start date";
        }
      
        
        if(findRole(program, party, type)!=null){
        	return "Role already exists";
        }
        return null;
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

}




