package life.qbic.openbis.openbisclient;

import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchDataSetsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchExperimentTypesCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchExperimentsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchProjectsCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchSampleTypesCompletely;
import static life.qbic.openbis.openbisclient.helper.OpenBisClientHelper.fetchSamplesCompletely;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.fetchoptions.EntityTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import life.qbic.openbis.openbisclient.helper.OpenBisClientHelper;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The type Open bis client.
 */
public class OpenBisClient implements IOpenBisClient {

  private final int TIMEOUT = 100000;
  private String userId, password, sessionToken, serviceURL, url;
  private IApplicationServerApi v3;
  private IDataStoreServerApi dss3;
  private static final Logger logger = LogManager.getLogger(OpenBisClient.class);

  /**
   * Instantiates a new Open bis client.
   *
   * @param userId the user id
   * @param password the password
   * @param apiURL the api url
   */
  public OpenBisClient(String userId, String password, String apiURL) {
    this.userId = userId;
    this.password = password;
    this.serviceURL = apiURL + IApplicationServerApi.SERVICE_URL;
    this.url = apiURL;
    // get a reference to AS API
    v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, serviceURL, TIMEOUT);
    dss3 = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, serviceURL, TIMEOUT);
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
  // TODO Added for testing reasons...
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
      // TODO Set sessionToken to null
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
    SearchResult<Space> spaces =
        v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
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
    SearchResult<Project> projects =
        v3.searchProjects(sessionToken, new ProjectSearchCriteria(), fetchProjectsCompletely());
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
    SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken,
        new ExperimentSearchCriteria(), fetchExperimentsCompletely());
    return experiments.getObjects();
  }


  /**
   * Function to retrieve all samples of a given experiment Note: seems to throw a
   * ch.systemsx.cisd.common.exceptions.UserFailureException if wrong identifier given TODO Should
   * we catch it and throw an illegalargumentexception instead? would be a lot clearer in my opinion
   *
   * @param experimentIdentifier identifier/code (both should work) of the openBIS experiment
   * @return list with all samples of the given experiment
   */
  @Override
  public List<Sample> getSamplesofExperiment(String experimentIdentifier) {
    ensureLoggedIn();

    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withExperiment().withCode().thatEquals(experimentIdentifier);

    SearchResult<Sample> samplesOfExperiment =
        v3.searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());
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

    SearchResult<Sample> samplesOfExperiment =
        v3.searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());
    return samplesOfExperiment.getObjects();

  }

  @Override
  public Sample getSampleByIdentifier(String sampleIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withId().thatEquals(new SampleIdentifier(sampleIdentifier));

    SearchResult<Sample> samples =
        v3.searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

    if (samples.getObjects().isEmpty()) {
      return null;
    } else {
      return samples.getObjects().get(0);
    }
  }


  @Override
  public List<PropertyType> getPropertiesOfExperimentType(ExperimentType type) {
    ExperimentTypeSearchCriteria criteria = new ExperimentTypeSearchCriteria();
    criteria.withCode().thatEquals(type.getCode());
    ExperimentTypeFetchOptions options = new ExperimentTypeFetchOptions();
    options.withPropertyAssignments();

    List<IEntityType> res = new ArrayList<>();

    res.addAll(v3.searchExperimentTypes(sessionToken, criteria, options).getObjects());

    if (res.isEmpty()) {
      throw new NotFetchedException("Experiment type could not be found: " + type.getCode());
    }
    if (res.size() > 1) {
      throw new NotFetchedException("More than one entity type found for: " + type.getCode());
    }

    IEntityType typeWithProperties = res.get(0);

    return OpenBisClientHelper.getPropertiesOfEntityType(typeWithProperties);
  }

  @Override
  public List<PropertyType> getPropertiesOfSampleType(SampleType type) {
    SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
    criteria.withCode().thatEquals(type.getCode());
    SampleTypeFetchOptions options = new SampleTypeFetchOptions();
    options.withPropertyAssignments();

    List<IEntityType> res = new ArrayList<>();

    if (res.isEmpty()) {
      throw new NotFetchedException("Sample type could not be found: " + type.getCode());
    }
    if (res.size() > 1) {
      throw new NotFetchedException("More than one entity type found for: " + type.getCode());
    }

    IEntityType typeWithProperties = res.get(0);

    return OpenBisClientHelper.getPropertiesOfEntityType(typeWithProperties);
  }

  @Override
  public List<PropertyType> getPropertiesOfDataSetType(DataSetType type) {
    DataSetTypeSearchCriteria criteria = new DataSetTypeSearchCriteria();
    criteria.withCode().thatEquals(type.getCode());
    DataSetTypeFetchOptions options = new DataSetTypeFetchOptions();
    options.withPropertyAssignments();

    List<IEntityType> res = new ArrayList<>();

    if (res.isEmpty()) {
      throw new NotFetchedException("DataSet type could not be found: " + type.getCode());
    }
    if (res.size() > 1) {
      throw new NotFetchedException("More than one entity type found for: " + type.getCode());
    }

    IEntityType typeWithProperties = res.get(0);

    return OpenBisClientHelper.getPropertiesOfEntityType(typeWithProperties);
  }

  @Override
  public List<Sample> getSamplesOfProject(String projIdentifier) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withOrOperator();
    sampleSearchCriteria.withExperiment().withProject().withCode().thatEquals(projIdentifier);
    sampleSearchCriteria.withExperiment().withProject().withId()
        .thatEquals(new ProjectIdentifier(projIdentifier));

    SearchResult<Sample> samples =
        v3.searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

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
    // TODO unclear if parents and children should be fetched or directly included into the list
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withCode().thatEquals(sampCode);

    SearchResult<Sample> samples =
        v3.searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());

    return samples.getObjects();
  }

  @Override
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withOrOperator();
    sc.withProject().withId().thatEquals(new ProjectIdentifier(projectIdentifier));
    sc.withProject().withCode().thatEquals(projectIdentifier);

    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

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

    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   *
   * @param projectIdentifier project identifer as defined by openbis, for which the experiments
   *        should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  @Override
  public List<Experiment> getExperimentsForProject(String projectIdentifier) {
    // TODO equal to getExperimentsOfProjectByIdentifier
    ensureLoggedIn();
    return getExperimentsOfProjectByIdentifier(projectIdentifier);
  }

  @Override
  public List<Experiment> getExperimentsOfProjectByCode(String projectCode) {
    // TODO Could be combined with getExperimentsOfProjectByIdentifier
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withProject().withCode().thatEquals(projectCode);

    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

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

    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  @Override
  public List<Experiment> getExperimentsOfType(String type) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withType().withCode().thatEquals(type);

    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    return experiments.getObjects();
  }

  @Override
  public List<Sample> getSamplesOfType(String type) {
    ensureLoggedIn();
    SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
    sampleSearchCriteria.withType().withCode().thatEquals(type);

    SearchResult<Sample> samples =
        v3.searchSamples(sessionToken, sampleSearchCriteria, fetchSamplesCompletely());
    return samples.getObjects();
  }

  @Override
  public List<Project> getProjectsOfSpace(String space) {
    ensureLoggedIn();
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withSpace().withCode().thatEquals(space);

    SearchResult<Project> projects = v3.searchProjects(sessionToken, sc, fetchProjectsCompletely());

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
    // this sets the user sessionToken
    loginAsUser(userID);
    List<String> spaceIdentifiers = new ArrayList<>();
    // we are not using external functions to make sure this user is actually used
    try {
      SearchResult<Space> spaces =
          v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
      if (spaces != null) {
        for (Space space : spaces.getObjects()) {
          spaceIdentifiers.add(space.getCode());
        }
      }
    } catch (UserFailureException u) {
      logger.error("Could not fetch spaces for user " + userID
          + ", because they could not be logged in. Is user " + this.userId + " an admin user?");
      logger.warn("No spaces were returned.");
    }
    logout();
    login();

    return spaceIdentifiers;
  }

  /**
   * Returns wether a user is instance admin in openBIS
   *
   * @return true, if user is instance admin, false otherwise
   */
  @Override
  public boolean isUserAdmin(String userID) {
    ensureLoggedIn();
    PersonSearchCriteria criteria = new PersonSearchCriteria();
    criteria.withUserId().thatEquals(userID);
    PersonFetchOptions options = new PersonFetchOptions();
    options.withRoleAssignments();
    SearchResult<Person> res = v3.searchPersons(sessionToken, criteria, options);
    for (Person p : res.getObjects()) {
      for (RoleAssignment r : p.getRoleAssignments()) {
        if (r.getRole().equals(Role.ADMIN)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Project getProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();
    ProjectSearchCriteria sc = new ProjectSearchCriteria();
    sc.withOrOperator();
    sc.withId().thatEquals(new ProjectIdentifier(projectIdentifier));
    sc.withCode().thatEquals(projectIdentifier);

    SearchResult<Project> projects = v3.searchProjects(sessionToken, sc, fetchProjectsCompletely());

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

    SearchResult<Project> projects = v3.searchProjects(sessionToken, sc, fetchProjectsCompletely());

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

    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

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

    SearchResult<Experiment> experiment =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    if (experiment.getObjects().isEmpty()) {
      return null;
    }
    return experiment.getObjects().get(0);
  }

  @Override
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    ensureLoggedIn();
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    sc.withId().thatEquals(new ExperimentIdentifier(experimentIdentifier));
    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

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
    // TODO does not work yet
    throw new NotImplementedException();
  }

  /**
   * Function to list all datasets of a specific openBIS project
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  @Override
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
    // TODO does not work yet
    throw new NotImplementedException();
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
    // TODO cannot find an opportunity to do that
    return null;
  }


  /**
   * Function to list the vocabulary codes for a given property which has been added to openBIS. The
   * property has to be a Controlled Vocabulary Property. Use
   *
   * @param property the property type
   * @return list of the vocabulary terms of the given property
   */
  @Override
  public List<String> listVocabularyTermsForProperty(PropertyType property) {
    throw new NotImplementedException();

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
    throw new NotImplementedException();
  }

  @Override
  public SampleType getSampleTypeByString(String sampleType) {
    SampleTypeSearchCriteria sc = new SampleTypeSearchCriteria();
    sc.withCode().thatEquals(sampleType);

    SearchResult<SampleType> sampleTypes =
        v3.searchSampleTypes(sessionToken, sc, fetchSampleTypesCompletely());

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

    SearchResult<ExperimentType> experimentTypes =
        v3.searchExperimentTypes(sessionToken, sc, fetchExperimentTypesCompletely());

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
   *         code and dataset_type. Throughs MalformedURLException if a url can not be created from
   *         the given parameters. NOTE: datastoreURL differs from serverURL only by the port ->
   *         quick hack used
   */
  @Override
  public URL getDataStoreDownloadURL(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    String base = this.serviceURL.split(".de")[0] + ".de";
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
    String base = this.serviceURL.split(".de")[0] + ".de";
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
    Map<Sample, List<Sample>> parentMap = new HashMap<>();
    for (Sample sample : samples) {
      List<Sample> parents = new ArrayList<>();
      try {
        parents = sample.getParents();
      } catch (Exception e) {
        logger.warn("Parents of sample were not available, contacting openBIS to fetch them.");
        List<Sample> samplesWithParents = getSamplesWithParentsAndChildren(sample.getCode());
        for (Sample s : samplesWithParents) {
          parents.addAll(s.getParents());
        }
      }
      parentMap.put(sample, parents);
    }

    return parentMap;
  }

  private String getSamplePropertyOrEmptyString(Sample sample, String propertyType) {
    String property = sample.getProperties().get(propertyType);
    String result = (property == null) ? "" : property;
    return result;
  }

  /**
   * Returns lines of a spreadsheet of humanly readable information of the samples in a project.
   * Only one requested layer of the data model is returned. Experimental factors are returned in
   * the properties xml format and should be parsed before the spreadsheet is presented to the user.
   *
   * @param projectCode The 5 letter QBiC code of the project
   * @param sampleType The openBIS sampleType that should be included in the result
   * @return List containing each line of the resulting TSV fil
   */
  public List<String> getProjectTSV(String projectCode, String sampleType) {
    List<String> res = new ArrayList<String>();
    // search all samples of project
    List<Sample> allSamples = getSamplesOfProject(projectCode);
    // filter all samples by types
    List<Sample> samples = new ArrayList<Sample>();
    for (Sample s : allSamples) {
      if (sampleType.equals(s.getType().getCode()))
        samples.add(s);
    }
    Map<String, String> ncbi = new HashMap<>();
    Map<String, String> taxonomyMap = getVocabCodesAndLabelsForVocab("Q_NCBI_TAXONOMY");
    for (String key : taxonomyMap.keySet()) {
      ncbi.put(taxonomyMap.get(key), key);
    }
    String header = "QBiC Code\tSecondary Name\tLab ID\tSample Type\tAttributes\tSource";
    if (!sampleType.equals("Q_BIOLOGICAL_ENTITY"))
      header += "\tSource Name(s)\tSource Lab ID(s)";
    if (sampleType.equals("Q_TEST_SAMPLE"))
      header += "\tExtract Code(s)\tExtract Name(s)\tExtract Lab ID(s)";
    res.add(header);
    for (Sample sample : samples) {
      String code = sample.getCode();
      List<String> row = new ArrayList<String>();
      row.add(code);
      String secName = getSamplePropertyOrEmptyString(sample, "Q_SECONDARY_NAME");
      row.add(secName);
      String extID = getSamplePropertyOrEmptyString(sample, "Q_EXTERNALDB_ID");
      row.add(extID);
      String extrType = getSamplePropertyOrEmptyString(sample, "Q_PRIMARY_TISSUE");
      if (extrType.isEmpty()) {
        extrType = getSamplePropertyOrEmptyString(sample, "Q_SAMPLE_TYPE");
      }
      if (extrType.equals("CELL_LINE")) {
        extrType = sample.getProperties().get("Q_TISSUE_DETAILED");
      }
      row.add(extrType);
      String props = sample.getProperties().get("Q_PROPERTIES");
      if (props == null)
        props = "<?xml";
      row.add(props);
      row.add(fetchSource(new ArrayList<Sample>(Arrays.asList(sample)), ncbi));
      if (!sampleType.equals("Q_BIOLOGICAL_ENTITY")) {
        Set<Sample> sources = fetchAncestorsOfType(new ArrayList<Sample>(Arrays.asList(sample)),
            "Q_BIOLOGICAL_ENTITY");
        row.add(getPropertyOfSamples(sources, "Q_SECONDARY_NAME"));
        row.add(getPropertyOfSamples(sources, "Q_EXTERNALDB_ID"));
      }
      if (sampleType.equals("Q_TEST_SAMPLE")) {
        Set<Sample> extracts = fetchAncestorsOfType(new ArrayList<Sample>(Arrays.asList(sample)),
            "Q_BIOLOGICAL_SAMPLE");
        List<String> codeL = new ArrayList<String>();
        for (Sample s : extracts) {
          codeL.add(s.getCode());
        }
        row.add(org.apache.commons.lang3.StringUtils.join(codeL, ", "));
        row.add(getPropertyOfSamples(extracts, "Q_SECONDARY_NAME"));
        row.add(getPropertyOfSamples(extracts, "Q_EXTERNALDB_ID"));
      }
      res.add(org.apache.commons.lang3.StringUtils.join(row, "\t"));
    }
    return res;
  }

  /**
   * Returns a string containing a specific property of multiple samples, separated by commas
   * 
   * @param samples Set of openbis samples
   * @param property Name of the openbis property
   * @return String of comma-separated property for all input samples
   */
  protected static String getPropertyOfSamples(Set<Sample> samples, String property) {
    List<String> resL = new ArrayList<String>();
    for (Sample s : samples) {
      resL.add(s.getProperties().get(property));
    }
    return org.apache.commons.lang3.StringUtils.join(resL, ", ");
  }

  /**
   * For a list of openbis Samples returns all ancestor samples of a specific sample type
   * 
   * @param samples Set of openbis Samples
   * @param sampleTypeCode Code of the sample type by which the ancestors should be filtered
   * @return Filtered set of all ancestor samples of a specific type
   */
  protected static Set<Sample> fetchAncestorsOfType(ArrayList<Sample> samples,
      String sampleTypeCode) {
    Set<Sample> targets = new HashSet<Sample>();
    while (!samples.isEmpty()) {
      Set<Sample> store = new HashSet<Sample>();
      for (Sample sample : samples) {
        if (!sample.getType().getCode().equals(sampleTypeCode)) {
          store.addAll(sample.getParents());
        } else
          targets.add(sample);
      }
      samples = new ArrayList<Sample>(store);
    }
    return targets;
  }

  /**
   * used by getProjectTSV to get the Patient(s)/Source(s) of a list of samples
   * 
   * @param samples List of openbis samples
   * @param ncbi List of openbis vocabulary terms of the NCBI taxonomy vocabulary
   * @return String representation of the root source(s) of thse samples
   */
  protected static String fetchSource(List<Sample> samples, Map<String, String> ncbi) {
    List<String> res = new ArrayList<String>();
    boolean isCellLine = false;
    Set<Sample> roots = new HashSet<Sample>();
    while (!samples.isEmpty()) {
      Set<Sample> store = new HashSet<Sample>();
      for (Sample sample : samples) {
        if (!sample.getType().getCode().equals("Q_BIOLOGICAL_ENTITY")) {
          store.addAll(sample.getParents());
          if (sample.getType().getCode().equals("Q_BIOLOGICAL_SAMPLE"))
            isCellLine = sample.getProperties().get("Q_PRIMARY_TISSUE").equals("CELL_LINE");
        } else
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
        for (String code : ncbi.keySet()) {
          if (organism.equals(code))
            desc = ncbi.get(code);
        }
        if (isCellLine)
          desc += " Cell Line";
        else if (desc.toLowerCase().equals("homo sapiens"))
          desc = "Patient";
        res.add(desc + ' ' + id);
      } else
        res.add("unknown source");
    }
    String[] resArr = new String[res.size()];
    resArr = res.toArray(resArr);
    return StringUtils.join(resArr, "+");
  }


  @Override
  public List<Sample> getChildrenSamples(Sample sample) {
    // TODO unnecessary method in v3 api
    return sample.getChildren();
  }

  @Override
  public boolean spaceExists(String spaceCode) {
    return listSpaces().contains(spaceCode);
  }

  @Override
  public boolean projectExists(String spaceCode, String projectCode) {
    return !v3.getProjects(sessionToken,
        Arrays.asList(new ProjectIdentifier(spaceCode, projectCode)), new ProjectFetchOptions())
        .isEmpty();
  }

  @Override
  public boolean expExists(String spaceCode, String projectCode, String experimentCode) {
    return !v3.getExperiments(sessionToken,
        Arrays.asList(new ExperimentIdentifier(spaceCode, projectCode, experimentCode)),
        new ExperimentFetchOptions()).isEmpty();
  }

  @Override
  public boolean sampleExists(String sampleCode) {
    return searchSampleByCode(sampleCode).size() > 0;
  }

  @Override
  public List<Sample> searchSampleByCode(String sampleCode) {
    SampleSearchCriteria sc = new SampleSearchCriteria();
    sc.withCode().thatEquals(sampleCode);
    SampleFetchOptions fetchOptions = new SampleFetchOptions();
    fetchOptions.withSpace();
    SearchResult<Sample> samples = v3.searchSamples(sessionToken, sc, fetchOptions);
    return samples.getObjects();
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
    VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
    criteria.withVocabulary().withCode().thatEquals(vocabularyCode);

    VocabularyTermFetchOptions options = new VocabularyTermFetchOptions();
    SearchResult<VocabularyTerm> searchResult =
        v3.searchVocabularyTerms(sessionToken, criteria, options);

    Map<String, String> res = new HashMap<String, String>();
    for (VocabularyTerm term : searchResult.getObjects()) {
      if (term.getLabel() != null && !term.getLabel().isEmpty()) {
        res.put(term.getLabel(), term.getCode());
      } else {
        res.put(term.getCode(), term.getCode());
      }
    }
    return res;
  }

  // public String translateVocabCode(String code, String vocabulary) {
  // checklogin();
  // IVocabularyTermId x = new VocabularyTermPermId(code, vocabulary);
  // VocabularyTermFetchOptions options = new VocabularyTermFetchOptions();
  //
  // Map<IVocabularyTermId, VocabularyTerm> res =
  // API.getVocabularyTerms(getActiveToken(), Arrays.asList(x), options);
  // return res.get(x).getLabel();
  // }
  //
  // public Map<String, String> getVocabLabelToCode(String vocabulary) {
  // checklogin();
  //

  // }


  @Override
  public Vocabulary getVocabulary(String vocabularyCode) {
    throw new NotImplementedException();
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
    return new ArrayList<>(getVocabCodesAndLabelsForVocab(vocabularyCode).values());
  }

  /**
   * Returns a list of all Experiments of a certain user.
   *
   * @param userID ID of user
   * @return A list containing the experiments
   */
  @Override
  public List<Experiment> getExperimentsForUser(String userID) {
    loginAsUser(userID);
    // we are not reusing other functions to be sure the user in question is actually used
    SearchResult<Experiment> experiments = null;
    try {
      experiments = v3.searchExperiments(sessionToken, new ExperimentSearchCriteria(),
          fetchExperimentsCompletely());
    } catch (UserFailureException u) {
      logger.error("Could not fetch experiments for user " + userID
          + ", because they could not be logged in. Is user " + this.userId + " an admin user?");
      logger.warn("No experiments were returned.");
    }
    logout();
    login();
    if (experiments == null || experiments.getObjects().isEmpty()) {
      return null;
    } else {
      return experiments.getObjects();
    }
  }

  @Override
  public void ingest(String dss, String serviceName, Map<String, Object> params) {
    ServiceFinder serviceFinder2 =
        new ServiceFinder("openbis", IQueryApiServer.QUERY_PLUGIN_SERVER_URL);
    IQueryApiServer openbisDssService =
        serviceFinder2.createService(IQueryApiServer.class, this.url);
    openbisDssService.createReportFromAggregationService(this.sessionToken, dss, serviceName,
        params);
  }

  @Override
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    ExperimentSearchCriteria sc = new ExperimentSearchCriteria();
    for (Project project : projectList) {
      sc.withProject().withCode().thatEquals(project.getCode());
    }
    SearchResult<Experiment> experiments =
        v3.searchExperiments(sessionToken, sc, fetchExperimentsCompletely());

    if (experiments.getObjects().isEmpty()) {
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
    throw new NotImplementedException();
  }

  @Override
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    throw new NotImplementedException();
  }

  /**
   * List all datasets for given sample identifiers
   *
   * @param sampleIdentifier list of sample identifiers
   * @return List of datasets
   */
  @Override
  public List<DataSet> listDataSetsForSamples(List<String> sampleIdentifier) {
    throw new NotImplementedException();
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
    throw new NotImplementedException();
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
    throw new NotImplementedException();
  }

}
