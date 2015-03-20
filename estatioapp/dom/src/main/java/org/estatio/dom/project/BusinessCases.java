package org.estatio.dom.project;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.DomainServiceLayout.MenuBar;
import org.estatio.dom.EstatioDomainService;
import org.joda.time.LocalDate;

@DomainService(repositoryFor = BusinessCase.class, nature=NatureOfService.VIEW)
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
		// Persist it
		persist(businesscase);
		
		return businesscase;
	}

}
