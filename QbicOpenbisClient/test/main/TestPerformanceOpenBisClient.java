package main;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

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
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Rule
  public ContiPerfRule i = new ContiPerfRule();
  
  //@Test
  public void OpenbisClientInitialization(){
      openbisClient = new OpenBisClient( config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS), config.getProperty(DATASOURCE_URL));
  }
  
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void getDataSetsOfProjectByIdentifier_QKSFF(){
  //~80 samples 40 datasets
  String project = "QKSFF";
  openbisClient.getDataSetsOfProjectByIdentifier(project);
  }
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void getDataSetsOfProjectByIdentifier_QTEST(){
  //~352 samples 29 datasets
  String project = "QTEST";
  openbisClient.getDataSetsOfProjectByIdentifier(project);
  }
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void getDataSetsOfProjectByIdentifier_QJFPH(){
  //QJFPH 160 150
  String project = "QJFPH";
  openbisClient.getDataSetsOfProjectByIdentifier(project);
  }
    
  public void printRunTimeInfo(String funcname, long time, int reruns, String project, int samples, int ds){
    System.out.println(String.format("%s took %f s for %s reloads for project %s with ~ %d samples and %d datasets Makes %f s on average. ",funcname, time /1000000000.0, reruns, project,samples,ds, time / 1000000000.0 / reruns));
  }
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void testRunTimeEnsureLoggedIn(){
      openbisClient.ensureLoggedIn();
  }
  
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void ensurLoggedInWithLogout(){
    openbisClient.logout();
    openbisClient.ensureLoggedIn();
  }
  
 @Test
  @PerfTest(invocations = 100, threads = 4)
  public void listProjects(){
    openbisClient.listProjects();
  }

 // @Test
 // @PerfTest(invocations = 100, threads = 4)
  public void getSpacesWithProjectInformation(){
      openbisClient.getFacade().getSpacesWithProjects();
  }
  
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void getSamplesofSpace(){
      openbisClient.getSamplesofSpace("TEST28");
  }
  
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void getExperimentsForProject_TEST28_QTEST(){
    openbisClient.getExperimentsForProject("/TEST28/QTEST");
  }
  @Test
  @PerfTest(invocations = 100, threads = 4)
  public void getExperimentsForProject_SANDBOX_PCT_QTEST(){
    openbisClient.getExperimentsForProject("/SANDBOX_PCT/QTEST");
  }
}
