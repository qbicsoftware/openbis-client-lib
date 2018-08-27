package life.qbic.openbis.openbisclient;

import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchDataSetsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchExperimentsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchProjectsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchSampleTypesCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchSamplesCompletely;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenBisClient implements IOpenBisClient {

  private final int TIMEOUT = 100000;
  private String userId, password, sessionToken;
  private IApplicationServerApi v3;
  private IDataStoreServerApi dss3;

  public OpenBisClient(String userId, String password, String serverURL) {
    this.userId = userId;
    this.password = password;
    // get a reference to AS API
    v3 = HttpInvokerUtils
        .createServiceStub(IApplicationServerApi.class, serverURL, TIMEOUT);
    dss3 = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, serverURL, TIMEOUT);
    sessionToken = null;
  }

  //TODO Added for testing reasons...
  public IApplicationServerApi getV3() {
    return v3;
  }

  /**
   * Checks if we are logged in
   */
  @Override
  public boolean loggedin() {
    try {
      return v3.isSessionActive(sessionToken);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * logs out of the OpenBIS server
   */
  @Override
  public void logout() {
    if (loggedin()) {
      v3.logout(sessionToken);
      //TODO Set sessionToken to null
      sessionToken = null;
    } else {
    }

  }

  /**
   * logs in to the OpenBIS server with the system userid after calling this function, the user has
   * to provide the password
   */
  @Override
  public void login() {
    if (loggedin()) {
      logout();
    }

    // login to obtain a session token
    sessionToken = v3.login(userId, password);
  }

  /**
   * Get session token of current openBIS session
   *
   * @return session token as string
   */
  @Override
  public String getSessionToken() {
    return sessionToken;
  }

  /**
   * Checks if logged in, reconnects if not
   */
  @Override
  public void ensureLoggedIn() {
    if (!this.loggedin()) {
      this.login();
    }
  }

  /**
   * Function to get a list of all space identifiers which are registered in this openBIS instance
   *
   * @return list with the identifiers of all available spaces
   */
  @Override
  public List<String> listSpaces() {
    ensureLoggedIn();
    SearchResult<Space> spaces = v3
        .searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
    List<String> spaceIdentifiers = new ArrayList<>();
    for (Space space : spaces.getObjects()) {
      spaceIdentifiers.add(space.getCode());
    }

    return spaceIdentifiers;
  }

  /**
   * Function to get all projects which are registered in this openBIS instance
   *
   * @return list with all projects which are registered in this openBIS instance
   */
  @Override
  public List<Project> listProjects() {
    ensureLoggedIn();
    SearchResult<Project> projects = v3
        .searchProjects(sessionToken, new ProjectSearchCriteria(), fetchProjectsCompletely());
    return projects.getObjects();
  }

  /**
   * Function to list all Experiments which are registered in the openBIS instance.
   *
   * @return list with all experiments registered in this openBIS instance
   */
  @Override
  public List<Experiment> listExperiments() {
    ensureLoggedIn();
    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, new ExperimentSearchCriteria(),
            fetchExperimentsCompletely());
    return experiments.getObjects();
  }


  /**
   * Function to retrieve all samples of a given experiment Note: seems to throw a
   * ch.systemsx.cisd.common.exceptions.UserFailureException if wrong identifier given TODO Should
   * we catch it and throw an illegalargumentexception instead? would be a lot clearer in my
   * opinion
   *
   * @param experimentIdentifier identifier/code (both should work) of the openBIS experiment
   * @return list with all samples of the given experiment
   */
  @Override
  public List<Sample> getSamplesofExperiment(String experimentIdentifier) {
    ensureLoggedIn();

    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withCode().thatEquals(experimentIdentifier);

    SearchResult<Sample> samplesOfExperiment = v3
        .searchSamples(sessionToken, sampleSearchCriteria,
            fetchSamplesCompletely());
    return samplesOfExperiment.getObjects();

  }

  /**
   * Function to retrieve all samples of a given space
   *
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all samples of the given space
   */
  @Override
  public List<Sample> getSamplesofSpace(String spaceIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withSpace().withCode().thatEquals(spaceIdentifier);

    SearchResult<Sample> samplesOfExperiment = v3
        .searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());
    return samplesOfExperiment.getObjects();

  }

