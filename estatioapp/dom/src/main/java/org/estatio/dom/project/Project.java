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

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.estatio.dom.EstatioDomainObject;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.WithReferenceUnique;
import org.estatio.dom.party.Party;
import org.joda.time.LocalDate;

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
                name = "findByReferenceOrName", language = "JDOQL",
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
        super("reference,startDate");
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

    private Party responsible;

    @Column(allowsNull = "false")
    public Party getResponsible() {
        return responsible;
    }

    public void setResponsible(Party responsible) {
        this.responsible = responsible;
    }

    // //////////////////////////////////////

    public Project postponeOneWeek(@ParameterLayout(named="Reason") String reason) {
        setStartDate(getStartDate().plusWeeks(1));
        return this;
    }

}
