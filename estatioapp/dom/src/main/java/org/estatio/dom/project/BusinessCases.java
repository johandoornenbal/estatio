package org.estatio.dom.project;

import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.estatio.dom.EstatioDomainService;
import org.joda.time.LocalDate;

@DomainService(repositoryFor = BusinessCase.class, nature=NatureOfService.DOMAIN)
public class BusinessCases extends EstatioDomainService<BusinessCase> {

	public BusinessCases(){
			super(BusinessCases.class, BusinessCase.class);
	}
	
	@Programmatic
	public BusinessCase newBusinessCase(
			final Project project,
			final String businessCaseDescription,
			final LocalDate reviewDate,
			final LocalDate date,
			final Integer businessCaseVersion,
			final boolean isActiveVersion
			){
		// Create businesscase instance
		BusinessCase businesscase = getContainer().newTransientInstance(BusinessCase.class);
		
		// Set values
		businesscase.setBusinessCaseDescription(businessCaseDescription);
		businesscase.setDate(date);
		businesscase.setNextReviewDate(reviewDate);
		businesscase.setProject(project);
		businesscase.setBusinessCaseVersion(businessCaseVersion);
		businesscase.setIsActiveVersion(isActiveVersion);
		// Persist it
		persist(businesscase);
		
		return businesscase;
	}
	
	@Programmatic
	public List<BusinessCase> businessCaseHistory(final Project project){
		return allMatches("findByProject", "project", project);
	}
	
	@Programmatic
	public BusinessCase FindActiveBusinessCaseOnProject(final Project project, final boolean isActiveVersion){
		return uniqueMatch("findByProjectAndActiveVersion", "project", project, "isActiveVersion", isActiveVersion);
	}
	
	@Programmatic
	public BusinessCase previousVersion(final BusinessCase businessCase){
		return uniqueMatch("findByProjectAndVersion", "project", businessCase.getProject(), "businessCaseVersion", businessCase.getBusinessCaseVersion() - 1);
	}

	@Programmatic
	public BusinessCase nextVersion(final BusinessCase businessCase){
		return uniqueMatch("findByProjectAndVersion", "project", businessCase.getProject(), "businessCaseVersion", businessCase.getBusinessCaseVersion() + 1);
	}

}
