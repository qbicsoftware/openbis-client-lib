package life.qbic.openbis.openbisclient;

import static com.google.common.truth.Truth.assertThat;
import static life.qbic.openbis.openbisclient.TestOpenBisClientHelper.assertDataSetCompletelyFetched;
import static life.qbic.openbis.openbisclient.TestOpenBisClientHelper.assertExperimentCompletetlyFetched;
import static life.qbic.openbis.openbisclient.TestOpenBisClientHelper.assertProjectCompletelyFetched;
import static life.qbic.openbis.openbisclient.TestOpenBisClientHelper.assertSampleCompletetlyFetched;
import static life.qbic.openbis.openbisclient.TestOpenBisClientHelper.assertSampleTypeCompletelyFetched;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jws.soap.SOAPBinding.Use;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestOpenBisClient {

  private static OpenBisClient openbisClient;
  private static String DATASOURCE_URL;
  private static Properties config;
  private static String PROPERTIES_PATH = "/Users/spaethju/qbic-ext.properties";

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void setUpBeforeClass() {
    config = new Properties();
    try {

      InputStream input = new FileInputStream(PROPERTIES_PATH);

      // load a properties file
      config = new Properties();
      config.load(input);

    } catch (Exception e) {
      System.out.println("Could not load the property file.");
      e.printStackTrace();
    }
    DATASOURCE_URL =
        config.getProperty("datasource.url") + IApplicationServerApi.SERVICE_URL;
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void setUp() {

    openbisClient =
        new OpenBisClient(config.getProperty("datasource.user"),
            config.getProperty("datasource.password"), DATASOURCE_URL);
    openbisClient.login();
  }

  @After
  public void tearDown() {
    openbisClient.logout();
    openbisClient = null;
  }

  @Test
  public void testOpenBisClient() {
    openbisClient =
        new OpenBisClient(config.getProperty("datasource.user"),
            config.getProperty("datasource.password"),
            DATASOURCE_URL);
    assertNotNull(openbisClient);
    openbisClient =
        new OpenBisClient("user", config.getProperty("datasource.password"),
            DATASOURCE_URL);
    assertFalse(openbisClient.loggedin());
    openbisClient =
        new OpenBisClient(config.getProperty("datasource.user"), "wrongpassword",
            DATASOURCE_URL);
    assertFalse(openbisClient.loggedin());
    openbisClient =
        new OpenBisClient(config.getProperty("datasource.user"),
            config.getProperty("datasource.pass"),
            "wrongurl");
    assertFalse(openbisClient.loggedin());
  }

  @Test
  public void testLoggedin() {
    assertTrue(openbisClient.loggedin());
    openbisClient =
        new OpenBisClient("someuser", config.getProperty("datasource.password"), DATASOURCE_URL);
    assertFalse(openbisClient.loggedin());
  }

  @Test
  public void testLogout() {
    assertTrue(openbisClient.loggedin());
    openbisClient.logout();
    assertFalse(openbisClient.loggedin());
  }

  @Test
  public void testLogin() {
    assertTrue(openbisClient.loggedin());
    openbisClient.logout();
    openbisClient.login();
    assertTrue(openbisClient.loggedin());
  }

  @Test()
  public void testGetSessionToken() {
    assertNotNull(openbisClient.getSessionToken());
    assertTrue(openbisClient.getV3().isSessionActive(openbisClient.getSessionToken()));
    String old_sessiontoken = openbisClient.getSessionToken();
    openbisClient.logout();
    assertNull(openbisClient.getSessionToken());
    assertFalse(openbisClient.getV3().isSessionActive(old_sessiontoken));
  }

  @Test
  public void testListSpaces() {
    List<String> spaceIdentifiers = openbisClient.listSpaces();
    assertThat(spaceIdentifiers.size()).isAtLeast(7);// 7 for zxmqw74 at point of test
    assertEquals(spaceIdentifiers.get(0).getClass(), String.class);
  }

  @Test
  public void testListProjects() {
    List<Project> projects = openbisClient.listProjects();
    assertThat(projects.size()).isAtLeast(143);// 143 for zxmqw74 at point of test
    assertEquals(projects.get(0).getClass(), Project.class);
    //TODO samples could not been fetched
    assertProjectCompletelyFetched(projects.get(0));
  }

  @Test
  public void testListExperiments() {
    List<Experiment> experiments = openbisClient.listExperiments();
    assertThat(experiments.size()).isAtLeast(10);
    assertEquals(experiments.get(0).getClass(), Experiment.class);
    assertExperimentCompletetlyFetched(experiments.get(0));
  }

  @Test
  public void testGetSamplesofExperiment() {
    List<Sample> samples = openbisClient.getSamplesofExperiment("QA001E1");
    Sample sample = samples.get(0);
    assertEquals(sample.getClass(), Sample.class);
    assertSampleCompletetlyFetched(sample);
  }

  @Test
  public void testGetSamplesofExperimentNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointerException");
    openbisClient.getSamplesofExperiment(null);
  }

  @Test
  public void testGetSamplesofExperimentEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getSamplesofExperiment("");
  }

  @Test
  public void testGetSamplesofExperimentNotExist() {
    //TODO should raise error instead returning empty list
    openbisClient.getSamplesofExperiment("doesnotexist");
  }

  @Test
  public void testGetSamplesofSpace() {
    List<Sample> samps = openbisClient.getSamplesofSpace("MFT_PICHLER_MULTISCALE");
    assertThat(samps.size()).isAtLeast(12);
    assertThat(samps.get(0)).isInstanceOf(Sample.class);
    assertSampleCompletetlyFetched(samps.get(0));
  }

  @Test
  public void testGetSamplesofSpaceNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointerException");
    openbisClient.getSamplesofSpace(null);
  }

  @Test
  public void testGetSamplesofSpaceEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getSamplesofSpace("");
  }

  @Test
  public void testGetSamplesofSpaceNotExist() {
    //TODO should raise error instead returning empty list
    openbisClient.getSamplesofSpace("doesnotexist");
  }

  @Test
  public void testGetSampleByIdentifier() {
    SampleIdentifier sampleId = openbisClient
        .getSampleByIdentifier("/MFT_PICHLER_MULTISCALE" + "/MSQMUSP061A9").getIdentifier();
    // Identifier exists
    assertThat(sampleId).isEqualTo(new SampleIdentifier("/MFT_PICHLER_MULTISCALE"
        + "/MSQMUSP061A9"));

    // Identifier exists
    Sample sample = openbisClient.getSampleByIdentifier("/MFT_PICHLER_MULTISCALE" +
        "/MSQMUSP061A9");
    assertSampleCompletetlyFetched(sample);
  }

  @Test
  public void testGetSampleByIdentifierWithCode() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("has to start with");
    openbisClient.getSampleByIdentifier("MSQMUSP061A9");
  }

  @Test
  public void testGetSampleByIdentifierNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Identifier id cannot be null");
    openbisClient.getSampleByIdentifier(null);
  }

  @Test
  public void testGetSampleByIdentifierEmpty() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Unspecified sample identifier");
    openbisClient.getSampleByIdentifier("");
  }

  @Test
  public void testGetSampleByIdentifierNotExist() {
    //TODO should throw an exception
    assertNull(openbisClient.getSampleByIdentifier("/does/not/exist"));
  }

  @Test
  public void testGetSampleByIdentifierWrongStart() {
    // Identifier starts without /
    exception.expect(RuntimeException.class);
    exception.expectMessage("has to start");
    openbisClient.getSampleByIdentifier("doesnotexist");
  }

  @Test
  public void testGetSamplesWithParentsAndChildren() {
    List<Sample> samples = openbisClient.getSamplesWithParentsAndChildren("MSQMUSP061A9");
    assertThat(samples.size()).isAtLeast(1);
    assertSampleCompletetlyFetched(samples.get(0));
  }

  @Test
  public void testGetSamplesWithParentsAndChildrenWithIdentifier() {
    List<Sample> samples = openbisClient.getSamplesWithParentsAndChildren(
        "/MFT_PICHLER_MULTISCALE" +
            "/MSQMUSP061A9");
    assertThat(samples.size()).isAtLeast(1);
    assertSampleCompletetlyFetched(samples.get(0));
  }

  @Test
  public void testGetSamplesWithParentsAndChildrenNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointer");
    openbisClient.getSamplesWithParentsAndChildren(null);
  }

  @Test
  public void testGetSamplesWithParentsAndChildrenEmpty() {
    exception.expect(UserFailureException.class);
    exception.expectMessage("invalid");
    openbisClient.getSamplesWithParentsAndChildren("");
  }

  @Test
  public void testGetSamplesWithParentsAndChildrenNotExist() {
    //TODO should raise an error instead returning empty list
    openbisClient.getSamplesWithParentsAndChildren("/does/not/exist");
  }

  @Test
  public void testGetSamplesOfProject() {
    // Code or Identifier exists
    List<Sample> sampsById = openbisClient.getSamplesOfProject("/MFT_SYNERGY_GLIOBLASTOMA_TREATMENT"
        + "/QGTSG");
    List<Sample> sampsByCode = openbisClient.getSamplesOfProject(
        "/MFT_SYNERGY_GLIOBLASTOMA_TREATMENT"
            + "/QGTSG");
    assertThat(sampsByCode.size()).isAtLeast(1);
    assertThat(sampsById.size()).isAtLeast(1);
    assertEquals(sampsByCode.size(), sampsById.size());

    assertThat(sampsByCode.get(0)).isInstanceOf(Sample.class);
    assertSampleCompletetlyFetched(sampsByCode.get(0));
  }

  @Test
  public void testGetSamplesOfProjectNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Identifier id cannot be null");
    openbisClient.getSamplesOfProject(null);
  }

  @Test
  public void testGetSamplesOfProjectEmpty() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Illegal empty identifier");
    openbisClient.getSamplesOfProject("");
  }

  @Test
  public void testGetSamplesOfProjectNotExist() {
    //TODO should raise error instead returning an empty list
    openbisClient.getSamplesOfProject("/does/notexist");
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifier() {
    // Id exists
    List<Experiment> expsById =
        openbisClient.getExperimentsOfProjectByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR");
    assertThat(expsById.size()).isAtLeast(1);
    assertThat(expsById.get(0)).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(expsById.get(0));
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifierWithCode() {
    List<Experiment> expsByCode = openbisClient.getExperimentsOfProjectByIdentifier("QSDPR");
    assertThat(expsByCode.size()).isAtLeast(1);
    assertThat(expsByCode.get(0)).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(expsByCode.get(0));
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifierNull() {
    // Identifier null
    exception.expect(RuntimeException.class);
    exception.expectMessage("Identifier id cannot be null");
    openbisClient.getExperimentsOfProjectByIdentifier(null);
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifierEmpty() {
    // Identifier empty
    exception.expect(RuntimeException.class);
    exception.expectMessage("Illegal empty identifier");
    openbisClient.getExperimentsOfProjectByIdentifier("");
  }

  @Test
  public void testGetExperimentsOfProjectByIdentifierNotExist() {
    // Identifier does not exist
    //TODO should raise error instead returning an empty list
    openbisClient.getExperimentsOfProjectByIdentifier("/does/notexist");
  }

  @Test
  public void testGetExperimentsForProject() {
    List<Experiment> experimentsString = openbisClient.getExperimentsForProject(
        "/MFT_PICHLER_MULTISCALE/QSDPR");
    assertThat(experimentsString.size()).isAtLeast(1);
    assertExperimentCompletetlyFetched(experimentsString.get(0));
    List<Experiment> experimentsObject =
        openbisClient.getExperimentsForProject(
            openbisClient.getProjectByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR"));
    assertThat(experimentsObject.size()).isAtLeast(1);
    assertExperimentCompletetlyFetched(experimentsObject.get(0));
    assertEquals(experimentsObject.size(), experimentsString.size());
  }

  @Test
  public void testGetExperimentsOfProjectByCode() {
    List<Experiment> expsByCode =
        openbisClient.getExperimentsOfProjectByCode("QSDPR");
    assertThat(expsByCode.size()).isAtLeast(1);
    assertThat(expsByCode.get(0)).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(expsByCode.get(0));
  }

  @Test
  public void testGetExperimentsOfProjectByCodeWithId() {
    List<Experiment> expsById = openbisClient.getExperimentsOfProjectByCode(
        "/MFT_PICHLER_MULTISCALE/QSDPR");
    assertThat(expsById.size()).isAtLeast(1);
    assertThat(expsById.get(0)).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(expsById.get(0));
  }

  @Test
  public void testGetExperimentsOfProjectByCodeNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointerException");
    openbisClient.getExperimentsOfProjectByCode(null);
  }

  @Test
  public void testGetExperimentsOfProjectByCodeEmpty() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("invalid");
    openbisClient.getExperimentsOfProjectByCode("");
  }

  @Test
  public void testGetExperimentsOfProjectByCodeNotExist() {
    //TODO should raise error instead returning an empty list
    openbisClient.getExperimentsOfProjectByCode("/does/notexist");
  }

  @Test
  public void testGetProjectExperimentMapping() {
    Map<String, List<Experiment>> map = openbisClient.getProjectExperimentMapping("IVAC_ALL");
    assertNotNull(map.keySet());
    for (String p : map.keySet()) {
      assertEquals(openbisClient.getExperimentsOfProjectByCode(p).size(), map.get(p).size());
    }
  }

  @Test
  public void testGetProjectExperimentMappingNull() {
    //TODO should throw an exception
    openbisClient.getProjectExperimentMapping(null);
  }

  @Test
  public void testGetProjectExperimentMappingEmpty() {
    //TODO should throw an exception
    openbisClient.getProjectExperimentMapping("");
  }

  @Test
  public void testGetProjectExperimentMappingNotExist() {
    //TODO should throw an exception
    openbisClient.getProjectExperimentMapping("/does/notexist");
  }

  @Test
  public void testGetExperimentsOfSpace() {
    List<Experiment> exps = openbisClient.getExperimentsOfSpace("MFT_PICHLER_MULTISCALE");
    assertThat(exps.size()).isAtLeast(8);
    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(exps.get(0));
  }

  @Test
  public void testGetExperimentsOfSpaceNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointerException");
    openbisClient.getExperimentsOfSpace(null);
  }

  @Test
  public void testGetExperimentsOfSpaceEmpty() {
    exception.expect(UserFailureException.class);
    exception.expectMessage("invalid");
    openbisClient.getExperimentsOfSpace("");
  }

  @Test
  public void testGetExperimentsOfSpaceNotExist() {
    //TODO should throw an exception
    openbisClient.getExperimentsOfSpace("/does/notexist");
  }

  @Test
  public void testGetExperimentsOfType() {
    List<Experiment> exps = openbisClient.getExperimentsOfType("MS_INJECT");
    assertThat(exps.size()).isAtLeast(1);
    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
    for (Experiment e : exps) {
      assertEquals(e.getType().getCode(), "MS_INJECT");
    }
    assertExperimentCompletetlyFetched(exps.get(0));
  }

  @Test
  public void testGetExperimentsOfTypeNull() {
    //TODO should throw an exception
    //openbisClient.getExperimentsOfType(null);
  }

  @Test
  public void testGetExperimentsOfTypeEmpty() {
    //TODO should throw an exception
    openbisClient.getExperimentsOfType("");
  }

  @Test
  public void testGetExperimentsOfTypeNotExist() {
    //TODO should throw an exception
    openbisClient.getExperimentsOfType("/does/notexist");
  }

  @Test
  public void testGetSamplesOfType() {
    List<Sample> samps = openbisClient.getSamplesOfType("Q_MS_RUN");
    assertThat(samps.size()).isAtLeast(1);
    assertThat(samps.get(0)).isInstanceOf(Sample.class);
    for (Sample e : samps) {
      assertEquals(e.getType().getCode(), "Q_MS_RUN");
    }

    assertSampleCompletetlyFetched(samps.get(0));
  }

  @Test
  public void testGetSamplesOfTypeNull() {
    //TODO should throw an exception
    //openbisClient.getSamplesOfType(null);
  }

  @Test
  public void testGetSamplesOfTypeEmpty() {
    //TODO should throw an exception
    openbisClient.getSamplesOfType("");
  }

  @Test
  public void testGetSamplesOfTypeNotExist() {
    //TODO should throw an exception
    openbisClient.getSamplesOfType("/does/notexist");
  }

  @Test
  public void testGetProjectsOfSpace() {
    List<Project> projs = openbisClient.getProjectsOfSpace("IVAC_ALL");
    assertThat(projs.size()).isAtLeast(13);
    assertThat(projs.get(0)).isInstanceOf(Project.class);
    assertProjectCompletelyFetched(projs.get(0));
  }

  @Test
  public void testGetProjectsOfSpaceNull() {
    //TODO should throw an exception
    openbisClient.getProjectsOfSpace(null);
  }

  @Test
  public void testGetProjectsOfSpaceEmpty() {
    //TODO should throw an exception
    openbisClient.getProjectsOfSpace("");
  }

  @Test
  public void testGetProjectsOfSpaceNotExist() {
    //TODO should throw an exception
    openbisClient.getProjectsOfSpace("/does/notexist");
  }

  @Test
  public void testGetProjectByIdentifier() {
    Project project = openbisClient.getProjectByIdentifier("/IVAC_ALL/QA001");
    assertThat(project).isInstanceOf(Project.class);
    assertEquals(project.getCode(), "QA001");
    assertProjectCompletelyFetched(project);
  }

  @Test
  public void testGetProjectByIdentifierWithCode() {
    Project project = openbisClient.getProjectByIdentifier("QA001");
    assertThat(project).isInstanceOf(Project.class);
    assertEquals(project.getCode(), "QA001");
    assertProjectCompletelyFetched(project);
  }

  @Test
  public void testGetProjectByIdentifierNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("id cannot be null");
    openbisClient.getProjectByIdentifier(null);
  }

  @Test
  public void testGetProjectByIdentifierEmpty() {
    //TODO should throw an exception
    assertNull(openbisClient.getProjectByIdentifier(""));
  }

  @Test
  public void testGetProjectByIdentifierNotExist() {
    //TODO should throw an exception
    assertNull(openbisClient.getProjectByIdentifier("/does/notexist"));
  }

  @Test
  public void testGetProjectByCode() {
    Project project = openbisClient.getProjectByCode("QA001");
    assertThat(project).isInstanceOf(Project.class);
    assertEquals(project.getCode(), "QA001");
    assertProjectCompletelyFetched(project);
  }

  @Test
  public void testGetProjectByCodeWithId() {
    Project project = openbisClient.getProjectByCode("/IVAC_ALL/QA001");
    assertThat(project).isInstanceOf(Project.class);
    assertEquals(project.getCode(), "QA001");
    assertProjectCompletelyFetched(project);
  }

  @Test
  public void testGetProjectByCodeNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("id cannot be null");
    openbisClient.getProjectByCode(null);
  }

  @Test
  public void testGetProjectByCodeEmpty() {
    //TODO should throw an exception
    assertNull(openbisClient.getProjectByCode(""));
  }

  @Test
  public void testGetProjectByCodeNotExist() {
    //TODO should throw an exception
    assertNull(openbisClient.getProjectByCode("/does/notexist"));
  }

  @Test
  public void testGetExperimentByCode() {
    Experiment experiment = openbisClient.getExperimentByCode("PTX_SOLGEL");
    assertThat(experiment).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(experiment);
    assertEquals(experiment.getCode(), "PTX_SOLGEL");
  }

  @Test
  public void testGetExperimentByCodeWithId() {
    Experiment experiment = openbisClient
        .getExperimentByCode("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL");
    assertThat(experiment).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(experiment);
    assertEquals(experiment.getCode(), "PTX_SOLGEL");
  }

  @Test
  public void testGetExperimentByCodeNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointerException");
    openbisClient.getExperimentByCode(null);
  }

  @Test
  public void testGetExperimentByCodeEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getExperimentByCode("");
  }

  @Test
  public void testGetExperimentByCodeNotExist() {
    //TODO should throw an exception
    assertNull(openbisClient.getExperimentByCode("/does/notexist"));
  }

  @Test
  public void testGetExperimentById() {
    Experiment experiment = openbisClient
        .getExperimentById("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL");
    assertThat(experiment).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(experiment);
    assertEquals(experiment.getCode(), "PTX_SOLGEL");
  }

  @Test
  public void testGetExperimentByIdWithCode() {
    exception.expect(UserFailureException.class);
    openbisClient.getExperimentById("PTX_SOLGEL");
  }

  @Test
  public void testGetExperimentByIdNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("cannot be null");
    openbisClient.getExperimentById(null);
  }

  @Test
  public void testGetExperimentByIdEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getExperimentById("");
  }

  @Test
  public void testGetExperimentByIdNotExist() {
    exception.expect(UserFailureException.class);
    openbisClient.getExperimentById("/does/notexist");
  }

  @Test
  public void testGetProjectOfExperimentByIdentifier() {
    Project p = openbisClient
        .getProjectOfExperimentByIdentifier("/MFT_PICHLER_MULTISCALE/QSDPR/PTX_SOLGEL");
    assertThat(p).isInstanceOf(Project.class);
    assertEquals(p.getCode(), "QSDPR");
  }

  @Test
  public void testGetProjectOfExperimentByIdentifierWithCode() {
    exception.expect(UserFailureException.class);
    openbisClient.getProjectOfExperimentByIdentifier("PTX_SOLGEL");
  }

  @Test
  public void testGetProjectOfExperimentByIdentifierNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("cannot be null");
    openbisClient.getProjectOfExperimentByIdentifier(null);
  }

  @Test
  public void testGetProjectOfExperimentByIdentifierEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getProjectOfExperimentByIdentifier("");
  }

  @Test
  public void testGetProjectOfExperimentByIdentifierNotExist() {
    exception.expect(UserFailureException.class);
    openbisClient.getProjectOfExperimentByIdentifier("/does/notexist");
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifier() {
    List<DataSet> dsets = openbisClient.getDataSetsOfSampleByIdentifier("/ABI_SYSBIO/QMARI117AV");
    assertThat(dsets.size()).isAtLeast(1);
    assertEquals(dsets.get(0).getClass(), DataSet.class);
    assertDataSetCompletelyFetched(dsets.get(0));
    assertEquals(dsets.get(0).getSample().getCode(), "QMARI117AV");
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifierWithCode() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("has to start");
    openbisClient.getDataSetsOfSampleByIdentifier("QMARI117AV");
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifierNull() {
    exception.expect(IllegalArgumentException.class);
    openbisClient.getDataSetsOfSampleByIdentifier(null);
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifierEmpty() {
    exception.expectMessage("Unspecified");
    openbisClient.getDataSetsOfSampleByIdentifier("");
  }

  @Test
  public void testGetDataSetsOfSampleByIdentifierNotExist() {
    //TODO should raise an exception instead returning empty list
    openbisClient.getDataSetsOfSampleByIdentifier("/doesnot/exist");
  }

  @Test
  public void testGetDataSetsOfSample() {
    List<DataSet> dsets = openbisClient.getDataSetsOfSample("QMARI117AV");
    assertThat(dsets.size()).isAtLeast(1);
    assertEquals(dsets.get(0).getClass(), DataSet.class);
    assertDataSetCompletelyFetched(dsets.get(0));
    assertEquals(dsets.get(0).getSample().getCode(), "QMARI117AV");
  }

  @Test
  public void testGetDataSetsOfSampleWithId() {
    List<DataSet> dsets = openbisClient.getDataSetsOfSample("/ABI_SYSBIO/QMARI117AV");
    assertThat(dsets.size()).isAtLeast(1);
    assertEquals(dsets.get(0).getClass(), DataSet.class);
    assertDataSetCompletelyFetched(dsets.get(0));
    assertEquals(dsets.get(0).getSample().getCode(), "QMARI117AV");
  }

  @Test
  public void testGetDataSetsOfSampleNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointer");
    openbisClient.getDataSetsOfSample(null);
  }

  @Test
  public void testGetDataSetsOfSampleEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getDataSetsOfSample("");
  }

  @Test
  public void testGetDataSetsOfSampleNotExist() {
    //TODO should raise an exception instead returning empty list
    openbisClient.getDataSetsOfSample("doesnotexist");
  }

  @Test
  public void testGetDataSetsOfExperiment() {
    Experiment ex = openbisClient.getExperimentByCode("HPTI");
    List<DataSet> dsets = openbisClient.getDataSetsOfExperiment(ex.getPermId().getPermId());
    assertThat(dsets.size()).isAtLeast(1);
    assertEquals(dsets.get(0).getClass(), DataSet.class);
    assertDataSetCompletelyFetched(dsets.get(0));
    assertEquals(dsets.get(0).getExperiment().getCode(), ex.getCode());
  }

  @Test
  public void testGetDataSetsOfExperimentNull() {
    exception.expectMessage("NullPointer");
    openbisClient.getDataSetsOfExperiment(null);
  }

  @Test
  public void testGetDataSetsOfExperimentEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getDataSetsOfExperiment("");
  }

  @Test
  public void testGetDataSetsOfExperimentNotExist() {
    //TODO should raise an exception instead returning empty list
    openbisClient.getDataSetsOfExperiment("doesnotexist");
  }

  @Test
  public void testGetDataSetsOfExperimentByIdentifier() {
    Experiment ex = openbisClient.getExperimentByCode("HPTI");
    List<DataSet> dsets =
        openbisClient.getDataSetsOfExperimentByIdentifier(ex.getIdentifier().getIdentifier());
    assertThat(dsets.size()).isAtLeast(1);
    assertEquals(dsets.get(0).getClass(), DataSet.class);
    assertDataSetCompletelyFetched(dsets.get(0));
    assertEquals(dsets.get(0).getExperiment().getCode(), ex.getCode());
  }

  @Test
  public void testGetDataSetsOfExperimentByIdentifierNull() {
    exception.expect(IllegalArgumentException.class);
    openbisClient.getDataSetsOfExperimentByIdentifier(null);
  }

  @Test
  public void testGetDataSetsOfExperimentByIdentifierEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getDataSetsOfExperimentByIdentifier("");
  }

  @Test
  public void testGetDataSetsOfExperimentByIdentifierNotExist() {
    exception.expect(UserFailureException.class);
    openbisClient.getDataSetsOfExperimentByIdentifier("/doesnot/exist");
  }

  @Test
  public void testGetDataSetsOfSpaceByIdentifier() {
    List<DataSet> dsets =
        openbisClient.getDataSetsOfSpaceByIdentifier(openbisClient.listSpaces().get(0));
    assertThat(dsets.size()).isAtLeast(1);
    assertEquals(dsets.get(0).getClass(), DataSet.class);
    assertDataSetCompletelyFetched(dsets.get(0));
  }

  @Test
  public void testGetDataSetsOfSpaceByIdentifierNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Null");
    openbisClient.getDataSetsOfSpaceByIdentifier(null);
  }

  @Test
  public void testGetDataSetsOfSpaceByIdentifierEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getDataSetsOfSpaceByIdentifier("");
  }

  @Test
  public void testGetDataSetsOfSpaceByIdentifierNotExist() {
    //TODO should raise an exception instead returning empty list
    openbisClient.getDataSetsOfSpaceByIdentifier("/doesnot/exist");
  }

  //TODO does not work yet
