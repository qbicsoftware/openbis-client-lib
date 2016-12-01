package main;

import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.WordUtils;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType.SampleTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;


public class OpenBisClientMock implements IOpenBisClient, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6218517668296784635L;
  /**
   * 
   */
  // private static final long serialVersionUID = 3926210649301601498L;
  private String sessionToken = "mocktoken";
  private Random random;
  private String userId;
  private String serverURL;
  List<String> mockSpaceNames;
  List<Project> mockProjects;
  List<Experiment> mockExperiments = new ArrayList<Experiment>();
  List<Sample> mockSamples = new ArrayList<Sample>();
  List<SampleType> mockSampleTypes;
  Map<String, List<String>> mockVocabularies;

  private final String dss = "DSS1";

  public OpenBisClientMock(String loginid, String password, String serverURL) {
    random = new Random();
    this.userId = loginid;
    this.serverURL = serverURL;
    initMockData();
  }

  private Sample newMockSample(String space, String project, String exp, String type,
      List<Sample> parents, Map<String, String> props) {
    EntityRegistrationDetailsInitializer rdi = new EntityRegistrationDetailsInitializer();
    SampleInitializer si = new SampleInitializer();
    for (String key : props.keySet()) {
      si.putProperty(key, props.get(key));
    }
    si.putProperty("placeholder", "empty");
    si.setPermId(randomString(20));
    si.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    si.setId(random.nextLong());
    String code = "";
    if (type.equals("Q_BIOLOGICAL_ENTITY"))
      code = randomEntity(project);
    else
      code = randomBarcode(project);
    si.setCode(code);
    si.setSampleTypeId(random.nextLong());
    si.setSampleTypeCode(type);
    si.setExperimentIdentifierOrNull("/" + space + "/" + project + "/" + exp);
    si.setIdentifier("/" + space + "/" + project + "/" + si.getCode());
    si.setParents(parents);
    return new Sample(si);
  }

  private void initMockData() {
    mockVocabularies = new HashMap<String, List<String>>();
    mockVocabularies.put("Q_NCBI_TAXONOMY", new ArrayList<String>(
        Arrays.asList("Homo Sapiens", "Arabidopsis thaliana", "Mus musculus")));
    mockVocabularies.put("Q_PRIMARY_TISSUES",
        new ArrayList<String>(Arrays.asList("Liver", "Blood", "Other")));
    mockVocabularies.put("Q_MS_DEVICES", new ArrayList<String>(Arrays.asList("Thermo", "Waters")));
    mockVocabularies.put("Q_SAMPLE_TYPES",
        new ArrayList<String>(Arrays.asList("DNA", "RNA", "PROTEINS", "PEPTIDES")));
    mockVocabularies.put("Q_MS_FRACTIONATION_PROTOCOLS",
        new ArrayList<String>(Arrays.asList("GelC", "Other")));
    mockVocabularies.put("Q_DIGESTION_PROTEASES",
        new ArrayList<String>(Arrays.asList("Trypsin", "Pepsin")));
    mockVocabularies.put("Q_CHROMATOGRAPHY_TYPES",
        new ArrayList<String>(Arrays.asList("RP_UPLC_C18_COLUMN", "Other")));
    mockVocabularies.put("Q_MS_PROTOCOLS",
        new ArrayList<String>(
            Arrays.asList("PCT_Elite_HCD_Top15_230min", "PCT_Elite_HCD_Top15_470min",
                "PCT_Elite_rCID_Top20_60min", "PCT_Elite_rCID_Top20_90min")));

    mockSpaceNames = new ArrayList<String>(
        Arrays.asList("SPACE1", "SPACE2", "SPACE_3", "SPACE4", "LONG-NAME_SPACE_NUMERO_5"));
    mockProjects = new ArrayList<Project>();
    mockProjects.add(new Project("SPACE1", "QABCD"));
    mockProjects.add(new Project("SPACE1", "QAAAA"));
    mockProjects.add(new Project("SPACE2", "QBBBB"));

    EntityRegistrationDetailsInitializer rdi = new EntityRegistrationDetailsInitializer();

    List<String> expTypes =
        new ArrayList<String>(Arrays.asList("Q_EXPERIMENTAL_DESIGN", "Q_SAMPLE_EXTRACTION",
            "Q_SAMPLE_PREPARATION", "Q_MHC_LIGAND_EXTRACTION", "Q_MS_MEASUREMENT"));
    ExperimentInitializer ei = new ExperimentInitializer();
    ei.setCode("QABCDE1");
    ei.setId(random.nextLong());
    ei.setExperimentTypeCode(expTypes.get(1));
    ei.setIdentifier("/SPACE1/QABCD/" + ei.getCode());
    ei.setPermId(randomString(20));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    mockExperiments.add(new Experiment(ei));
    ei.setCode("QABCDE2");
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setId(random.nextLong());
    ei.setExperimentTypeCode(expTypes.get(2));
    ei.setIdentifier("/SPACE1/QABCD/" + ei.getCode());
    ei.setPermId(randomString(20));
    mockExperiments.add(new Experiment(ei));
    ei.setCode("QABCDE3");
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setId(random.nextLong());
    ei.setExperimentTypeCode(expTypes.get(3));
    ei.setPermId(randomString(20));
    ei.setIdentifier("/SPACE1/QABCD/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));
    ei.setCode("QABCDE4");
    ei.setPermId(randomString(20));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setId(random.nextLong());
    ei.setExperimentTypeCode(expTypes.get(3));
    ei.setIdentifier("/SPACE1/QABCD/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));

    ei.setCode("QAAAAE1");
    ei.setPermId(randomString(20));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setId(random.nextLong());
    ei.setExperimentTypeCode(expTypes.get(1));
    ei.setIdentifier("/SPACE1/QAAAA/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));

    ei.setPermId(randomString(20));
    ei.setCode("QAAAAE2");
    ei.setId(random.nextLong());
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setExperimentTypeCode(expTypes.get(2));
    ei.setIdentifier("/SPACE1/QAAAA/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));
    ei.setCode("QAAAAE3");
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setPermId(randomString(20));
    ei.setId(random.nextLong());
    ei.setExperimentTypeCode(expTypes.get(3));
    ei.setIdentifier("/SPACE1/QAAAA/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setCode("QBBBBE1");
    ei.setId(random.nextLong());
    ei.setPermId(randomString(20));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setExperimentTypeCode(expTypes.get(1));
    ei.setIdentifier("/SPACE2/QBBBB/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));
    ei.setCode("QBBBBE2");
    ei.setId(random.nextLong());
    ei.setPermId(randomString(20));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setExperimentTypeCode(expTypes.get(2));
    ei.setIdentifier("/SPACE2/QBBBB/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));
    ei.setPermId(randomString(20));
    ei.setRegistrationDetails(new EntityRegistrationDetails(rdi));
    ei.setId(random.nextLong());
    ei.setCode("QBBBBE3");
    ei.setExperimentTypeCode(expTypes.get(3));
    ei.setIdentifier("/SPACE2/QBBBB/" + ei.getCode());
    mockExperiments.add(new Experiment(ei));

    List<Sample> p1e1 = new ArrayList<Sample>();

    p1e1.add(newMockSample("SPACE1", "QABCD", "QABCDE1", "Q_BIOLOGICAL_ENTITY", null,
        new HashMap<String, String>()));
    p1e1.add(newMockSample("SPACE1", "QABCD", "QABCDE1", "Q_BIOLOGICAL_ENTITY", null,
        new HashMap<String, String>()));
    p1e1.add(newMockSample("SPACE1", "QABCD", "QABCDE1", "Q_BIOLOGICAL_ENTITY", null,
        new HashMap<String, String>()));

    List<Sample> p1e2 = new ArrayList<Sample>();

    Map<String, String> props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#1");
    props.put("Q_SECONDARY_NAME", "tissue extract 1");
    props.put("Q_PRIMARY_TISSUE", "Liver");
    Sample s = newMockSample("SPACE1", "QABCD", "QABCDE2", "Q_BIOLOGICAL_SAMPLE",
        new ArrayList<Sample>(Arrays.asList(p1e1.get(0))), props);

    p1e2.add(s);
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#2");
    props.put("Q_SECONDARY_NAME", "tissue extract 2");
    props.put("Q_PRIMARY_TISSUE", "Liver");
    s = newMockSample("SPACE1", "QABCD", "QABCDE2", "Q_BIOLOGICAL_SAMPLE",
        new ArrayList<Sample>(Arrays.asList(p1e1.get(1))), props);

    p1e2.add(s);
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#3");
    props.put("Q_SECONDARY_NAME", "tissue extract 3");
    props.put("Q_PRIMARY_TISSUE", "Liver");
    s = newMockSample("SPACE1", "QABCD", "QABCDE2", "Q_BIOLOGICAL_SAMPLE",
        new ArrayList<Sample>(Arrays.asList(p1e1.get(2))), props);
    p1e2.add(s);
    List<Sample> p1e3 = new ArrayList<Sample>();
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#1");
    props.put("Q_SECONDARY_NAME", "protein extract 1");
    props.put("Q_SAMPLE_TYPE", "PROTEINS");
    s = newMockSample("SPACE1", "QABCD", "QABCDE3", "Q_TEST_SAMPLE",
        new ArrayList<Sample>(Arrays.asList(p1e2.get(0))), props);

    p1e3.add(s);
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#2");
    props.put("Q_SECONDARY_NAME", "protein extract 2");
    props.put("Q_SAMPLE_TYPE", "PROTEINS");
    s = newMockSample("SPACE1", "QABCD", "QABCDE3", "Q_TEST_SAMPLE",
        new ArrayList<Sample>(Arrays.asList(p1e2.get(1))), props);

    p1e3.add(s);
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#3");
    props.put("Q_SECONDARY_NAME", "protein extract 3");
    props.put("Q_SAMPLE_TYPE", "PROTEINS");
    s = newMockSample("SPACE1", "QABCD", "QABCDE3", "Q_TEST_SAMPLE",
        new ArrayList<Sample>(Arrays.asList(p1e2.get(2))), props);

    p1e3.add(s);

    List<Sample> p1e4 = new ArrayList<Sample>();

    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#1");
    props.put("Q_SECONDARY_NAME", "dna extract 1");
    props.put("Q_SAMPLE_TYPE", "DNA");
    s = newMockSample("SPACE1", "QABCD", "QABCDE4", "Q_TEST_SAMPLE", p1e2, props);

    p1e4.add(s);
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#2");
    props.put("Q_SECONDARY_NAME", "dna extract 2");
    props.put("Q_SAMPLE_TYPE", "DNA");
    s = newMockSample("SPACE1", "QABCD", "QABCDE4", "Q_TEST_SAMPLE", p1e2, props);

    p1e4.add(s);
    props = new HashMap<String, String>();
    props.put("Q_EXTERNALDB_ID", "#3");
    props.put("Q_SECONDARY_NAME", "dna extract 3");
    props.put("Q_SAMPLE_TYPE", "DNA");
    s = newMockSample("SPACE1", "QABCD", "QABCDE4", "Q_TEST_SAMPLE", p1e2, props);
    p1e4.add(s);

    mockSamples.addAll(p1e1);
    mockSamples.addAll(p1e2);
    mockSamples.addAll(p1e3);
    mockSamples.addAll(p1e4);

    mockSampleTypes = new ArrayList<SampleType>();
    SampleTypeInitializer ti = new SampleTypeInitializer();
    ti.setCode("Q_BIOLOGICAL_ENTITY");
    mockSampleTypes.add(new SampleType(ti));
    ti = new SampleTypeInitializer();
    ti.setCode("Q_BIOLOGICAL_SAMPLE");
    mockSampleTypes.add(new SampleType(ti));
    ti = new SampleTypeInitializer();
    ti.setCode("Q_TEST_SAMPLE");
    mockSampleTypes.add(new SampleType(ti));
    ti = new SampleTypeInitializer();
    ti.setCode("Q_MS_MEASUREMENT");
    mockSampleTypes.add(new SampleType(ti));

    initVocabs();
  }

  public static void main(String[] args) {
    OpenBisClientMock openbis = new OpenBisClientMock("", "", "");
    openbis.login();
    Map<String, String> taxMap = openbis.getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
    Map<String, String> tissueMap = openbis.getVocabCodesAndLabelsForVocab("Q_PRIMARY_TISSUES");
    Map<String, String> deviceMap = openbis.getVocabCodesAndLabelsForVocab("Q_MS_DEVICES");
    Map<String, String> cellLinesMap = openbis.getVocabCodesAndLabelsForVocab("Q_CELL_LINES");
    List<String> sampleTypes = openbis.getVocabCodesForVocab("Q_SAMPLE_TYPES");
    List<String> fractionationTypes = openbis.getVocabCodesForVocab("Q_MS_FRACTIONATION_PROTOCOLS");
    List<String> enzymes = openbis.getVocabCodesForVocab("Q_DIGESTION_PROTEASES");
    Map<String, String> antibodiesWithLabels = openbis.getVocabCodesAndLabelsForVocab("Q_ANTIBODY");
    List<String> msProtocols = openbis.getVocabCodesForVocab("Q_MS_PROTOCOLS");
    List<String> lcmsMethods = openbis.getVocabCodesForVocab("Q_MS_LCMS_METHODS");
    List<String> chromTypes = openbis.getVocabCodesForVocab("Q_CHROMATOGRAPHY_TYPES");
  }

  private void initVocabs() {
    //
    // VocabularyInitializer vi = new VocabularyInitializer();
    // vi.setCode(code);
    // vi.setTerms(terms);
  }

  /**
   * Checks if we are logged in
   */
  public boolean loggedin() {
    System.out.println("mock logged in");
    return true;
  }

  /**
   * logs out of the OpenBIS server
   */
  public void logout() {
    System.out.println("mock logged out");
  }

  /**
   * logs in to the OpenBIS server with the system userid after calling this function, the user has
   * to provide the password
   */
  public void login() {
    if (this.loggedin())
      this.logout();
  }

  /**
   * Get session token of current openBIS session
   * 
   * @return session token as string
   */
  public String getSessionToken() {
    return this.sessionToken;
  }

  /**
   * Get a openBIS service facade when logged in to get functionality to retrieve data for example.
   * 
   * @return a IOpenbisServiceFacade which provides various functions
   */
  public IOpenbisServiceFacade getFacade() {
    ensureLoggedIn();
    return null;
  }

  /**
   * Getter function for GeneralInformation Service
   * 
   * @return GeneralInformationService instance
   */
  public IGeneralInformationService getOpenbisInfoService() {
    return null;
  }

  /**
   * Setter function for GeneralInformation Service
   * 
   * @param openbisInfoService a GeneralInformationService instance
   * @return
   */
  public void setOpenbisInfoService(IGeneralInformationService openbisInfoService) {}

  /**
   * Logs out before garbage is collected
   */
  protected void finalize() throws Throwable {
    this.logout();
    super.finalize();
  }

  /**
   * Checks if logged in, reconnects if not
   */
  public void ensureLoggedIn() {}

  private String randomString(int length) {
    char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWX_-".toCharArray();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      char c = chars[random.nextInt(chars.length)];
      sb.append(c);
    }
    return sb.toString();
  }

  private String randomEntity(String project) {
    String base = project + "ENTITY-";
    char[] chars = "123456789".toCharArray();
    char c = chars[random.nextInt(chars.length)];
    return base + Character.toString(c);
  }

  private String randomBarcode(String project) {
    String base = project;
    int num = random.nextInt(999) + 1;
    base += Integer.toString(num) + "A";
    return base + checksum(base);
  }

  private String randomExperiment(String project) {
    char[] chars = "123456789".toCharArray();
    int num = random.nextInt(12);
    return project + "E" + Integer.toString(num);
  }

  /**
   * Function to get all spaces which are registered in this openBIS instance
   * 
   * @return list with the identifiers of all available spaces
   */
  public List<String> listSpaces() {
    return mockSpaceNames;
  }

  /**
   * ingests external ids given a mapping between QBiC sample IDs and the external ids
   * 
   * @param idMap
   */
  public void setExternalIDs(Map<String, String> idMap) {
    Map<String, Object> params = new HashMap<String, Object>();
    List<String> ids = new ArrayList<String>(idMap.keySet());
    List<String> types = new ArrayList<String>(Arrays.asList("Q_EXTERNALDB_ID"));
    params.put("identifiers", ids);
    params.put("types", types);
    params.put("Q_EXTERNALDB_ID", idMap);
    System.out.println("mock ingesting " + params);
  }

  /**
   * Function to get all projects which are registered in this openBIS instance
   * 
   * @return list with all projects which are registered in this openBIS instance
   */
  public List<Project> listProjects() {
    return mockProjects;
  }

  /**
   * Function to list all Experiments which are registered in the openBIS instance.
   * 
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> listExperiments() {
    return mockExperiments;
  }

  // TODO use search service with experiment code ?
  /**
   * Function to retrieve all samples of a given experiment Note: seems to throw a
   * ch.systemsx.cisd.common.exceptions.UserFailureException if wrong identifier given TODO Should
   * we catch it and throw an illegalargumentexception instead? would be a lot clearer in my opinion
   * 
   * @param experimentIdentifier identifier/code (both should work) of the openBIS experiment
   * @return list with all samples of the given experiment
   * 
   */
  public List<Sample> getSamplesofExperiment(String experimentIdentifier) {
    List<Sample> res = new ArrayList<Sample>();

    for (Sample s : mockSamples) {
      if (experimentIdentifier.equals(s.getExperimentIdentifierOrNull()))
        res.add(s);
    }
    return res;
  }

  /**
   * Function to retrieve all samples of a given space
   * 
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all samples of the given space
   */
  public List<Sample> getSamplesofSpace(String spaceIdentifier) {
    List<Sample> res = new ArrayList<Sample>();

    for (Sample s : mockSamples) {
      if (spaceIdentifier.equals(s.getSpaceCode()))// TODO space null?
        res.add(s);
    }
    return res;
  }

  /**
   * Updates or adds properties for one sample
   * 
   * @param sampleID ID of long type, can be fetched from openBIS samples
   * @param properties A map of all sample properties to be updated
   */
  public void updateSampleMetadata(long sampleID, Map<String, String> properties) {
    System.out.println("mock updating sample metadata");
  }

  /**
   * Function to retrieve a sample by it's identifier or code Note: seems to throw a
   * java.lang.IndexOutOfBoundsException if wrong identifier given TODO Should we catch it and throw
   * an illegalargumentexception instead? would be a lot clearer in my opinion
   * 
   * @param sampleIdentifier identifier or code of the sample
   * @return the sample with the given identifier
   */
  public Sample getSampleByIdentifier(String sampleIdentifier) {
    for (Sample s : mockSamples) {
      if (sampleIdentifier.equals(s.getIdentifier()))
        return s;
    }
    return null;
  }

  /**
   * Function to get all samples of a specific project
   * 
   * @param projIdentifierOrCode identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesOfProject(String projIdentifier) {
    List<Sample> res = new ArrayList<Sample>();

    for (Sample s : mockSamples) {
      String space = s.getSpaceCode();
      String proj = s.getIdentifier().split("/")[2];
      if (("/" + space + "/" + proj).equals(projIdentifier))
        res.add(s);
    }
    return res;
  }

  /**
   * Function to get all samples of a specific project
   * 
   * @param projIdentifierOrCode identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesOfProjectBySearchService(String projIdentifier) {
    return getSamplesOfProject(projIdentifier);
  }

  /**
   * Function to get all samples with their parents and children of a specific project
   * 
   * @param projIdentifierOrCode identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesWithParentsAndChildrenOfProjectBySearchService(
      String projIdentifier) {
    return getSamplesOfProject(projIdentifier);
  }

  /**
   * Function to get a sample with its parents and children
   * 
   * @param sampCode code of the openBIS sample
   * @return sample
   */
  public List<Sample> getSamplesWithParentsAndChildren(String sampCode) {
    List<Sample> res = new ArrayList<Sample>();

    for (Sample s : mockSamples) {
      if (sampCode.equals(s.getCode())) {
        res.add(s);
        res.addAll(s.getChildren());
        res.addAll(s.getParents());
      }
    }
    return res;
  }


  /**
   * returns a list of all Experiments connected to the project with the identifier from openBis
   * 
   * @param projectIdentifier identifier of the given openBIS project
   * @return list of all experiments of the given project
   */
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier) {
    List<Experiment> res = new ArrayList<Experiment>();

    for (Experiment e : mockExperiments) {
      String[] splt = e.getIdentifier().split("/");
      String projID = "/" + splt[1] + "/" + splt[2];
      if (projectIdentifier.equals(projID))// TODO space null?
        res.add(e);
    }
    return res;
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance. av: 19353 ms
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject(Project project) {
    return this.getExperimentsOfProjectByIdentifier(project.getIdentifier());
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance. runtime for all projects: samples: 25, 4 threads max: 14496 average: 13705.6 median:
   * 13908
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject2(Project project) {
    return getExperimentsForProject(project);
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject2(String projectCode) {
    List<Experiment> res = new ArrayList<Experiment>();

    for (Experiment e : mockExperiments) {
      String[] splt = e.getIdentifier().split("/");
      String proj = splt[2];
      if (projectCode.equals(proj))
        res.add(e);
    }
    return res;
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   * 
   * samples: 25, 4 threads max: 15627 average: 14582.08 median: 15031
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject3(Project project) {
    return getExperimentsForProject(project);
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject3(String projectCode) {
    return getExperimentsForProject2(projectCode);
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   * 
   * @param projectIdentifer project identifer as defined by openbis, for which the experiments
   *        should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject(String projectIdentifier) {
    return this.getExperimentsOfProjectByIdentifier(projectIdentifier);
  }


  /**
   * returns a list of all Experiments connected to a Project code in openBIS
   * 
   * @param projectCode code of the given openBIS project
   * @return list of all experiments of the given project
   */
  public List<Experiment> getExperimentsOfProjectByCode(String projectCode) {
    return getExperimentsForProject2(projectCode);
  }

  /**
   * Function to get all experiments for a given space and the information to which project the
   * corresponding experiment belongs to
   * 
   * @param spaceIdentifier identifier of a openBIS space
   * @return map containing all projects (keys) and lists with all connected experiments (values)
   */
  public Map<String, List<Experiment>> getProjectExperimentMapping(String spaceIdentifier) {
    Map<String, List<Experiment>> mapping = new HashMap<String, List<Experiment>>();

    for (Experiment e : this.getExperimentsOfSpace(spaceIdentifier)) {
      String[] splitted = e.getIdentifier().split("/");
      String key = "/" + splitted[1] + "/" + splitted[2];
      List<Experiment> value = mapping.get(key);

      if (value != null) {
        mapping.get(key).add(e);
      } else {
        List<Experiment> exps = new ArrayList<Experiment>();
        exps.add(e);
        mapping.put(key, exps);
      }
    }

    return mapping;
  }

  /**
   * Function to retrieve all experiments of a given space
   * 
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all experiments connected to this space
   */
  public List<Experiment> getExperimentsOfSpace(String spaceIdentifier) {
    List<Experiment> res = new ArrayList<Experiment>();

    for (Experiment e : mockExperiments) {
      String[] splt = e.getIdentifier().split("/");
      String space = splt[1];
      if (spaceIdentifier.equals(space))
        res.add(e);
    }
    return res;
  }

  /**
   * Function to retrieve all experiments of a specific given type
   * 
   * @param type identifier of the openBIS experiment type
   * @return list with all experiments of this given type
   */
  public List<Experiment> getExperimentsOfType(String type) {
    List<Experiment> res = new ArrayList<Experiment>();

    for (Experiment e : mockExperiments) {
      if (type.equals(e.getExperimentTypeCode()))
        res.add(e);
    }
    return res;
  }

  /**
   * Function to retrieve all samples of a specific given type
   * 
   * @param type identifier of the openBIS sample type
   * @return list with all samples of this given type
   */
  public List<Sample> getSamplesOfType(String type) {
    List<Sample> res = new ArrayList<Sample>();

    for (Sample s : mockSamples) {
      if (type.equals(s.getSampleTypeCode()))
        res.add(s);
    }
    return res;
  }



  /**
   * Function to retrieve all projects of a given space from openBIS.
   * 
   * @param space identifier of the openBIS space
   * @return a list with all projects objects for the given space
   */
  public List<Project> getProjectsOfSpace(String space) {
    List<Project> res = new ArrayList<Project>();

    for (Project p : mockProjects) {
      if (space.equals(p.getSpaceCode()))
        res.add(p);
    }
    return res;
  }

  /**
   * Function to retrieve a project from openBIS by the identifier of the project.
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return project with the given id
   */
  public Project getProjectByIdentifier(String projectIdentifier) {
    for (Project p : mockProjects) {
      if (projectIdentifier.equals(p.getIdentifier()))
        return p;
    }
    return null;
  }

  /**
   * Function to retrieve a project from openBIS by the code of the project.
   * 
   * @param projectCode code of the openBIS project
   * @return project with the given code
   */
  public Project getProjectByCode(String projectCode) {
    for (Project p : mockProjects) {
      if (projectCode.equals(p.getCode()))
        return p;
    }
    return null;
  }

  /**
   * Function to retrieve a experiment from openBIS by the code of the experiment.
   * 
   * @param experimentCode code of the openBIS experiment
   * @return experiment with the given code
   */
  public Experiment getExperimentByCode(String experimentCode) {
    for (Experiment e : mockExperiments) {
      if (experimentCode.equals(e.getCode()))
        return e;
    }
    return null;
  }

  /**
   * Function to retrieve a experiment from openBIS by the code of the experiment.
   * 
   * @param experimentId id of the openBIS experiment
   * @return experiment with the given code
   */
  public Experiment getExperimentById(String experimentId) {
    for (Experiment e : mockExperiments) {
      if (experimentId.equals(e.getIdentifier()))
        return e;
    }
    return null;
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance. runtime for all projects: samples: 25, 4 threads max: 14496 average: 13705.6 median:
   * 13908
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentById2(String expIdentifer) {
    return new ArrayList<Experiment>(Arrays.asList(getExperimentById(expIdentifer)));
  }



  /**
   * Function to retrieve the project of an experiment from openBIS
   * 
   * @param experimentIdentifier identifier of the openBIS experiment
   * @return project connected to the given experiment
   */
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    Experiment e = getExperimentById(experimentIdentifier);
    return getProjectByCode(e.getIdentifier().split("/")[2]);

  }

  /**
   * Function to list all datasets of a specific sample (watch out there are different dataset
   * classes)
   * 
   * @param sampleIdentifier identifier of the openBIS sample
   * @return list with all datasets of the given sample
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfSampleByIdentifier(
      String sampleIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific sample (watch out there are different dataset
   * classes)
   * 
   * @param sampleCode code or identifier of the openBIS sample
   * @return list with all datasets of the given sample
   */
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfSample(
      String sampleCode) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific experiment (watch out there are different dataset
   * classes)
   * 
   * @param experimentPermID permId of the openBIS experiment
   * @return list with all datasets of the given experiment
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfExperiment(
      String experimentPermID) {
    return null;// TODO
  }

  /**
   * Returns all datasets of a given experiment. The new version should run smoother
   * 
   * @param experimentIdentifier identifier or code of the openbis experiment
   * @return list of all datasets of the given experiment
   */
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfExperimentByIdentifier(
      String experimentIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific openBIS space
   * 
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all datasets of the given space
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfSpaceByIdentifier(
      String spaceIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfProjectByIdentifier(
      String projectIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<DataSet> getDataSetsOfProjectByIdentifierWithSearchCriteria(
      String projectIdentifier) {
    return null;// TODO
  }

  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getClientDatasetsOfProjectByIdentifierWithSearchCriteria(
      String projectIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific openBIS experiment
   * 
   * @param experimentCode code of the openBIS experiment
   * @return list with all datasets of the given experiment
   */
  public List<DataSet> getDataSetsOfExperimentByCodeWithSearchCriteria(String experimentCode) {
    return null;// TODO
  }


  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getClientDataSetsOfExperimentByCodeWithSearchCriteria(
      String experimentCode) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfProjects2(
      List<Project> projectIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all datasets of a specific type
   * 
   * @param type identifier of the openBIS type
   * @return list with all datasets of the given type
   */
  public List<DataSet> getDataSetsByType(String type) {
    return null;// TODO
  }

  /**
   * Function to list all attachments of a sample
   * 
   * @param sampleIdentifier identifier of the openBIS sample
   * @return list with all attachments connected to the given sample
   */
  public List<Attachment> listAttachmentsForSampleByIdentifier(String sampleIdentifier) {
    return null;// TODO
  }

  /**
   * Function to list all attachments of a project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all attachments connected to the given project
   */
  public List<Attachment> listAttachmentsForProjectByIdentifier(String projectIdentifier) {
    return null;// TODO
  }

  /**
   * Function to add an attachment to a existing project in openBIS by calling the corresponding
   * ingestion service of openBIS. TODO specify which parameters have to be there may not work yet!
   * 
   * @param parameter map with needed information for registration process by ingestion service
   */
  public void addAttachmentToProject(Map<String, Object> parameter) {
    System.out.println("mock added attachtment to project");
  }

  /**
   * Queries an aggregation service for openBIS data
   * 
   * @param name the name of the aggregation service, as specified in the config file of the openBIS
   *        instance
   * @param parameters a map of parameters
   * @return a QueryTableModel object containing the aggregated information
   */
  public QueryTableModel getAggregationService(String name, Map<String, Object> parameters) {
    return null;// TODO
  }

  /**
   * Returns all users of a Space.
   * 
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  public Set<String> getSpaceMembers(String spaceCode) {
    return null;// TODO
  }

  /**
   * Function to retrieve all properties which have been assigned to a specific entity type
   * 
   * @param entity_type entitiy type
   * @return list of properties which are assigned to the entity type
   */
  public List<PropertyType> listPropertiesForType(EntityType entity_type) {
    // PropertyType p = new PropertyType(initializer);TODO
    return null;
  }

  /**
   * Function to list the vocabulary terms for a given property which has been added to openBIS. The
   * property has to be a Controlled Vocabulary Property.
   * 
   * @param property the property type
   * @return list of the vocabulary terms of the given property
   */
  public List<String> listVocabularyTermsForProperty(PropertyType property) {
    List<String> terms = new ArrayList<String>();
    ControlledVocabularyPropertyType controlled_vocab = (ControlledVocabularyPropertyType) property;
    for (VocabularyTerm term : controlled_vocab.getTerms()) {
      terms.add(term.getLabel().toString());
    }
    return terms;
  }

  /**
   * Function to get the label of a CV item for some property
   * 
   * @param propertyType the property type
   * @param propertyValue the property value
   * @return Label of CV item
   */
  public String getCVLabelForProperty(PropertyType propertyType, String propertyValue) {
    ControlledVocabularyPropertyType controlled_vocab =
        (ControlledVocabularyPropertyType) propertyType;

    for (VocabularyTerm term : controlled_vocab.getTerms()) {
      if (term.getCode().equals(propertyValue)) {
        return term.getLabel();
      }
    }
    throw new IllegalArgumentException();
  }


  /**
   * Function to get a SampleType object of a sample type
   * 
   * @param sampleType the sample type as string
   * @return the SampleType object of the corresponding sample type
   */
  public SampleType getSampleTypeByString(String sampleType) {

    List<SampleType> types = this.getFacade().listSampleTypes();
    SampleType st = null;
    for (SampleType t : types) {
      if (t.getCode().equals(sampleType)) {
        st = t;
      }
    }
    return st;
  }

  /**
   * Function to retrieve all samples of a specific given type
   * 
   * @param type identifier of the openBIS sample type
   * @return list with all samples of this given type
   */
  public Map<String, SampleType> getSampleTypes() {
    List<SampleType> sampleTypes = this.getFacade().listSampleTypes();
    HashMap<String, SampleType> types = new HashMap<String, SampleType>(sampleTypes.size());
    for (SampleType t : sampleTypes) {
      types.put(t.getCode(), t);
    }
    return types;
  }



  /**
   * Function to get a ExperimentType object of a experiment type
   * 
   * @param experimentType the experiment type as string
   * @return the ExperimentType object of the corresponding experiment type
   */
  public ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType getExperimentTypeByString(
      String experimentType) {
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType> types =
        this.getFacade().listExperimentTypes();
    ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType st = null;
    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType t : types) {
      if (t.getCode().equals(experimentType)) {
        st = t;
      }
    }
    return st;
  }

  /**
   * Function to get the labels of all property types of a specific instance type
   * 
   * @param entityType the instance type
   * @return map with types as keys and labels as values
   */
  public Map<String, String> getLabelsofProperties(EntityType entityType) {
    List<PropertyType> prop_types = this.listPropertiesForType(entityType);
    Map<String, String> prop_types_labels = new HashMap<String, String>();

    for (PropertyType pt : prop_types) {
      prop_types_labels.put(pt.getCode(), pt.getLabel());
    }
    return prop_types_labels;
  }

  /**
   * Function to trigger ingestion services registered in openBIS
   * 
   * @param serviceName name of the ingestion service which should be triggered
   * @param parameters map with needed information for registration process
   * @return object name of the QueryTableModel which is returned by the aggregation service
   */
  public String triggerIngestionService(String serviceName, Map<String, Object> parameters) {
    return "mock triggered ingestion service";
  }

  // TODO specify parameters needed for ingestion service
  /**
   * Function to add children samples to a sample (parent) using the corresponding ingestition
   * service
   * 
   * @param parameters map with needed information for registration process
   * @return object name of the QueryTableModel which is returned by the aggregation service
   */
  public String addParentChildConnection(Map<String, Object> parameters) {
    return "mock triggered add parent child connection ingestion service";
  }

  // TODO probably not needed anymore
  /**
   * Function to trigger the registration of new openBIS instances like projects, experiments and
   * samples. (This function also used to trigger the barcode generation for samples.)
   * 
   * @param params map with needed information for registration process
   * @param name name of the service for the corresponding registration
   * @param number_of_samples_offset offset to generate correct barcodes (depending on number of
   *        samples) by accounting for delay of registration process
   * @return object name of the QueryTableModel which is returned by the aggregation service
   */
  public String addNewInstance(Map<String, Object> params, String service,
      int number_of_samples_offset) {
    return "mock added instance";
  }

  /**
   * Function to create a QBiC barcode string for a sample based on the project ID. QBiC barcode
   * format: Q + project_ID + sample number + X + checksum
   * 
   * TODO check if it works for all cases, check for null ?
   * 
   * @param proj ID of the project
   * @return the QBiC barcode as string
   */
  public String generateBarcode(String proj, int number_of_samples_offset) {
    Project project = this.getProjectByIdentifier(proj);
    // Project project = getProjectofExperiment(exp);
    int number_of_samples = getSamplesOfProject(project.getCode()).size();
    // System.out.println(number_of_samples);

    String barcode = project.getCode() + String.format("%03d", (number_of_samples + 1)) + "S";
    // String barcode = project.getCode() + String.format("%03d", Math.max(1, number_of_samples +
    // number_of_samples_offset)) + "S";
    barcode += checksum(barcode);
    return barcode;
  }

  /**
   * Function map an integer value to a char
   * 
   * @param i the integer value which should be mapped
   * @return the resulting char value
   */
  public static char mapToChar(int i) {
    i += 48;
    if (i > 57) {
      i += 7;
    }
    return (char) i;
  }

  /**
   * Function to generate the checksum for the given barcode string
   * 
   * @param s the barcode string
   * @return the checksum for the given barcode
   */
  public static char checksum(String s) {
    int i = 1;
    int sum = 0;
    for (int idx = 0; idx <= s.length() - 1; idx++) {
      sum += (((int) s.charAt(idx))) * i;
      i += 1;
    }
    return mapToChar(sum % 34);
  }

  /**
   * Function to transform openBIS entity type to human readable text. Performs String replacement
   * and does not query openBIS!
   * 
   * @param entityCode the entity code as string
   * @return entity code as string in human readable text
   */
  public String openBIScodeToString(String entityCode) {
    entityCode = WordUtils.capitalizeFully(entityCode.replace("_", " ").toLowerCase());
    String edit_string = entityCode.replace("Ngs", "NGS").replace("Hla", "HLA")
        .replace("Rna", "RNA").replace("Dna", "DNA").replace("Ms", "MS");
    if (edit_string.startsWith("Q ")) {
      edit_string = edit_string.replace("Q ", "");
    }
    return edit_string;
  }

  /**
   * Function to get the download url for a file stored in the openBIS datastore server. Note that
   * this method does no checks, whether datasetcode or openbisFilename do exist. Deprecated: Use
   * getUrlForDataset() instead
   * 
   * 
   * @throws MalformedURLException Returns an download url for the openbis dataset with the given
   *         code and dataset_type. Throughs MalformedURLException if a url can not be created from
   *         the given parameters. NOTE: datastoreURL differs from serverURL only by the port ->
   *         quick hack used
   * @param dataSetCode code of the openBIS dataset
   * @param openbisFilename name of the file stored in the given dataset
   * @return URL object of the download url for the given file
   */
  @Deprecated
  public URL getDataStoreDownloadURL(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    String base = this.serverURL.split(".de")[0] + ".de";
    String downloadURL = base + ":444";
    downloadURL += "/datastore_server/";

    downloadURL += dataSetCode;
    downloadURL += "/original/";
    downloadURL += openbisFilename;
    downloadURL += "?mode=simpleHtml&sessionID=";
    downloadURL += this.getSessionToken();
    return new URL(downloadURL);
  }

  public URL getDataStoreDownloadURLLessGeneric(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    String base = this.serverURL.split(".de")[0] + ".de";
    String downloadURL = base + ":444";
    downloadURL += "/datastore_server/";

    downloadURL += dataSetCode;
    downloadURL += "/";
    downloadURL += openbisFilename;
    downloadURL += "?mode=simpleHtml&sessionID=";
    downloadURL += this.getSessionToken();
    return new URL(downloadURL);
  }

  /**
   * Returns a Map that maps samples to a list of samples of their parent samples
   * 
   * @param samples A list of openBIS samples
   * @return Map<Sample, List<Sample>> containing a mapping between children and parents of samples
   */
  public Map<Sample, List<Sample>> getParentMap(List<Sample> samples) {
    Map<Sample, List<Sample>> results = new HashMap<Sample, List<Sample>>();
    for (Sample p : samples) {
      String permID = p.getPermId();
      List<Sample> children = getFacade().listSamplesOfSample(permID);
      for (Sample c : children) {
        if (results.containsKey(c))
          results.get(c).add(p);
        else
          results.put(c, new ArrayList<Sample>(Arrays.asList(p)));
      }
    }
    return results;
  }

  /**
   * used by getProjectTSV to get the Patient(s)/Source(s) of a list of samples
   */
  private String fetchSource(List<Sample> samples, List<VocabularyTerm> terms, List<String> res) {
    Set<Sample> roots = new HashSet<Sample>();
    while (!samples.isEmpty()) {
      Set<Sample> store = new HashSet<Sample>();
      for (Sample sample : samples) {
        if (!sample.getSampleTypeCode().equals("Q_BIOLOGICAL_ENTITY"))
          store.addAll(sample.getParents());
        else
          roots.add(sample);
      }
      samples = new ArrayList<Sample>(store);
    }
    for (Sample sample : roots) {
      String id = sample.getCode();
      try {
        id = id.split("-")[1];
      } catch (ArrayIndexOutOfBoundsException e) {
      }
      String organism = sample.getProperties().get("Q_NCBI_ORGANISM");
      if (organism != null) {
        String desc = "";
        for (VocabularyTerm term : terms) {
          if (organism.equals(term.getCode()))
            desc = term.getLabel();
        }
        if (desc.toLowerCase().equals("homo sapiens"))
          desc = "Patient";
        res.add(desc + ' ' + id);
      } else
        res.add("unknown source");
    }
    String[] resArr = new String[res.size()];
    resArr = res.toArray(resArr);
    return StringUtils.join(resArr, "+");
  }

  /**
   * TODO implement function that, given a project, returns a graph model of the samples. probably
   * use SampleFetchOption.DESCENDANTS
   */

  /**
   * Returns lines of a spreadsheet of humanly readable information of the samples in a project.
   * Only one requested layer of the data model is returned. Experimental factors are returned in
   * the properties xml format and should be parsed before the spreadsheet is presented to the user.
   * 
   * @param projectCode The 5 letter QBiC code of the project
   * @param sampleType The openBIS sampleType that should be included in the result
   * @return
   */
  public List<String> getProjectTSV(String projectCode, String sampleType) {
    List<String> res = new ArrayList<String>();
    List<Sample> allSamples = mockSamples;
    // filter all samples by types
    List<Sample> samples = new ArrayList<Sample>();
    for (Sample s : allSamples) {
      if (sampleType.equals(s.getSampleTypeCode()))
        samples.add(s);
    }
    // sort remaining samples-
    // Arrays.sort(samples);

    Vocabulary voc = getVocabulary("Q_NCBI_TAXONOMY");
    String header = "QBiC Code\tSecondary Name\tSource Name\tExternal ID\tSample Type\tAttributes";
    res.add(header);
    for (Sample sample : samples) {
      String code = sample.getCode();
      String row = "";
      row += code + "\t";
      String secName = sample.getProperties().get("Q_SECONDARY_NAME");
      if (secName == null)
        secName = "";
      row += secName + "\t";
      row += fetchSource(new ArrayList<Sample>(Arrays.asList(sample)), voc.getTerms(),
          new ArrayList<String>()) + "\t";
      String extID = sample.getProperties().get("Q_EXTERNALDB_ID");
      if (extID == null)
        extID = "";
      row += extID + "\t";
      String extrType = sample.getProperties().get("Q_PRIMARY_TISSUE");
      if (extrType == null)
        extrType = sample.getProperties().get("Q_SAMPLE_TYPE");
      if (extrType == null)
        extrType = "";
      if (extrType.equals("CELL_LINE"))
        extrType = sample.getProperties().get("Q_TISSUE_DETAILED");
      row += extrType + "\t";
      // row += sample.getSampleTypeCode()+ "\t";
      String props = sample.getProperties().get("Q_PROPERTIES");
      if (props == null)
        props = "";
      row += props;

      res.add(row);
    }
    return res;
  }

  /**
   * Function to retrieve parent samples of a sample
   * 
   * @param sampleCode Code of the query sample
   * @return List of parent samples
   */
  public List<Sample> getChildrenBySearchService(String sampleCode) {
    for (Sample s : mockSamples) {
      if (s.getCode().equals(sampleCode))
        return s.getChildren();
    }
    return new ArrayList<Sample>();
  }


  /**
   * Function to retrieve parent samples of a sample
   * 
   * @param sampleCode Code of the query sample
   * @return List of parent samples
   */
  public List<Sample> getParentsBySearchService(String sampleCode) {
    for (Sample s : mockSamples) {
      if (s.getCode().equals(sampleCode))
        return s.getParents();
    }
    return new ArrayList<Sample>();
  }

  /**
   * 
   * @param sample
   * @return
   */
  public List<Sample> getChildrenSamples(Sample sample) {
    return getFacade().listSamplesOfSample(sample.getPermId());
  }

  /**
   * Checks if a space object of a certain code exists in openBIS
   * 
   * @param spaceCode the code of an openBIS space
   * @return true, if the space exists, false otherwise
   */
  public boolean spaceExists(String name) {
    return mockSpaceNames.contains(name);
  }

  /**
   * Checks if a project object of a certain code exists under a given space in openBIS
   * 
   * @param spaceCode the code of an openBIS space
   * @param projectCode the code of a project
   * @return true, if the project exists under this space, false otherwise
   */
  public boolean projectExists(String spaceCode, String projectCode) {
    for (Project p : mockProjects) {
      if (p.getCode().equals(projectCode) && p.getSpaceCode().equals(spaceCode))
        return true;
    }
    return false;
  }

  /**
   * Checks if an experiment of a certain code exists under a given space and project in openBIS
   * 
   * @param spaceCode the code of an openBIS space
   * @param projectCode the code of a project in openBIS
   * @param experimentCode the code of an experiment
   * @return true, if the experiment exists under this project and space, false otherwise
   */
  public boolean expExists(String spaceCode, String projectCode, String experimentCode) {
    if (projectExists(spaceCode, projectCode)) {
      for (Experiment e : getExperimentsOfProjectByCode(projectCode)) {
        if (e.getCode().equals(experimentCode))
          return true;
      }
    }
    return false;
  }

  /**
   * Checks if a sample of a given code exists in openBIS
   * 
   * @param sampleCode the code of a sample
   * @return true, if the sample exists, false otherwise
   */
  public boolean sampleExists(String name) {
    for (Sample s : mockSamples) {
      if (s.getCode().equals(name))
        return true;
    }
    return false;
  }

  /**
   * Compute status of project by checking status of the contained experiments
   * 
   * @param project the Project object
   * @return ratio of finished experiments in this project
   */
  public float computeProjectStatus(Project project) {
    float finishedExperiments = 0f;

    List<Experiment> experiments = this.getExperimentsOfProjectByCode(project.getCode());
    float numberExperiments = experiments.size();

    for (Experiment e : experiments) {
      if (e.getProperties().keySet().contains("Q_CURRENT_STATUS")) {
        if (e.getProperties().get("Q_CURRENT_STATUS").equals("FINISHED")) {
          finishedExperiments += 1.0;
        } ;
      }
    }
    if (numberExperiments > 0) {
      return finishedExperiments / experiments.size();
    } else {
      return 0f;
    }
  }

  /**
   * Compute status of project by checking status of the contained experiments Note: There is no
   * check whether the given experiments really belong to one project. You have to enusre that
   * yourself
   * 
   * @param experiments list of experiments of a project.
   * @return ratio of finished experiments in this project
   */
  public float computeProjectStatus(List<Experiment> experiments) {
    float finishedExperiments = 0f;

    float numberExperiments = experiments.size();

    for (Experiment e : experiments) {
      if (e.getProperties().keySet().contains("Q_CURRENT_STATUS")) {
        if (e.getProperties().get("Q_CURRENT_STATUS").equals("FINISHED")) {
          finishedExperiments += 1.0;
        } ;
      }
    }
    if (numberExperiments > 0) {
      return finishedExperiments / experiments.size();
    } else {
      return 0f;
    }
  }

  /**
   * Returns a map of Labels (keys) and Codes (values) in a Vocabulary in openBIS
   * 
   * @param vocabularyCode Code of the Vocabulary type
   * @return A map containing the labels as keys and codes as values in String format
   */
  public Map<String, String> getVocabCodesAndLabelsForVocab(String vocabularyCode) {
    Map<String, String> map = new HashMap<String, String>();
    for (String code : getVocabCodesForVocab(vocabularyCode)) {
      map.put(code.replace("_", " "), code);
    }
    return map;
  }

  public Vocabulary getVocabulary(String vocabularyCode) {
    // TODO
    return null;
  }

  /**
   * Returns a list of all Codes in a Vocabulary in openBIS. This is useful when labels don't exist
   * or are not needed.
   * 
   * @param vocabularyCode Code of the Vocabulary type
   * @return A list containing the codes of the vocabulary type
   */
  public List<String> getVocabCodesForVocab(String vocabularyCode) {
    if (mockVocabularies.containsKey(vocabularyCode))
      return mockVocabularies.get(vocabularyCode);
    else {
      String base = vocabularyCode;
      if (base.startsWith("Q_"))
        base = base.substring(2);
      ArrayList<String> res = new ArrayList<String>();
      for (int i = 1; i < 10; i++) {
        res.add(base + Integer.toString(i));
      }
      return res;
    }
  }

  /**
   * Returns a list of all Codes in a Vocabulary in openBIS. This is useful when labels don't exist
   * or are not needed.
   * 
   * @param vocabularyCode Code of the Vocabulary type
   * @return A list containing the codes of the vocabulary type
   */
  public List<Experiment> getExperimentsForUser(String userID) {
    System.out.println("mock is showing all experiments, for any user");
    return mockExperiments;
  }

  /**
   * Function to talk to ingestions services (python scripts) of this openBIS instance
   * 
   * @param dss the name of the dss-instance (e.g. DSS1 for most cases)
   * @param serviceName label of the ingestion service to call (this is defined in the ingestion
   *        service properties)
   * @param params A Map of parameters to send to the ingestion service
   */
  public void ingest(String dss, String serviceName, Map<String, Object> params) {
    System.out.println("mock ingest stuff");
  }

  /**
   * List all experiments for given list of projects
   * 
   * @param projectList list of project identifiers
   * @return List of experiments
   */
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    List<Experiment> res = new ArrayList<Experiment>();
    for (Project p : projectList) {
      res.addAll(getExperimentsForProject(p));
    }
    return res;
  }

  /**
   * List all datasets for given experiment identifiers
   * 
   * @param experimentIdentifiers list of experiment identifiers
   * @return List of datasets
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> listDataSetsForExperiments(
      List<String> experimentIdentifiers) {
    return null;// TODO
  }

  /**
   * List all samples for given project identifiers
   * 
   * @param projectIdentifiers list of project identifiers
   * @return List of samples
   */
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    List<Sample> res = new ArrayList<Sample>();
    for (String p : projectIdentifiers) {
      res.addAll(getSamplesOfProject(p));
    }
    return res;
  }

  /**
   * List all datasets for given sample identifiers
   * 
   * @param sampleIdentifier list of sample identifiers
   * @return List of datasets
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> listDataSetsForSamples(
      List<String> sampleIdentifier) {
    return null;// TODO
  }

  /**
   * Retrieve datastore download url of dataset
   * 
   * @param datasetCode Code of dataset
   * @param datasetName File name of dataset
   * @return URL to datastore location
   */
  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {

    return new URL("mock url for dataset");
  }

  /**
   * Retrieve inputstream for dataset
   * 
   * @param datasetCode Code of dataset
   * @return input stream for dataset
   */
  public InputStream getDatasetStream(String datasetCode) {

    return null;// TODO
  }

  /**
   * Retrieve inputstream for dataset in folder
   * 
   * @param datasetCode Code of dataset
   * @param folder Folder of dataset
   * @return input stream of datasets
   */
  public InputStream getDatasetStream(String datasetCode, String folder) {

    return null; // TODO
  }

  /**
   * returns file information for a given number of datasets. params should look something like
   * this: {'codes': [datasetcode1,datasetcode2...]} returns a QueryTableModel with the following
   * entries: DATA_SET_CODE equals datasetcode given in params RELATIVE_PATH normally in the form
   * 'original/results/file.txt' FILE_NAME just contains the basename e.g. 'file.txt' SIZE_IN_BYTES
   * java long value IS_DIRECTORY true or false LAST_MODIFIED ...
   * 
   * @param params
   * @param IllegalArgumentException
   * @return
   */
  public QueryTableModel queryFileInformation(Map<String, List<String>> params)
      throws IllegalArgumentException {
    return null;// TODO
  }

  @Override
  public List<String> getUserSpaces(String userID) {
    return mockSpaceNames;
  }

  @Override
  public boolean isUserAdmin(String userID) {
    return true;
  }

}
