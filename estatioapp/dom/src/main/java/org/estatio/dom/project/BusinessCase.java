package org.estatio.dom.project;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.estatio.dom.EstatioDomainObject;
import org.joda.time.LocalDate;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@Queries({
    @Query(
            name = "findByProject", language = "JDOQL",
            value = "SELECT " +
                    "FROM org.estatio.dom.project.BusinessCase " +
                    "WHERE project == :project "),
    @Query(
            name = "findByProjectAndVersion", language = "JDOQL",
            value = "SELECT " +
                    "FROM org.estatio.dom.project.BusinessCase " +
                    "WHERE project == :project && businessCaseVersion == :businessCaseVersion"),
    @Query(
            name = "findByProjectAndActiveVersion", language = "JDOQL",
            value = "SELECT " +
                    "FROM org.estatio.dom.project.BusinessCase " +
                    "WHERE project == :project && isActiveVersion == :isActiveVersion")                       
})
@DomainObject(editing=Editing.DISABLED)
public class BusinessCase extends EstatioDomainObject<BusinessCase> {

	public BusinessCase() {
		super("project, date, lastUpdated desc nullsLast, businessCaseDescription");
	}
	
	public String title() {
		return "Businesscase : "+ this.getProject().getReference();
	}
	// //////////////////////////////////////
	
	private String businessCaseDescription;

	@Column(allowsNull = "false")
    @PropertyLayout(multiLine = 5, describedAs = "Reason for the project and expected benefits")
	public String getBusinessCaseDescription() {
		return businessCaseDescription;
	}

	public void setBusinessCaseDescription(String businessCaseDescription) {
		this.businessCaseDescription = businessCaseDescription;
	}

	// //////////////////////////////////////

	private Project project;
	
	@Column(allowsNull = "false")
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	// //////////////////////////////////////

	private LocalDate date;
	
	@Column(allowsNull = "false")
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	// //////////////////////////////////////
	
	private LocalDate lastUpdated;
	
	@Column(allowsNull = "true")
	public LocalDate getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDate date) {
		this.lastUpdated = date;
	}

	// //////////////////////////////////////
	
	private LocalDate nextReviewDate;
	
	@Column(allowsNull = "true")
	public LocalDate getNextReviewDate() {
		return nextReviewDate;
	}

	public void setNextReviewDate(LocalDate date) {
		this.nextReviewDate = date;
	}

	// //////////////////////////////////////

	private Integer businessCaseVersion;
	
	@Column(allowsNull = "false")
	public Integer getBusinessCaseVersion() {
		return businessCaseVersion;
	}

	public void setBusinessCaseVersion(Integer businessCaseVersion) {
		this.businessCaseVersion = businessCaseVersion;
	}
	
	// //////////////////////////////////////
	
	private boolean isActiveVersion;
	
	@Column(allowsNull = "false")
	@Property(hidden=Where.EVERYWHERE)
	public boolean getIsActiveVersion() {
		return isActiveVersion;
	}

	public void setIsActiveVersion(boolean isActiveVersion) {
		this.isActiveVersion = isActiveVersion;
	}

	
	
	// //////////////////////////////////////
	
	@Action(semantics=SemanticsOf.NON_IDEMPOTENT)
	public BusinessCase updateBusinessCase(
			@ParameterLayout(
					named = "Business Case Description",
					multiLine = 5
					)
			final String businessCaseDescription,
			@ParameterLayout(named = "Next review date")
			final LocalDate reviewDate){
		
		new LocalDate();
		final LocalDate now = LocalDate.now();
		
		BusinessCase businesscase = businesscases.newBusinessCase(this.project, businessCaseDescription, reviewDate, this.date, now, this.getBusinessCaseVersion() + 1, true);
		
		// Set old version isActive property to false and persist it
		this.setIsActiveVersion(false);
		persistIfNotAlready(this);
		
		return businesscase;
	}
	
	public String default0UpdateBusinessCase(){
		return this.getBusinessCaseDescription();
	}
	
	public LocalDate default1UpdateBusinessCase(){
		return this.getNextReviewDate();
	}
	
	public boolean hideUpdateBusinessCase(final String businessCaseDescription, final LocalDate reviewDate) {
		
		if (this.getIsActiveVersion()) {
			return false;
		}
		
		return true;
	}
	
	public String validateUpdateBusinessCase(final String businessCaseDescription, final LocalDate reviewDate) {
		
		if (!this.getIsActiveVersion()) {
			return "This is no active version of the business case and cannot be updated";
		}
		
		new LocalDate();
		LocalDate now = LocalDate.now();
		if (reviewDate.isBefore(now)) {
			return "A review date should not be in the past";
		}
		
		return null;
	}
	
	// //////////////////////////////////////
	
	@Inject
	BusinessCases businesscases;
	
}
