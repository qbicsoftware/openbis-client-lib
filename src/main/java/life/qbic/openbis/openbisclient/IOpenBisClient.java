package life.qbic.openbis.openbisclient;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IOpenBisClient {

  public boolean loggedin();

  /**
   * logs out of the OpenBIS server
   */
  public void logout();

  /**
   * logs in to the OpenBIS server with the system userid after calling this function, the user has
   * to provide the password
   */
  public void login();

  /**
   * Get session token of current openBIS session
   *
   * @return session token as string
   */
  public String getSessionToken();

//  /**
//   * Get a openBIS service facade when logged in to get functionality to retrieve data for example.
//   *
//   * @return a IOpenbisServiceFacade which provides various functions
//   */
//  public IOpenbisServiceFacade getFacade();
//
//  /**
//   * Getter function for GeneralInformation Service
//   *
//   * @return GeneralInformationService instance
//   */
//  public IGeneralInformationService getOpenbisInfoService();
//
//  /**
//   * Setter function for GeneralInformation Service
//   *
//   * @param openbisInfoService a GeneralInformationService instance
//   * @return
//   */
//  public void setOpenbisInfoService(IGeneralInformationService openbisInfoService);

  /**
   * Checks if logged in, reconnects if not
   */
  public void ensureLoggedIn();

  /**
   * Function to get all spaces which are registered in this openBIS instance
   *
   * @return list with the identifiers of all available spaces
   */
  public List<String> listSpaces();

//  /**
//   * ingests external ids given a mapping between QBiC sample IDs and the external ids
//   *
//   * @param idMap
//   */
//  public void setExternalIDs(Map<String, String> idMap);

  /**
   * Function to get all projects which are registered in this openBIS instance
   *
   * @return list with all projects which are registered in this openBIS instance
   */
  public List<Project> listProjects();

  /**
   * Function to list all Experiments which are registered in the openBIS instance.
   *
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> listExperiments();

  // TODO use search service with experiment code ?

  /**
   * Function to retrieve all samples of a given experiment Note: seems to throw a
   * ch.systemsx.cisd.common.exceptions.UserFailureException if wrong identifier given TODO Should
   * we catch it and throw an illegalargumentexception instead? would be a lot clearer in my
   * opinion
   *
   * @param experimentIdentifier identifier/code (both should work) of the openBIS experiment
   * @return list with all samples of the given experiment
   */
  public List<Sample> getSamplesofExperiment(String experimentIdentifier);

  /**
   * Function to retrieve all samples of a given space
   *
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all samples of the given space
   */
  public List<Sample> getSamplesofSpace(String spaceIdentifier);

//  /**
//   * Updates or adds properties for one sample
//   *
//   * @param sampleID ID of long type, can be fetched from openBIS samples
//   * @param properties A map of all sample properties to be updated
//   */
//  public void updateSampleMetadata(long sampleID, Map<String, String> properties);
//

  /**
   * Function to retrieve a sample by it's identifier or code Note: seems to throw a
   * java.lang.IndexOutOfBoundsException if wrong identifier given TODO Should we catch it and throw
   * an illegalargumentexception instead? would be a lot clearer in my opinion
   *
   * @param sampleIdentifier identifier or code of the sample
   * @return the sample with the given identifier
   */
  public Sample getSampleByIdentifier(String sampleIdentifier);

  /**
   * Function to get all samples of a specific project
   *
   * @param projIdentifier identifier of the openBIS project
   * @return list with all samples connected to the given project
   */
  public List<Sample> getSamplesOfProject(String projIdentifier);

//  /**
//   * Function to get all samples of a specific project
//   *
//   * @param projIdentifier identifier of the openBIS project
//   * @return list with all samples connected to the given project
//   */
//  public List<Sample> getSamplesOfProjectBySearchService(String projIdentifier);
//
//  /**
//   * Function to get all samples with their parents and children of a specific project
//   *
//   * @param projIdentifier identifier of the openBIS project
//   * @return list with all samples connected to the given project
//   */
//  public List<Sample> getSamplesWithParentsAndChildrenOfProjectBySearchService(
//      String projIdentifier);

  /**
   * Function to get a sample with its parents and children
   *
   * @param sampCode code of the openBIS sample
   * @return sample
   */
  public List<Sample> getSamplesWithParentsAndChildren(String sampCode);


  /**
   * returns a list of all Experiments connected to the project with the identifier from openBis
   *
   * @param projectIdentifier identifier of the given openBIS project
   * @return list of all experiments of the given project
   */
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier);

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance. av: 19353 ms
   *
   * @param project the project for which the experiments should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject(Project project);

