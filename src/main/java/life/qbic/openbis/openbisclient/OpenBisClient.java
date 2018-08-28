package life.qbic.openbis.openbisclient;

import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchDataSetsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchExperimentTypesCompletely;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.WordUtils;

/**
 * The type Open bis client.
 */
public class OpenBisClient implements IOpenBisClient {

  private final int TIMEOUT = 100000;
  private String userId, password, sessionToken, serverURL;
  private IApplicationServerApi v3;
  private IDataStoreServerApi dss3;

  /**
   * Instantiates a new Open bis client.
   *
   * @param userId the user id
   * @param password the password
   * @param serverURL the server url
   */
  public OpenBisClient(String userId, String password, String serverURL) {
    this.userId = userId;
    this.password = password;
    this.serverURL = serverURL;
    // get a reference to AS API
    v3 = HttpInvokerUtils
        .createServiceStub(IApplicationServerApi.class, serverURL, TIMEOUT);
    dss3 = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, serverURL, TIMEOUT);
    sessionToken = null;
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
   * Gets v 3.
   *
   * @return the v 3
   */
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

  public void loginAsUser(String user) {
    if (loggedin()) {
      logout();
    }

    // login to obtain a session token
    sessionToken = v3.loginAs(userId, password, user);
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

  @Override
  public Sample getSampleByIdentifier(String sampleIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withId().thatEquals(new SampleIdentifier(sampleIdentifier));

    SearchResult<Sample> samples = v3
        .searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

    if (samples.getObjects().isEmpty()) {
      return null;
    } else {
      return samples.getObjects().get(0);
    }
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

  /**
   * Function to get a sample with its parents and children
   *
   * @param sampCode code of the openBIS sample
   * @return sample
   */
  @Override
  public List<Sample> getSamplesWithParentsAndChildren(String sampCode) {
    //TODO unclear if parents and children should be fetched or directly included into the list
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withCode().thatEquals(sampCode);
    SampleFetchOptions sampleFetchOptions = fetchSamplesCompletely();
    sampleFetchOptions.withChildrenUsing(fetchSamplesCompletely());
    sampleFetchOptions.withParentsUsing(fetchSamplesCompletely());

    SearchResult<Sample> samples = v3
        .searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

    return samples.getObjects();
  }

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

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance. av: 19353 ms
   *
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  @Override
  public List<Experiment> getExperimentsForProject(Project project) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withProject().withCode().thatEquals(project.getCode());

    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   *
   * @param projectIdentifier project identifer as defined by openbis, for which the experiments
   * should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  @Override
  public List<Experiment> getExperimentsForProject(String projectIdentifier) {
    //TODO equal to getExperimentsOfProjectByIdentifier
    ensureLoggedIn();
    return getExperimentsOfProjectByIdentifier(projectIdentifier);
  }

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

  /**
   * Returns Space names a given user should be able to see
   *
   * @param userID Username found in openBIS
   * @return List of space names with projects this user has access to
   */
  @Override
  public List<String> getUserSpaces(String userID) {
    logout();

    loginAsUser(userID);
    List<String> spacesOfUser = listSpaces();
    logout();
    login();

    return spacesOfUser;
  }

  /**
   * Returns wether a user is instance admin in openBIS
   *
   * @return true, if user is instance admin, false otherwise
   */
  @Override
  public boolean isUserAdmin(String userID) {
    //TODO cant find method
    return false;
  }

  @Override
  public Project getProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withOrOperator();
    sc.withId().thatEquals(new ProjectIdentifier(projectIdentifier));
    sc.withCode().thatEquals(projectIdentifier);

    SearchResult<Project> projects = v3
        .searchProjects(sessionToken, sc, fetchProjectsCompletely());

    if (projects.getObjects().isEmpty()) {
      return null;
    } else {
      return projects.getObjects().get(0);
    }
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

    if (projects.getObjects().isEmpty()) {
      return null;
    } else {
      return projects.getObjects().get(0);
    }
  }

  @Override
  public Experiment getExperimentByCode(String experimentCode) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withCode().thatEquals(experimentCode);

    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    if (experiments.getObjects().isEmpty()) {
      return null;
    } else {
      return experiments.getObjects().get(0);
    }
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

  @Override
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withId().thatEquals(new ExperimentIdentifier(experimentIdentifier));
    SearchResult<Experiment> experiments = v3
        .searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    if (experiments.getObjects().isEmpty()) {
      return null;
    } else {
      return experiments.getObjects().get(0).getProject();
    }
  }

