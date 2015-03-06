package main;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import static com.google.common.truth.Truth.ASSERT;

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

  //@Test
  public void testgetSamplesofExperiment() {
    
    String expIdentifier = "QL008E3";
    //System.out.println(openbisClient.getSamplesofExp(expIdentifier).size());
    //openbisClient.getSamplesofExp(null);
    
    IOpenbisServiceFacade facade = openbisClient.getFacade();
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
  
  //@Test
  public void testgetSampleByIdentifier(){
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
    //@Test
    public void testgetDatasetsOfSample(){
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
    //@Test
    public void testlistAttachmentforSamples(){
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
    
    //@Test
    public void testOpenbisClientInitializationTime(){
      Properties config = new Properties();
      try {
        config.load(new FileReader("/home/wojnar/QBiC/Portlets/GenericWorkflowInterfaceConfigurationFiles/portlets/portlets.properties"));
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      long time = 0;
      for(int i = 0; i < 100; i++){
        openbisClient.logout();
        long start = System.nanoTime();
        openbisClient = new OpenBisClient( config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS), config.getProperty(DATASOURCE_URL));
        long stop = System.nanoTime();
        time += stop - start;
        
      }
      System.out.println(String.format("Openbisclient took %f s for 100 reloads. Makes %f s on average. ", time /1000000000.0, time / 1000000000.0 / 100));
      
    }
    
    @Test
    public void getDataSetsOfProjectByIdentifierTime(){
      long time = 0;
      int reruns = 5;
      String project = "QKSFF";
      int samples = 80;
      int ds = 40;
      for(int i = 0; i < reruns; i++){
        long start = System.nanoTime();
        openbisClient.getDataSetsOfProjectByIdentifier(project);
        long stop = System.nanoTime();
        time += stop - start;
      }//~80 samples 40 datasets
      printRunTimeInfo("getDataSetsOfProjectByIdentifier", time, reruns, project, samples, ds);
      
      time = 0;
      project = "QTEST";
      samples = 352;
      ds = 29;
      for(int i = 0; i < reruns; i++){
        long start = System.nanoTime();
        openbisClient.getDataSetsOfProjectByIdentifier(project);
        long stop = System.nanoTime();
        time += stop - start;
      }//~352 samples 29 datasets
      printRunTimeInfo("getDataSetsOfProjectByIdentifier", time, reruns, project, samples, ds);
      
      //QJFPH 160 150
      time = 0;
      project = "QJFPH";
      samples = 160;
      ds = 150;
      for(int i = 0; i < reruns; i++){
        long start = System.nanoTime();
        openbisClient.getDataSetsOfProjectByIdentifier(project);
        long stop = System.nanoTime();
        time += stop - start;
      }//~352 samples 29 datasets
      printRunTimeInfo("getDataSetsOfProjectByIdentifier", time, reruns, project, samples, ds);
      
      
    }
    public void printRunTimeInfo(String funcname, long time, int reruns, String project, int samples, int ds){
      System.out.println(String.format("%s took %f s for %s reloads for project %s with ~ %d samples and %d datasets Makes %f s on average. ",funcname, time /1000000000.0, reruns, project,samples,ds, time / 1000000000.0 / reruns));
    }
    @Test
    public void testEnsureLoggedInTime(){
      long time = 0;
      int reruns = 5;
      String project = "no project";
      int samples = -1;
      int ds = -1;
      for(int i = 0; i < reruns; i++){
        long start = System.nanoTime();
        openbisClient.ensureLoggedIn();
        long stop = System.nanoTime();
        time += stop - start;
      }//~80 samples 40 datasets
      printRunTimeInfo("ensureLoggedIn", time, reruns, project, samples, ds);
      
      
      time = 0;
      for(int i = 0; i < reruns; i++){
        long start = System.nanoTime();
        openbisClient.logout();
        openbisClient.ensureLoggedIn();
        
        long stop = System.nanoTime();
        time += stop - start;
      }//~80 samples 40 datasets
      printRunTimeInfo("ensureLoggedIn with log outs", time, reruns, project, samples, ds);
    }
  
  
  
  
  
}
