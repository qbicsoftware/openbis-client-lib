package main;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * dss client, a proxy to the generic openbis api
 * This client is based on DSS Client for openBIS written by Emanuel Schmid and the OpenbisConnector written by Bela Hullar
 * @author wojnar
 *
 */
public class OpenBisClient{
  int timeout = 120; // 2 minutes
  int tolimit = 600;
  IOpenbisServiceFacade facade;
  IGeneralInformationService openbisInfoService;
  private IQueryApiServer openbisDssService;
  String sessionToken;
  String userId;
  String password;
  String serverURL;
  boolean verbose;

  public OpenBisClient(String loginid, String password, String serverURL, boolean verbose) {
    this.userId = loginid;
    this.password = password;
    this.serverURL = serverURL;
    this.verbose = verbose;
    this.facade = null;
    this.login();
    //		List<SampleType> x = facade.listSampleTypes();
    //		for(SampleType s : x) {
    //			System.out.println(s.getCode());
    //			System.out.println(s.getDescription());
    //			List<PropertyTypeGroup> gs = s.getPropertyTypeGroups();
    //			for(PropertyTypeGroup g : gs) {
    //				System.out.println(g.getPropertyTypes());
    //			}
    //			System.out.println();
    //		}
  }

  //TODO
  //	public List<Sample> getInfoServiceSamples(List<Sample> samps) {
  //		return this.openbisInfoService.filterSamplesVisibleToUser(sessionToken, samps, "kxmsn01");
  //	}

  //TODO
  public HashMap<String,List<String>> getPropertyTypeMapForSampleType(String code) {
    for(SampleType st : facade.listSampleTypes()) {
      if(st.getCode().equals(code)) {
        Map<String, List<String>> res = new HashMap<String,List<String>>();
        for(PropertyTypeGroup g : st.getPropertyTypeGroups()) {
          System.out.println();
          System.out.println("PropertyTypeGroupName "+g.getName());
          for(PropertyType p : g.getPropertyTypes()) {
            System.out.println(p.getCode());
            System.out.println(p.getDataType());
            System.out.println(p.getDescription());
            System.out.println(p.getLabel());
          }
        }
        //				return st.getPropertyTypeGroups();
      }
    }
    return null;
  }

  public void ingest(String dss, String serviceName, Map<String, Object> params) {
    this.openbisDssService.createReportFromAggregationService(this.sessionToken, dss, serviceName, params);
  }

  public boolean loggedin(){
    if (this.facade == null)
      return false;
    try{
      this.facade.checkSession();
    }
    catch(InvalidSessionException e){
      return false;
    }
    return true;
  }
  /**
   * logs out of the OpenBIS server
   */
  public void logout(){
    try{
      this.facade.logout();
    }
    catch (Exception e){
      //Nothing todo here
    } finally{
      this.facade = null;
    }
  }
  /**
   *  logs in to the OpenBIS server with the system userid
        after calling this function, the user has to provide the password
   */
  public void login(){
    if(this.loggedin())
      this.logout();

    int trialno = 3;
    int notrial = 0;
    int timeoutStep = this.timeout;
    while(true){
      try{
        facade = OpenbisServiceFacadeFactory.tryCreate(this.userId, this.password, this.serverURL, this.timeout*1000);
        ServiceFinder sf1 = new ServiceFinder("openbis", IQueryApiServer.QUERY_PLUGIN_SERVER_URL);
        ServiceFinder sf2 = new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        openbisInfoService = sf2.createService(IGeneralInformationService.class, serverURL);
        openbisDssService = sf1.createService(IQueryApiServer.class, this.serverURL);
        sessionToken = openbisInfoService.tryToAuthenticateForAllServices(this.userId, this.password);
        break;
      }
      catch (Exception e){
        System.err.print(e);
        if(e.getMessage().contains("Read timed out")){
          if (this.timeout >= this.tolimit)
            throw new InvalidAuthenticationException("Login failed because of read timeout");
          this.timeout += timeoutStep;

        }
        else{
          notrial++;
          if(notrial >= trialno) 
            throw new InvalidAuthenticationException("Login failed, retried too often.");
          try {
            Thread.sleep(10);
          } catch (InterruptedException e1) {
            e1.printStackTrace();
          }
          if(facade == null){
            return;//throw new UnexpectedException("OpenBis facade is not available");
          }
        }
      }
    }
  }

  public IOpenbisServiceFacade getFacade(){
    if(!this.loggedin()){
      this.login();
    }
    return facade;
  }

  protected void finalize() throws Throwable {
    this.logout();
    super.finalize();
  }

  public List<Project> listProjects() {
    if(!this.loggedin()){
      this.login();
    }
    List<Project> projects = facade.listProjects();
    return projects;
  }

  public List<Project> getProjectsofSpace(String space) {
    List<Project> projects = new ArrayList<Project>();
    List<Project> foundProjects = facade.listProjects();

    for (Project proj : foundProjects) {
      if (space.equals(proj.getSpaceCode())){
        projects.add(proj);
      }
    }
    return projects;
  }

  //TODO
  public boolean spaceExists(String name) {
    for(SpaceWithProjectsAndRoleAssignments s : facade.getSpacesWithProjects()) {
      if(s.getCode().equals(name)) return true;
    }
    return false;
  }

  public boolean projectExists(String space, String name) {
    for(Project p : getProjectsofSpace(space)) {
      if(p.getCode().equals(name)) return true;
    }
    return false;
  }

  public boolean expExists(String space, String project, String name) {
    if(projectExists(space, project)) {
      for(Experiment e : getExperimentsofProject(project)) {
        if(e.getCode().equals(name)) return true;
      }
    }
    return false;
  }

