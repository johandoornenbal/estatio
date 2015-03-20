package org.estatio.dom.project;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.estatio.dom.EstatioDomainObject;
import org.joda.time.LocalDate;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@DomainObject(editing=Editing.DISABLED)
public class BusinessCase extends EstatioDomainObject<BusinessCase> {

	public BusinessCase() {
		super("project, date, businessCaseDescription");
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
}