//  @Test
//  public void testGetDataSetsOfProjectByIdentifier() {
//    List<DataSet> dsets =
//        openbisClient.getDataSetsOfProjectByIdentifier("/QBIC_RESEARCH/QHPTI");
//    assertThat(dsets.size()).isAtLeast(1);
//    assertEquals(dsets.get(0).getClass(), DataSet.class );
//    assertDataSetCompletelyFetched(dsets.get(0));
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifierWithCode() {
//    openbisClient.getDataSetsOfProjectByIdentifier("QHPTI");
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifierNull() {
//    exception.expect(RuntimeException.class);
//    exception.expectMessage("Null");
//    openbisClient.getDataSetsOfProjectByIdentifier(null);
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifierEmpty() {
//    exception.expect(UserFailureException.class);
//    openbisClient.getDataSetsOfProjectByIdentifier("");
//  }
//
//  @Test
//  public void testGetDataSetsOfProjectByIdentifierNotExist() {
//    //TODO should raise an exception instead returning empty list
//    openbisClient.getDataSetsOfProjectByIdentifier("/doesnot/exist");
//  }

  @Test
  public void testGetDataSetsByType() {
    List<DataSet> dataSets = openbisClient.getDataSetsByType("PDF");
    assertThat(dataSets.size()).isAtLeast(3);
    assertThat(dataSets.get(0)).isInstanceOf(DataSet.class);
    for (DataSet s : dataSets) {
      assertDataSetCompletelyFetched(s);
      assertEquals(s.getType().getCode(), "PDF");
    }
  }

  @Test
  public void testGetDataSetsByTypeNull() {
    //TODO should throw an exception
    //openbisClient.getDataSetsByType(null);
  }

  @Test
  public void testGetDataSetsByTypeEmpty() {
    //TODO should throw an exception
    openbisClient.getDataSetsByType("");
  }

  @Test
  public void testGetDataSetsByTypeNotExist() {
    //TODO should throw an exception
    openbisClient.getDataSetsByType("/does/notexist");
  }


  @Test
  public void testListAttachmentsForSampleByIdentifier() {
    List<Attachment> attachments = openbisClient.listAttachmentsForSampleByIdentifier(
        "/MFT_PICHLER_MULTISCALE"
            + "/QSDPR002A2");
    assertThat(attachments.size()).isAtLeast(1);
    assertEquals(attachments.get(0).getClass(), Attachment.class);

  }

  @Test
  public void testListAttachmentsForSampleByIdentifierWithCode() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("has to start with");
    openbisClient.listAttachmentsForSampleByIdentifier("QSDPR002A2");
  }

  @Test
  public void testListAttachmentsForSampleByIdentifierNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("cannot be null");
    openbisClient.listAttachmentsForSampleByIdentifier(null);
  }

  @Test
  public void testListAttachmentsForSampleByIdentifierEmpty() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("Unspecified");
    openbisClient.listAttachmentsForSampleByIdentifier("");
  }

  @Test
  public void testListAttachmentsForSampleByIdentifierNotExist() {
    exception.expect(NullPointerException.class);
    openbisClient.listAttachmentsForSampleByIdentifier("/does/notexist");
  }

  @Test
  public void testListAttachmentsForProjectByIdentifier() {
    List<Attachment> attachments = openbisClient.listAttachmentsForProjectByIdentifier(
        "/EXT_SEIFERT_METAPROTEOMICS/QJSMP");
    assertThat(attachments.size()).isAtLeast(1);
    assertEquals(attachments.get(0).getClass(), Attachment.class);

  }

  @Test
  public void testListAttachmentsForProjectByIdentifierWithCode() {
    List<Attachment> attachments = openbisClient.listAttachmentsForProjectByIdentifier("QJSMP");
    assertThat(attachments.size()).isAtLeast(1);
    assertEquals(attachments.get(0).getClass(), Attachment.class);
  }

  @Test
  public void testListAttachmentsForProjectByIdentifierNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("cannot be null");
    openbisClient.listAttachmentsForProjectByIdentifier(null);
  }

  @Test
  public void testListAttachmentsForProjectByIdentifierEmpty() {
    exception.expect(NullPointerException.class);
    openbisClient.listAttachmentsForProjectByIdentifier("");
  }

  @Test
  public void testListAttachmentsForProjectByIdentifierNotExist() {
    exception.expect(NullPointerException.class);
    openbisClient.listAttachmentsForProjectByIdentifier("/does/notexist");
  }

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
//        assertTrue(openbisClient.listVocabularyTermsForProperty(type).contains("Homo sapiens"));
//        assertFalse(openbisClient.listVocabularyTermsForProperty(type).contains("Liver"));
//        assertFalse(openbisClient.listVocabularyTermsForProperty(type).contains("Pan sapiens"));
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

  @Test
  public void testGetSampleTypeByString() {
    SampleType sampleType = openbisClient.getSampleTypeByString("Q_TEST_SAMPLE");
    assertThat(sampleType).isInstanceOf(SampleType.class);
    assertSampleTypeCompletelyFetched(sampleType);
    assertEquals(sampleType.getCode(), "Q_TEST_SAMPLE");
  }

  @Test
  public void testGetSampleTypeByStringNull() {
    //TODO Should throw an error
    openbisClient.getSampleTypeByString(null);
  }

  @Test
  public void testGetSampleTypeByStringEmpty() {
    //TODO Should throw an exception
    assertNull(openbisClient.getSampleTypeByString(""));
  }

  @Test
  public void testGetSampleTypeByStringNotExist() {
    //TODO Should throw an exception
    assertNull(openbisClient.getSampleTypeByString("does/not/exist"));
  }

  @Test
  public void testGetExperimentTypeByString() {
    assertThat(openbisClient.getExperimentTypeByString("Q_EXPERIMENTAL_DESIGN"))
        .isInstanceOf(ExperimentType.class);
    assertNull(openbisClient.getExperimentTypeByString("xyz"));
  }

  //  @Test
