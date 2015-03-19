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
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.estatio.dom.EstatioDomainObject;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.WithReferenceUnique;
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
                        "FROM org.estatio.dom.project.Project " +
                        "WHERE reference == :reference "),
        @Query(
                name = "matchByReferenceOrName", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.project.Project " +
                        "WHERE reference.matches(:matcher) || name.matches(:matcher) "),
        @Query(
                name = "findByResponsible", language = "JDOQL",
                value = "SELECT " +
                        "FROM org.estatio.dom.project.Project " +
                        "WHERE responsible == :responsible ")
})
@DomainObject(editing=Editing.DISABLED, autoCompleteRepository=Projects.class, autoCompleteAction = "autoComplete")
public class Project 
		extends EstatioDomainObject<Project>
		implements WithReferenceUnique {

    public Project() {
        super("reference, name, startDate");
    }

    // //////////////////////////////////////

    private String reference;

    @Column(allowsNull = "false")
    @Property(regexPattern = RegexValidation.REFERENCE)
    @PropertyLayout(describedAs = "Unique reference code for this project")
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

    private LocalDate startDate;

    @Column(allowsNull = "true")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    // //////////////////////////////////////

    private LocalDate endDate;

    @Column(allowsNull = "true")
    @Persistent
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // //////////////////////////////////////

    private Program program;

    @Column(allowsNull = "false")
    @Property(hidden=Where.REFERENCES_PARENT)
    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    // //////////////////////////////////////

    // TODO: validatie op leeg zijn startdatum enzo
    public Project postponeOneWeek(@ParameterLayout(named="Reason") String reason) {
        setStartDate(getStartDate().plusWeeks(1));
        return this;
    }
    
    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "project")
    private SortedSet<ProjectRole> roles = new TreeSet<ProjectRole>();

    @CollectionLayout(render=RenderType.EAGERLY)
    public SortedSet<ProjectRole> getRoles() {
        return roles;
    }

    public void setRoles(final SortedSet<ProjectRole> roles) {
        this.roles = roles;
    }
    
    public Project newRole(
            final @ParameterLayout(named = "Type") ProjectRoleType type,
            final Party party,
            final @ParameterLayout(named = "Start date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate startDate,
            final @ParameterLayout(named = "End date") @Parameter(optionality=Optionality.OPTIONAL) LocalDate endDate) {
        createRole(type, party, startDate, endDate);
        return this;
    }

    public String validateNewRole(
            final ProjectRoleType type,
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
    public ProjectRole createRole(
            final ProjectRoleType type, final Party party, final LocalDate startDate, final LocalDate endDate) {
        final ProjectRole role = newTransientInstance(ProjectRole.class);
        role.setStartDate(startDate);
        role.setEndDate(endDate);
        role.setType(type);
        role.setParty(party);
        role.setProject(this);

        persistIfNotAlready(role);

        return role;
    }

}