  /**
   * Function to list all datasets of a specific sample (watch out there are different dataset
   * classes)
   *
   * @param sampleIdentifier identifier of the openBIS sample
   * @return list with all datasets of the given sample
   */
  @Override
  public List<DataSet> getDataSetsOfSampleByIdentifier(String sampleIdentifier) {
    DataSetSearchCriteria sc = new DataSetSearchCriteria();
    sc.withOrOperator();
    sc.withSample().withId().thatEquals(new SampleIdentifier(sampleIdentifier));
    SearchResult<DataSet> dataSets = v3.searchDataSets(sessionToken, sc, fetchDataSetsCompletely());

    return dataSets.getObjects();
  }

  /**
   * Function to list all datasets of a specific sample (watch out there are different dataset
   * classes)
   *
   * @param sampleCode code or identifier of the openBIS sample
   * @return list with all datasets of the given sample
   */
  @Override
  public List<DataSet> getDataSetsOfSample(String sampleCode) {
    DataSetSearchCriteria sc = new DataSetSearchCriteria();
    sc.withSample().withCode().thatEquals(sampleCode);
    SearchResult<DataSet> dataSets = v3.searchDataSets(sessionToken, sc, fetchDataSetsCompletely());

    return dataSets.getObjects();
  }

  /**
   * Function to list all datasets of a specific experiment (watch out there are different dataset
   * classes)
   *
   * @param experimentPermID permId of the openBIS experiment
   * @return list with all datasets of the given experiment
   */
  @Override
  public List<DataSet> getDataSetsOfExperiment(String experimentPermID) {
    DataSetSearchCriteria sc = new DataSetSearchCriteria();
    sc.withExperiment().withPermId().thatEquals(experimentPermID);
    SearchResult<DataSet> dataSets = v3.searchDataSets(sessionToken, sc, fetchDataSetsCompletely());

    return dataSets.getObjects();
  }

  /**
   * Returns all datasets of a given experiment. The new version should run smoother
   *
   * @param experimentIdentifier identifier or code of the openbis experiment
   * @return list of all datasets of the given experiment
   */
  @Override
  public List<DataSet> getDataSetsOfExperimentByIdentifier(String experimentIdentifier) {
    DataSetSearchCriteria sc = new DataSetSearchCriteria();
    sc.withExperiment().withId().thatEquals(new ExperimentIdentifier(experimentIdentifier));
    SearchResult<DataSet> dataSets = v3.searchDataSets(sessionToken, sc, fetchDataSetsCompletely());
    return dataSets.getObjects();
  }

  /**
   * Function to list all datasets of a specific openBIS space
   *
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all datasets of the given space
   */
  @Override
  public List<DataSet> getDataSetsOfSpaceByIdentifier(String spaceIdentifier) {
    DataSetSearchCriteria sc = new DataSetSearchCriteria();
    sc.withSample().withSpace().withCode().thatEquals(spaceIdentifier);
    SearchResult<DataSet> dataSets = v3.searchDataSets(sessionToken, sc, fetchDataSetsCompletely());
    return dataSets.getObjects();
  }

  /**
   * Function to list all datasets of a specific openBIS project
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  @Override
  public List<DataSet> getDataSetsOfProjectByIdentifier(String projectIdentifier) {
    //TODO does not work yet
    return null;
  }

  /**
   * Function to list all datasets of a specific openBIS project
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  @Override
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
    //TODO does not work yet
    return null;
  }

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

  /**
   * Returns all users of a Space.
   *
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  @Override
  public Set<String> getSpaceMembers(String spaceCode) {
    //TODO cannot find an opportunity to do that
    return null;
  }


  /**
   * Function to list the vocabulary terms for a given property which has been added to openBIS. The
   * property has to be a Controlled Vocabulary Property.
   *
   * @param property the property type
   * @return list of the vocabulary terms of the given property
   */
  @Override
  public List<String> listVocabularyTermsForProperty(PropertyType property) {
    return null;

  }