//  public void testGetLabelsofProperties() {
//    SampleType type = openbisClient.getSampleTypeByString("Q_BIOLOGICAL_ENTITY");
//    Map<String, String> prop = openbisClient.getLabelsofProperties(type);
//    assertThat(prop.get("Q_NCBI_ORGANISM").equals("NCBI organism"));
//    assertThat(prop.size()).isNotNull();
//    assertThat(prop.size()).isGreaterThan(0);
//  }
//
  @Test
  public void testGenerateBarcode() {
    //TODO Maybe an old version... thats why the tests failed here
//    assertThat(openbisClient.generateBarcode("/TEST28/QTEST", 0)).startsWith("QTEST001");
//    assertThat(openbisClient.generateBarcode("/TEST28/QTEST", 99)).startsWith("QTEST100");
//    assertThat(openbisClient.generateBarcode("/TEST28/QTEST", 1002)).startsWith("QTEST004");
  }


  @Test
  public void testChecksum() {
    String input = "QTEST005X";
    char output = OpenBisClient.checksum(input);
    assertEquals('M', output);
  }

  @Test
  public void testOpenBIScodeToString() {
    assertThat(openbisClient.openBIScodeToString("Q_BIOLOGICAL_ENTITY")).isEqualTo(
        "Biological Entity");
  }

  @Test
  public void testGetDataStoreDownloadURL() throws MalformedURLException {//TODO deprecated
    String code = "20150317113748250-9094";
    String file = "032_CRa_H9M5_THP1_NM104_0h_2_pos_RP_high_mr_QMARI074A9.mzML";
    String url = openbisClient.getDataStoreDownloadURL(code, file).toString();
    String controlUrl = String.format("https://qbis.qbic.uni-tuebingen.de:444/datastore_server/%s/original/%s?mode=simpleHtml&sessionID=%s",code,file, openbisClient.getSessionToken());
    assertThat(url).isEqualTo(controlUrl);
  }

  @Test
  public void testGetDataStoreDownloadURLLessGeneric() throws MalformedURLException {
    //TODO
  }

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

  @Test
  public void testGetChildrenSamples() {
    Sample sample = openbisClient.getSampleByIdentifier("/CONFERENCE_DEMO/QTGPRENTITY-1");
    List<Sample> samples = openbisClient.getChildrenSamples(sample);

    assertThat(samples.size()).isAtLeast(1);
    assertSampleCompletetlyFetched(samples.get(0));
    assertTrue(sample.getChildren().contains(samples.get(0)));
  }

  @Test
  public void testGetChildrenSamplesNull() {
    exception.expect(NullPointerException.class);
    openbisClient.getChildrenSamples(null);
  }


  @Test
  public void testSpaceExists() {
    assertTrue(openbisClient.spaceExists("ABI_SYSBIO"));
    assertFalse(openbisClient.spaceExists("DEEP_SPACE_NINE"));
  }

  @Test
  public void testSpaceExistsNull() {
    //TODO should maybe raise an error
    assertFalse(openbisClient.spaceExists(null));
  }

  @Test
  public void testProjectExists() {
    assertTrue(openbisClient.projectExists("ABI_SYSBIO", "QMARI"));
    assertFalse(openbisClient.projectExists("ABI_SYSBIO", "QQQQQ"));
    assertFalse(openbisClient.projectExists("DEEP_SPACE_NINE", "QMARI"));
  }

  @Test
  public void testProjectExistsNull() {
    //TODO should maybe raise an error
    assertFalse(openbisClient.projectExists(null, null));
  }

  @Test
  public void testExpExists() {
    assertTrue(openbisClient.expExists("ABI_SYSBIO", "QMARI", "QMARIE3"));
    assertFalse(openbisClient.expExists("DEEP_SPACE_NINE", "QMARI", "QMARIE3"));
    assertFalse(openbisClient.expExists("ABI_SYSBIO", "QQQQQ", "QMARIE3"));
    assertFalse(openbisClient.expExists("ABI_SYSBIO", "QMARI", "QQQQQE3"));
  }

  @Test
  public void testExpExistsNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointer");
    assertFalse(openbisClient.expExists(null, null, null));
  }

  @Test
  public void testSampleExists() {
    assertTrue(openbisClient.sampleExists("QMARI074A9"));
    assertFalse(openbisClient.sampleExists("QQQQQ999XX"));
  }

  @Test
  public void testSampleExistsNull() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("NullPointer");
    assertFalse(openbisClient.sampleExists(null));
  }

  @Test
  public void testComputeProjectStatus() {
    //TODO
  }

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

  @Test
  public void testGetExperimentsForUser() {
    String user = "zxmqw74";
    List<Experiment> exps = openbisClient.getExperimentsForUser(user);
    assertThat(exps.size()).isAtLeast(1);
    assertThat(exps.get(0)).isInstanceOf(Experiment.class);
    assertExperimentCompletetlyFetched(exps.get(0));
  }

  @Test
  public void testGetExperimentsForUserNull() {
    exception.expect(UserFailureException.class);
    openbisClient.getExperimentsForUser(null);
  }

  @Test
  public void testGetExperimentsForUserEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getExperimentsForUser("");
  }

  @Test
  public void testGetExperimentsForUserNotExist() {
    //TODO should raise another exception
    exception.expect(AssertionError.class);
    openbisClient.getExperimentsForUser("zxxxxx12");
  }

  @Test
  public void testListExperimentsOfProjects() {
    Project qshow = openbisClient.getProjectByIdentifier("/CONFERENCE_DEMO/QTGPR");
    List<Experiment> exps = openbisClient.listExperimentsOfProjects(Arrays.asList(qshow));
    assertThat(exps.size()).isEqualTo(73);
  }

  @Test
  public void testListExperimentsOfProjectsNull() {
    exception.expect(NullPointerException.class);
    openbisClient.listExperimentsOfProjects(null);
  }

  @Test
  public void testListExperimentsOfProjectsNullInsideList() {
    exception.expect(NullPointerException.class);
    openbisClient.listExperimentsOfProjects(Arrays.asList(null));
  }

