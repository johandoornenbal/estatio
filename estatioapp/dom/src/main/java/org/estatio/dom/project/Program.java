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

import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
//import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.Title;
import org.estatio.dom.EstatioDomainObject;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.WithReferenceUnique;
import org.estatio.dom.asset.Property;
import org.estatio.dom.party.Party;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@Queries({
        @Query(
                name = "findByReference", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.project.Program " +
                        "WHERE reference == :reference "),
        @Query(
                name = "matchByReferenceOrName", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.project.Program " +
                        "WHERE reference.matches(:matcher) || name.matches(:matcher) "),
        @Query(
                name = "findByProperty", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.project.Program " +
                        "WHERE property == :property ")
})
@DomainObject(editing=Editing.DISABLED, autoCompleteRepository=Programs.class, autoCompleteAction = "autoComplete")
public class Program 
			extends EstatioDomainObject<Program> 
			implements WithReferenceUnique {

	public Program() {
		super("reference, name, programGoal");
	}
	
    // //////////////////////////////////////

    private String reference;

    @Column(allowsNull = "false")
    @org.apache.isis.applib.annotation.Property(regexPattern = RegexValidation.REFERENCE)
    @PropertyLayout(describedAs = "Unique reference code for this program")
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    // //////////////////////////////////////

    private String name;

    @Title
    @Column(allowsNull = "false")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    // //////////////////////////////////////

    private String programGoal;

    @Column(allowsNull = "false")
    @PropertyLayout(multiLine = 5)
    public String getProgramGoal() {
        return programGoal;
    }

    public void setProgramGoal(String programGoal) {
        this.programGoal = programGoal;
    }

    // //////////////////////////////////////

    private Property property;

    @Column(allowsNull = "true")
    @Persistent
    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "program")
    private SortedSet<ProgramRole> roles = new TreeSet<ProgramRole>();

    @CollectionLayout(render=RenderType.EAGERLY)
    public SortedSet<ProgramRole> getRoles() {
        return roles;
    }

    public void setRoles(final SortedSet<ProgramRole> roles) {
        this.roles = roles;
    }
    
    public Program newRole(
            final @ParameterLayout(named = "Type") ProgramRoleType type,
            final Party party,
            final @ParameterLayout(named = "Start date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate startDate,
            final @ParameterLayout(named = "End date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate endDate) {
        createRole(type, party, startDate, endDate);
        return this;
    }

    public String validateNewRole(
            final ProgramRoleType type,
            final Party party,
            final LocalDate startDate,
            final LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return "End date cannot be earlier than start date";
        }
        if (!Sets.filter(getRoles(), type.matchingRole()).isEmpty()) {
            return "Add a successor/predecessor from existing role";
        }
        return null;
    }

    @Programmatic
    public ProgramRole createRole(
            final ProgramRoleType type, final Party party, final LocalDate startDate, final LocalDate endDate) {
        final ProgramRole role = newTransientInstance(ProgramRole.class);
        role.setStartDate(startDate);
        role.setEndDate(endDate);
        role.setType(type);
        role.setParty(party);
        role.setProgram(this);

        persistIfNotAlready(role);

        return role;
    }

}
