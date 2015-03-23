package org.estatio.dom.project;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.estatio.dom.EstatioDomainService;
import org.joda.time.LocalDate;

@DomainService(repositoryFor = BusinessCase.class, nature=NatureOfService.VIEW_CONTRIBUTIONS_ONLY)
@DomainServiceLayout(menuOrder="35", menuBar=MenuBar.PRIMARY, named="Projects")
public class BusinessCases extends EstatioDomainService<BusinessCase> {

	public BusinessCases(){
			super(BusinessCases.class, BusinessCase.class);
	}
	
	@Action(semantics=SemanticsOf.NON_IDEMPOTENT)
	@ActionLayout(contributed=Contributed.AS_ACTION)
    @MemberOrder(sequence = "1")
	public BusinessCase newBusinessCase(
			final Project project,
			@ParameterLayout(
					named = "Business Case Description",
					multiLine = 5
					)
			final String businessCaseDescription,
			@ParameterLayout(named = "Next review date")
			final LocalDate reviewDate
			){
		// Create businesscase instance
		BusinessCase businesscase = getContainer().newTransientInstance(BusinessCase.class);
		
		// Set values
		businesscase.setBusinessCaseDescription(businessCaseDescription);
		new LocalDate();
		final LocalDate now = LocalDate.now();
		businesscase.setDate(now);
		businesscase.setNextReviewDate(reviewDate);
		businesscase.setProject(project);
		businesscase.setBusinessCaseVersion(1);
		businesscase.setIsActiveVersion(true);
		// Persist it
		persist(businesscase);
		
		return businesscase;
	}
	
	public boolean hideNewBusinessCase(final Project project, final String businessCaseDescription, final LocalDate reviewDate){
		
		if (!allMatches("findByProject", "project", project).isEmpty()){
			return true;
		}
		
		return false;
	}
	
	public String validateNewBusinessCase(final Project project, final String businessCaseDescription, final LocalDate reviewDate){
		
		if (!allMatches("findByProject", "project", project).isEmpty()){
			return "This project has a business case already; use update business case instead";
		}
		
		return null;
	}
	
//	@Action(semantics=SemanticsOf.SAFE)
//	@ActionLayout(contributed=Contributed.AS_ASSOCIATION)
//	@CollectionLayout(render=RenderType.EAGERLY)
//	public List<BusinessCase> businessCaseHistory(final Project project){
//		return allMatches("findByProject", "project", project);
//	}
	
	@Action(semantics=SemanticsOf.SAFE)
	@ActionLayout(contributed=Contributed.AS_ASSOCIATION)
	public BusinessCase businesCase(final Project project){
		return uniqueMatch("findByProjectAndActiveVersion", "project", project, "isActiveVersion", true);
	}
	
	@Action(semantics=SemanticsOf.SAFE)
	@ActionLayout(contributed=Contributed.AS_ACTION)
	public BusinessCase previousVersion(final BusinessCase businessCase){
		return uniqueMatch("findByProjectAndVersion", "project", businessCase.getProject(), "businessCaseVersion", businessCase.getBusinessCaseVersion() - 1);
	}
	
	public String disablePreviousVersion(final BusinessCase businessCase){
		
		if(businessCase.getBusinessCaseVersion() > 1){
			return null;
		}
		
		return "There is no previous";
	}
	
	@Action(semantics=SemanticsOf.SAFE)
	@ActionLayout(contributed=Contributed.AS_ACTION)
	public BusinessCase nextVersion(final BusinessCase businessCase){
		return uniqueMatch("findByProjectAndVersion", "project", businessCase.getProject(), "businessCaseVersion", businessCase.getBusinessCaseVersion() + 1);
	}

	public String disableNextVersion(final BusinessCase businessCase){
		
		if(businessCase.getIsActiveVersion()){
			return "There is no next";
		}
		
		return null;
	}

}