//  /**
//   * Function to list all Experiments for a specific project which are registered in the openBIS
//   * instance. runtime for all projects: samples: 25, 4 threads max: 14496 average: 13705.6 median:
//   * 13908
//   *
//   * @param project the project for which the experiments should be listed
//   * @return list with all experiments registered in this openBIS instance
//   */
//  public List<Experiment> getExperimentsForProject2(Project project);
//
//  /**
//   * Function to list all Experiments for a specific project which are registered in the openBIS
//   * instance.
//   *
//   * @param projectCode the project code for which the experiments should be listed
//   * @return list with all experiments registered in this openBIS instance
//   */
//  public List<Experiment> getExperimentsForProject2(String projectCode);
//
//  /**
//   * Function to list all Experiments for a specific project which are registered in the openBIS
//   * instance.
//   *
//   * samples: 25, 4 threads max: 15627 average: 14582.08 median: 15031
//   *
//   * @param project the project for which the experiments should be listed
//   * @return list with all experiments registered in this openBIS instance
//   */
//  public List<Experiment> getExperimentsForProject3(Project project);
//
//  /**
//   * Function to list all Experiments for a specific project which are registered in the openBIS
//   * instance.
//   *
//   * @param projectCode the project code for which the experiments should be listed
//   * @return list with all experiments registered in this openBIS instance
//   */
//  public List<Experiment> getExperimentsForProject3(String projectCode);
//

  /**
   * Function to list all Experiments for a specific project which are registered in the openBIS
   * instance.
   *
   * @param projectIdentifier project identifer as defined by openbis, for which the experiments
   * should be listed
   * @return list with all experiments registered in this openBIS instance
   */
  public List<Experiment> getExperimentsForProject(String projectIdentifier);


  /**
   * returns a list of all Experiments connected to a Project code in openBIS
   *
   * @param projectCode code of the given openBIS project
   * @return list of all experiments of the given project
   */
  public List<Experiment> getExperimentsOfProjectByCode(String projectCode);

  /**
   * Function to get all experiments for a given space and the information to which project the
   * corresponding experiment belongs to
   *
   * @param spaceIdentifier identifier of a openBIS space
   * @return map containing all projects (keys) and lists with all connected experiments (values)
   */
  public Map<String, List<Experiment>> getProjectExperimentMapping(String spaceIdentifier);

  /**
   * Function to retrieve all experiments of a given space
   *
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all experiments connected to this space
   */
  public List<Experiment> getExperimentsOfSpace(String spaceIdentifier);

  /**
   * Function to retrieve all experiments of a specific given type
   *
   * @param type identifier of the openBIS experiment type
   * @return list with all experiments of this given type
   */
  public List<Experiment> getExperimentsOfType(String type);

  /**
   * Function to retrieve all samples of a specific given type
   *
   * @param type identifier of the openBIS sample type
   * @return list with all samples of this given type
   */
  public List<Sample> getSamplesOfType(String type);

  /**
   * Function to retrieve all projects of a given space from openBIS.
   *
   * @param space identifier of the openBIS space
   * @return a list with all projects objects for the given space
   */
  public List<Project> getProjectsOfSpace(String space);

  /**
   * Returns Space names a given user should be able to see
   *
   * @param userID Username found in openBIS
   * @return List of space names with projects this user has access to
   */
  public List<String> getUserSpaces(String userID);

  /**
   * Returns wether a user is instance admin in openBIS
   *
   * @return true, if user is instance admin, false otherwise
   */
  public boolean isUserAdmin(String userID);

  /**
   * Function to retrieve a project from openBIS by the identifier of the project.
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return project with the given id
   */
  public Project getProjectByIdentifier(String projectIdentifier);

  /**
   * Function to retrieve a project from openBIS by the code of the project.
   *
   * @param projectCode code of the openBIS project
   * @return project with the given code
   */
  public Project getProjectByCode(String projectCode);

  /**
   * Function to retrieve a experiment from openBIS by the code of the experiment.
   *
   * @param experimentCode code of the openBIS experiment
   * @return experiment with the given code
   */
  public Experiment getExperimentByCode(String experimentCode);

  /**
   * Function to retrieve a experiment from openBIS by the code of the experiment.
   *
   * @param experimentId id of the openBIS experiment
   * @return experiment with the given code
   */
  public Experiment getExperimentById(String experimentId);