//  @Override
//  public void updateSampleMetadata(long sampleID, Map<String, String> properties) {
//    //TODO Wait for openbis test instance
//  }

  @Override
  public Sample getSampleByIdentifier(String sampleIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withId().thatEquals(new SampleIdentifier(sampleIdentifier));

    SearchResult<Sample> samples = v3
        .searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

    return samples.getObjects().get(0);
  }

  @Override
  public List<Sample> getSamplesOfProject(String projIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withOrOperator();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(projIdentifier);
    sampleSearchCriteria.withExperiment().withProject().withId()
        .thatEquals(new ProjectIdentifier(projIdentifier));

    SearchResult<Sample> samples = v3
        .searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

    return samples.getObjects();
  }

//  @Override
//  public List<Sample> getSamplesOfProjectBySearchService(String projIdentifier) {
//    return null;
//  }
//
//  @Override
//  public List<Sample> getSamplesWithParentsAndChildrenOfProjectBySearchService(
//      String projIdentifier) {
//    return null;
//  }
//
//  @Override
//  public List<Sample> getSamplesWithParentsAndChildren(String sampCode) {
//    return null;
//  }

  @Override
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withOrOperator();
    sc.withProject().withId().thatEquals(new ProjectIdentifier(projectIdentifier));
    sc.withProject().withCode().thatEquals(projectIdentifier);

    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

//  @Override
//  public List<Experiment> getExperimentsForProject(Project project) {
//    return null;
//  }
//
//  @Override
//  public List<Experiment> getExperimentsForProject2(Project project) {
//    return null;
//  }
//
//  @Override
//  public List<Experiment> getExperimentsForProject2(String projectCode) {
//    return null;
//  }
//
//  @Override
//  public List<Experiment> getExperimentsForProject3(Project project) {
//    return null;
//  }
//
//  @Override
//  public List<Experiment> getExperimentsForProject3(String projectCode) {
//    return null;
//  }
//
//  @Override
//  public List<Experiment> getExperimentsForProject(String projectIdentifier) {
//    return null;
//  }

  @Override
  public List<Experiment> getExperimentsOfProjectByCode(String projectCode) {
    //TODO Could be combined with getExperimentsOfProjectByIdentifier
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withProject().withCode().thatEquals(projectCode);

    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  @Override
  public Map<String, List<Experiment>> getProjectExperimentMapping(String spaceIdentifier) {
    Map<String, List<Experiment>> projectExperimentMapping = new HashMap<>();
    List<Project> projects = getProjectsOfSpace(spaceIdentifier);
    for (Project project : projects) {
      String code = project.getCode();
      projectExperimentMapping.put(code, getExperimentsOfProjectByCode(code));
    }

    return projectExperimentMapping;
  }

  @Override
  public List<Experiment> getExperimentsOfSpace(String spaceIdentifier) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withProject().withSpace().withCode().thatEquals(spaceIdentifier);

    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  @Override
  public List<Experiment> getExperimentsOfType(String type) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withType().withCode().thatEquals(type);

    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  @Override
  public List<Sample> getSamplesOfType(String type) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withType().withCode().thatEquals(type);

    SearchResult<Sample> samples = v3
        .searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());
    return samples.getObjects();
  }

  @Override
  public List<Project> getProjectsOfSpace(String space) {
    ensureLoggedIn();
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withSpace().withCode().thatEquals(space);

    SearchResult<Project> projects = v3
        .searchProjects(sessionToken, sc, fetchProjectsCompletely());

    return projects.getObjects();
  }

//  @Override
//  public List<String> getUserSpaces(String userID) {
//    ensureLoggedIn();
//    SpaceSearchCriteria sc = new SpaceSearchCriteria();
//
//    SearchResult<Space> spaces = v3
//        .searchSpaces(sessionToken, sc, new SpaceFetchOptions());
//
//    List<String> spaceCodes = new ArrayList<>();
//    for (Space space : spaces.getObjects()) {
//      spaceCodes.add(space.getCode());
//    }
//    return spaceCodes;
//  }