  /**
   * Function to get the label of a CV item for some property
   *
   * @param propertyType the property type
   * @param propertyValue the property value
   * @return Label of CV item
   */
  @Override
  public String getCVLabelForProperty(PropertyType propertyType, String propertyValue) {
    return null;
  }

  @Override
  public SampleType getSampleTypeByString(String sampleType) {
    SampleTypeSearchCriteria sc = new SampleTypeSearchCriteria();
    sc.withCode().thatEquals(sampleType);

    SearchResult<SampleType> sampleTypes = v3.searchSampleTypes(sessionToken, sc,
        fetchSampleTypesCompletely());

    if (sampleTypes.getObjects().isEmpty()) {
      return null;
    } else {
      return sampleTypes.getObjects().get(0);
    }

  }

  /**
   * Function to retrieve a map with sample type code as key and the sample type object as value
   *
   * @return map with sample types
   */
  @Override
  public Map<String, SampleType> getSampleTypes() {
    SearchResult<SampleType> sampleTypes = v3.searchSampleTypes(sessionToken,
        new SampleTypeSearchCriteria(), fetchSampleTypesCompletely());

    Map<String, SampleType> types = new HashMap<>();
    for (SampleType t : sampleTypes.getObjects()) {
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
  @Override
  public ExperimentType getExperimentTypeByString(String experimentType) {

    ExperimentTypeSearchCriteria sc = new ExperimentTypeSearchCriteria();
    sc.withCode().thatContains(experimentType);

    SearchResult<ExperimentType> experimentTypes = v3.searchExperimentTypes(sessionToken, sc,
        fetchExperimentTypesCompletely());

    if (experimentTypes.getObjects().isEmpty()) {
      return null;
    } else {
      return experimentTypes.getObjects().get(0);
    }
  }

  /**
   * Function to trigger ingestion services registered in openBIS
   *
   * @param serviceName name of the ingestion service which should be triggered
   * @param parameters map with needed information for registration process
   * @return object name of the QueryTableModel which is returned by the aggregation service
   */
  @Override
  public String triggerIngestionService(String serviceName, Map<String, Object> parameters) {
    return null;
  }

  @Override
  public String generateBarcode(String proj, int number_of_samples_offset) {
    Project project = getProjectByIdentifier(proj);
    int numberOfSamples = getSamplesOfProject(project.getCode()).size();
    String barcode = project.getCode() + String.format("%03d", (numberOfSamples + 1)) + "S";
    barcode += checksum(barcode);

    return barcode;
  }

  /**
   * Function to transform openBIS entity type to human readable text. Performs String replacement
   * and does not query openBIS!
   *
   * @param entityCode the entity code as string
   * @return entity code as string in human readable text
   */
  @Override
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
   * @param dataSetCode code of the openBIS dataset
   * @param openbisFilename name of the file stored in the given dataset
   * @return URL object of the download url for the given file
   * @throws MalformedURLException Returns an download url for the openbis dataset with the given
   * code and dataset_type. Throughs MalformedURLException if a url can not be created from the
   * given parameters. NOTE: datastoreURL differs from serverURL only by the port -> quick hack
   * used
   */
  @Override
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

  @Override
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


  @Override
  public Map<Sample, List<Sample>> getParentMap(List<Sample> samples) {
    //TODO samples must have fetched parents!
    Map<Sample, List<Sample>> parentMap = new HashMap<>();
    for (Sample sample : samples) {
      parentMap.put(sample, sample.getParents());
    }

    return parentMap;
  }

  /**
   * Returns lines of a spreadsheet of humanly readable information of the samples in a project.
   * Only one requested layer of the data model is returned. Experimental factors are returned in
   * the properties xml format and should be parsed before the spreadsheet is presented to the
   * user.
   *
   * @param projectCode The 5 letter QBiC code of the project
   * @param sampleType The openBIS sampleType that should be included in the result
   */
  @Override
  public List<String> getProjectTSV(String projectCode, String sampleType) {
    return null;
  }


  @Override
  public List<Sample> getChildrenSamples(Sample sample) {
    //TODO unnecessary method in v3 api
    return sample.getChildren();
  }

  @Override
  public boolean spaceExists(String spaceCode) {
    return listSpaces().contains(spaceCode);
  }

  @Override
  public boolean projectExists(String spaceCode, String projectCode) {
    //TODO why do we need the space code here? Then the method should be named differently
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withSpace().withCode().thatEquals(spaceCode);
    sc.withCode().thatEquals(projectCode);
    SearchResult<Project> projects = v3.searchProjects(sessionToken, sc, new ProjectFetchOptions());

    return projectCode != null && projects.getTotalCount() != 0;
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
    return samples
        .getTotalCount() != 0;
  }

  /**
   * Compute status of project by checking status of the contained experiments
   *
   * @param project the Project object
   * @return ratio of finished experiments in this project
   */
  @Override
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
  @Override
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
  @Override
  public Map<String, String> getVocabCodesAndLabelsForVocab(String vocabularyCode) {
    return null;
  }

  @Override
  public Vocabulary getVocabulary(String vocabularyCode) {
    return null;
  }

  /**
   * Returns a list of all Codes in a Vocabulary in openBIS. This is useful when labels don't exist
   * or are not needed.
   *
   * @param vocabularyCode Code of the Vocabulary type
   * @return A list containing the codes of the vocabulary type
   */
  @Override
  public List<String> getVocabCodesForVocab(String vocabularyCode) {
    return null;
  }

  /**
   * Returns a list of all Experiments of a certain user.
   *
   * @param userID ID of user
   * @return A list containing the experiments
   */
  @Override
  public List<Experiment> getExperimentsForUser(String userID) {
    ensureLoggedIn();
    loginAsUser(userID);
    SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken,
        new ExperimentSearchCriteria(), fetchExperimentsCompletely());

    logout();
    login();
    if (experiments.getObjects().isEmpty()) {
      return null;
    } else {
      return experiments.getObjects();
    }
  }


  @Override
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    for (Project project : projectList) {
      sc.withProject().withCode().thatEquals(project.getCode());
    }
    SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, sc,
        fetchExperimentsCompletely());