  public boolean sampleExists(String name) {
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, name));
    for(Sample x : this.openbisInfoService.searchForSamples(sessionToken, sc)) {
      if(x.getCode().equals(name)) return true;
    }
    return false;
  }

  public List<Experiment> listExperimentsForProject(
      Project project) {
    if(!this.loggedin()){
      this.login();
    }
    List<String> temp = new ArrayList<String>();
    temp.add(project.getIdentifier());
    List<Experiment> experiments = facade.listExperimentsForProjects(temp);
    return experiments;
  }

  ///SAMPLES
  public List<Sample> getSamplesofExperiment(String exp) {
    SearchCriteria sc = new SearchCriteria();
    SearchCriteria ec = new SearchCriteria();
    ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, exp));
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
    return this.openbisInfoService.searchForSamples(sessionToken, sc);
  }

  public List<Sample> getSamplesofSpace(String space) {
    SearchCriteria sc = new SearchCriteria();        
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space));
    List<Sample> foundSamples = this.openbisInfoService.searchForSamples(sessionToken, sc);
    return foundSamples;
  }

  public List<Sample> getSamplesofProjects(ArrayList<String> queries) {
    if(queries.isEmpty()) return new ArrayList<Sample>();

    List<String> projects  = new ArrayList<String>();
    List<String> foundExps  = new ArrayList<String>();
    List<Project> foundProjects = facade.listProjects();
    for (Project proj : foundProjects) {
      if (queries.contains(proj.getCode())){
        projects.add(proj.getIdentifier());
      }
    }
    List<Experiment> foundExp = facade.listExperimentsForProjects(projects);
    for (Experiment exp : foundExp) {
      foundExps.add(exp.getIdentifier());
    }
    List<Sample> foundSamples = new ArrayList<Sample>();
    for(String eName : foundExps) {
      foundSamples.addAll(facade.listSamplesForExperiments(new ArrayList<String>(Arrays.asList(eName))));
    }
    return foundSamples; 
  }

  public List<Sample> getSamplesofType(String type) {
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
    List<Sample> foundSamples = this.openbisInfoService.searchForSamples(this.sessionToken, sc);    
    return foundSamples;
  }

  ///EXPERIMENTS
  public List<Experiment> getExperimentsofProject(String project) {
    if(project==null) return new ArrayList<Experiment>();
    String projName = "";
    List<Project> projects = facade.listProjects();
    for(Project p : projects) {
      if(p.getCode().equals(project)) projName = "/"+p.getSpaceCode()+"/"+p.getCode();
    }
    List<Experiment> res = facade.listExperimentsForProjects(new ArrayList<String>(Arrays.asList(projName)));  
    return res;
  }

  public List<Experiment> getExperimentsofSpace(String space) {
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space));
    List<Experiment> foundExps = this.openbisInfoService.searchForExperiments(this.sessionToken, sc);     
    return foundExps;
  }

  //TODO
  public List<Attachment> listAttachmentsForSample(String sample_id) {
    return this.openbisInfoService.listAttachmentsForSample(this.sessionToken, new SampleIdentifierId(sample_id), true);
  }

  public List<Experiment> getExperimentsofType(String type) {
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
    List<Experiment> foundExps = this.openbisInfoService.searchForExperiments(this.sessionToken, sc);
    return foundExps;
  }

  ///DATASETS
  //Sample --> Datasets
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfSample(String code) {
    SearchCriteria ec = new SearchCriteria();
    ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createSampleCriteria(ec));
    return openbisInfoService.searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  //Experiment --> Datasets
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfExperiment(String expCode) {
    SearchCriteria ec = new SearchCriteria();
    ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, expCode));
    SearchCriteria sc = new SearchCriteria();
    sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
    return openbisInfoService.searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  //Space --> Datasets
  public List<DataSet> getDataSetsOfSpace(String space) {
    List<Sample> samples = getSamplesofSpace(space);
    ArrayList<String> ids = new ArrayList<String>();
    for (Iterator<Sample> iterator = samples.iterator(); iterator.hasNext();) {
      Sample s = (Sample) iterator.next();
      ids.add(s.getIdentifier());
    }
    return facade.listDataSetsForSamples(ids);
  }

  //Project --> Datasets
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfProject(String projCode) {
    List<Sample> samps = getSamplesofProjects(new ArrayList<String>(Arrays.asList(projCode)));
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> res = new ArrayList<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet>();
    for (Iterator<Sample> iterator = samps.iterator(); iterator.hasNext();) {
      Sample sample = (Sample) iterator.next();
      res.addAll(getDataSetsOfSample(sample.getCode()));
    }
    return res;
  }

  //Type --> Datasets
  public List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> getDataSetsOfType(String type) {
    SearchCriteria sc = new SearchCriteria();
    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, type));
    return openbisInfoService.searchForDataSetsOnBehalfOfUser(sessionToken, sc, userId);
  }

  public HashMap<String, ArrayList<String>> getParentMap(List<Sample> samples) {
    HashMap<String, ArrayList<String>> results = new HashMap<String,ArrayList<String>>();
    for(Sample p : samples) {
      String pID = p.getCode();
      String permID = p.getPermId();
      List<Sample> children = getFacade().listSamplesOfSample(permID);
      for(Sample c : children) {
        String cID = c.getCode();
        if(results.containsKey(cID)) results.get(cID).add(pID);
        else results.put(cID,new ArrayList<String>(Arrays.asList(pID)));
      }
    }
    return results;
  }

}
