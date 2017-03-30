package life.qbic.openbis.openbisclient;

import static com.google.common.truth.Truth.ASSERT;


import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.*;


import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

public class TestPerformanceOpenBisClient {

  private static OpenBisClient openbisClient;
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  private static Properties config;
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    config = new Properties();
    List<String> configs =
            new ArrayList<String>(Arrays.asList("/Users/frieda/Desktop/testing/portlet.properties",
                    "/home/rayslife/portlet.properties", "/usr/local/share/guse/portlets.properties",
                    "/home/wojnar/QBiC/liferay-portal-6.2-ce-ga4/mainportlet-ext.properties",
                    "/etc/portal_testing/portlet_testopenbis.properties"));
    for (String s : configs) {
      File f = new File(s);
      if (f.exists())
        config.load(new FileReader(s));
    }
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
    List<DataSet> datasets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/EXT_SCHENKE_LAYLAND/QKSFF");
    if(!datasets.isEmpty()){
      System.out.println(datasets.get(0).getDataSetTypeCode());
      System.out.println(datasets.get(0).getExternalDataSetCode());
      System.out.println(datasets.get(0).getExternalDataSetLink());
      System.out.println(datasets.get(0).getExternalDataManagementSystem());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getCode());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getLabel());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getDatabaseInstance().getIdentifier());
    } 
  } 
  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getClientDataSetsOfProjectByIdentifierWithSearchCriteria_EXT_SCHENKE_LAYLAND_QKSFF(){
    List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/EXT_SCHENKE_LAYLAND/QKSFF");
    if(!datasets.isEmpty()){
      System.out.println(datasets.get(0).getDataSetTypeCode());
      System.out.println(datasets.get(0).getExternalDataSetCode());
      System.out.println(datasets.get(0));
      System.out.println(datasets.get(0).tryGetInternalPathInDataStore());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getCode());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getLabel());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getDatabaseInstance().getIdentifier());
    } 
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
    List<DataSet> datasets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/IVAC_ALL/QA011");
    if(!datasets.isEmpty()){
      System.out.println(datasets.get(0).getDataSetTypeCode());
      System.out.println(datasets.get(0).getExternalDataSetCode());
      System.out.println(datasets.get(0).getExternalDataSetLink());
      System.out.println(datasets.get(0).getExternalDataManagementSystem());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getCode());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getLabel());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getDatabaseInstance().getIdentifier());
    } 
  }
 
  //0 datasets  
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getClientDataSetsOfProjectByIdentifierWithSearchCriteria_IVAC_ALL_QL011(){
    List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets = openbisClient.getClientDatasetsOfProjectByIdentifierWithSearchCriteria("/IVAC_ALL/QA011");
    if(!datasets.isEmpty()){
      System.out.println(datasets.get(0).getDataSetTypeCode());
      System.out.println(datasets.get(0).getExternalDataSetCode());
      System.out.println(datasets.get(0).getExternalDataSetLink());
      System.out.println(datasets.get(0).getExternalDataManagementSystem());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getCode());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getLabel());
      //System.out.println(datasets.get(0).getExternalDataManagementSystem().getDatabaseInstance().getIdentifier());
    } 
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
  List<DataSet> datasets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/MFT_FRICK_RNAPHOSPHO_TLR5/QJFPH");
  if(!datasets.isEmpty()){
    System.out.println(datasets.get(0).getDataSetTypeCode());
    System.out.println(datasets.get(0).getExternalDataSetCode());
    System.out.println(datasets.get(0).getExternalDataSetLink());
    System.out.println(datasets.get(0).getExternalDataManagementSystem());
    //System.out.println(datasets.get(0).getExternalDataManagementSystem().getCode());
    //System.out.println(datasets.get(0).getExternalDataManagementSystem().getLabel());
    //System.out.println(datasets.get(0).getExternalDataManagementSystem().getDatabaseInstance().getIdentifier());
  }
  
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
  @Test
  @PerfTest(invocations = 25, threads = 4)
  public void getSpaceMembers_QMARI(){
      openbisClient.getSpaceMembers("ABI_SYSBIO");
  }
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void getDataset1(){
    ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataset = openbisClient.getFacade().getDataSet("20150224174804803-6961");
  }
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void getDataset2(){
    ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataset = openbisClient.getFacade().getDataSet("20150317114547138-9319"); 
  }
  
  
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void listfiles1(){
    ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataset = openbisClient.getFacade().getDataSet("20150224174804803-6961");
    dataset.listFiles("original", true);  
  }
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void listfiles2(){
    ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataset = openbisClient.getFacade().getDataSet("20150317114547138-9319");
    dataset.listFiles("original", true);  
  }
  
  /**
   * samples: 25, 4 threads
max:     8118
average: 6795.0
median:  6623

   */
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void getExperimentById(){
    openbisClient.getExperimentById("/ABI_SYSBIO/QMARI/QMARIE3");
  }
 
  @Test
  @PerfTest(invocations = 1, threads = 1 ) 
  public void getExperimentById2(){
    List<Experiment> experiments = openbisClient.getExperimentById2("/ABI_SYSBIO/QMARI/QMARIE3");
    for(Experiment exp : experiments){
      System.out.println(exp);
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void get_projects_with_data(){
    
    List<String> codes = new ArrayList<String>();
    List<Project> projects = openbisClient.listProjects();
    for(Project project: projects) {
      codes.add(project.getCode());
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("codes", codes);
    QueryTableModel res = openbisClient.getAggregationService("get-projects-with-data", params);
    for (Serializable[] ss : res.getRows()) {
      System.out.println("next");
      for(Object x : ss)
        System.out.println(x.toString());
    }
  }
  
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void getSampleTypes(){
    Map<String, SampleType> types = openbisClient.getSampleTypes();
  }
  @Test
  @PerfTest(invocations = 25, threads = 4) 
  public void getSampleTypeByString(){
    openbisClient.getSampleTypeByString("Q_TEST_SAMPLE");
  }
  
  @Test
  @PerfTest(invocations = 1, threads = 1) 
  public void sampleParentsAndChildren(){
    List<Sample> samples  = openbisClient.getSamplesofSpace("ABI_SYSBIO");
    System.out.println("ABI_SYSBIO has + " + samples.size() + " number of samples.");
    for(Sample sample : samples){
      int parents = 0;
      int children = 0;
      try{
        //parents = openbisClient.getParents(sample.getCode()).size();//sample.getParents().size();
        children = openbisClient.getChildrenSamples(sample).size();//sample.getChildren().size();
      }catch(Exception e){
        //shut up
      }
      System.out.println(sample.getCode() + " has " + parents + " parents and " + children + " children.");
    }
    
  }
  
}