//  @Override
//  public boolean isUserAdmin(String userID) {
//    ensureLoggedIn();
//    SessionInformation sessionInformation = v3.getSessionInformation(sessionToken);
//    return sessionInformation.getUserName().equals("admin");
//  }

  @Override
  public Project getProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withOrOperator();
    sc.withId().thatEquals(new ProjectIdentifier(projectIdentifier));
    sc.withCode().thatEquals(projectIdentifier);

    SearchResult<Project> projects = v3
        .searchProjects(sessionToken, sc, fetchProjectsCompletely());

    return projects.getObjects().get(0);
  }

  @Override
  public Project getProjectByCode(String projectCode) {
    ensureLoggedIn();
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withOrOperator();
    sc.withId().thatEquals(new ProjectIdentifier(projectCode));
    sc.withCode().thatEquals(projectCode);

    SearchResult<Project> projects = v3
        .searchProjects(sessionToken, sc, fetchProjectsCompletely());

    return projects.getObjects().get(0);
  }

  @Override
  public Experiment getExperimentByCode(String experimentCode) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withCode().thatEquals(experimentCode);

    SearchResult<Experiment> experiment = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiment.getObjects().get(0);
  }

  @Override
  public Experiment getExperimentById(String experimentId) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withOrOperator();
    sc.withId().thatEquals(new ExperimentIdentifier(experimentId));

    SearchResult<Experiment> experiment = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiment.getObjects().get(0);
  }

//  @Override
//  public List<Experiment> getExperimentById2(String expIdentifer) {
//    return null;
//  }

  @Override
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withId().thatEquals(new ExperimentIdentifier(experimentIdentifier));
    SearchResult<Experiment> experiment = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiment.getObjects().get(0).getProject();
  }

//  @Override
//  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
//    return null;
//  }

  @Override
  public List<DataSet> getDataSetsByType(String type) {
    ensureLoggedIn();
    DataSetSearchCriteria sc = new DataSetSearchCriteria();
    sc.withType().withCode().thatEquals(type);

    SearchResult<DataSet> dataSets = v3.searchDataSets(sessionToken, sc, fetchDataSetsCompletely());

    return dataSets.getObjects();
  }

  @Override
  public List<Attachment> listAttachmentsForSampleByIdentifier(String sampleIdentifier) {
    ensureLoggedIn();

    return getSampleByIdentifier(sampleIdentifier).getAttachments();
  }

  @Override
  public List<Attachment> listAttachmentsForProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();

    return getProjectByIdentifier(projectIdentifier).getAttachments();
  }

//  @Override
//  public void addAttachmentToProject(Map<String, Object> parameter) {
//    //TODO Wait for openbis test instance
//  }

//  @Override
//  public Set<String> getSpaceMembers(String spaceCode) {
//    return null;
//  }

//  @Override
//  public List<String> listVocabularyTermsForProperty(PropertyType property) {
//    return null;
//  }
//
//  @Override
//  public String getCVLabelForProperty(PropertyType propertyType, String propertyValue) {
//    return null;
//  }

  @Override
  public SampleType getSampleTypeByString(String sampleType) {
    SampleTypeSearchCriteria sc = new SampleTypeSearchCriteria();
    sc.withCode().thatEquals(sampleType);

    SearchResult<SampleType> sampleTypes = v3.searchSampleTypes(sessionToken, sc,
        fetchSampleTypesCompletely());

    return sampleTypes.getObjects().get(0);
  }

