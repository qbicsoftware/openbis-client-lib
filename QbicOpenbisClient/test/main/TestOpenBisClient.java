package main;

import static com.google.common.truth.Truth.ASSERT;
import static org.junit.Assert.fail;

import java.io.FileReader;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestOpenBisClient {

  private static OpenBisClient openbisClient;
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Properties config = new Properties();
    config.load(new FileReader("/home/wojnar/QBiC/Portlets/GenericWorkflowInterfaceConfigurationFiles/portlets/portlets.properties"));
    openbisClient = new OpenBisClient( config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS), config.getProperty(DATASOURCE_URL));
  }

  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testOpenBisClient() {
    fail("Not yet implemented");
  }

  @Test
  public void testLoggedin() {
    fail("Not yet implemented");
  }

  @Test
  public void testLogout() {
    fail("Not yet implemented");
  }

  @Test
  public void testLogin() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetSessionToken() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetFacade() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetOpenbisInfoService() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetOpenbisInfoService() {
    fail("Not yet implemented");
  }

  @Test
  public void testEnsureLoggedIn() {
    fail("Not yet implemented");
  }

  @Test
  public void testListSpaces() {
    fail("Not yet implemented");
  }

  @Test
  public void testListProjects() {
    fail("Not yet implemented");
  }

  @Test
  public void testListExperiments() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetSamplesofExperiment() {
    
    //get sample by experiment identifier
    ASSERT.that(openbisClient.getSamplesofExperiment("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL").size()).isAtLeast(16);
    //try to get sample by experiment code fails
    // in reality it seems to throw a ch.systemsx.cisd.common.exceptions.UserFailureException
    try{
      openbisClient.getSamplesofExperiment("PTX_SOLGEL").size();
      fail("should not work with experiment code alone");
    }
    catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
    //try to get sample by experiment type fails
    try{
      openbisClient.getSamplesofExperiment("MS_INJECT").size();
      fail("should not work with experiment code alone");
    }
    catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
    //try to get sample by experiment code fails
    try{
      openbisClient.getSamplesofExperiment("20140617164748908-3036").size();
      fail("should not work with code alone");
    }
    catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
  }

  @Test
  public void testGetSamplesofSpace() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetSampleByIdentifier() {
    //it seems that this function really does not care, as long as the sample name is given
    ASSERT.that(openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL/QSDPR002A2").getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    ASSERT.that(openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A2").getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    ASSERT.that(openbisClient.getSampleByIdentifier("QSDPR002A2").getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
   
    ASSERT.that(openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/QSDPR002A2").getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    ASSERT.that(openbisClient.getSampleByIdentifier("/QSDPR/QSDPR002A2").getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    //:)
    ASSERT.that(openbisClient.getSampleByIdentifier("/I_do_not/care_what_is_upfront/QSDPR002A2").getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    //does not work by type
    try{
      openbisClient.getSampleByIdentifier("MS_INJECT").getIdentifier(); //->  java.lang.IndexOutOfBoundsException
      fail("should not work with type code alone");
    }catch (Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
      
    }
    // does not work with dataset code
    try{
      openbisClient.getSampleByIdentifier("20140617164748908-3036").getIdentifier();//->  java.lang.IndexOutOfBoundsException
      fail("should not work with code alone");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    // does not work with empty string
    try{
      openbisClient.getSampleByIdentifier("").getIdentifier();//->  ch.systemsx.cisd.common.exceptions.UserFailureException: Search pattern '' is invalid.
      fail("should not work with empty string");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    // does not work with null
    try{
      openbisClient.getSampleByIdentifier(null).getIdentifier();//->  NullPointerException
      fail("should not work with null");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(NullPointerException.class);
    }
  }

  @Test
  public void testGetSamplesOfProject() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentsForProjectProject() {
    try{
      openbisClient.getExperimentsForProject("");// -> ch.systemsx.cisd.common.exceptions.UserFailureException: Illegal empty identifier
      fail("should not work with empty string");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
   try{
     String test = null;
     openbisClient.getExperimentsForProject(test);// ch.systemsx.cisd.common.exceptions.UserFailureException: Illegal empty identifier
     fail("null not an identifer");
   }catch(Exception e){
     ASSERT.that(e).isInstanceOf(Exception.class);
   }
    ASSERT.that(openbisClient.getExperimentsForProject("/TEST28/QTEST").size()).isAtLeast(22);
    //this one really cares about proper identifier
    try{
      openbisClient.getExperimentsForProject("QTEST");//java.lang.IllegalArgumentException: Unspecified space code.
      fail(" not an identifer");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try{
      openbisClient.getExperimentsForProject("/MFT_PICHLER_MULTISCALE/QSDPR002A23");
      fail("that sample does not exist");
    }catch(Exception e){// ->ch.systemsx.cisd.common.exceptions.UserFailureException: Projects '[DEFAULT:/MFT_PICHLER_MULTISCALE/QSDPR002A23]' unknown.
      ASSERT.that(e).isInstanceOf(Exception.class);
    }     
  }

  @Test
  public void testGetExperimentsForProjectString() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentsOfProjectByCode() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetProjectExperimentMapping() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentsOfSpace() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentsOfType() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetSamplesOfType() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetProjectsOfSpace() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetProjectByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetProjectByCode() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentByCode() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentById() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetProjectOfExperimentByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataSetsOfSample() {
    //it does not really matter as long as you have the last part correctly
    ASSERT.that(openbisClient.getDataSetsOfSample("/TEST28/QTEST005HR").size()).isAtLeast(1);
    ASSERT.that(openbisClient.getDataSetsOfSample("QTEST005HR").size()).isAtLeast(1);
    ASSERT.that(openbisClient.getDataSetsOfSample("/whatever/does/not/matter/QTEST005HR").size()).isAtLeast(1);
    //does not throw any exception, just return 0
    ASSERT.that(openbisClient.getDataSetsOfSample("MS_INJECT").size()).isEqualTo(0);
    ASSERT.that(openbisClient.getDataSetsOfSample("20140617164748908-3036").size()).isEqualTo(0);
    
    try{
      openbisClient.getDataSetsOfSample("").size();
      fail("should not work with empty string");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);//->  ch.systemsx.cisd.common.exceptions.UserFailureException: Search pattern '' is invalid.
    }
    try{
      openbisClient.getDataSetsOfSample(null).size();
      fail("should not work with null");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(NullPointerException.class);
    }
  }

  @Test
  public void testGetDataSetsOfExperiment() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataSetsOfExperimentByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataSetsOfSpaceByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataSetsOfProjectByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataSetsByType() {
    fail("Not yet implemented");
  }

  @Test
  public void testListAttachmentsForSampleByIdentifier() {
    //TODO throw sensible exceptions
    try{
      openbisClient.listAttachmentsForSampleByIdentifier("");// -> ch.systemsx.cisd.common.exceptions.UserFailureException: Illegal empty identifier
      fail("should not work with empty string");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
   try{
     openbisClient.listAttachmentsForSampleByIdentifier(null);// -> java.lang.IllegalArgumentException: Identifier cannot be null
     fail("null not an identifer");
   }catch(Exception e){
     ASSERT.that(e).isInstanceOf(Exception.class);
   }
    ASSERT.that(openbisClient.listAttachmentsForSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A2").size()).isAtLeast(1);
    //this one really cares about proper identifier
    try{
      openbisClient.listAttachmentsForSampleByIdentifier("QSDPR002A2");
      fail(" not an identifer");
    }catch(Exception e){
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try{
      openbisClient.listAttachmentsForSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A23");
      fail("that sample does not exist");
    }catch(Exception e){// ->ch.systemsx.cisd.common.exceptions.UserFailureException: No sample found for id '/MFT_PICHLER_MULTISCALE/QSDPR002A23'.
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testListAttachmentsForProjectByIdentifier() {
    fail("Not yet implemented");
  }

  @Test
  public void testAddAttachmentToProject() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetSpaceMembers() {
    fail("Not yet implemented");
  }

  @Test
  public void testListPropertiesForType() {
    fail("Not yet implemented");
  }

  @Test
  public void testListVocabularyTermsForProperty() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetCVLabelForProperty() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetSampleTypeByString() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentTypeByString() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetLabelsofProperties() {
    fail("Not yet implemented");
  }

  @Test
  public void testTriggerIngestionService() {
    fail("Not yet implemented");
  }

  @Test
  public void testAddParentChildConnection() {
    fail("Not yet implemented");
  }

  @Test
  public void testAddNewInstance() {
    fail("Not yet implemented");
  }

  @Test
  public void testGenerateBarcode() {
    fail("Not yet implemented");
  }

  @Test
  public void testMapToChar() {
    fail("Not yet implemented");
  }

  @Test
  public void testChecksum() {
    fail("Not yet implemented");
  }

  @Test
  public void testOpenBIScodeToString() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDataStoreDownloadURL() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetParentMap() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetParents() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetChildrenSamples() {
    fail("Not yet implemented");
  }

  @Test
  public void testSpaceExists() {
    fail("Not yet implemented");
  }

  @Test
  public void testProjectExists() {
    fail("Not yet implemented");
  }

  @Test
  public void testExpExists() {
    fail("Not yet implemented");
  }

  @Test
  public void testSampleExists() {
    fail("Not yet implemented");
  }

  @Test
  public void testComputeProjectStatus() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetVocabCodesAndLabelsForVocab() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetVocabCodesForVocab() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetExperimentsForUser() {
    fail("Not yet implemented");
  }

  @Test
  public void testIngest() {
    fail("Not yet implemented");
  }

  @Test
  public void testListExperimentsOfProjects() {
    fail("Not yet implemented");
  }

  @Test
  public void testListDataSetsForExperiments() {
    fail("Not yet implemented");
  }

  @Test
  public void testListSamplesForProjects() {
    fail("Not yet implemented");
  }

  @Test
  public void testListDataSetsForSamples() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetUrlForDataset() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDatasetStreamString() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetDatasetStreamStringString() {
    fail("Not yet implemented");
  }

}