    if (experiments.getObjects().isEmpty()){
      return null;
    } else {
      return experiments.getObjects();
    }
  }

  /**
   * List all datasets for given experiment identifiers
   *
   * @param experimentIdentifiers list of experiment identifiers
   * @return List of datasets
   */
  @Override
  public List<DataSet> listDataSetsForExperiments(List<String> experimentIdentifiers) {
    return null;
  }

  @Override
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    return null;
  }

  /**
   * List all datasets for given sample identifiers
   *
   * @param sampleIdentifier list of sample identifiers
   * @return List of datasets
   */
  @Override
  public List<DataSet> listDataSetsForSamples(List<String> sampleIdentifier) {
    return null;
  }

  /**
   * Retrieve datastore download url of dataset
   *
   * @param datasetCode Code of dataset
   * @param datasetName File name of dataset
   * @return URL to datastore location
   */
  @Override
  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {
    return getDataStoreDownloadURL(datasetCode, datasetName);
  }

  /**
   * Retrieve inputstream for dataset
   *
   * @param datasetCode Code of dataset
   * @return input stream for dataset
   */
  @Override
  public InputStream getDatasetStream(String datasetCode) {
    return null;
  }

  /**
   * Retrieve inputstream for dataset in folder
   *
   * @param datasetCode Code of dataset
   * @param folder Folder of dataset
   * @return input stream of datasets
   */
  @Override
  public InputStream getDatasetStream(String datasetCode, String folder) {
    return null;
  }

}