//  @Override
//  public Map<String, SampleType> getSampleTypes() {
//    return null;
//  }
//
//  @Override
//  public String triggerIngestionService(String serviceName, Map<String, Object> parameters) {
//    return null;
//  }
//
//  @Override
//  public String addParentChildConnection(Map<String, Object> parameters) {
//    //TODO Wait for openbis test instance
//
//    return null;
//  }
//
//  @Override
//  public String addNewInstance(Map<String, Object> params, String service,
//      int number_of_samples_offset) {
//    //TODO Wait for openbis test instance
//
//    return null;
//  }

  @Override
  public String generateBarcode(String proj, int number_of_samples_offset) {
    Project project = getProjectByIdentifier(proj);
    int numberOfSamples = getSamplesOfProject(project.getCode()).size();
    String barcode = project.getCode() + String.format("%03d", (numberOfSamples + 1)) + "S";
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


//  @Override
//  public String openBIScodeToString(String entityCode) {
//    return null;
//  }
//
//  @Override
//  public URL getDataStoreDownloadURL(String dataSetCode, String openbisFilename)
//      throws MalformedURLException {
//    return null;
//  }
//
//  @Override
//  public URL getDataStoreDownloadURLLessGeneric(String dataSetCode, String openbisFilename)
//      throws MalformedURLException {
//    return null;
//  }

  @Override
  public Map<Sample, List<Sample>> getParentMap(List<Sample> samples) {
    //TODO samples must have fetched parents!
    Map<Sample, List<Sample>> parentMap = new HashMap<>();
    for (Sample sample : samples) {
      parentMap.put(sample, sample.getParents());
    }

    return parentMap;
  }

//  @Override
//  public List<String> getProjectTSV(String projectCode, String sampleType) {
//    return null;
//  }
//
//  @Override
//  public List<Sample> getChildrenBySearchService(String sampleCode) {
//    return null;
//  }
//
//  @Override
//  public List<Sample> getParentsBySearchService(String sampleCode) {
//    return null;
//  }

  @Override
  public List<Sample> getChildrenSamples(Sample sample) {
    //TODO unnecessary method in v3 api
    return sample.getChildren();
  }

  @Override
  public boolean spaceExists(String spaceCode) {
    SpaceSearchCriteria sc = new SpaceSearchCriteria();
    sc.withCode().thatEquals(spaceCode);
    SearchResult<Space> spaces = v3.searchSpaces(sessionToken, sc, new SpaceFetchOptions());
    return spaces.getTotalCount() != 0;
  }

  @Override
  public boolean projectExists(String spaceCode, String projectCode) {
    //TODO why do we need the space code here? Then the method should be named differently
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withSpace().withCode().thatEquals(spaceCode);
    sc.withCode().thatEquals(projectCode);
    SearchResult<Project> projects = v3.searchProjects(sessionToken, sc, new ProjectFetchOptions());
    return projects.getTotalCount() != 0;
  }

  @Override
  public boolean expExists(String spaceCode, String projectCode, String experimentCode) {
    //TODO why do we need the space code here? Then the method should be named differently
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withProject().withSpace().withCode().thatEquals(spaceCode);
    sc.withCode().thatEquals(experimentCode);
    sc.withProject().withCode().thatEquals(projectCode);
    SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, sc,
        new ExperimentFetchOptions());
    return experiments.getTotalCount() != 0;
  }

  @Override
  public boolean sampleExists(String sampleCode) {
    SampleSearchCriteria sc = new SampleSearchCriteria();
    sc.withCode().thatEquals(sampleCode);
    SearchResult<Sample> samples = v3.searchSamples(sessionToken, sc, new SampleFetchOptions());
    return samples.getTotalCount() != 0;
  }

//  @Override
//  public float computeProjectStatus(Project project) {
//    return 0;
//  }
//
//  @Override
//  public float computeProjectStatus(List<Experiment> experiments) {
//    return 0;
//  }
//
//  @Override
//  public Map<String, String> getVocabCodesAndLabelsForVocab(String vocabularyCode) {
//    return null;
//  }
//
//  @Override
//  public Vocabulary getVocabulary(String vocabularyCode) {
//    return null;
//  }
//
//  @Override
//  public List<String> getVocabCodesForVocab(String vocabularyCode) {
//    return null;
//  }
//
//  @Override
//  public List<Experiment> getExperimentsForUser(String userID) {
//    return null;
//  }
//
//  @Override
//  public void ingest(String dss, String serviceName, Map<String, Object> params) {
//
//  }

  @Override
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    return null;
  }

  @Override
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    return null;
  }

//  @Override
//  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {
//    return null;
//  }
//
//  @Override
//  public InputStream getDatasetStream(String datasetCode) {
//    return null;
//  }
//
//  @Override
//  public InputStream getDatasetStream(String datasetCode, String folder) {
//    return null;
//  }
}
