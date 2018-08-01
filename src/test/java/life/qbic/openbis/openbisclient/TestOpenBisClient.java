package life.qbic.openbis.openbisclient;

import static com.google.common.truth.Truth.assertThat;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import java.util.List;
import java.util.Properties;
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
  private static Properties config;

  @BeforeClass
  public static void setUpBeforeClass() {
    config = new Properties();
    config.setProperty(DATASOURCE_URL,
        "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL);
    config.setProperty(DATASOURCE_USER, "");
    config.setProperty(DATASOURCE_PASS, "");
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void setUp() {

    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
  }

  @After
  public void tearDown() {
    openbisClient.logout();
    openbisClient = null;
  }

//  @Test
//  public void testOpenBisClient() {
//    openbisClient =
//        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
//            config.getProperty(DATASOURCE_URL));
//    assertThat(openbisClient.getFacade() != null);
//    openbisClient =
//        new OpenBisClient("user", config.getProperty(DATASOURCE_PASS),
//            config.getProperty(DATASOURCE_URL));
//    assertThat(!openbisClient.loggedin());
//    openbisClient =
//        new OpenBisClient(config.getProperty(DATASOURCE_USER), "wrongpassword",
//            config.getProperty(DATASOURCE_URL));
//    assertThat(!openbisClient.loggedin());
//    openbisClient =
//        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
//            "wrongurl");
//    assertThat(!openbisClient.loggedin());
//  }

  @Test
  public void testLoggedin() {
    assertThat(openbisClient.loggedin());
    openbisClient =
        new OpenBisClient("someuser", config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    assertThat(!openbisClient.loggedin());
  }

  @Test
  public void testLogout() {
    assertThat(openbisClient.loggedin());
    openbisClient.logout();
    assertThat(!openbisClient.loggedin());
  }

  @Test
  public void testLogin() {
    openbisClient.login();
    assertThat(openbisClient.loggedin());
    openbisClient.logout();
    openbisClient.login();
    assertThat(openbisClient.loggedin());
  }

  @Test
  public void testGetSessionToken() {
    openbisClient.ensureLoggedIn();
    assertThat(openbisClient.getSessionToken() != null);
    openbisClient.logout();
    assertThat(openbisClient.getSessionToken() == null);
  }

  @Test
  public void testListSpaces() {
    assertThat(openbisClient.listSpaces().size()).isAtLeast(7);// 7 for zxmqw74 at point of test
  }

  @Test
  public void testListProjects() {
    System.out.println(openbisClient.listProjects().size());
    assertThat(openbisClient.listProjects().size()).isAtLeast(143);// 143 for zxmqw at point of test
    assertThat(openbisClient.listProjects().get(0)).isInstanceOf(Project.class);
  }

  @Test
  public void testListExperiments() {
    List<Experiment> exps = openbisClient.listExperiments();
    assertThat(exps.size()).isAtLeast(10);
    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
  }

  @Test
  public void testGetSamplesofExperiment() {
    List<Sample> samples = openbisClient.getSamplesofExperiment("QA001E1");
    Sample sample = samples.get(0);
    TestOpenBisClientHelper.assertSampleAllFetched(sample);
  }

//
//  @Test
//  public void testGetSamplesofSpace() {
//    List<Sample> samps = openbisClient.getSamplesofSpace("MFT_PICHLER_MULTISCALE");
//    assertThat(samps.size()).isAtLeast(12);
//    assertThat(samps.get(0)).isInstanceOf(Sample.class);
//  }
//
//  @Test
//  public void testGetSampleByIdentifier() {
//    // it seems that this function really does not care, as long as the sample name is given
//    assertThat(
//        openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL/QSDPR002A2")
//            .getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
//    assertThat(
//        openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A2").getIdentifier())
//        .isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
//    assertThat(openbisClient.getSampleByIdentifier("QSDPR002A2").getIdentifier()).isEqualTo(
//        "/MFT_PICHLER_MULTISCALE/QSDPR002A2");
//
//    assertThat(
//        openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/QSDPR002A2")
//            .getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
//    assertThat(openbisClient.getSampleByIdentifier("/QSDPR/QSDPR002A2").getIdentifier())
//        .isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
//    // :)
//    assertThat(
//        openbisClient.getSampleByIdentifier("/I_do_not/care_what_is_upfront/QSDPR002A2")
//            .getIdentifier()).isEqualTo("/MFT_PICHLER_MULTISCALE/QSDPR002A2");
//    // does not work by type
//    try {
//      openbisClient.getSampleByIdentifier("MS_INJECT").getIdentifier(); // ->
//      // java.lang.IndexOutOfBoundsException
//      fail("should not work with type code alone");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//
//    }
//    // does not work with dataset code
//    try {
//      openbisClient.getSampleByIdentifier("20140617164748908-3036").getIdentifier();// ->
//      // java.lang.IndexOutOfBoundsException
//      fail("should not work with code alone");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    // does not work with empty string
//    try {
//      openbisClient.getSampleByIdentifier("").getIdentifier();// ->
//      // ch.systemsx.cisd.common.exceptions.UserFailureException:
//      // Search pattern '' is invalid.
//      fail("should not work with empty string");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    // does not work with null
//    try {
//      openbisClient.getSampleByIdentifier(null).getIdentifier();// -> NullPointerException
//      fail("should not work with null");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(NullPointerException.class);
//    }
//  }
//
//  @Test
//  public void testGetSamplesOfProject() {
//    List<Sample> samps = openbisClient.getSamplesOfProject("/MFT_PICHLER_MULTISCALE/QSDPR");
//    assertThat(samps.size()).isAtLeast(12);
//    assertThat(samps.get(0)).isInstanceOf(Sample.class);
//    assertThat(samps.equals(openbisClient.getSamplesOfProject("QSDPR")));// should work for id and
//    // code
//  }
//
//  @Test
//  public void testGetExperimentsOfProjectByIdentifier() {
//    List<Experiment> exps =
//        openbisClient.getExperimentsOfProjectByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR");
//    assertThat(exps.size()).isAtLeast(1);
//    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
//    try {
//      openbisClient.getExperimentsOfProjectByIdentifier("QSDPR");
//      fail("should not work with code alone");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test (expected = UserFailureException.class)
//  public void testGetExperimentsForProjectString() {
//
//    openbisClient.getExperimentsForProject("");// ->
//    // ch.systemsx.cisd.common.exceptions.UserFailureException:
//    // Illegal empty identifier
//
//    String test = null;
//    openbisClient.getExperimentsForProject(test);// ch.systemsx.cisd.common.exceptions.UserFailureException:
//    // Illegal empty identifier
//  }
//
//  @Test
//  public void testGetExperimentsForProjectStringSuccessfully() {
//    assertThat(openbisClient.getExperimentsForProject("/TEST28/QTEST").size()).isAtLeast(22);
//    // this one really cares about proper identifier
//    openbisClient.getExperimentsForProject("/CONFERENCE_DEMO/QTGPR");
//
//  }
//
//  @Test (expected = IllegalArgumentException.class)
//  public void testGetExperimentsForProjectStringWrongSpace(){
//    openbisClient.getExperimentsForProject("QTEST");// java.lang.IllegalArgumentException:
//    // Unspecified space code.
//  }
//
//  @Test
//  public void testGetExperimentsForProjectProject() {
//    Project p = openbisClient.getProjectByIdentifier("/TEST28/QTEST");
//    List<Experiment> exps = openbisClient.getExperimentsForProject(p);
//    assertThat(exps.size()).isAtLeast(22);
//    assertThat(openbisClient.getExperimentsForProject("/TEST28/QTEST").equals(exps));
//  }
//
//  @Test
//  public void testGetExperimentsOfProjectByCode() {
//    assertThat(openbisClient.getExperimentsOfProjectByCode("QTEST").size()).isAtLeast(22);
//    try {
//      System.out.println(openbisClient.getExperimentsOfProjectByCode("/TEST28/QTEST"));
//      fail("not a code");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetProjectExperimentMapping() {
//    Map<String, List<Experiment>> map = openbisClient.getProjectExperimentMapping("IVAC_ALL");
//    assertThat(map.keySet() != null);
//    for (String p : map.keySet()) {
//      assertThat(openbisClient.getExperimentsOfProjectByIdentifier(p).equals(map.get(p)));
//    }
//  }
//
//  @Test
//  public void testGetExperimentsOfSpace() {
//    List<Experiment> exps = openbisClient.getExperimentsOfSpace("MFT_PICHLER_MULTISCALE");
//    assertThat(exps.size()).isAtLeast(8);
//    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
//  }
//
//  @Test
//  public void testGetExperimentsOfType() {
//    List<Experiment> exps = openbisClient.getExperimentsOfType("MS_INJECT");
//    assertThat(exps.size()).isAtLeast(1);
//    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
//    for (Experiment e : exps) {
//      assertThat(e.getExperimentTypeCode().equals("MS_INJECT"));
//    }
//  }
//
//  @Test
//  public void testGetSamplesOfType() {
//    List<Sample> samps = openbisClient.getSamplesOfType("Q_MS_RUN");
//    assertThat(samps.size()).isAtLeast(1);
//    assertThat(samps.get(0)).isInstanceOf(Sample.class);
//    for (Sample e : samps) {
//      assertThat(e.getSampleTypeCode().equals("Q_MS_RUN"));
//    }
//  }
//
//  @Test
//  public void testGetProjectsOfSpace() {
//    List<Project> projs = openbisClient.getProjectsOfSpace("IVAC_ALL");
//    assertThat(projs.size()).isAtLeast(13);
//    assertThat(projs.get(0)).isInstanceOf(Project.class);
//  }
//
//  @Test
//  public void testGetProjectByIdentifier() {
//    assertThat(openbisClient.getProjectByIdentifier("/IVAC_ALL/QA001"))
//        .isInstanceOf(Project.class);
//
//    try {
//      System.out.println("testGetProjectByIdentifier"
//          + openbisClient.getProjectByIdentifier("QA001"));
//      fail("not an identifier");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//
//    try {
//      System.out.println("testGetProjectByIdentifier"
//          + openbisClient.getProjectByIdentifier("/IVAC_ALL/QL001"));
//      fail("does not exist");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//
//
//  }
//
//  @Test
//  public void testGetProjectByCode() {
//    assertThat(openbisClient.getProjectByCode("QA001")).isInstanceOf(Project.class);
//    try {
//      System.out
//          .println("testGetProjectByCode" + openbisClient.getProjectByCode("/IVAC_ALL/QA001"));
//      fail("not a code");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetExperimentByCode() {
//    assertThat(openbisClient.getExperimentByCode("PTX_SOLGEL")).isInstanceOf(Experiment.class);
//    try {
//      System.out.println("testGetExperimentByCode"
//          + openbisClient.getExperimentByCode("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL"));
//      fail("not a code");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetExperimentById() {
//    assertThat(openbisClient.getExperimentById("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL"))
//        .isInstanceOf(Experiment.class);
//    try {
//      System.out.println("testGetExperimentById" + openbisClient.getExperimentById("PTX_SOLGEL"));
//      fail("not an identifier");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetProjectOfExperimentByIdentifier() {
//    Project p =
//        openbisClient
//            .getProjectOfExperimentByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL");
//    assertThat(p).isInstanceOf(Project.class);
//    assertThat(p.getCode().equals("QSDPR"));
//    try {
//      System.out.println("testGetProjectOfExperimentByIdentifier"
//          + openbisClient.getExperimentById("PTX_SOLGEL"));
//      fail("not an identifier");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetDataSetsOfSampleByIdentifier() {
//    List<DataSet> dsets = openbisClient.getDataSetsOfSampleByIdentifier("/ABI_SYSBIO/QMARI117AV");
//    assertThat(dsets.size()).isEqualTo(6);
//    try {
//      System.out.println("testGetDataSetsOfSampleByIdentifier"
//          + openbisClient.getDataSetsOfSampleByIdentifier("QMARI117AV"));
//      fail("not an identifier");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetDataSetsOfSample() {
//    // it does not really matter as long as you have the last part correctly
//    assertThat(openbisClient.getDataSetsOfSample("/CONFERENCE_DEMO/NGS1QTGPR004AT").size()).isEqualTo(1);
//
//    // does not throw any exception, just return 0
//    assertThat(openbisClient.getDataSetsOfSample("MS_INJECT").size()).isEqualTo(0);
//    assertThat(openbisClient.getDataSetsOfSample("20140617164748908-3036").size()).isEqualTo(0);
//
//    try {
//      openbisClient.getDataSetsOfSample("").size();
//      fail("should not work with empty string");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);// ->
//      // ch.systemsx.cisd.common.exceptions.UserFailureException:
//      // Search pattern '' is invalid.
//    }
//    try {
//      openbisClient.getDataSetsOfSample(null).size();
//      fail("should not work with null");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(NullPointerException.class);
//    }
//  }
//
//  @Test
//  public void testGetDataSetsOfExperiment() {
//    Experiment ex = openbisClient.getExperimentByCode("HPTI");
//    List<DataSet> dsets = openbisClient.getDataSetsOfExperiment(ex.getPermId());
//    assertThat(dsets.size()).isEqualTo(43);
//    try {
//      System.out.println("testGetDataSetsOfExperiment"
//          + openbisClient.getDataSetsOfExperiment("HPTI"));
//      fail("not perm id but code");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    try {
//      System.out.println("testGetDataSetsOfExperiment"
//          + openbisClient.getExperimentByCode("/QBIC/QHPTI/HPTI"));
//      fail("not perm id but id");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetDataSetsOfExperimentByIdentifier() {
//    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets =
//        openbisClient.getDataSetsOfExperimentByIdentifier("/CONFERENCE_DEMO/QTGPRE/QTGPRE1");
//    assertThat(dsets.size()).isEqualTo(0);
//
//  }
//
//  @Test
//  public void testGetDataSetsOfExperimentByIdentifierWrongExperiment(){
//    assertThat(openbisClient.getDataSetsOfExperimentByIdentifier("lulu")).isEmpty();
//    //TODO seems to return the right datasets even if a code is used. either change function name or do something about it
//  }
//
//  // testGetDataSetsOfExperimentByIdentifier[DataSet[20140226191333949-1374,/QBIC/QHPTI/HPTI,/QBIC/QHPTI016CA,CEL,{}],
//  // DataSet[20140226191340992-1375,/QBIC/QHPTI/HPTI,/QBIC/QHPTI005CT,CEL,{}],
//  // DataSet[20140226191357420-1377,/QBIC/QHPTI/HPTI,/QBIC/QHPTI011C4,CEL,{}],
//  // DataSet[20140226191404414-1378,/QBIC/QHPTI/HPTI,/QBIC/QHPTI017CI,CEL,{}],
//  // DataSet[20140226191411466-1379,/QBIC/QHPTI/HPTI,/QBIC/QHPTI018CQ,CEL,{}],
//  // DataSet[20140226191418161-1380,/QBIC/QHPTI/HPTI,/QBIC/QHPTI014CS,CEL,{}],
//  // DataSet[20140226191426681-1381,/QBIC/QHPTI/HPTI,/QBIC/QHPTI019C0,CEL,{}],
//  // DataSet[20140226191434066-1382,/QBIC/QHPTI/HPTI,/QBIC/QHPTI007CB,CEL,{}],
//  // DataSet[20140226191441617-1383,/QBIC/QHPTI/HPTI,/QBIC/QHPTI034TP,CEL,{}],
//  // DataSet[20140226191450535-1384,/QBIC/QHPTI/HPTI,/QBIC/QHPTI036T7,CEL,{}],
//  // DataSet[20140226191459114-1385,/QBIC/QHPTI/HPTI,/QBIC/QHPTI030PP,CEL,{}],
//  // DataSet[20140226191507803-1386,/QBIC/QHPTI/HPTI,/QBIC/QHPTI024PG,CEL,{}],
//  // DataSet[20140226191515532-1387,/QBIC/QHPTI/HPTI,/QBIC/QHPTI027P6,CEL,{}],
//  // DataSet[20140226191525844-1388,/QBIC/QHPTI/HPTI,/QBIC/QHPTI022CJ,CEL,{}],
//  // DataSet[20140226191934851-1389,/QBIC/QHPTI/HPTI,/QBIC/QHPTI039TV,CEL,{}],
//  // DataSet[20140226192020080-1390,/QBIC/QHPTI/HPTI,/QBIC/QHPTI042TG,CEL,{}],
//  // DataSet[20140226192028926-1391,/QBIC/QHPTI/HPTI,/QBIC/QHPTI043TO,CEL,{}],
//  // DataSet[20140226192037373-1392,/QBIC/QHPTI/HPTI,/QBIC/QHPTI040T0,CEL,{}],
//  // DataSet[20140226192044952-1393,/QBIC/QHPTI/HPTI,/QBIC/QHPTI041T8,CEL,{}],
//  // DataSet[20140226192157487-1394,/QBIC/QHPTI/HPTI,/QBIC/QHPTI031PX,CEL,{}],
//  // DataSet[20140226192204809-1395,/QBIC/QHPTI/HPTI,/QBIC/QHPTI032P7,CEL,{}],
//  // DataSet[20140226192216725-1396,/QBIC/QHPTI/HPTI,/QBIC/QHPTI033TH,CEL,{}],
//  // DataSet[20131202021322407-907,/QBIC/QHPTI/HPTI,/QBIC/QHPTI004CL,PDF,{}],
//  // DataSet[20140226192230877-1398,/QBIC/QHPTI/HPTI,/QBIC/QHPTI037TF,CEL,{}],
//  // DataSet[20140226192238831-1399,/QBIC/QHPTI/HPTI,/QBIC/QHPTI038TN,CEL,{}],
//  // DataSet[20140226192246158-1400,/QBIC/QHPTI/HPTI,/QBIC/QHPTI021CB,CEL,{}],
//  // DataSet[20140226192253581-1401,/QBIC/QHPTI/HPTI,/QBIC/QHPTI023CR,CEL,{}],
//  // DataSet[20140226192301114-1402,/QBIC/QHPTI/HPTI,/QBIC/QHPTI025PO,CEL,{}],
//  // DataSet[20140226192309806-1403,/QBIC/QHPTI/HPTI,/QBIC/QHPTI026PW,CEL,{}],
//  // DataSet[20140226192317480-1404,/QBIC/QHPTI/HPTI,/QBIC/QHPTI028PE,CEL,{}],
//  // DataSet[20140226192324474-1405,/QBIC/QHPTI/HPTI,/QBIC/QHPTI029PM,CEL,{}],
//  // DataSet[20130904105623877-566,/QBIC/QHPTI/HPTI,/QBIC/QHPTI002TU,PDF,{}],
//  // DataSet[20130904105639119-568,/QBIC/QHPTI/HPTI,/QBIC/QHPTI001TM,PDF,{}],
//  // DataSet[20140226192223870-1397,/QBIC/QHPTI/HPTI,/QBIC/QHPTI035TX,CEL,{}],
//  // DataSet[20140226191227525-1365,/QBIC/QHPTI/HPTI,/QBIC/QHPTI013CK,CEL,{}],
//  // DataSet[20140226191235791-1366,/QBIC/QHPTI/HPTI,/QBIC/QHPTI004CL,CEL,{}],
//  // DataSet[20140226191243383-1367,/QBIC/QHPTI/HPTI,/QBIC/QHPTI015C2,CEL,{}],
//  // DataSet[20140226191250200-1368,/QBIC/QHPTI/HPTI,/QBIC/QHPTI012CC,CEL,{}],
//  // DataSet[20140226191257669-1369,/QBIC/QHPTI/HPTI,/QBIC/QHPTI020C3,CEL,{}],
//  // DataSet[20140226191304706-1370,/QBIC/QHPTI/HPTI,/QBIC/QHPTI010CU,CEL,{}],
//  // DataSet[20140226191311589-1371,/QBIC/QHPTI/HPTI,/QBIC/QHPTI009CR,CEL,{}],
//  // DataSet[20140226191318642-1372,/QBIC/QHPTI/HPTI,/QBIC/QHPTI008CJ,CEL,{}],
//  // DataSet[20140226191326878-1373,/QBIC/QHPTI/HPTI,/QBIC/QHPTI006C3,CEL,{}]]
//
//  @Test
//  public void testGetDataSetsOfSpaceByIdentifier() {
//    List<DataSet> dsets = openbisClient.getDataSetsOfSpaceByIdentifier("CONFERENCE_DEMO");
//    assertThat(dsets.size()).isEqualTo(93);
//    assertThat(dsets.get(0)).isInstanceOf(DataSet.class);
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifier_project_with_datasets() {
//    List<DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifier("/CONFERENCE_DEMO/QTGPR");
//    assertThat(dsets.size()).isEqualTo(93);
//    try {
//      openbisClient.getDataSetsOfProjectByIdentifier("QHPTI");
//      fail("not an identifier");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//  @Test
//  public void testGetDataSetsOfProjectByIdentifier_project_with_no_experiments() {
//    List<DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifier("/DEFAULT/DEFAULT");
//    assertThat(dsets.size()).isEqualTo(17);
//    try {
//      openbisClient.getDataSetsOfProjectByIdentifier("ECKH1");
//      fail("not an identifier");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifierWithSearchCriteria_project_with_datasets() {
//    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/CONFERENCE_DEMO/QTGPR");
//    assertThat(dsets.size()).isEqualTo(93);
//
//    assertThat(openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("idonotexist")).isEmpty();
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifierWithSearchCriteria_project_with_no_experiments() {
//    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets = openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("/ECKH/ECKH1");
//    assertThat(dsets.size()).isEqualTo(0);
//    try {
//      openbisClient.getDataSetsOfProjectByIdentifierWithSearchCriteria("idonotexist");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//
//
//  @Test
//  public void testGetDataSetsByType() {
//    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dsets =
//        openbisClient.getDataSetsByType("PDF");
//    assertThat(dsets.size()).isAtLeast(3);
//    assertThat(dsets.get(0)).isInstanceOf(
//        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.class);
//    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet s : dsets) {
//      assertThat(s.getDataSetTypeCode().equals("PDF"));
//    }
//  }
//
//  @Test
//  public void testListAttachmentsForSampleByIdentifier() {
//    // TODO throw sensible exceptions
//    try {
//      openbisClient.listAttachmentsForSampleByIdentifier("");// ->
//      // ch.systemsx.cisd.common.exceptions.UserFailureException:
//      // Illegal empty identifier
//      fail("should not work with empty string");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    try {
//      openbisClient.listAttachmentsForSampleByIdentifier(null);// ->
//      // java.lang.IllegalArgumentException:
//      // Identifier cannot be null
//      fail("null not an identifer");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    assertThat(
//        openbisClient.listAttachmentsForSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A2")
//            .size()).isAtLeast(1);
//    // this one really cares about proper identifier
//    try {
//      openbisClient.listAttachmentsForSampleByIdentifier("QSDPR002A2");
//      fail(" not an identifer");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    try {
//      openbisClient.listAttachmentsForSampleByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR002A23");
//      fail("that sample does not exist");
//    } catch (Exception e) {// ->ch.systemsx.cisd.common.exceptions.UserFailureException: No sample
//      // found for id '/MFT_PICHLER_MULTISCALE/QSDPR002A23'.
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//  }
//
//
//  @Test
//  public void testGetSpaceMembers() {
//    assertThat(openbisClient.getSpaceMembers("MNF_KOHLBACHER_ARZNEIMITTEL").contains("iisko01"));
//    assertThat(openbisClient.getSpaceMembers("MNF_KOHLBACHER_ARZNEIMITTEL").contains("iisai01"));
//    assertThat(!openbisClient.getSpaceMembers("MNF_KOHLBACHER_ARZNEIMITTEL").contains("kxmve01"));
//    assertThat(openbisClient.getSpaceMembers("") == null);
//  }
//
//  @Test
//  public void testListPropertiesForType() {
//    assertThat(
//        openbisClient.listPropertiesForType(
//            openbisClient.getExperimentTypeByString("Q_EXPERIMENTAL_DESIGN")).get(0)).isInstanceOf(
//        PropertyType.class);
//    assertThat(
//        openbisClient.listPropertiesForType(openbisClient.getSampleTypeByString("Q_TEST_SAMPLE"))
//            .get(0)).isInstanceOf(PropertyType.class);
//    try {
//      openbisClient.listPropertiesForType(openbisClient.getSampleTypeByString("unknown"));
//      fail("should not work with non-existing type");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//
//  }
//
//  @Test
//  public void testListVocabularyTermsForProperty() {
//    List<PropertyType> types =
//        openbisClient.listPropertiesForType(openbisClient
//            .getSampleTypeByString("Q_BIOLOGICAL_ENTITY"));
//    for (PropertyType type : types) {
//      if (type.getCode().equals("Q_NCBI_ORGANISM")) {
//        assertThat(openbisClient.listVocabularyTermsForProperty(type).contains("Homo sapiens"));
//        assertThat(!openbisClient.listVocabularyTermsForProperty(type).contains("Liver"));
//        assertThat(!openbisClient.listVocabularyTermsForProperty(type).contains("Pan sapiens"));
//      }
//    }
//  }
//
//  @Test
//  public void testGetCVLabelForProperty() {
//    List<PropertyType> types =
//        openbisClient.listPropertiesForType(openbisClient
//            .getSampleTypeByString("Q_BIOLOGICAL_ENTITY"));
//    for (PropertyType type : types) {
//      if (type.getCode().equals("Q_NCBI_ORGANISM")) {
//        assertThat(openbisClient.getCVLabelForProperty(type, "0").equals("Other"));
//        assertThat(openbisClient.getCVLabelForProperty(type, "10116").equals("Rattus norvegicus"));
//        try {
//          openbisClient.getCVLabelForProperty(type, "-10");
//          fail("should not work with non vocabulary code");
//        } catch (Exception e) {
//          assertThat(e).isInstanceOf(Exception.class);
//        }
//      }
//    }
//  }
//
//  @Test
//  public void testGetSampleTypeByString() {
//    assertThat(openbisClient.getSampleTypeByString("Q_TEST_SAMPLE"))
//        .isInstanceOf(SampleType.class);
//    assertThat(openbisClient.getSampleTypeByString("unknown") == null);
//  }
//
//  @Test
//  public void testGetExperimentTypeByString() {
//    assertThat(openbisClient.getExperimentTypeByString("Q_EXPERIMENTAL_DESIGN")).isInstanceOf(
//        ExperimentType.class);
//    assertThat(openbisClient.getExperimentTypeByString("xyz") == null);
//  }
//
//  @Test
//  public void testGetLabelsofProperties() {
//    SampleType type = openbisClient.getSampleTypeByString("Q_BIOLOGICAL_ENTITY");
//    Map<String, String> prop = openbisClient.getLabelsofProperties(type);
//    assertThat(prop.get("Q_NCBI_ORGANISM").equals("NCBI organism"));
//    assertThat(prop.size()).isNotNull();
//    assertThat(prop.size()).isGreaterThan(0);
//  }
//
//  @Test
//  public void testGenerateBarcode() {
//    assertThat(openbisClient.generateBarcode("/TEST28/QTEST", 0).startsWith("QTEST001"));
//    assertThat(openbisClient.generateBarcode("/TEST28/QTEST", 99).startsWith("QTEST100"));
//    assertThat(openbisClient.generateBarcode("/TEST28/QTEST", 1002).startsWith("QTEST004"));
//  }
//
//
//  @Test
//  public void testChecksum() {
//    String input = "QTEST005X";
//    char output = OpenBisClient.checksum(input);
//    assertThat(output == 'M');
//  }
//
//  @Test
//  public void testOpenBIScodeToString() {
//    assertThat(openbisClient.openBIScodeToString("Q_BIOLOGICAL_ENTITY")).isEqualTo(
//        "Biological Entity");
//  }
//
//  @Test
//  public void testGetDataStoreDownloadURL() throws MalformedURLException {//TODO deprecated
//    String code = "20150317113748250-9094";
//    String file = "032_CRa_H9M5_THP1_NM104_0h_2_pos_RP_high_mr_QMARI074A9.mzML";
//    String url = openbisClient.getDataStoreDownloadURL(code, file).toString();
//    String controlUrl = String.format("https://qbis.qbic.uni-tuebingen.de:444/datastore_server/%s/original/%s?mode=simpleHtml&sessionID=%s",code,file, openbisClient.getSessionToken());
//    assertThat(url).isEqualTo(controlUrl);
//    //This method is deprecated so we will not further work on it. However, it is not checking correctness of code and filename.
//    /*try {
//      System.out.println(openbisClient.getDataStoreDownloadURL(code, "WRONG"));
//      fail("should not work with nonexisting file name");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }
//    try {
//      openbisClient.getDataStoreDownloadURL("notacode", file);
//      fail("should not work with nonexisting dataset code");
//    } catch (Exception e) {
//      assertThat(e).isInstanceOf(Exception.class);
//    }*/
//  }
//
//  /*@Test
//  public void testGetParentMap() {
//    Map<Sample, List<Sample>> map =
//        openbisClient.getParentMap(openbisClient.getSamplesOfProject("/ABI_SYSBIO/QMARI"));
//    for (Sample child : map.keySet()) {
//      String type = child.getSampleTypeCode();
//      for (Sample parent : map.get(child)) {
//        switch (type) {
//          case "Q_BIOLOGICAL_ENTITY":
//            assertThat(parent == null);
//            break;
//          case "Q_BIOLOGICAL_SAMPLE":
//            assertThat(parent.getSampleTypeCode().equals("Q_BIOLOGICAL_ENTITY"));
//            break;
//          case "Q_TEST_SAMPLE":
//            assertThat(parent.getSampleTypeCode().equals("Q_BIOLOGICAL_SAMPLE"));
//            break;
//          default:
//            break;
//        }
//      }
//    }
//  }
//  */
//
//  @Test
//  public void testGetParents() {
//    List<Sample> parents = openbisClient.getParentsBySearchService("QTGPR001A5");
//    assertThat(parents.size()).isEqualTo(1);
//    assertThat(parents.get(0).getCode()).isEqualTo("QTGPRENTITY-1");
//  }
//
//  @Test
//  public void testGetChildrenSamples() {
//    List<Sample> children =
//        openbisClient.getChildrenSamples(openbisClient
//            .getSampleByIdentifier("/CONFERENCE_DEMO/QTGPRENTITY-1"));
//    assertThat(children.size()).isAtLeast(1);
//    List<String> codes = new ArrayList<String>();
//    for (Sample s : children) {
//      codes.add(s.getCode());
//    }
//    assertThat(codes.contains("QTGPR001A5"));
//  }
//
//
//  @Test
//  public void testSpaceExists() {
//    assertThat(openbisClient.spaceExists("ABI_SYSBIO"));
//    assertThat(!openbisClient.spaceExists("DEEP_SPACE_NINE"));
//  }
//
//  @Test
//  public void testProjectExists() {
//    assertThat(openbisClient.projectExists("ABI_SYSBIO", "QMARI"));
//    assertThat(!openbisClient.projectExists("ABI_SYSBIO", "QQQQQ"));
//    assertThat(!openbisClient.projectExists("DEEP_SPACE_NINE", "QMARI"));
//  }
//
//  @Test
//  public void testExpExists() {
//    assertThat(openbisClient.expExists("ABI_SYSBIO", "QMARI", "QMARIE3"));
//    assertThat(!openbisClient.expExists("DEEP_SPACE_NINE", "QMARI", "QMARIE3"));
//    assertThat(!openbisClient.expExists("ABI_SYSBIO", "QQQQQ", "QMARIE3"));
//    assertThat(!openbisClient.expExists("ABI_SYSBIO", "QMARI", "QQQQQE3"));
//  }
//
//  @Test
//  public void testSampleExists() {
//    assertThat(openbisClient.sampleExists("QMARI074A9"));
//    assertThat(!openbisClient.sampleExists("QQQQQ999XX"));
//  }
//
//  //@Test
//  public void testComputeProjectStatus() {
//    // TODO
//  }
//
//  @Test
//  public void testGetVocabCodesAndLabelsForVocab() {
//    Map<String, String> map = openbisClient.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
//    assertThat(map.get("Bos taurus")).isEqualTo("9913");
//    assertThat(map.get("Sus scrofa")).isEqualTo("9823");
//    assertThat(map.get("Other")).isEqualTo("0");
//    assertThat(map.get("Hound of Baskervilles")).isEqualTo(null);
//    assertThat(map.get("Bos_taurus")).isEqualTo(null);
//  }
//
//  @Test
//  public void testGetVocabCodesForVocab() {
//    List<String> lis = openbisClient.getVocabCodesForVocab("Q_NCBI_TAXONOMY");
//    assertThat(lis.contains("Homo sapiens"));
//    assertThat(!lis.contains("Homo_sapiens"));
//    assertThat(!lis.contains("Yeti sapiens"));
//  }
//
//  @Test
//  public void testGetExperimentsForUser() {
//    String user = "iisko01";
//    List<Experiment> exps = openbisClient.getExperimentsForUser(user);
//    for (Experiment e : exps) {
//      assertThat(openbisClient.getSpaceMembers(e.getIdentifier().split("/")[1]).contains(user));
//    }
//  }
//
//  @Test
//  public void testListExperimentsOfProjects() {
//    Project qshow = openbisClient.getProjectByIdentifier("/CONFERENCE_DEMO/QTGPR");
//    List<Experiment> exps = openbisClient.listExperimentsOfProjects(Arrays.asList(qshow));
//    assertThat(exps.size()).isEqualTo(61);
//  }
//
//  @Test
//  public void testListDataSetsForExperiments() {
//    List<DataSet> dsets =
//        openbisClient.listDataSetsForExperiments(Arrays.asList("/CONFERENCE_DEMO/QTGPR/QTGPRE6"));
//    assertThat(dsets.size()).isEqualTo(1);
//  }
//
//  @Test
//  public void testListSamplesForProjects() {
//    List<Sample> samps = openbisClient.listSamplesForProjects(Arrays.asList("/CONFERENCE_DEMO/QTGPR"));
//    assertThat(samps.size()).isEqualTo(117);
//
//  }
//
//  @Test
//  public void testListDataSetsForSamples() {
//    assertThat(
//        openbisClient.listDataSetsForSamples(
//            Arrays.asList("/CONFERENCE_DEMO/NGSQTGPR003AL")).size()).isEqualTo(1);
//  }
//
//  @Test
//  public void testGetUrlForDataset() throws MalformedURLException {
//    String code = "20150317113748250-9094";
//    String file = "032_CRa_H9M5_THP1_NM104_0h_2_pos_RP_high_mr_QMARI074A9.mzML";
//    assertThat(openbisClient.getUrlForDataset(code, file).equals(
//        "https://qbis.qbic.uni-tuebingen.d4/datastore_server/" + code + "/original/" + file
//            + "?mode=simpleHtml&sessionID=" + openbisClient.getSessionToken()));
//    openbisClient.getUrlForDataset(code, "WRONG");
//    openbisClient.getUrlForDataset("notacode", file);
//  }
//
//  //@Test
//  public void testGetDatasetStreamString() {
//    // TODO
//  }
//
//  //@Test
//  public void testGetDatasetStreamStringString() {
//    // TODO
//  }
//
//  // TODO functions that change openBIS
//  //@Test
//  public void testIngest() {
//    fail("Not yet implemented");
//  }
//
//  //@Test
//  public void testTriggerIngestionService() {
//    fail("Not yet implemented");
//  }
//
//  //@Test
//  public void testAddParentChildConnection() {
//    fail("Not yet implemented");
//  }
//
//  //@Test
//  public void testAddAttachmentToProject() {
//    fail("Not yet implemented");
//  }
//
//  //@Test
//  public void testAddNewInstance() {
//    // openbisClient.addNewInstance(params, service, number_of_samples_offset)
//    fail("Not yet implemented");
//  }
//
//  @Test
//  public void getSampleTypes(){
//    Map<String, SampleType> types = openbisClient.getSampleTypes();
//    assertThat(types.size()).isGreaterThan(0);
//  }
//
//  @Test
//  public void testGetPropertyOfSamples() {
//    Set<Sample> input = new HashSet<Sample>(
//        Arrays.asList(openbisClient.getSampleByIdentifier("/CONFERENCE_DEMO/QTGPRENTITY-20"),
//            openbisClient.getSampleByIdentifier("/CONFERENCE_DEMO/QTGPRENTITY-21"),
//            openbisClient.getSampleByIdentifier("/CONFERENCE_DEMO/QTGPRENTITY-22")));
//    Set<String> correct = new HashSet<String>(
//        Arrays.asList("Puerto Rican ; female", "Puerto Rican ; male", "Tuscan ; female"));
//    String res = HelperMethods.getPropertyOfSamples(input, "Q_SECONDARY_NAME");
//    Set<String> testSet = new HashSet<String>(Arrays.asList(res.split(", ")));
//    assertThat(correct.equals(testSet));
//  }
//
//  @Test
//  public void testFetchAncestorsOfType() {
//    List<Sample> all = openbisClient
//        .getSamplesWithParentsAndChildrenOfProjectBySearchService("/CONFERENCE_DEMO/QTGPR");
//    List<Sample> entities = new ArrayList<Sample>();
//    List<Sample> extracts = new ArrayList<Sample>();
//    ArrayList<Sample> tests = new ArrayList<Sample>();
//    for (Sample s : all) {
//      String type = s.getSampleTypeCode();
//      switch (type) {
//        case "Q_BIOLOGICAL_ENTITY":
//          entities.add(s);
//          break;
//        case "Q_BIOLOGICAL_SAMPLE":
//          extracts.add(s);
//          break;
//        case "Q_TEST_SAMPLE":
//          tests.add(s);
//          break;
//        default:
//          break;
//      }
//    }
//    assertThat(HelperMethods.fetchAncestorsOfType(tests, "Q_BIOLOGICAL_ENTITY").equals(entities));
//    assertThat(HelperMethods.fetchAncestorsOfType(tests, "Q_BIOLOGICAL_SAMPLE").equals(extracts));
//  }
//
//  @Test
//  public void testGetProjectTSV() {
//    assertThat(openbisClient.getProjectTSV("QTGPR", "Q_BIOLOGICAL_ENTITY").size()==21);//20 samples + header
//  }

}