//  /**
//   * Function to list all Experiments for a specific project which are registered in the openBIS
//   * instance. runtime for all projects: samples: 25, 4 threads max: 14496 average: 13705.6 median:
//   * 13908
//   *
//   * @param expIdentifer the experiment identifier for which the experiments should be listed
//   * @return list with all experiments registered in this openBIS instance
//   */
//  public List<Experiment> getExperimentById2(String expIdentifer);

  /**
   * Function to retrieve the project of an experiment from openBIS
   *
   * @param experimentIdentifier identifier of the openBIS experiment
   * @return project connected to the given experiment
   */
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier);

  /**
   * Function to list all datasets of a specific sample (watch out there are different dataset
   * classes)
   *
   * @param sampleIdentifier identifier of the openBIS sample
   * @return list with all datasets of the given sample
   */
  public List<DataSet> getDataSetsOfSampleByIdentifier(
      String sampleIdentifier);

  /**
   * Function to list all datasets of a specific sample (watch out there are different dataset
   * classes)
   *
   * @param sampleCode code or identifier of the openBIS sample
   * @return list with all datasets of the given sample
   */
  public List<DataSet> getDataSetsOfSample(
      String sampleCode);

  /**
   * Function to list all datasets of a specific experiment (watch out there are different dataset
   * classes)
   *
   * @param experimentPermID permId of the openBIS experiment
   * @return list with all datasets of the given experiment
   */
  public List<DataSet> getDataSetsOfExperiment(
      String experimentPermID);

  /**
   * Returns all datasets of a given experiment. The new version should run smoother
   *
   * @param experimentIdentifier identifier or code of the openbis experiment
   * @return list of all datasets of the given experiment
   */
  public List<DataSet> getDataSetsOfExperimentByIdentifier(
      String experimentIdentifier);

  /**
   * Function to list all datasets of a specific openBIS space
   *
   * @param spaceIdentifier identifier of the openBIS space
   * @return list with all datasets of the given space
   */
  public List<DataSet> getDataSetsOfSpaceByIdentifier(
      String spaceIdentifier);

  /**
   * Function to list all datasets of a specific openBIS project
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<DataSet> getDataSetsOfProjectByIdentifier(
      String projectIdentifier);

//  /**
//   * Function to list all datasets of a specific openBIS project
//   *
//   * @param projectIdentifier identifier of the openBIS project
//   * @return list with all datasets of the given project
//   */
//  public List<DataSet> getDataSetsOfProjectByIdentifierWithSearchCriteria(String projectIdentifier);
//
//  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getClientDatasetsOfProjectByIdentifierWithSearchCriteria(
//      String projectIdentifier);
//
//  /**
//   * Function to list all datasets of a specific openBIS experiment
//   *
//   * @param experimentCode code of the openBIS experiment
//   * @return list with all datasets of the given experiment
//   */
//  public List<DataSet> getDataSetsOfExperimentByCodeWithSearchCriteria(String experimentCode);
//
//  public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getClientDataSetsOfExperimentByCodeWithSearchCriteria(
//      String experimentCode);

  /**
   * Function to list all datasets of a specific openBIS project
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all datasets of the given project
   */
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier);

