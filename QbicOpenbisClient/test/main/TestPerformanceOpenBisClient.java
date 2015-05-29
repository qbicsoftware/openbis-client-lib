package main;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

public class TestPerformanceOpenBisClient {

  private static OpenBisClient openbisClient;
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  private static Properties config;
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    config = new Properties();
    config.load(new FileReader("/home/wojnar/QBiC/Portlets/GenericWorkflowInterfaceConfigurationFiles/portlets/portlets.properties"));
    openbisClient = new OpenBisClient( config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS), config.getProperty(DATASOURCE_URL));
    openbisClient.login();
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  List<Project> projects;
  
  @Before
  public void setUp() throws Exception {
    openbisClient.login();
    projects  = openbisClient.listProjects();
  }

  @After
  public void tearDown() throws Exception {}

  @Rule
  public ContiPerfRule i = new ContiPerfRule();
  
  //@Test
  public void OpenbisClientInitialization(){
      openbisClient = new OpenBisClient( config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS), config.getProperty(DATASOURCE_URL));
  }
    
  public void printRunTimeInfo(String funcname, long time, int reruns, String project, int samples, int ds){
    System.out.println(String.format("%s took %f s for %s reloads for project %s with ~ %d samples and %d datasets Makes %f s on average. ",funcname, time /1000000000.0, reruns, project,samples,ds, time / 1000000000.0 / reruns));
  }
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void testRunTimeEnsureLoggedIn(){
      openbisClient.ensureLoggedIn();
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void ensurLoggedInWithLogout(){
    openbisClient.logout();
    openbisClient.ensureLoggedIn();
  }
  
 @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listProjects(){
    openbisClient.listProjects();
  }

  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getSpacesWithProjectInformation(){
      openbisClient.getFacade().getSpacesWithProjects();
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getSamplesofSpace(){
      openbisClient.getSamplesofSpace("TEST28");
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getExperimentsForProject_TEST28_QTEST(){
    openbisClient.getExperimentsForProject("/TEST28/QTEST");
  }
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getExperimentsForProject_IVAC_ALL_QL011(){
    openbisClient.getExperimentsForProject("/IVAC_ALL/QA011");
  }
  
//~352 samples 29 datasets  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProjectByIdentifier_TEST28_QTEST(){
    openbisClient.getDataSetsOfProjectByIdentifier("/TEST28/QTEST");
  }
  
//~352 samples 29 datasets  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProjectByIdentifierWithSearchCriteria_TEST28_QTEST(){
    openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/TEST28/QTEST");
  }
  
  
//~352 samples 29 datasets  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProject_with_getExperimentsOfProjectByIdentifier_TEST28_QTEST(){
    ArrayList<String> ids = new ArrayList<String>();
    for (Experiment e : openbisClient.getExperimentsOfProjectByIdentifier("/TEST28/QTEST"))
      ids.add(e.getIdentifier());
    openbisClient.listDataSetsForExperiments(ids);
  }
  
//~80 samples 40 datasets 
  @Test
  @PerfTest(invocations = 25, threads = 4)

  public void getDataSetsOfProjectByIdentifier_EXT_SCHENKE_LAYLAND_QKSFF(){
    openbisClient.getDataSetsOfProjectByIdentifier("/EXT_SCHENKE_LAYLAND/QKSFF");
  }
  
//~80 samples 40 datasets 
  @Test
  @PerfTest(invocations = 25, threads = 4)

  public void getDataSetsOfProjectByIdentifierWithSearchCriteria_EXT_SCHENKE_LAYLAND_QKSFF(){
    openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/EXT_SCHENKE_LAYLAND/QKSFF");
  } 
  
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProject_with_getExperimentsOfProjectByIdentifier_EXT_SCHENKE_LAYLAND_QKSFF(){
    ArrayList<String> ids = new ArrayList<String>();
    for (Experiment e : openbisClient.getExperimentsOfProjectByIdentifier("/EXT_SCHENKE_LAYLAND/QKSFF"))
      ids.add(e.getIdentifier());
    openbisClient.listDataSetsForExperiments(ids);
  }
  //0 datasets  
  @Test
  @PerfTest(invocations = 25, threads = 4)

  public void getDataSetsOfProjectByIdentifier_IVAC_ALL_QL011(){
    openbisClient.getDataSetsOfProjectByIdentifier("/IVAC_ALL/QA011");
  }
  
 
  //0 datasets  
  @Test
  @PerfTest(invocations = 25, threads = 4)

  public void getDataSetsOfProjectByIdentifierWithSearchCriteria_IVAC_ALL_QL011(){
    openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/IVAC_ALL/QA011");
  }
  
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProject_with_getExperimentsOfProjectByIdentifier_IVAC_ALL_QL011(){
    ArrayList<String> ids = new ArrayList<String>();
    for (Experiment e : openbisClient.getExperimentsOfProjectByIdentifier("/IVAC_ALL/QA011"))
      ids.add(e.getIdentifier());
    openbisClient.listDataSetsForExperiments(ids);
  }
//QJFPH 160 150
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProjectByIdentifier_MFT_FRICK_RNAPHOSPHO_TLR5_QJFPH(){
  openbisClient.getDataSetsOfProjectByIdentifier("/MFT_FRICK_RNAPHOSPHO_TLR5/QJFPH");
  }

//QJFPH 160 150
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProjectByIdentifierWithSearchCriteria_MFT_FRICK_RNAPHOSPHO_TLR5_QJFPH(){
  openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/MFT_FRICK_RNAPHOSPHO_TLR5/QJFPH");
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProject_with_getExperimentsOfProjectByIdentifier_MFT_FRICK_RNAPHOSPHO_TLR5_QJFPH(){
    ArrayList<String> ids = new ArrayList<String>();
    for (Experiment e : openbisClient.getExperimentsOfProjectByIdentifier("/MFT_FRICK_RNAPHOSPHO_TLR5/QJFPH"))
      ids.add(e.getIdentifier());
    openbisClient.listDataSetsForExperiments(ids);
  }

  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProjects(){
    int size = openbisClient.getDataSetsOfProjects(openbisClient.listProjects()).size();
    System.out.println(size);
  }
  
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getDataSetsOfProject2(){
    int size = openbisClient.getDataSetsOfProjects2(openbisClient.listProjects()).size();
    System.out.println(size);
  }
  
  
  @Test
  @PerfTest(invocations = 4, threads = 4)
  public void computeProjectStatus(){
    for(Project project: projects ){
      float status = openbisClient.computeProjectStatus(project);
      System.out.println(status);
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject(){
    for(Project project: projects ){
      openbisClient.getExperimentsForProject(project);
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject2(){
    for(Project project: projects ){
      openbisClient.getExperimentsForProject2(project);
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject3(){
    for(Project project: projects ){
      openbisClient.getExperimentsForProject3(project);
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject_QMARI(){
      openbisClient.getExperimentsForProject("/ABI_SYSBIO/QMARI");
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject2_QMARI(){
      System.out.println(openbisClient.getExperimentsForProject2("/ABI_SYSBIO/QMARI").size());
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject3_QMARI(){
      openbisClient.getExperimentsForProject3("QMARI");
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void listExperimentsForProject4_QMARI(){
      openbisClient.getExperimentsOfProjectByCode("QMARI");
  }
  

}
