package org.estatio.dom.project;

import java.util.List;

import org.apache.isis.applib.query.Query;
import org.estatio.dom.FinderInteraction;
import org.estatio.dom.FinderInteraction.FinderMethod;
import org.junit.Before;

public class BusinessCasesTest {
	
    FinderInteraction finderInteraction;

    BusinessCases businessCases;

    @Before
    public void setup() {
    	businessCases = new BusinessCases() {

            @Override
            protected <T> T firstMatch(Query<T> query) {
                finderInteraction = new FinderInteraction(query, FinderMethod.FIRST_MATCH);
                return null;
            }

            @Override
            protected List<BusinessCase> allInstances() {
                finderInteraction = new FinderInteraction(null, FinderMethod.ALL_INSTANCES);
                return null;
            }

            @Override
            protected <T> List<T> allMatches(Query<T> query) {
                finderInteraction = new FinderInteraction(query, FinderMethod.ALL_MATCHES);
                return null;
            }
        };

    }
    
    public static class FindActiveBusinessCaseOnProject extends BusinessCasesTest {
    	
//        @Rule
//        public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
//        
//        @Mock
//        private Project project;
//        private boolean isActiveVersion;
//
//        @Test
//        public void happyCase() {
//
//        	businessCases.FindActiveBusinessCaseOnProject(project, isActiveVersion);
//
//            assertThat(finderInteraction.getFinderMethod(), is(FinderMethod.ALL_MATCHES));
//            assertThat(finderInteraction.getResultType(), IsisMatchers.classEqualTo(BusinessCase.class));
//            assertThat(finderInteraction.getQueryName(), is("findByProjectAndActiveVersion"));
//            assertThat(finderInteraction.getArgumentsByParameterName().get("project"), is((Object) project));
//            assertThat(finderInteraction.getArgumentsByParameterName().size(), is(1));
//        }

    }

}