/**
 //* Function to list all datasets of a specific openBIS project
 //*
 //* @param projectIdentifier identifier of the openBIS project
 //* @return list with all datasets of the given project
 //*/
//public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetsOfProjects2(List<Project> projectIdentifier);

  /**
   * Function to list all datasets of a specific type
   *
   * @param type identifier of the openBIS type
   * @return list with all datasets of the given type
   */
  public List<DataSet> getDataSetsByType(String type);

  /**
   * Function to list all attachments of a sample
   *
   * @param sampleIdentifier identifier of the openBIS sample
   * @return list with all attachments connected to the given sample
   */
  public List<Attachment> listAttachmentsForSampleByIdentifier(String sampleIdentifier);

  /**
   * Function to list all attachments of a project
   *
   * @param projectIdentifier identifier of the openBIS project
   * @return list with all attachments connected to the given project
   */
  public List<Attachment> listAttachmentsForProjectByIdentifier(String projectIdentifier);

//  /**
//   * Function to add an attachment to a existing project in openBIS by calling the corresponding
//   * ingestion service of openBIS. TODO specify which parameters have to be there may not work yet!
//   *
//   * @param parameter map with needed information for registration process by ingestion service
//   */
//  public void addAttachmentToProject(Map<String, Object> parameter);

//  /**
//   * Queries an aggregation service for openBIS data
//   *
//   * @param name the name of the aggregation service, as specified in the config file of the openBIS
//   *        instance
//   * @param parameters a map of parameters
//   * @return a QueryTableModel object containing the aggregated information
//   */
//  public QueryTableModel getAggregationService(String name, Map<String, Object> parameters);

  /**
   * Returns all users of a Space.
   *
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  public Set<String> getSpaceMembers(String spaceCode);

//  /**
//   * Function to retrieve all properties which have been assigned to a specific entity type
//   *
//   * @param entity_type entitiy type
//   * @return list of properties which are assigned to the entity type
//   */
//  public List<PropertyType> listPropertiesForType(EntityType entity_type);

  /**
   * Function to list the vocabulary terms for a given property which has been added to openBIS. The
   * property has to be a Controlled Vocabulary Property.
   *
   * @param property the property type
   * @return list of the vocabulary terms of the given property
   */
  public List<String> listVocabularyTermsForProperty(PropertyType property);

  /**
   * Function to get the label of a CV item for some property
   *
   * @param propertyType the property type
   * @param propertyValue the property value
   * @return Label of CV item
   */
  public String getCVLabelForProperty(PropertyType propertyType, String propertyValue);

  /**
   * Function to get a SampleType object of a sample type
   *
   * @param sampleType the sample type as string
   * @return the SampleType object of the corresponding sample type
   */
  public SampleType getSampleTypeByString(String sampleType);

  /**
   * Function to retrieve all samples of a specific given type
   *
   * @return list with all samples of this given type
   */
  public Map<String, SampleType> getSampleTypes();

  /**
   * Function to get a ExperimentType object of a experiment type
   *
   * @param experimentType the experiment type as string
   * @return the ExperimentType object of the corresponding experiment type
   */
  public ExperimentType getExperimentTypeByString(
      String experimentType);

//  /**
//   * Function to get the labels of all property types of a specific instance type
//   *
//   * @param entityType the instance type
///   * @return map with types as keys and labels as values
//   */
//  public Map<String, String> getLabelsofProperties(EntityType entityType);

  /**
   * Function to trigger ingestion services registered in openBIS
   *
   * @param serviceName name of the ingestion service which should be triggered
   * @param parameters map with needed information for registration process
   * @return object name of the QueryTableModel which is returned by the aggregation service
   */
  public String triggerIngestionService(String serviceName, Map<String, Object> parameters);

