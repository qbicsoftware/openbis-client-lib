package life.qbic.openbis.openbisclient;

import static com.google.common.truth.Truth.ASSERT;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

import java.util.Properties;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;

public class TestOpenBisClient {

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
            "/home/rayslife/portlet.properties", "/usr/local/share/guse/portlets.properties", "/home/wojnar/QBiC/liferay-portal-6.2-ce-ga4/mainportlet-ext.properties"));
    for (String s : configs) {
      File f = new File(s);
      if (f.exists())
        config.load(new FileReader(s));
    }
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
  }

  @After
  public void tearDown() throws Exception {
    openbisClient.logout();
    openbisClient = null;
  }

  @Test
  public void testOpenBisClient() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    ASSERT.that(openbisClient.getFacade() != null);
    openbisClient =
        new OpenBisClient("user", config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    ASSERT.that(!openbisClient.loggedin());
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), "wrongpassword",
            config.getProperty(DATASOURCE_URL));
    ASSERT.that(!openbisClient.loggedin());
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            "wrongurl");
    ASSERT.that(!openbisClient.loggedin());
  }

  @Test
  public void testLoggedin() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    ASSERT.that(openbisClient.loggedin());
    openbisClient =
        new OpenBisClient("someuser", config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    ASSERT.that(!openbisClient.loggedin());
  }

  @Test
  public void testLogout() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    ASSERT.that(openbisClient.loggedin());
    openbisClient.logout();
    ASSERT.that(!openbisClient.loggedin());
  }

  @Test
  public void testLogin() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    ASSERT.that(openbisClient.loggedin());
    openbisClient.logout();
    openbisClient.login();
    ASSERT.that(openbisClient.loggedin());
  }

  @Test
  public void testGetSessionToken() {
    openbisClient.ensureLoggedIn();
    ASSERT.that(openbisClient.getSessionToken().startsWith("admin"));
    openbisClient.logout();
    try {
      openbisClient.getSessionToken();
      fail("should not work when logged out");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
  }

  @Test
  public void testGetFacade() {
    openbisClient.ensureLoggedIn();
    ASSERT.that(openbisClient.getFacade()).isInstanceOf(IOpenbisServiceFacade.class);
    openbisClient.logout();
    ASSERT.that(openbisClient.getFacade() == null);
  }

  @Test
  public void testGetOpenbisInfoService() {
    // test double login
    openbisClient.ensureLoggedIn();
    ASSERT.that(openbisClient.getOpenbisInfoService()).isInstanceOf(
        IGeneralInformationService.class);
    openbisClient.ensureLoggedIn();
    ASSERT.that(openbisClient.getOpenbisInfoService()).isInstanceOf(
        IGeneralInformationService.class);
    openbisClient.logout();
    ASSERT.that(openbisClient.getOpenbisInfoService() == null);
  }

  @Test
  public void testSetOpenbisInfoService() {
    openbisClient.ensureLoggedIn();
    IGeneralInformationService old = openbisClient.getOpenbisInfoService();
    openbisClient.setOpenbisInfoService(null);
    ASSERT.that(openbisClient.getOpenbisInfoService() == null);
    openbisClient.setOpenbisInfoService(old);
    ASSERT.that(openbisClient.getOpenbisInfoService().equals(old));
  }

  @Test
  public void testEnsureLoggedIn() {
    openbisClient.logout();
    try {
      openbisClient.getSessionToken();
      fail("should not work when logged out");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
    // logout --> ensure
    openbisClient.ensureLoggedIn();
    ASSERT.that(openbisClient.getSessionToken().startsWith("admin"));
    // login --> ensure
    openbisClient.login();
    openbisClient.ensureLoggedIn();
    ASSERT.that(openbisClient.getSessionToken().startsWith("admin"));
  }

  @Test
  public void testListSpaces() {
    ASSERT.that(openbisClient.listSpaces().size()).isAtLeast(49);// 49 at point of test
  }

  @Test
  public void testListProjects() {
    ASSERT.that(openbisClient.listProjects().size()).isAtLeast(78);// 78 at point of test
    ASSERT.that(openbisClient.listProjects().get(0)).isInstanceOf(Project.class);
  }

  @Test
  public void testListExperiments() {
    List<Experiment> exps = openbisClient.listExperiments();
    ASSERT.that(exps.size()).isAtLeast(10);
    ASSERT.that(exps.get(0)).isInstanceOf(Experiment.class);
  }

  @Test
  public void testGetSamplesofExperiment() {

    // get sample by experiment identifier
    ASSERT.that(
        openbisClient.getSamplesofExperiment("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL").size())
        .isAtLeast(16);
    // try to get sample by experiment code fails
    // in reality it seems to throw a ch.systemsx.cisd.common.exceptions.UserFailureException
    try {
      openbisClient.getSamplesofExperiment("PTX_SOLGEL").size();
      fail("should not work with experiment code alone");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
    // try to get sample by experiment type fails
    try {
      openbisClient.getSamplesofExperiment("MS_INJECT").size();
      fail("should not work with experiment code alone");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
    // try to get sample by experiment code fails
    try {
      openbisClient.getSamplesofExperiment("20140617164748908-3036").size();
      fail("should not work with code alone");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);//
    }
  }

  @Test
  public void testGetSamplesofSpace() {
    List<Sample> samps = openbisClient.getSamplesofSpace("MFT_PICHLER_MULTISCALE");
    ASSERT.that(samps.size()).isAtLeast(12);
    ASSERT.that(samps.get(0)).isInstanceOf(Sample.class);
  }

  @Test
  public void testGetSampleByIdentifier() {
    // it seems that this function really does not care, as long as the sample name is given
    ASSERT.that(
        openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL/QSDPR002A2")
            .getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    ASSERT.that(
        openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A2").getIdentifier())
        .isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    ASSERT.that(openbisClient.getSampleByIdentifier("QSDPR002A2").getIdentifier()).isEqualTo(
        "/MFT_PICHLER_MULTISCALE/QSDPR002A2");

    ASSERT.that(
        openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/QSDPR002A2")
            .getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    ASSERT.that(openbisClient.getSampleByIdentifier("/QSDPR/QSDPR002A2").getIdentifier())
        .isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    // :)
    ASSERT.that(
        openbisClient.getSampleByIdentifier("/I_do_not/care_what_is_upfront/QSDPR002A2")
            .getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
    // does not work by type
    try {
      openbisClient.getSampleByIdentifier("MS_INJECT").getIdentifier(); // ->
                                                                        // java.lang.IndexOutOfBoundsException
      fail("should not work with type code alone");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);

    }
    // does not work with dataset code
    try {
      openbisClient.getSampleByIdentifier("20140617164748908-3036").getIdentifier();// ->
                                                                                    // java.lang.IndexOutOfBoundsException
      fail("should not work with code alone");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    // does not work with empty string
    try {
      openbisClient.getSampleByIdentifier("").getIdentifier();// ->
                                                              // ch.systemsx.cisd.common.exceptions.UserFailureException:
                                                              // Search pattern '' is invalid.
      fail("should not work with empty string");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    // does not work with null
    try {
      openbisClient.getSampleByIdentifier(null).getIdentifier();// -> NullPointerException
      fail("should not work with null");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(NullPointerException.class);
    }
  }

  @Test
  public void testGetSamplesOfProject() {
    List<Sample> samps = openbisClient.getSamplesOfProject("/MFT_PICHLER_MULTISCALE/QSDPR");
    ASSERT.that(samps.size()).isAtLeast(12);
    ASSERT.that(samps.get(0)).isInstanceOf(Sample.class);
    ASSERT.that(samps.equals(openbisClient.getSamplesOfProject("QSDPR")));// should work for id and
                                                                          // code
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifier() {
    List<Experiment> exps =
        openbisClient.getExperimentsOfProjectByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR");
    ASSERT.that(exps.size()).isAtLeast(1);
    ASSERT.that(exps.get(0)).isInstanceOf(Experiment.class);
    try {
      openbisClient.getExperimentsOfProjectByIdentifier("QSDPR");
      fail("should not work with code alone");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetExperimentsForProjectString() {
    try {
      openbisClient.getExperimentsForProject("");// ->
                                                 // ch.systemsx.cisd.common.exceptions.UserFailureException:
                                                 // Illegal empty identifier
      fail("should not work with empty string");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      String test = null;
      openbisClient.getExperimentsForProject(test);// ch.systemsx.cisd.common.exceptions.UserFailureException:
                                                   // Illegal empty identifier
      fail("null not an identifer");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    ASSERT.that(openbisClient.getExperimentsForProject("/TEST28/QTEST").size()).isAtLeast(22);
    // this one really cares about proper identifier
    try {
      openbisClient.getExperimentsForProject("QTEST");// java.lang.IllegalArgumentException:
                                                      // Unspecified space code.
      fail(" not an identifer");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      openbisClient.getExperimentsForProject("/MFT_PICHLER_MULTISCALE/QSDPR002A23");
      fail("that sample does not exist");
    } catch (Exception e) {// ->ch.systemsx.cisd.common.exceptions.UserFailureException: Projects
                           // '[DEFAULT:/MFT_PICHLER_MULTISCALE/QSDPR002A23]' unknown.
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetExperimentsForProjectProject() {
    Project p = openbisClient.getProjectByIdentifier("/TEST28/QTEST");
    List<Experiment> exps = openbisClient.getExperimentsForProject(p);
    ASSERT.that(exps.size()).isAtLeast(22);
    ASSERT.that(openbisClient.getExperimentsForProject("/TEST28/QTEST").equals(exps));
  }

  @Test
  public void testGetExperimentsOfProjectByCode() {
    ASSERT.that(openbisClient.getExperimentsOfProjectByCode("QTEST").size()).isAtLeast(22);
    try {
      System.out.println(openbisClient.getExperimentsOfProjectByCode("/TEST28/QTEST"));
      fail("not a code");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetProjectExperimentMapping() {
    Map<String, List<Experiment>> map = openbisClient.getProjectExperimentMapping("IVAC_ALL");
    ASSERT.that(map.keySet() != null);
    for (String p : map.keySet()) {
      ASSERT.that(openbisClient.getExperimentsOfProjectByIdentifier(p).equals(map.get(p)));
    }
  }

  @Test
  public void testGetExperimentsOfSpace() {
    List<Experiment> exps = openbisClient.getExperimentsOfSpace("MFT_PICHLER_MULTISCALE");
    ASSERT.that(exps.size()).isAtLeast(8);
    ASSERT.that(exps.get(0)).isInstanceOf(Experiment.class);
  }

  @Test
  public void testGetExperimentsOfType() {
    List<Experiment> exps = openbisClient.getExperimentsOfType("MS_INJECT");
    ASSERT.that(exps.size()).isAtLeast(1);
    ASSERT.that(exps.get(0)).isInstanceOf(Experiment.class);
    for (Experiment e : exps) {
      ASSERT.that(e.getExperimentTypeCode().equals("MS_INJECT"));
    }
  }

  @Test
  public void testGetSamplesOfType() {
    List<Sample> samps = openbisClient.getSamplesOfType("Q_MS_RUN");
    ASSERT.that(samps.size()).isAtLeast(1);
    ASSERT.that(samps.get(0)).isInstanceOf(Sample.class);
    for (Sample e : samps) {
      ASSERT.that(e.getSampleTypeCode().equals("Q_MS_RUN"));
    }
  }

  @Test
  public void testGetProjectsOfSpace() {
    List<Project> projs = openbisClient.getProjectsOfSpace("IVAC_ALL");
    ASSERT.that(projs.size()).isAtLeast(13);
    ASSERT.that(projs.get(0)).isInstanceOf(Project.class);
  }

  @Test
  public void testGetProjectByIdentifier() {
    ASSERT.that(openbisClient.getProjectByIdentifier("/IVAC_ALL/QA001"))
        .isInstanceOf(Project.class);
    
    try {
      System.out.println("testGetProjectByIdentifier"
          + openbisClient.getProjectByIdentifier("QA001"));
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    
    try {
      System.out.println("testGetProjectByIdentifier"
          + openbisClient.getProjectByIdentifier("/IVAC_ALL/QL001"));
      fail("does not exist");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    
    
  }

  @Test
  public void testGetProjectByCode() {
    ASSERT.that(openbisClient.getProjectByCode("QA001")).isInstanceOf(Project.class);
    try {
      System.out
          .println("testGetProjectByCode" + openbisClient.getProjectByCode("/IVAC_ALL/QA001"));
      fail("not a code");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetExperimentByCode() {
    ASSERT.that(openbisClient.getExperimentByCode("PTX_SOLGEL")).isInstanceOf(Experiment.class);
    try {
      System.out.println("testGetExperimentByCode"
          + openbisClient.getExperimentByCode("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL"));
      fail("not a code");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetExperimentById() {
    ASSERT.that(openbisClient.getExperimentById("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL"))
        .isInstanceOf(Experiment.class);
    try {
      System.out.println("testGetExperimentById" + openbisClient.getExperimentById("PTX_SOLGEL"));
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetProjectOfExperimentByIdentifier() {
    Project p =
        openbisClient
            .getProjectOfExperimentByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL");
    ASSERT.that(p).isInstanceOf(Project.class);
    ASSERT.that(p.getCode().equals("QSDPR"));
    try {
      System.out.println("testGetProjectOfExperimentByIdentifier"
          + openbisClient.getExperimentById("PTX_SOLGEL"));
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifier() {
    List<DataSet> dsets = openbisClient.getDataSetsOfSampleByIdentifier("/ABI_SYSBIO/QMARI117AV");
    ASSERT.that(dsets.size()).isEqualTo(6);
    try {
      System.out.println("testGetDataSetsOfSampleByIdentifier"
          + openbisClient.getDataSetsOfSampleByIdentifier("QMARI117AV"));
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetDataSetsOfSample() {
    // it does not really matter as long as you have the last part correctly
    ASSERT.that(openbisClient.getDataSetsOfSample("/TEST28/QTEST005HR").size()).isAtLeast(1);
    ASSERT.that(openbisClient.getDataSetsOfSample("QTEST005HR").size()).isAtLeast(1);
    ASSERT.that(openbisClient.getDataSetsOfSample("/whatever/does/not/matter/QTEST005HR").size())
        .isAtLeast(1);
    // does not throw any exception, just return 0
    ASSERT.that(openbisClient.getDataSetsOfSample("MS_INJECT").size()).isEqualTo(0);
    ASSERT.that(openbisClient.getDataSetsOfSample("20140617164748908-3036").size()).isEqualTo(0);

    try {
      openbisClient.getDataSetsOfSample("").size();
      fail("should not work with empty string");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);// ->
                                                   // ch.systemsx.cisd.common.exceptions.UserFailureException:
                                                   // Search pattern '' is invalid.
    }
    try {
      openbisClient.getDataSetsOfSample(null).size();
      fail("should not work with null");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(NullPointerException.class);
    }
  }

  @Test
  public void testGetDataSetsOfExperiment() {
    Experiment ex = openbisClient.getExperimentByCode("HPTI");
    List<DataSet> dsets = openbisClient.getDataSetsOfExperiment(ex.getPermId());
    ASSERT.that(dsets.size()).isEqualTo(43);
    try {
      System.out.println("testGetDataSetsOfExperiment"
          + openbisClient.getDataSetsOfExperiment("HPTI"));
      fail("not perm id but code");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      System.out.println("testGetDataSetsOfExperiment"
          + openbisClient.getExperimentByCode("/QBIC/QHPTI/HPTI"));
      fail("not perm id but id");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetDataSetsOfExperimentByIdentifier() {
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets =
        openbisClient.getDataSetsOfExperimentByIdentifier("/QBIC/QHPTI/HPTI");
    ASSERT.that(dsets.size()).isEqualTo(43);
    try {
      System.out.println("testGetDataSetsOfExperimentByIdentifier"
          + openbisClient.getDataSetsOfExperimentByIdentifier("HPTI"));
      fail("not an identifier"); //TODO seems to return the right datasets even if a code is used. either change function name or do something about it
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  // testGetDataSetsOfExperimentByIdentifier[DataSet[20140226191333949-1374,/QBIC/QHPTI/HPTI,/QBIC/QHPTI016CA,CEL,{}],
  // DataSet[20140226191340992-1375,/QBIC/QHPTI/HPTI,/QBIC/QHPTI005CT,CEL,{}],
  // DataSet[20140226191357420-1377,/QBIC/QHPTI/HPTI,/QBIC/QHPTI011C4,CEL,{}],
  // DataSet[20140226191404414-1378,/QBIC/QHPTI/HPTI,/QBIC/QHPTI017CI,CEL,{}],
  // DataSet[20140226191411466-1379,/QBIC/QHPTI/HPTI,/QBIC/QHPTI018CQ,CEL,{}],
  // DataSet[20140226191418161-1380,/QBIC/QHPTI/HPTI,/QBIC/QHPTI014CS,CEL,{}],
  // DataSet[20140226191426681-1381,/QBIC/QHPTI/HPTI,/QBIC/QHPTI019C0,CEL,{}],
  // DataSet[20140226191434066-1382,/QBIC/QHPTI/HPTI,/QBIC/QHPTI007CB,CEL,{}],
  // DataSet[20140226191441617-1383,/QBIC/QHPTI/HPTI,/QBIC/QHPTI034TP,CEL,{}],
  // DataSet[20140226191450535-1384,/QBIC/QHPTI/HPTI,/QBIC/QHPTI036T7,CEL,{}],
  // DataSet[20140226191459114-1385,/QBIC/QHPTI/HPTI,/QBIC/QHPTI030PP,CEL,{}],
  // DataSet[20140226191507803-1386,/QBIC/QHPTI/HPTI,/QBIC/QHPTI024PG,CEL,{}],
  // DataSet[20140226191515532-1387,/QBIC/QHPTI/HPTI,/QBIC/QHPTI027P6,CEL,{}],
  // DataSet[20140226191525844-1388,/QBIC/QHPTI/HPTI,/QBIC/QHPTI022CJ,CEL,{}],
  // DataSet[20140226191934851-1389,/QBIC/QHPTI/HPTI,/QBIC/QHPTI039TV,CEL,{}],
  // DataSet[20140226192020080-1390,/QBIC/QHPTI/HPTI,/QBIC/QHPTI042TG,CEL,{}],
  // DataSet[20140226192028926-1391,/QBIC/QHPTI/HPTI,/QBIC/QHPTI043TO,CEL,{}],
  // DataSet[20140226192037373-1392,/QBIC/QHPTI/HPTI,/QBIC/QHPTI040T0,CEL,{}],
  // DataSet[20140226192044952-1393,/QBIC/QHPTI/HPTI,/QBIC/QHPTI041T8,CEL,{}],
  // DataSet[20140226192157487-1394,/QBIC/QHPTI/HPTI,/QBIC/QHPTI031PX,CEL,{}],
  // DataSet[20140226192204809-1395,/QBIC/QHPTI/HPTI,/QBIC/QHPTI032P7,CEL,{}],
  // DataSet[20140226192216725-1396,/QBIC/QHPTI/HPTI,/QBIC/QHPTI033TH,CEL,{}],
  // DataSet[20131202021322407-907,/QBIC/QHPTI/HPTI,/QBIC/QHPTI004CL,PDF,{}],
  // DataSet[20140226192230877-1398,/QBIC/QHPTI/HPTI,/QBIC/QHPTI037TF,CEL,{}],
  // DataSet[20140226192238831-1399,/QBIC/QHPTI/HPTI,/QBIC/QHPTI038TN,CEL,{}],
  // DataSet[20140226192246158-1400,/QBIC/QHPTI/HPTI,/QBIC/QHPTI021CB,CEL,{}],
  // DataSet[20140226192253581-1401,/QBIC/QHPTI/HPTI,/QBIC/QHPTI023CR,CEL,{}],
  // DataSet[20140226192301114-1402,/QBIC/QHPTI/HPTI,/QBIC/QHPTI025PO,CEL,{}],
  // DataSet[20140226192309806-1403,/QBIC/QHPTI/HPTI,/QBIC/QHPTI026PW,CEL,{}],
  // DataSet[20140226192317480-1404,/QBIC/QHPTI/HPTI,/QBIC/QHPTI028PE,CEL,{}],
  // DataSet[20140226192324474-1405,/QBIC/QHPTI/HPTI,/QBIC/QHPTI029PM,CEL,{}],
  // DataSet[20130904105623877-566,/QBIC/QHPTI/HPTI,/QBIC/QHPTI002TU,PDF,{}],
  // DataSet[20130904105639119-568,/QBIC/QHPTI/HPTI,/QBIC/QHPTI001TM,PDF,{}],
  // DataSet[20140226192223870-1397,/QBIC/QHPTI/HPTI,/QBIC/QHPTI035TX,CEL,{}],
  // DataSet[20140226191227525-1365,/QBIC/QHPTI/HPTI,/QBIC/QHPTI013CK,CEL,{}],
  // DataSet[20140226191235791-1366,/QBIC/QHPTI/HPTI,/QBIC/QHPTI004CL,CEL,{}],
  // DataSet[20140226191243383-1367,/QBIC/QHPTI/HPTI,/QBIC/QHPTI015C2,CEL,{}],
  // DataSet[20140226191250200-1368,/QBIC/QHPTI/HPTI,/QBIC/QHPTI012CC,CEL,{}],
  // DataSet[20140226191257669-1369,/QBIC/QHPTI/HPTI,/QBIC/QHPTI020C3,CEL,{}],
  // DataSet[20140226191304706-1370,/QBIC/QHPTI/HPTI,/QBIC/QHPTI010CU,CEL,{}],
  // DataSet[20140226191311589-1371,/QBIC/QHPTI/HPTI,/QBIC/QHPTI009CR,CEL,{}],
  // DataSet[20140226191318642-1372,/QBIC/QHPTI/HPTI,/QBIC/QHPTI008CJ,CEL,{}],
  // DataSet[20140226191326878-1373,/QBIC/QHPTI/HPTI,/QBIC/QHPTI006C3,CEL,{}]]

  @Test
  public void testGetDataSetsOfSpaceByIdentifier() {
    List<DataSet> dsets = openbisClient.getDataSetsOfSpaceByIdentifier("QBIC");
    ASSERT.that(dsets.size()).isAtLeast(43);
    ASSERT.that(dsets.get(0)).isInstanceOf(DataSet.class);
  }

  @Test
  public void testGetDataSetsOfProjectByIdentifier_project_with_datasets() {
    List<DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifier("/QBIC/QHPTI");
    ASSERT.that(dsets.size()).isEqualTo(43);
    try {
      openbisClient.getDataSetsOfProjectByIdentifier("QHPTI");
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }
  @Test
  public void testGetDataSetsOfProjectByIdentifier_project_with_no_experiments() {
    List<DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifier("/ECKH/ECKH1");
    ASSERT.that(dsets.size()).isEqualTo(0);
    try {
      openbisClient.getDataSetsOfProjectByIdentifier("ECKH1");
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }
 
  @Test
  public void testGetDataSetsOfProjectByIdentifierWithSearchCriteria_project_with_datasets() {
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/QBIC/QHPTI");
    ASSERT.that(dsets.size()).isEqualTo(43);
    try {
      openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("QHPTI");
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }
  @Test
  public void testGetDataSetsOfProjectByIdentifierWithSearchCriteria_project_with_no_experiments() {
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/ECKH/ECKH1");
    ASSERT.that(dsets.size()).isEqualTo(0);
    try {
      openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("ECKH1");
      fail("not an identifier");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }
  
  

  @Test
  public void testGetDataSetsByType() {
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets =
        openbisClient.getDataSetsByType("PDF");
    ASSERT.that(dsets.size()).isAtLeast(3);
    ASSERT.that(dsets.get(0)).isInstanceOf(
        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.class);
    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet s : dsets) {
      ASSERT.that(s.getDataSetTypeCode().equals("PDF"));
    }
  }

  @Test
  public void testListAttachmentsForSampleByIdentifier() {
    // TODO throw sensible exceptions
    try {
      openbisClient.listAttachmentsForSampleByIdentifier("");// ->
                                                             // ch.systemsx.cisd.common.exceptions.UserFailureException:
                                                             // Illegal empty identifier
      fail("should not work with empty string");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      openbisClient.listAttachmentsForSampleByIdentifier(null);// ->
                                                               // java.lang.IllegalArgumentException:
                                                               // Identifier cannot be null
      fail("null not an identifer");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    ASSERT.that(
        openbisClient.listAttachmentsForSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A2")
            .size()).isAtLeast(1);
    // this one really cares about proper identifier
    try {
      openbisClient.listAttachmentsForSampleByIdentifier("QSDPR002A2");
      fail(" not an identifer");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      openbisClient.listAttachmentsForSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A23");
      fail("that sample does not exist");
    } catch (Exception e) {// ->ch.systemsx.cisd.common.exceptions.UserFailureException: No sample
                           // found for id '/MFT_PICHLER_MULTISCALE/QSDPR002A23'.
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testListAttachmentsForProjectByIdentifier() {
    ASSERT.that(openbisClient.listAttachmentsForProjectByIdentifier("/QBIC/QHPTI").size())
        .isAtLeast(15);
  }


  @Test
  public void testGetSpaceMembers() {
    ASSERT.that(openbisClient.getSpaceMembers("MNF_KOHLBACHER_ARZNEIMITTEL").contains("iisko01"));
    ASSERT.that(openbisClient.getSpaceMembers("MNF_KOHLBACHER_ARZNEIMITTEL").contains("iisai01"));
    ASSERT.that(!openbisClient.getSpaceMembers("MNF_KOHLBACHER_ARZNEIMITTEL").contains("kxmve01"));
    ASSERT.that(openbisClient.getSpaceMembers("") == null);
  }

  @Test
  public void testListPropertiesForType() {
    ASSERT.that(
        openbisClient.listPropertiesForType(
            openbisClient.getExperimentTypeByString("Q_EXPERIMENTAL_DESIGN")).get(0)).isInstanceOf(
        PropertyType.class);
    ASSERT.that(
        openbisClient.listPropertiesForType(openbisClient.getSampleTypeByString("Q_TEST_SAMPLE"))
            .get(0)).isInstanceOf(PropertyType.class);
    try {
      openbisClient.listPropertiesForType(openbisClient.getSampleTypeByString("unknown"));
      fail("should not work with non-existing type");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }

  }

  @Test
  public void testListVocabularyTermsForProperty() {
    List<PropertyType> types =
        openbisClient.listPropertiesForType(openbisClient
            .getSampleTypeByString("Q_BIOLOGICAL_ENTITY"));
    for (PropertyType type : types) {
      if (type.getCode().equals("Q_NCBI_ORGANISM")) {
        ASSERT.that(openbisClient.listVocabularyTermsForProperty(type).contains("Homo sapiens"));
        ASSERT.that(!openbisClient.listVocabularyTermsForProperty(type).contains("Liver"));
        ASSERT.that(!openbisClient.listVocabularyTermsForProperty(type).contains("Pan sapiens"));
      } else {
        try {
          openbisClient.listVocabularyTermsForProperty(type);
          fail("should not work with non vocabulary type");
        } catch (Exception e) {
          ASSERT.that(e).isInstanceOf(Exception.class);
        }
      }
    }
  }

  @Test
  public void testGetCVLabelForProperty() {
    List<PropertyType> types =
        openbisClient.listPropertiesForType(openbisClient
            .getSampleTypeByString("Q_BIOLOGICAL_ENTITY"));
    for (PropertyType type : types) {
      if (type.getCode().equals("Q_NCBI_ORGANISM")) {
        ASSERT.that(openbisClient.getCVLabelForProperty(type, "0").equals("Other"));
        ASSERT.that(openbisClient.getCVLabelForProperty(type, "10116").equals("Rattus norvegicus"));
        try {
          openbisClient.getCVLabelForProperty(type, "-10");
          fail("should not work with non vocabulary code");
        } catch (Exception e) {
          ASSERT.that(e).isInstanceOf(Exception.class);
        }
      }
    }
  }

  @Test
  public void testGetSampleTypeByString() {
    ASSERT.that(openbisClient.getSampleTypeByString("Q_TEST_SAMPLE"))
        .isInstanceOf(SampleType.class);
    ASSERT.that(openbisClient.getSampleTypeByString("unknown") == null);
  }

  @Test
  public void testGetExperimentTypeByString() {
    ASSERT.that(openbisClient.getExperimentTypeByString("Q_EXPERIMENTAL_DESIGN")).isInstanceOf(
        ExperimentType.class);
    ASSERT.that(openbisClient.getExperimentTypeByString("xyz") == null);
  }

  @Test
  public void testGetLabelsofProperties() {
    SampleType type = openbisClient.getSampleTypeByString("Q_BIOLOGICAL_ENTITY");
    Map<String, String> prop = openbisClient.getLabelsofProperties(type);
    ASSERT.that(prop.get("Q_NCBI_ORGANISM").equals("NCBI organism"));
    ASSERT.that(prop.size()).isEqualTo(5);
  }

  @Test
  public void testGenerateBarcode() {
    ASSERT.that(openbisClient.generateBarcode("/TEST28/QTEST", 0).startsWith("QTEST001"));
    ASSERT.that(openbisClient.generateBarcode("/TEST28/QTEST", 99).startsWith("QTEST100"));
    ASSERT.that(openbisClient.generateBarcode("/TEST28/QTEST", 1002).startsWith("QTEST004"));
  }

  @Test
  public void testMapToChar() {
    // TODO why can't i test all these chars?
  }

  @Test
  public void testChecksum() {
    String input = "QTEST005X";
    char output = OpenBisClient.checksum(input);
    ASSERT.that(output == 'M');
  }

  @Test
  public void testOpenBIScodeToString() {
    ASSERT.that(openbisClient.openBIScodeToString("Q_BIOLOGICAL_ENTITY")).isEqualTo(
        "Biological Entity");
  }

  @Test
  public void testGetDataStoreDownloadURL() throws MalformedURLException {//TODO deprecated
    String code = "20150317113748250-9094";
    String file = "032_CRa_H9M5_THP1_NM104_0h_2_pos_RP_high_mr_QMARI074A9.mzML";
    String url = openbisClient.getDataStoreDownloadURL(code, file).toString();
    String controlUrl = String.format("https://qbis.qbic.uni-tuebingen.de:444/datastore_server/%s/original/%s?mode=simpleHtml&sessionID=%s",code,file, openbisClient.getSessionToken());
    ASSERT.that(url).isEqualTo(controlUrl);
    //This method is deprecated so we will not further work on it. However, it is not checking correctness of code and filename.
    /*try {
      System.out.println(openbisClient.getDataStoreDownloadURL(code, "WRONG"));
      fail("should not work with nonexisting file name");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      openbisClient.getDataStoreDownloadURL("notacode", file);
      fail("should not work with nonexisting dataset code");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }*/
  }

  /*@Test
  public void testGetParentMap() {
    Map<Sample, List<Sample>> map =
        openbisClient.getParentMap(openbisClient.getSamplesOfProject("/ABI_SYSBIO/QMARI"));
    for (Sample child : map.keySet()) {
      String type = child.getSampleTypeCode();
      for (Sample parent : map.get(child)) {
        switch (type) {
          case "Q_BIOLOGICAL_ENTITY":
            ASSERT.that(parent == null);
            break;
          case "Q_BIOLOGICAL_SAMPLE":
            ASSERT.that(parent.getSampleTypeCode().equals("Q_BIOLOGICAL_ENTITY"));
            break;
          case "Q_TEST_SAMPLE":
            ASSERT.that(parent.getSampleTypeCode().equals("Q_BIOLOGICAL_SAMPLE"));
            break;
          default:
            break;
        }
      }
    }
  }
  +/

 /* @Test
  public void testGetParents() {
    List<Sample> parents = openbisClient.getParents("QMARI074A9");
    ASSERT.that(parents.size()).isEqualTo(1);
    ASSERT.that(parents.get(0).getCode()).isEqualTo("QMARI002AC");
  }*/

  @Test
  public void testGetChildrenSamples() {
    List<Sample> children =
        openbisClient.getChildrenSamples(openbisClient
            .getSampleByIdentifier("/ABI_SYSBIO/QMARIENTITY-1"));
    ASSERT.that(children.size()).isAtLeast(5);
    List<String> codes = new ArrayList<String>();
    for (Sample s : children) {
      codes.add(s.getCode());
    }
    ASSERT.that(codes.contains("QMARI149A0"));
  }
  

  @Test
  public void testSpaceExists() {
    ASSERT.that(openbisClient.spaceExists("ABI_SYSBIO"));
    ASSERT.that(!openbisClient.spaceExists("DEEP_SPACE_NINE"));
  }

  @Test
  public void testProjectExists() {
    ASSERT.that(openbisClient.projectExists("ABI_SYSBIO", "QMARI"));
    ASSERT.that(!openbisClient.projectExists("ABI_SYSBIO", "QQQQQ"));
    ASSERT.that(!openbisClient.projectExists("DEEP_SPACE_NINE", "QMARI"));
  }

  @Test
  public void testExpExists() {
    ASSERT.that(openbisClient.expExists("ABI_SYSBIO", "QMARI", "QMARIE3"));
    ASSERT.that(!openbisClient.expExists("DEEP_SPACE_NINE", "QMARI", "QMARIE3"));
    ASSERT.that(!openbisClient.expExists("ABI_SYSBIO", "QQQQQ", "QMARIE3"));
    ASSERT.that(!openbisClient.expExists("ABI_SYSBIO", "QMARI", "QQQQQE3"));
  }

  @Test
  public void testSampleExists() {
    ASSERT.that(openbisClient.sampleExists("QMARI074A9"));
    ASSERT.that(!openbisClient.sampleExists("QQQQQ999XX"));
  }

  @Test
  public void testComputeProjectStatus() {
    // TODO
  }

  @Test
  public void testGetVocabCodesAndLabelsForVocab() {
    Map<String, String> map = openbisClient.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
    ASSERT.that(map.get("Bos taurus")).isEqualTo("9913");
    ASSERT.that(map.get("Sus scrofa")).isEqualTo("9823");
    ASSERT.that(map.get("Other")).isEqualTo("0");
    ASSERT.that(map.get("Hound of Baskervilles")).isEqualTo(null);
    ASSERT.that(map.get("Bos_taurus")).isEqualTo(null);
  }

  @Test
  public void testGetVocabCodesForVocab() {
    List<String> lis = openbisClient.getVocabCodesForVocab("Q_NCBI_TAXONOMY");
    ASSERT.that(lis.contains("Homo sapiens"));
    ASSERT.that(!lis.contains("Homo_sapiens"));
    ASSERT.that(!lis.contains("Yeti sapiens"));
  }

  @Test
  public void testGetExperimentsForUser() {
    String user = "iisko01";
    List<Experiment> exps = openbisClient.getExperimentsForUser(user);
    for (Experiment e : exps) {
      ASSERT.that(openbisClient.getSpaceMembers(e.getIdentifier().split("/")[1]).contains(user));
    }
  }

  @Test
  public void testListExperimentsOfProjects() {
    Project hpti = openbisClient.getProjectByIdentifier("/QBIC/QHPTI");
    Project qmari = openbisClient.getProjectByIdentifier("/ABI_SYSBIO/QMARI");
    List<Experiment> exps = openbisClient.listExperimentsOfProjects(Arrays.asList(hpti));
    ASSERT.that(exps.size()).isEqualTo(1);
    exps = openbisClient.listExperimentsOfProjects(Arrays.asList(hpti, qmari));
    ASSERT.that(exps.size()).isAtLeast(8);
  }

  @Test
  public void testListDataSetsForExperiments() {
    List<DataSet> dsets =
        openbisClient.listDataSetsForExperiments(Arrays.asList("/QBIC/QHPTI/HPTI"));
    ASSERT.that(dsets.size()).isEqualTo(43);
    dsets =
        openbisClient.listDataSetsForExperiments(Arrays.asList("/QBIC/QHPTI/HPTI",
            "/ABI_SYSBIO/QMARI/QMARIE3"));
    ASSERT.that(dsets.size()).isEqualTo(432 + 43);
    dsets =
        openbisClient.listDataSetsForExperiments(Arrays.asList("/QBIC/QHPTI/HPTI",
            "/ABI_SYSBIO/QMARI/nonexistent"));
    ASSERT.that(dsets.size()).isEqualTo(43);
  }

  @Test
  public void testListSamplesForProjects() {
    List<Sample> samps = openbisClient.listSamplesForProjects(Arrays.asList("/QBIC/QHPTI"));
    ASSERT.that(samps.size()).isEqualTo(43);
    samps = openbisClient.listSamplesForProjects(Arrays.asList("/QBIC/QHPTI", "/ABI_SYSBIO/QMARI"));
    ASSERT.that(samps.size()).isAtLeast(191);
    samps =
        openbisClient.listSamplesForProjects(Arrays
            .asList("/QBIC/QHPTI", "/ABI_SYSBIO/nonexistent"));
    ASSERT.that(samps.size()).isEqualTo(43);
  }

  @Test
  public void testListDataSetsForSamples() {
    ASSERT.that(
        openbisClient.listDataSetsForSamples(
            Arrays.asList("/ABI_SYSBIO/QMARI074A9", "/QBIC/QHPTI005CT")).size()).isEqualTo(6 + 1);
    ASSERT.that(
        openbisClient.listDataSetsForSamples(Arrays.asList("/ABI_SYSBIO/QMARI074A9")).size())
        .isEqualTo(6);
    ASSERT.that(
        openbisClient.listDataSetsForSamples(
            Arrays.asList("/ABI_SYSBIO/QMARI749A9", "/QBIC/QHPTI005CT")).size()).isEqualTo(1);
  }

  @Test
  public void testGetUrlForDataset() throws MalformedURLException {
    String code = "20150317113748250-9094";
    String file = "032_CRa_H9M5_THP1_NM104_0h_2_pos_RP_high_mr_QMARI074A9.mzML";
    ASSERT.that(openbisClient.getUrlForDataset(code, file).equals(
        "https://qbis.qbic.uni-tuebingen.d4/datastore_server/" + code + "/original/" + file
            + "?mode=simpleHtml&sessionID=" + openbisClient.getSessionToken()));
    try {
      openbisClient.getUrlForDataset(code, "WRONG");
      fail("should not work with nonexisting file name"); //TODO
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
    try {
      openbisClient.getUrlForDataset("notacode", file);
      fail("should not work with nonexisting dataset code");
    } catch (Exception e) {
      ASSERT.that(e).isInstanceOf(Exception.class);
    }
  }

  @Test
  public void testGetDatasetStreamString() {
    // TODO
  }

  @Test
  public void testGetDatasetStreamStringString() {
    // TODO
  }

  // TODO functions that change openBIS
  @Test
  public void testIngest() {
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
  public void testAddAttachmentToProject() {
    fail("Not yet implemented");
  }

  @Test
  public void testAddNewInstance() {
    // openbisClient.addNewInstance(params, service, number_of_samples_offset)
    fail("Not yet implemented");
  }
  
  @Test
  public void getSampleTypes(){
    Map<String, SampleType> types = openbisClient.getSampleTypes();
    ASSERT.that(types.size()).isGreaterThan(0);
  }

}
