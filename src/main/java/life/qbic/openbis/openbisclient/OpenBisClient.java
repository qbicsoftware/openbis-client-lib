package life.qbic.openbis.openbisclient;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import life.qbic.openbis.openbisclient.helper.OpenBisHelper;

public class OpenBisClient implements IOpenBisClient{

  private String userId, password, sessionToken;
  private final int TIMEOUT = 10000;
  private IApplicationServerApi v3;

  public OpenBisClient(String userId, String password, String serverURL) {
    this.userId = userId;
    this.password = password;

    // get a reference to AS API
    v3 = HttpInvokerUtils
        .createServiceStub(IApplicationServerApi.class, serverURL, TIMEOUT);
    sessionToken = null;
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
    SearchResult<Space> spaces = v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
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
    SearchResult<Project> projects = v3.searchProjects(sessionToken, new ProjectSearchCriteria(), new ProjectFetchOptions());
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
    SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, new ExperimentSearchCriteria(), new ExperimentFetchOptions());
    return experiments.getObjects();
  }


  /**
   * Function to retrieve all samples of a given experiment Note: seems to throw a
   * ch.systemsx.cisd.common.exceptions.UserFailureException if wrong identifier given TODO Should
   * we catch it and throw an illegalargumentexception instead? would be a lot clearer in my opinion
   *
   * @param experimentIdentifier identifier/code (both should work) of the openBIS experiment
   * @return list with all samples of the given experiment
   *
   */
  @Override
  public List<Sample> getSamplesofExperiment(String experimentIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withCode().thatEquals(experimentIdentifier);

    SearchResult<Sample> samplesOfExperiment = v3.searchSamples(sessionToken, sampleSearchCriteria,
        OpenBisHelper.fetchAllSamples());
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

    SearchResult<Sample> samplesOfExperiment = v3.searchSamples(sessionToken, sampleSearchCriteria, OpenBisHelper.fetchAllSamples());
    return samplesOfExperiment.getObjects();
  }

  @Override
  public void updateSampleMetadata(long sampleID, Map<String, String> properties) {

  }

  @Override
  public Sample getSampleByIdentifier(String sampleIdentifier) {
    return null;
  }

  @Override
  public List<Sample> getSamplesOfProject(String projIdentifier) {
    return null;
  }

  @Override
  public List<Sample> getSamplesOfProjectBySearchService(String projIdentifier) {
    return null;
  }

  @Override
  public List<Sample> getSamplesWithParentsAndChildrenOfProjectBySearchService(
      String projIdentifier) {
    return null;
  }

  @Override
  public List<Sample> getSamplesWithParentsAndChildren(String sampCode) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject(Project project) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject2(Project project) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject2(String projectCode) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject3(Project project) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject3(String projectCode) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject(String projectIdentifier) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfProjectByCode(String projectCode) {
    return null;
  }

  @Override
  public Map<String, List<Experiment>> getProjectExperimentMapping(String spaceIdentifier) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfSpace(String spaceIdentifier) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfType(String type) {
    return null;
  }

  @Override
  public List<Sample> getSamplesOfType(String type) {
    return null;
  }

  @Override
  public List<Project> getProjectsOfSpace(String space) {
    return null;
  }

  @Override
  public List<String> getUserSpaces(String userID) {
    return null;
  }

  @Override
  public boolean isUserAdmin(String userID) {
    return false;
  }

  @Override
  public Project getProjectByIdentifier(String projectIdentifier) {
    return null;
  }

  @Override
  public Project getProjectByCode(String projectCode) {
    return null;
  }

  @Override
  public Experiment getExperimentByCode(String experimentCode) {
    return null;
  }

  @Override
  public Experiment getExperimentById(String experimentId) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentById2(String expIdentifer) {
    return null;
  }

  @Override
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
    return null;
  }

  @Override
  public List<DataSet> getDataSetsByType(String type) {
    return null;
  }

  @Override
  public List<Attachment> listAttachmentsForSampleByIdentifier(String sampleIdentifier) {
    return null;
  }

  @Override
  public List<Attachment> listAttachmentsForProjectByIdentifier(String projectIdentifier) {
    return null;
  }

  @Override
  public void addAttachmentToProject(Map<String, Object> parameter) {

  }

  @Override
  public Set<String> getSpaceMembers(String spaceCode) {
    return null;
  }

  @Override
  public List<String> listVocabularyTermsForProperty(PropertyType property) {
    return null;
  }

  @Override
  public String getCVLabelForProperty(PropertyType propertyType, String propertyValue) {
    return null;
  }

  @Override
  public SampleType getSampleTypeByString(String sampleType) {
    return null;
  }

  @Override
  public Map<String, SampleType> getSampleTypes() {
    return null;
  }

  @Override
  public String triggerIngestionService(String serviceName, Map<String, Object> parameters) {
    return null;
  }

  @Override
  public String addParentChildConnection(Map<String, Object> parameters) {
    return null;
  }

  @Override
  public String addNewInstance(Map<String, Object> params, String service,
      int number_of_samples_offset) {
    return null;
  }

  @Override
  public String generateBarcode(String proj, int number_of_samples_offset) {
    return null;
  }

  @Override
  public String openBIScodeToString(String entityCode) {
    return null;
  }

  @Override
  public URL getDataStoreDownloadURL(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    return null;
  }

  @Override
  public URL getDataStoreDownloadURLLessGeneric(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    return null;
  }

  @Override
  public Map<Sample, List<Sample>> getParentMap(List<Sample> samples) {
    return null;
  }

  @Override
  public List<String> getProjectTSV(String projectCode, String sampleType) {
    return null;
  }

  @Override
  public List<Sample> getChildrenBySearchService(String sampleCode) {
    return null;
  }

  @Override
  public List<Sample> getParentsBySearchService(String sampleCode) {
    return null;
  }

  @Override
  public List<Sample> getChildrenSamples(Sample sample) {
    return null;
  }

  @Override
  public boolean spaceExists(String spaceCode) {
    return false;
  }

  @Override
  public boolean projectExists(String spaceCode, String projectCode) {
    return false;
  }

  @Override
  public boolean expExists(String spaceCode, String projectCode, String experimentCode) {
    return false;
  }

  @Override
  public boolean sampleExists(String sampleCode) {
    return false;
  }

  @Override
  public float computeProjectStatus(Project project) {
    return 0;
  }

  @Override
  public float computeProjectStatus(List<Experiment> experiments) {
    return 0;
  }

  @Override
  public Map<String, String> getVocabCodesAndLabelsForVocab(String vocabularyCode) {
    return null;
  }

  @Override
  public Vocabulary getVocabulary(String vocabularyCode) {
    return null;
  }

  @Override
  public List<String> getVocabCodesForVocab(String vocabularyCode) {
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForUser(String userID) {
    return null;
  }

  @Override
  public void ingest(String dss, String serviceName, Map<String, Object> params) {

  }

  @Override
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    return null;
  }

  @Override
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    return null;
  }

  @Override
  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {
    return null;
  }

  @Override
  public InputStream getDatasetStream(String datasetCode) {
    return null;
  }

  @Override
  public InputStream getDatasetStream(String datasetCode, String folder) {
    return null;
  }
}