//  // TODO specify parameters needed for ingestion service
//
//  /**
//   * Function to add children samples to a sample (parent) using the corresponding ingestition
//   * service
//   *
//   * @param parameters map with needed information for registration process
//   * @return object name of the QueryTableModel which is returned by the aggregation service
//   */
//  public String addParentChildConnection(Map<String, Object> parameters);
//
//  // TODO probably not needed anymore
//
//  /**
//   * Function to trigger the registration of new openBIS instances like projects, experiments and
//   * samples. (This function also used to trigger the barcode generation for samples.)
//   *
//   * @param params map with needed information for registration process
//   * @param service name of the service for the corresponding registration
//   * @param number_of_samples_offset offset to generate correct barcodes (depending on number of
//   * samples) by accounting for delay of registration process
//   * @return object name of the QueryTableModel which is returned by the aggregation service
//   */
//  public String addNewInstance(Map<String, Object> params, String service,
//      int number_of_samples_offset);

  /**
   * Function to create a QBiC barcode string for a sample based on the project ID. QBiC barcode
   * format: Q + project_ID + sample number + X + checksum
   *
   * TODO check if it works for all cases, check for null ?
   *
   * @param proj ID of the project
   * @return the QBiC barcode as string
   */
  public String generateBarcode(String proj, int number_of_samples_offset);

  /**
   * Function to transform openBIS entity type to human readable text. Performs String replacement
   * and does not query openBIS!
   *
   * @param entityCode the entity code as string
   * @return entity code as string in human readable text
   */
  public String openBIScodeToString(String entityCode);

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
  @Deprecated
  public URL getDataStoreDownloadURL(String dataSetCode, String openbisFilename)
      throws MalformedURLException;

  public URL getDataStoreDownloadURLLessGeneric(String dataSetCode, String openbisFilename)
      throws MalformedURLException;

  /**
   * Returns a Map that maps samples to a list of samples of their parent samples
   *
   * @param samples A list of openBIS samples
   * @return Map<Sample
               *       ,       List
               *                                                                                                                                                           <
               *                                                                                                                                                                                                                                                                                           Sample>> containing a
   * mapping between children and parents of samples
   */
  public Map<Sample, List<Sample>> getParentMap(List<Sample> samples);

  /**
   * Returns lines of a spreadsheet of humanly readable information of the samples in a project.
   * Only one requested layer of the data model is returned. Experimental factors are returned in
   * the properties xml format and should be parsed before the spreadsheet is presented to the
   * user.
   *
   * @param projectCode The 5 letter QBiC code of the project
   * @param sampleType The openBIS sampleType that should be included in the result
   */
  public List<String> getProjectTSV(String projectCode, String sampleType);

//  /**
//   * Function to retrieve parent samples of a sample
//   *
//   * @param sampleCode Code of the query sample
//   * @return List of parent samples
//   */
//  public List<Sample> getChildrenBySearchService(String sampleCode);
//
//
//  /**
//   * Function to retrieve parent samples of a sample
//   *
//   * @param sampleCode Code of the query sample
//   * @return List of parent samples
//   */
//  public List<Sample> getParentsBySearchService(String sampleCode);

  /**
   *
   * @param sample
   * @return
   */
  public List<Sample> getChildrenSamples(Sample sample);

  /**
   * Checks if a space object of a certain code exists in openBIS
   *
   * @param spaceCode the code of an openBIS space
   * @return true, if the space exists, false otherwise
   */
  public boolean spaceExists(String spaceCode);

  /**
   * Checks if a project object of a certain code exists under a given space in openBIS
   *
   * @param spaceCode the code of an openBIS space
   * @param projectCode the code of a project
   * @return true, if the project exists under this space, false otherwise
   */
  public boolean projectExists(String spaceCode, String projectCode);

  /**
   * Checks if an experiment of a certain code exists under a given space and project in openBIS
   *
   * @param spaceCode the code of an openBIS space
   * @param projectCode the code of a project in openBIS
   * @param experimentCode the code of an experiment
   * @return true, if the experiment exists under this project and space, false otherwise
   */
  public boolean expExists(String spaceCode, String projectCode, String experimentCode);

  /**
   * Checks if a sample of a given code exists in openBIS
   *
   * @param sampleCode the code of a sample
   * @return true, if the sample exists, false otherwise
   */
  public boolean sampleExists(String sampleCode);

  /**
   * Compute status of project by checking status of the contained experiments
   *
   * @param project the Project object
   * @return ratio of finished experiments in this project
   */
  public float computeProjectStatus(Project project);

  /**
   * Compute status of project by checking status of the contained experiments Note: There is no
   * check whether the given experiments really belong to one project. You have to enusre that
   * yourself
   *
   * @param experiments list of experiments of a project.
   * @return ratio of finished experiments in this project
   */
  public float computeProjectStatus(List<Experiment> experiments);

  /**
   * Returns a map of Labels (keys) and Codes (values) in a Vocabulary in openBIS
   *
   * @param vocabularyCode Code of the Vocabulary type
   * @return A map containing the labels as keys and codes as values in String format
   */
  public Map<String, String> getVocabCodesAndLabelsForVocab(String vocabularyCode);

  public Vocabulary getVocabulary(String vocabularyCode);

  /**
   * Returns a list of all Codes in a Vocabulary in openBIS. This is useful when labels don't exist
   * or are not needed.
   *
   * @param vocabularyCode Code of the Vocabulary type
   * @return A list containing the codes of the vocabulary type
   */
  public List<String> getVocabCodesForVocab(String vocabularyCode);

  /**
   * Returns a list of all Experiments of a certain user.
   *
   * @param userID ID of user
   * @return A list containing the codes of the vocabulary type
   */
  public List<Experiment> getExperimentsForUser(String userID);

