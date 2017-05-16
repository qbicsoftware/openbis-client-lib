package life.qbic.openbis.openbisclient;

import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import org.apache.commons.lang.WordUtils;


/**
 * dss client, a proxy to the generic openbis api This client is based on DSS Client for openBIS
 * written by Emanuel Schmid and the OpenbisConnector written by Bela Hullar
 * 
 * @author wojnar
 */
public class OpenBisClient implements IOpenBisClient, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6218517668296784635L;
  /**
   * 
   */
  // private static final long serialVersionUID = 3926210649301601498L;
  private int timeout = 30;
  private int tolimit = 90;
  private IOpenbisServiceFacade facade;
  private IGeneralInformationService openbisInfoService;
  private IQueryApiServer openbisDssService;
  private String sessionToken;
  private String userId;
  private String password;
  private String serverURL;

  private final String dss = "DSS1";

  public OpenBisClient(String loginid, String password, String serverURL) {
    this.userId = loginid;
    this.password = password;
    this.serverURL = serverURL;
    this.facade = null;
  }

  /**
   * Checks if we are logged in
   */
  public boolean loggedin() {
    if (this.facade == null || openbisInfoService == null || openbisDssService == null)
      return false;
    try {
      this.facade.checkSession();
    } catch (InvalidSessionException e) {
      return false;
    }
    return (openbisInfoService.isSessionActive(this.sessionToken));
  }

  /**
   * logs out of the OpenBIS server
   */
  public void logout() {
    try {
      this.facade.logout();
      this.openbisDssService.logout(sessionToken);
      this.openbisInfoService.logout(sessionToken);
    } catch (Exception e) {
      // Nothing todo here
    } finally {
      this.facade = null;
      this.openbisDssService = null;
      this.openbisInfoService = null;
    }
  }

  /**
   * logs in to the OpenBIS server with the system userid after calling this function, the user has
   * to provide the password
   */
  public void login() {
    if (this.loggedin())
      this.logout();

    int trialno = 3;
    int notrial = 0;
    int timeoutStep = this.timeout;
    while (true) {
      try {
        facade = OpenbisServiceFacadeFactory.tryCreate(this.userId, this.password, this.serverURL,
            this.timeout * 1000); // it seems tryCreate has its own retry/timeout? did not return
                                  // anything while testing
        ServiceFinder serviceFinder =
            new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        ServiceFinder serviceFinder2 =
            new ServiceFinder("openbis", IQueryApiServer.QUERY_PLUGIN_SERVER_URL);

        this.setOpenbisInfoService(
            serviceFinder.createService(IGeneralInformationService.class, this.serverURL));
        this.openbisDssService =
            serviceFinder2.createService(IQueryApiServer.class, this.serverURL);
        this.sessionToken = this.getOpenbisInfoService()
            .tryToAuthenticateForAllServices(this.userId, this.password);
        break;
      } catch (Exception e) {
        System.out.println("exception");
        if (e.getMessage() != null && e.getMessage().contains("Read timed out")) {
          if (this.timeout >= this.tolimit)
            throw new InvalidAuthenticationException("Login failed because of read timeout.");
          this.timeout += timeoutStep;

        } else {
          notrial++;
          if (notrial >= trialno)
            throw new InvalidAuthenticationException(
                String.format("Login failed after %d trials.", trialno));
          try {
            Thread.sleep(10);
          } catch (InterruptedException e1) {
            e1.printStackTrace();
          }
          if (facade == null) {
            throw new NullPointerException(
                "OpenBis facade is not available. Check connection, password and user.");
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    OpenBisClient o = new OpenBisClient("admin", "abc", "https://test.qbic.de:443");
    o.login();
  }

  /**
   * Get session token of current openBIS session
   * 
   * @return session token as string
   */
  public String getSessionToken() {
    if (!this.getOpenbisInfoService().isSessionActive(this.sessionToken)) {
      this.logout();
      this.login();
    }
    return this.sessionToken;
  }

  /**
   * Get a openBIS service facade when logged in to get functionality to retrieve data for example.
   * 
   * @return a IOpenbisServiceFacade which provides various functions
   */
  public IOpenbisServiceFacade getFacade() {
    ensureLoggedIn();
    return facade;
  }

  /**
   * Getter function for GeneralInformation Service
   * 
   * @return GeneralInformationService instance
   */
  public IGeneralInformationService getOpenbisInfoService() {
    return openbisInfoService;
  }

  /**
   * Setter function for GeneralInformation Service
   * 
   * @param openbisInfoService a GeneralInformationService instance
   * @return
   */
  public void setOpenbisInfoService(IGeneralInformationService openbisInfoService) {
    this.openbisInfoService = openbisInfoService;
  }

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
  public void ensureLoggedIn() {
    if (!this.loggedin()) {
      this.login();
    }
  }

  /**
   * Function to get all spaces which are registered in this openBIS instance
   * 
   * @return list with the identifiers of all available spaces
   */
  public List<String> listSpaces() {
    List<SpaceWithProjectsAndRoleAssignments> spaces_roles = facade.getSpacesWithProjects();
    List<String> spaces = new ArrayList<String>();
    for (SpaceWithProjectsAndRoleAssignments sp : spaces_roles) {
      spaces.add(sp.getCode());
    }
    return spaces;
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
    ingest("DSS1", "update-sample-metadata", params);
  }

  /**
   * Function to get all projects which are registered in this openBIS instance
   * 
   * @return list with all projects which are registered in this openBIS instance
   */
  public List<Project> listProjects() {
    return this.getFacade().listProjects();
  }

  /**
   * Function to list all Experiments which are registered in the openBIS instance.
   * 
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> listExperiments() {
    ensureLoggedIn();
    List<String> spaces = this.listSpaces();
    List<Experiment> foundExps = new ArrayList<Experiment>();

    for (String s : spaces) {
      SearchCriteria sc = new SearchCriteria();
      sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, s));
      foundExps.addAll(this.getOpenbisInfoService().searchForExperiments(this.sessionToken, sc));
    }
    return foundExps;
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
    ensureLoggedIn();
    /*
     * SearchCriteria sc = new SearchCriteria(); SearchCriteria ec = new SearchCriteria();
     * ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
     * experimentIdentifier)); sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
     * List<Sample> foundSamples = this.getOpenbisInfoService().searchForSamples(sessionToken, sc);
     * return foundSamples;
     */
    return this.getOpenbisInfoService().listSamplesForExperiment(sessionToken,
        experimentIdentifier);
  }

  /**
   * Function to retrieve all samples of a given space
   * 
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all samples of the given space
   */
  public List<Sample> getSamplesofSpace(String spaceIdentifier) {
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, spaceIdentifier));
    List<Sample> foundSamples = this.getOpenbisInfoService().searchForSamples(sessionToken, sc);

    return foundSamples;
  }

  /**
   * Updates or adds properties for one sample
   * 
   * @param sampleID ID of long type, can be fetched from openBIS samples
   * @param properties A map of all sample properties to be updated
   */
  public void updateSampleMetadata(long sampleID, Map<String, String> properties) {
    ServiceFinder serviceFinder =
        new ServiceFinder("openbis", IGeneralInformationChangingService.SERVICE_URL);
    IGeneralInformationChangingService service =
        serviceFinder.createService(IGeneralInformationChangingService.class, serverURL);
    service.updateSampleProperties(sessionToken, sampleID, properties);
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
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sampleIdentifier));
    List<Sample> foundSamples = this.getOpenbisInfoService().searchForSamples(sessionToken, sc);
    if (foundSamples.size() < 1) {
      throw new IllegalArgumentException();
    } else {
      return foundSamples.get(0);
    }
  }

  /**
   * Function to get all samples of a specific project
   * 
   * @param projIdentifierOrCode identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesOfProject(String projIdentifier) {
    ensureLoggedIn();
    List<String> projects = new ArrayList<String>();
    List<Project> foundProjects = facade.listProjects();
    List<Sample> foundSamples = new ArrayList<Sample>();
    for (Project proj : foundProjects) {
      if (projIdentifier.equals(proj.getIdentifier())) {
        projects.add(proj.getIdentifier());
      }
    }
    if (projects.size() > 0) {
      List<Experiment> foundExp = facade.listExperimentsForProjects(projects);
      for (Experiment exp : foundExp) {
        // TODO search service?
        /*
         * SearchCriteria sc = new SearchCriteria(); SearchCriteria ec = new SearchCriteria();
         * ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
         * exp.getIdentifier())); sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
         */
        foundSamples.addAll(getSamplesofExperiment(exp.getIdentifier()));
      }
    }
    return foundSamples;
  }

  /**
   * Function to get all samples of a specific project
   * 
   * @param projIdentifierOrCode identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesOfProjectBySearchService(String projIdentifier) {
    ensureLoggedIn();
    List<Sample> foundSamples = new ArrayList<Sample>();
    SearchCriteria sc = new SearchCriteria();
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(SearchCriteria.MatchClause
        .createAttributeMatch(SearchCriteria.MatchClauseAttribute.PROJECT, projIdentifier));
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    foundSamples = this.getOpenbisInfoService().searchForSamples(sessionToken, sc);
    return foundSamples;
  }

  /**
   * Function to get all samples with their parents and children of a specific project
   * 
   * @param projIdentifierOrCode identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesWithParentsAndChildrenOfProjectBySearchService(
      String projIdentifier) {
    ensureLoggedIn();

    SearchCriteria sc = new SearchCriteria();
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(SearchCriteria.MatchClause
        .createAttributeMatch(SearchCriteria.MatchClauseAttribute.PROJECT, projIdentifier));
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS,
        SampleFetchOption.PROPERTIES, SampleFetchOption.DESCENDANTS);
    List<Sample> allSamples = facade.searchForSamples(sc, fetchOptions);

    return allSamples;
  }

  /**
   * Function to get a sample with its parents and children
   * 
   * @param sampCode code of the openBIS sample
   * @return sample
   */
  public List<Sample> getSamplesWithParentsAndChildren(String sampCode) {
    ensureLoggedIn();

    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause
        .createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampCode));
    EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS,
        SampleFetchOption.PROPERTIES, SampleFetchOption.DESCENDANTS);
    List<Sample> allSamples = this.getFacade().searchForSamples(sc, fetchOptions);

    return allSamples;
  }


  /**
   * returns a list of all Experiments connected to the project with the identifier from openBis
   * 
   * @param projectIdentifier identifier of the given openBIS project
   * @return list of all experiments of the given project
   */
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier) {
    List<String> projects = new ArrayList<String>();
    projects.add(projectIdentifier);
    List<Experiment> foundExps = this.getFacade().listExperimentsForProjects(projects);
    return foundExps;
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
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, project.getCode()));
    return getFacade().searchForExperiments(pc);
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject2(String projectCode) {
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, projectCode));
    return getFacade().searchForExperiments(pc);
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
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, project.getCode()));
    return getOpenbisInfoService().searchForExperiments(getSessionToken(), pc);
  }

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   * 
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject3(String projectCode) {
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, projectCode));
    return getOpenbisInfoService().searchForExperiments(getSessionToken(), pc);
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
    if (projectCode.contains("/") || projectCode.isEmpty())
      throw new IllegalArgumentException();
    else {
      String projID = "";
      List<Project> projects = this.getFacade().listProjects();
      for (Project p : projects) {
        if (p.getCode().equals(projectCode))
          projID = "/" + p.getSpaceCode() + "/" + p.getCode();
      }
      if (!projID.isEmpty())
        return getExperimentsOfProjectByIdentifier(projID);
      return new ArrayList<Experiment>();
    }
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
    ensureLoggedIn();
    if (spaceIdentifier.isEmpty()) {
      List<Experiment> foundExps = this.listExperiments();
      return foundExps;
    } else {
      SearchCriteria sc = new SearchCriteria();
      sc.addMatchClause(
          MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, spaceIdentifier));
      List<Experiment> foundExps =
          this.getOpenbisInfoService().searchForExperiments(this.sessionToken, sc);
      return foundExps;
    }
  }

  /**
   * Function to retrieve all experiments of a specific given type
   * 
   * @param type identifier of the openBIS experiment type
   * @return list with all experiments of this given type
   */
  public List<Experiment> getExperimentsOfType(String type) {
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
    List<Experiment> foundExps =
        this.getOpenbisInfoService().searchForExperiments(this.sessionToken, sc);
    return foundExps;
  }

  /**
   * Function to retrieve all samples of a specific given type
   * 
   * @param type identifier of the openBIS sample type
   * @return list with all samples of this given type
   */
  public List<Sample> getSamplesOfType(String type) {
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
    List<Sample> foundSamples =
        this.getOpenbisInfoService().searchForSamples(this.sessionToken, sc);
    return foundSamples;
  }



  /**
   * Function to retrieve all projects of a given space from openBIS.
   * 
   * @param space identifier of the openBIS space
   * @return a list with all projects objects for the given space
   */
  public List<Project> getProjectsOfSpace(String space) {
    List<Project> projects = new ArrayList<Project>();
    List<Project> foundProjects = facade.listProjects();

    for (Project proj : foundProjects) {
      if (space.equals(proj.getSpaceCode())) {
        projects.add(proj);
      }
    }
    return projects;
  }

  /**
   * Function to retrieve a project from openBIS by the identifier of the project.
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return project with the given id
   */
  public Project getProjectByIdentifier(String projectIdentifier) {
    if (!projectIdentifier.contains("/") || projectIdentifier.isEmpty())
      throw new IllegalArgumentException(
          String.format("project identifer %s is not a valid identifier", projectIdentifier));
    else {
      List<Project> projects = this.listProjects();
      Project project = null;
      for (Project p : projects) {
        if (p.getIdentifier().equals(projectIdentifier)) {
          project = p;
        }
      }
      if (project == null) {
        throw new IllegalArgumentException(
            String.format("project %s does not exist.", projectIdentifier));
      }
      return project;
    }
  }

  /**
   * Function to retrieve a project from openBIS by the code of the project.
   * 
   * @param projectCode code of the openBIS project
   * @return project with the given code
   */
  public Project getProjectByCode(String projectCode) {
    if (projectCode.contains("/") || projectCode.isEmpty())
      throw new IllegalArgumentException();
    else {
      List<Project> projects = this.listProjects();
      Project project = null;
      for (Project p : projects) {
        if (p.getCode().equals(projectCode)) {
          project = p;
          break;
        }
      }
      return project;
    }
  }

  /**
   * Function to retrieve a experiment from openBIS by the code of the experiment.
   * 
   * @param experimentCode code of the openBIS experiment
   * @return experiment with the given code
   */
  public Experiment getExperimentByCode(String experimentCode) {
    if (experimentCode.contains("/") || experimentCode.isEmpty())
      throw new IllegalArgumentException();
    else {
      List<Experiment> experiments = this.listExperiments();
      Experiment experiment = null;
      for (Experiment e : experiments) {
        if (e.getCode().equals(experimentCode)) {
          experiment = e;
          break;
        }
      }
      return experiment;
    }
  }

  /**
   * Function to retrieve a experiment from openBIS by the code of the experiment.
   * 
   * @param experimentId id of the openBIS experiment
   * @return experiment with the given code
   */
  public Experiment getExperimentById(String experimentId) {
    if (!experimentId.contains("/") || experimentId.isEmpty())
      throw new IllegalArgumentException();
    else {
      List<Experiment> experiments = this.listExperiments();
      Experiment experiment = null;
      for (Experiment e : experiments) {
        if (e.getIdentifier().equals(experimentId)) {
          experiment = e;
          break;
        }
      }
      return experiment;
    }
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
    if (expIdentifer == null || !expIdentifer.contains("/") || expIdentifer.isEmpty()) {
      throw new IllegalArgumentException();
    }
    String[] split = expIdentifer.split("/");
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, split[split.length - 1]));
    return getFacade().searchForExperiments(pc);
  }



  /**
   * Function to retrieve the project of an experiment from openBIS
   * 
   * @param experimentIdentifier identifier of the openBIS experiment
   * @return project connected to the given experiment
   */
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    if (!experimentIdentifier.contains("/") || experimentIdentifier.isEmpty())
      throw new IllegalArgumentException();
    else {
      List<Project> projects = this.facade.listProjects();
      String project = experimentIdentifier.split(("/"))[2];
      Project found_proj = null;

      for (Project proj : projects) {
        if (project.equals(proj.getIdentifier().split("/")[2])) {
          found_proj = proj;
        }
      }
      return found_proj;
    }
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
    if (!sampleIdentifier.contains("/") || sampleIdentifier.isEmpty())
      throw new IllegalArgumentException();
    else {
      List<String> identifier = new ArrayList<String>();
      identifier.add(sampleIdentifier);
      return this.facade.listDataSetsForSamples(identifier);
    }
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
    ensureLoggedIn();
    SearchCriteria ec = new SearchCriteria();
    ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sampleCode));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createSampleCriteria(ec));
    return getOpenbisInfoService().searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
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
    String permPattern = "[0-9]{17}-[0-9]+";
    if (!experimentPermID.matches(permPattern) || experimentPermID.isEmpty())
      throw new IllegalArgumentException();
    else
      return this.getFacade().listDataSetsForExperiment(experimentPermID);
  }

  /**
   * Returns all datasets of a given experiment. The new version should run smoother
   * 
   * @param experimentIdentifier identifier or code of the openbis experiment
   * @return list of all datasets of the given experiment
   */
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfExperimentByIdentifier(
      String experimentIdentifier) {
    ensureLoggedIn();
    SearchCriteria ec = new SearchCriteria();
    String[] idSplit = experimentIdentifier.split("/");
    ec.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, idSplit[idSplit.length - 1]));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
    return getOpenbisInfoService().searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  /**
   * Function to list all datasets of a specific openBIS space
   * 
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all datasets of the given space
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfSpaceByIdentifier(
      String spaceIdentifier) {
    List<Sample> samples = getSamplesofSpace(spaceIdentifier);
    ArrayList<String> ids = new ArrayList<String>();
    for (Iterator<Sample> iterator = samples.iterator(); iterator.hasNext();) {
      Sample s = (Sample) iterator.next();
      ids.add(s.getIdentifier());
    }
    if (ids.isEmpty()) {
      return new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
    }
    return this.getFacade().listDataSetsForSamples(ids);
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfProjectByIdentifier(
      String projectIdentifier) {
    ArrayList<String> ids = new ArrayList<String>();
    for (Experiment e : getExperimentsOfProjectByIdentifier(projectIdentifier))
      ids.add(e.getIdentifier());
    if (ids.isEmpty()) {
      return new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
    } else {
      return listDataSetsForExperiments(ids);
    }

    // List<Sample> samps = getSamplesOfProject(projectIdentifier);
    // List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> res =
    // new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
    // for (Iterator<Sample> iterator = samps.iterator(); iterator.hasNext();) {
    // Sample sample = (Sample) iterator.next();
    // res.addAll(getDataSetsOfSampleByIdentifier(sample.getIdentifier()));
    // }
    // return res;
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<DataSet> getDataSetsOfProjectByIdentifierWithSearchCriteria(
      String projectIdentifier) {
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, projectIdentifier));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    return getOpenbisInfoService().searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getClientDatasetsOfProjectByIdentifierWithSearchCriteria(
      String projectIdentifier) {
    SearchCriteria sc = new SearchCriteria();
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, projectIdentifier));

    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    return getFacade().searchForDataSets(sc);
  }

  /**
   * Function to list all datasets of a specific openBIS experiment
   * 
   * @param experimentCode code of the openBIS experiment
   * @return list with all datasets of the given experiment
   */
  public List<DataSet> getDataSetsOfExperimentByCodeWithSearchCriteria(String experimentCode) {
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, experimentCode));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    return getOpenbisInfoService().searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }


  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getClientDataSetsOfExperimentByCodeWithSearchCriteria(
      String experimentCode) {
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, experimentCode));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    return getFacade().searchForDataSets(sc);
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
    StringBuilder sb = new StringBuilder();
    for (Project criteria : projectIdentifier) {
      sb.append(criteria.getCode());
      sb.append(" ");
    }
    SearchCriteria sc = new SearchCriteria();
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, sb.toString()));

    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    return getOpenbisInfoService().searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfProjects2(
      List<Project> projectIdentifier) {
    StringBuilder sb = new StringBuilder();
    for (Project criteria : projectIdentifier) {
      sb.append(criteria.getCode());
      sb.append(" ");
    }
    SearchCriteria sc = new SearchCriteria();
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(
        MatchClause.createAttributeMatch(MatchClauseAttribute.PROJECT, sb.toString()));

    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    return getFacade().searchForDataSets(sc);
  }



  /**
   * Function to list all datasets of a specific openBIS project
   * 
   * @param projectCode code of the openBIS project
   * @return list with all datasets of the given project
   */
  // public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>
  // getDataSetsOfProjectByCode(
  // String projectCode) {
  // List<Sample> samps = getSamplesOfProject(projectCode); //TODO this needs an identifier, it is
  // useless at the moment!
  // System.out.println(samps);
  // List<DataSet> res = new ArrayList<DataSet>();
  // for (Iterator<Sample> iterator = samps.iterator(); iterator.hasNext();) {
  // Sample sample = (Sample) iterator.next();
  // res.addAll(getDataSetsOfSample(sample.getCode()));
  // }
  // return res;
  // }

  /**
   * Function to list all datasets of a specific type
   * 
   * @param type identifier of the openBIS type
   * @return list with all datasets of the given type
   */
  public List<DataSet> getDataSetsByType(String type) {
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
    return getOpenbisInfoService().searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  /**
   * Function to list all attachments of a sample
   * 
   * @param sampleIdentifier identifier of the openBIS sample
   * @return list with all attachments connected to the given sample
   */
  public List<Attachment> listAttachmentsForSampleByIdentifier(String sampleIdentifier) {
    ensureLoggedIn();
    return getOpenbisInfoService().listAttachmentsForSample(this.sessionToken,
        new SampleIdentifierId(sampleIdentifier), true);
  }

  /**
   * Function to list all attachments of a project
   * 
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all attachments connected to the given project
   */
  public List<Attachment> listAttachmentsForProjectByIdentifier(String projectIdentifier) {
    ensureLoggedIn();
    return this.getOpenbisInfoService().listAttachmentsForProject(this.sessionToken,
        new ProjectIdentifierId(projectIdentifier), true);
  }

  /**
   * Function to add an attachment to a existing project in openBIS by calling the corresponding
   * ingestion service of openBIS. TODO specify which parameters have to be there may not work yet!
   * 
   * @param parameter map with needed information for registration process by ingestion service
   */
  public void addAttachmentToProject(Map<String, Object> parameter) {
    ensureLoggedIn();
    System.out.println(this.openbisDssService.createReportFromAggregationService(this.sessionToken,
        dss, "add-attachment", parameter));
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
    ensureLoggedIn();
    return this.openbisDssService.createReportFromAggregationService(this.sessionToken, dss, name,
        parameters);
  }

  /**
   * Returns all users of a Space.
   * 
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  public Set<String> getSpaceMembers(String spaceCode) {
    List<SpaceWithProjectsAndRoleAssignments> spaces = this.getFacade().getSpacesWithProjects();
    for (SpaceWithProjectsAndRoleAssignments space : spaces) {
      if (space.getCode().equals(spaceCode)) {
        return space.getUsers();
      }
    }
    return null;
  }

  /**
   * Function to retrieve all properties which have been assigned to a specific entity type
   * 
   * @param entity_type entitiy type
   * @return list of properties which are assigned to the entity type
   */
  public List<PropertyType> listPropertiesForType(EntityType entity_type) {
    List<PropertyType> property_types = new ArrayList<PropertyType>();
    List<PropertyTypeGroup> props = entity_type.getPropertyTypeGroups();
    for (PropertyTypeGroup pg : props) {
      for (PropertyType prop_type : pg.getPropertyTypes()) {
        property_types.add(prop_type);
      }
    }
    return property_types;
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
    return this.openbisDssService
        .createReportFromAggregationService(this.sessionToken, dss, serviceName, parameters)
        .toString();
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
    return this.openbisDssService.createReportFromAggregationService(this.sessionToken, dss,
        "create-parent-child", parameters).toString();
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
    @SuppressWarnings("unchecked")
    Map<String, String> properties = (Map<String, String>) params.get("properties");
    if ((params.get("properties") != null) && properties.get("QBIC_BARCODE") != null) {
      String barcode = generateBarcode(
          "/" + params.get("space").toString() + "/" + params.get("project").toString(),
          number_of_samples_offset);
      properties.put("QBIC_BARCODE", barcode);
      params.put("code", barcode);
    }
    // System.out.println(params);
    return this.openbisDssService
        .createReportFromAggregationService(this.sessionToken, dss, service, params).toString();
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
   * TODO implement function that, given a project, returns a graph model of the samples. probably
   * use SampleFetchOption.DESCENDANTS
   */


  /**
   * Function to retrieve parent samples of a sample
   * 
   * @param sampleCode Code of the query sample
   * @return List of parent samples
   */
  public List<Sample> getChildrenBySearchService(String sampleCode) {
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause
        .createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode));
    // List<Sample> foundSample = this.facade.searchForSamples(sc);

    SearchCriteria sampleSc = new SearchCriteria();
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(sc));
    List<Sample> foundParentSamples =
        this.getOpenbisInfoService().searchForSamples(sessionToken, sampleSc);

    return foundParentSamples;
  }


  /**
   * Function to retrieve parent samples of a sample
   * 
   * @param sampleCode Code of the query sample
   * @return List of parent samples
   */
  public List<Sample> getParentsBySearchService(String sampleCode) {
    ensureLoggedIn();
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause
        .createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode));

    SearchCriteria sampleSc = new SearchCriteria();
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(sc));
    List<Sample> foundParentSamples =
        this.getOpenbisInfoService().searchForSamples(sessionToken, sampleSc);

    return foundParentSamples;
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
    for (SpaceWithProjectsAndRoleAssignments s : this.facade.getSpacesWithProjects()) {
      if (s.getCode().equals(name))
        return true;
    }
    return false;
  }

  /**
   * Checks if a project object of a certain code exists under a given space in openBIS
   * 
   * @param spaceCode the code of an openBIS space
   * @param projectCode the code of a project
   * @return true, if the project exists under this space, false otherwise
   */
  public boolean projectExists(String spaceCode, String projectCode) {
    for (Project p : getProjectsOfSpace(spaceCode)) {
      if (p.getCode().equals(projectCode))
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
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, name));
    for (Sample x : this.getOpenbisInfoService().searchForSamples(sessionToken, sc)) {
      if (x.getCode().equals(name))
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
    for (Vocabulary v : facade.listVocabularies()) {
      if (v.getCode().equals(vocabularyCode)) {
        Map<String, String> map = new HashMap<String, String>();
        for (VocabularyTerm t : v.getTerms()) {
          map.put(t.getLabel(), t.getCode());
        }
        return map;
      }
    }
    return null;
  }

  public Vocabulary getVocabulary(String vocabularyCode) {
    for (Vocabulary v : facade.listVocabularies()) {
      if (v.getCode().equals(vocabularyCode))
        return v;
    }
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
    ArrayList<String> res = new ArrayList<String>();
    res.addAll(this.getVocabCodesAndLabelsForVocab(vocabularyCode).values());
    return res;
  }

  /**
   * Returns a list of all Codes in a Vocabulary in openBIS. This is useful when labels don't exist
   * or are not needed.
   * 
   * @param vocabularyCode Code of the Vocabulary type
   * @return A list containing the codes of the vocabulary type
   */
  public List<Experiment> getExperimentsForUser(String userID) {
    List<Experiment> res = new ArrayList<Experiment>();
    List<Project> projects = openbisInfoService.listProjectsOnBehalfOfUser(sessionToken, userID);
    for (Project p : projects) {
      res.addAll(getExperimentsForProject(p));
    }
    return res;
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
    if (openbisDssService == null) {
      ServiceFinder serviceFinder2 =
          new ServiceFinder("openbis", IQueryApiServer.QUERY_PLUGIN_SERVER_URL);
      openbisDssService = serviceFinder2.createService(IQueryApiServer.class, this.serverURL);
    }
    this.openbisDssService.createReportFromAggregationService(this.sessionToken, dss, serviceName,
        params);
  }

  /**
   * List all experiments for given list of projects
   * 
   * @param projectList list of project identifiers
   * @return List of experiments
   */
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    return this.getOpenbisInfoService().listExperiments(this.getSessionToken(), projectList, null);

  }

  /**
   * List all datasets for given experiment identifiers
   * 
   * @param experimentIdentifiers list of experiment identifiers
   * @return List of datasets
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> listDataSetsForExperiments(
      List<String> experimentIdentifiers) {
    return this.getFacade().listDataSetsForExperiments(experimentIdentifiers);
  }

  /**
   * List all samples for given project identifiers
   * 
   * @param projectIdentifiers list of project identifiers
   * @return List of samples
   */
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    return this.getFacade().listSamplesForProjects(projectIdentifiers);
  }

  /**
   * List all datasets for given sample identifiers
   * 
   * @param sampleIdentifier list of sample identifiers
   * @return List of datasets
   */
  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> listDataSetsForSamples(
      List<String> sampleIdentifier) {
    return this.getFacade().listDataSetsForSamples(sampleIdentifier);
  }

  /**
   * Retrieve datastore download url of dataset
   * 
   * @param datasetCode Code of dataset
   * @param datasetName File name of dataset
   * @return URL to datastore location
   */
  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {

    return this.getDataStoreDownloadURL(datasetCode, datasetName);
  }

  /**
   * Retrieve inputstream for dataset
   * 
   * @param datasetCode Code of dataset
   * @return input stream for dataset
   */
  public InputStream getDatasetStream(String datasetCode) {

    IOpenbisServiceFacade facade = this.getFacade();
    ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet = facade.getDataSet(datasetCode);
    FileInfoDssDTO[] filelist = dataSet.listFiles("original", false);
    return dataSet.getFile(filelist[0].getPathInDataSet());
  }

  /**
   * Retrieve inputstream for dataset in folder
   * 
   * @param datasetCode Code of dataset
   * @param folder Folder of dataset
   * @return input stream of datasets
   */
  public InputStream getDatasetStream(String datasetCode, String folder) {

    IOpenbisServiceFacade facade = this.getFacade();
    ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet = facade.getDataSet(datasetCode);
    FileInfoDssDTO[] filelist =
        dataSet.listFiles((new StringBuilder("original/")).append(folder).toString(), false);
    return dataSet.getFile(filelist[0].getPathInDataSet());
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
    final String key = "codes";
    Map<String, Object> tmp = new HashMap<String, Object>();
    if (params.containsKey(key)) {
      tmp.put(key, params.get(key));
    } else {
      throw new IllegalArgumentException(String.format(
          "params should contain one entry, with key '%s'. Values should contain dataset codes.",
          key));
    }
    return getAggregationService("query-files", tmp);
  }

  @Override
  public List<String> getUserSpaces(String userID) {
    List<String> userSpaces = new ArrayList<String>();
    for (SpaceWithProjectsAndRoleAssignments s : facade.getSpacesWithProjects()) {
      Set<Role> roles = s.getRoles(userID);
      if (!roles.isEmpty()) {
        userSpaces.add(s.getCode());
      }
    }
    return userSpaces;
  }

  @Override
  public boolean isUserAdmin(String userID) {
    boolean isAdmin = false;
    for (SpaceWithProjectsAndRoleAssignments s : facade.getSpacesWithProjects()) {
      if (isAdmin) {
        return true;
      } else {
        Set<Role> roles = s.getRoles(userID);
        if (!roles.isEmpty())
          for (Role r : roles)
            isAdmin = r.toString().equals("ADMIN(instance)");
      }
    }
    return false;
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
  @Override
  public List<String> getProjectTSV(String projectCode, String sampleType) {
    List<String> res = new ArrayList<String>();
    // search all samples of project
    SearchCriteria sc = new SearchCriteria();
    SearchCriteria pc = new SearchCriteria();
    pc.addMatchClause(SearchCriteria.MatchClause
            .createAttributeMatch(SearchCriteria.MatchClauseAttribute.PROJECT, projectCode));
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(pc));
    EnumSet<SampleFetchOption> fetchOptions =
            EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES);
    List<Sample> allSamples = facade.searchForSamples(sc, fetchOptions);
    // filter all samples by types
    List<Sample> samples = new ArrayList<Sample>();
    for (Sample s : allSamples) {
      if (sampleType.equals(s.getSampleTypeCode()))
        samples.add(s);
    }
    // sort remaining samples-
    // Arrays.sort(samples);

    Vocabulary voc = getVocabulary("Q_NCBI_TAXONOMY");
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
      String secName = sample.getProperties().get("Q_SECONDARY_NAME");
      if (secName == null)
        secName = "";
      row.add(secName);
      String extID = sample.getProperties().get("Q_EXTERNALDB_ID");
      if (extID == null)
        extID = "";
      row.add(extID);
      String extrType = sample.getProperties().get("Q_PRIMARY_TISSUE");
      if (extrType == null)
        extrType = sample.getProperties().get("Q_SAMPLE_TYPE");
      if (extrType == null)
        extrType = "";
      if (extrType.equals("CELL_LINE"))
        extrType = sample.getProperties().get("Q_TISSUE_DETAILED");
      row.add(extrType);
      String props = sample.getProperties().get("Q_PROPERTIES");
      if (props == null)
        props = "<?xml";
      row.add(props);
      row.add(HelperMethods.fetchSource(new ArrayList<Sample>(Arrays.asList(sample)), voc.getTerms()));
      if (!sampleType.equals("Q_BIOLOGICAL_ENTITY")) {
        Set<Sample> sources = HelperMethods.fetchAncestorsOfType(new ArrayList<Sample>(Arrays.asList(sample)),
                "Q_BIOLOGICAL_ENTITY");
        row.add(HelperMethods.getPropertyOfSamples(sources, "Q_SECONDARY_NAME"));
        row.add(HelperMethods.getPropertyOfSamples(sources, "Q_EXTERNALDB_ID"));
      }
      if (sampleType.equals("Q_TEST_SAMPLE")) {
        Set<Sample> extracts = HelperMethods.fetchAncestorsOfType(new ArrayList<Sample>(Arrays.asList(sample)),
                "Q_BIOLOGICAL_SAMPLE");
        List<String> codeL = new ArrayList<String>();
        for (Sample s : extracts) {
          codeL.add(s.getCode());
        }
        row.add(org.apache.commons.lang.StringUtils.join(codeL, ", "));
        row.add(HelperMethods.getPropertyOfSamples(extracts, "Q_SECONDARY_NAME"));
        row.add(HelperMethods.getPropertyOfSamples(extracts, "Q_EXTERNALDB_ID"));
      }
      res.add(org.apache.commons.lang.StringUtils.join(row,"\t"));
    }
    return res;
  }


}