//  @Test
//  public void testListDataSetsForExperiments() {
//    List<DataSet> dsets =
//        openbisClient.listDataSetsForExperiments(Arrays.asList("/CONFERENCE_DEMO/QTGPR/QTGPRE6"));
//    assertThat(dsets.size()).isEqualTo(1);
//  }

  @Test
  public void testListSamplesForProjects() {
    List<Sample> samps = openbisClient.listSamplesForProjects(Arrays.asList("/CONFERENCE_DEMO/QTGPR"));
    assertThat(samps.size()).isAtLeast(117);

  }

//  @Test
//  public void testListDataSetsForSamples() {
//    assertThat(
//        openbisClient.listDataSetsForSamples(
//            Arrays.asList("/CONFERENCE_DEMO/NGSQTGPR003AL")).size()).isEqualTo(1);
//  }

  @Test
  public void testGetUrlForDataset() throws MalformedURLException {
    String code = "20150317113748250-9094";
    String file = "032_CRa_H9M5_THP1_NM104_0h_2_pos_RP_high_mr_QMARI074A9.mzML";
    assertEquals(openbisClient.getUrlForDataset(code, file).toString(),
        "https://qbis.qbic.uni-tuebingen.de:444/datastore_server/" + code + "/original/" + file
            + "?mode=simpleHtml&sessionID=" + openbisClient.getSessionToken());
    openbisClient.getUrlForDataset(code, "WRONG");
    openbisClient.getUrlForDataset("notacode", file);
  }

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

  @Test
  public void getSampleTypes() {
    //TODO maybe not the best test yet...
    Map<String, SampleType> types = openbisClient.getSampleTypes();
    assertThat(types.size()).isGreaterThan(0);
  }

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

  @Test
  public void testGetUserSpaces() {
    List<String> userSpaces = openbisClient.getUserSpaces("zxmqw74");
    assertThat(userSpaces.size()).isAtLeast(8);
  }

  @Test
  public void testGetUserSpacesNull() {
    exception.expect(UserFailureException.class);
    openbisClient.getUserSpaces(null);
  }


  @Test
  public void testGetUserSpacesEmpty() {
    exception.expect(UserFailureException.class);
    openbisClient.getUserSpaces("");
  }

  @Test
  public void testGetUserSpacesNotExist() {
    //TODO should raise an exception
    openbisClient.getUserSpaces("doesnotexist");
  }

//  @Test
//  public void testIsUserAdmin() {
//    openbisClient.isUserAdmin("zxmqw74");
//  }


}