//  /**
//   * Function to talk to ingestions services (python scripts) of this openBIS instance
//   *
//   * @param dss the name of the dss-instance (e.g. DSS1 for most cases)
//   * @param serviceName label of the ingestion service to call (this is defined in the ingestion
//   * service properties)
//   * @param params A Map of parameters to send to the ingestion service
//   */
//  public void ingest(String dss, String serviceName, Map<String, Object> params);

  /**
   * List all experiments for given list of projects
   *
   * @param projectList list of project identifiers
   * @return List of experiments
   */
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList);

  /**
   * List all datasets for given experiment identifiers
   *
   * @param experimentIdentifiers list of experiment identifiers
   * @return List of datasets
   */
  public List<DataSet> listDataSetsForExperiments(
      List<String> experimentIdentifiers);

  /**
   * List all samples for given project identifiers
   *
   * @param projectIdentifiers list of project identifiers
   * @return List of samples
   */
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers);

  /**
   * List all datasets for given sample identifiers
   *
   * @param sampleIdentifier list of sample identifiers
   * @return List of datasets
   */
  public List<DataSet> listDataSetsForSamples(
      List<String> sampleIdentifier);

  /**
   * Retrieve datastore download url of dataset
   *
   * @param datasetCode Code of dataset
   * @param datasetName File name of dataset
   * @return URL to datastore location
   */
  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException;

  /**
   * Retrieve inputstream for dataset
   *
   * @param datasetCode Code of dataset
   * @return input stream for dataset
   */
  public InputStream getDatasetStream(String datasetCode);

  /**
   * Retrieve inputstream for dataset in folder
   *
   * @param datasetCode Code of dataset
   * @param folder Folder of dataset
   * @return input stream of datasets
   */
  public InputStream getDatasetStream(String datasetCode, String folder);

//  /**
//   * returns file information for a given number of datasets. params should look something like
//   * this: {'codes': [datasetcode1,datasetcode2...]} returns a QueryTableModel with the following
//   * entries: DATA_SET_CODE equals datasetcode given in params RELATIVE_PATH normally in the form
//   * 'original/results/file.txt' FILE_NAME just contains the basename e.g. 'file.txt' SIZE_IN_BYTES
//   * java long value IS_DIRECTORY true or false LAST_MODIFIED ...
//   *
//   * @param params
//   * @param IllegalArgumentException
//   * @return
//   */
//  public QueryTableModel queryFileInformation(Map<String, List<String>> params)
//      throws IllegalArgumentException;
}
