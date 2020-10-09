package life.qbic.openbis.openbisclient;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import life.qbic.openbis.openbisclient.helper.OpenBisClientHelper;

public class OpenBisClientMock implements IOpenBisClient {

  @Override
  public boolean loggedin() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void logout() {
    // TODO Auto-generated method stub

  }

  @Override
  public void login() {
    // TODO Auto-generated method stub

  }

  @Override
  public String getSessionToken() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void ensureLoggedIn() {
    // TODO Auto-generated method stub

  }

  @Override
  public List<String> listSpaces() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Project> listProjects() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> listExperiments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> getSamplesofExperiment(String experimentIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> getSamplesofSpace(String spaceIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Sample getSampleByIdentifier(String sampleIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> getSamplesOfProject(String projIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> getSamplesWithParentsAndChildren(String sampCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfProjectByIdentifier(String projectIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject(Project project) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForProject(String projectIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfProjectByCode(String projectCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, List<Experiment>> getProjectExperimentMapping(String spaceIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfSpace(String spaceIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsOfType(String type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> getSamplesOfType(String type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Project> getProjectsOfSpace(String space) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getUserSpaces(String userID) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isUserAdmin(String userID) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Project getProjectByIdentifier(String projectIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Project getProjectByCode(String projectCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Experiment getExperimentByCode(String experimentCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Experiment getExperimentById(String experimentId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Project getProjectOfExperimentByIdentifier(String experimentIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfSampleByIdentifier(String sampleIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfSample(String sampleCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfExperiment(String experimentPermID) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfExperimentByIdentifier(String experimentIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfSpaceByIdentifier(String spaceIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfProjectByIdentifier(String projectIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsOfProjects(List<Project> projectIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> getDataSetsByType(String type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Attachment> listAttachmentsForSampleByIdentifier(String sampleIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Attachment> listAttachmentsForProjectByIdentifier(String projectIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> getSpaceMembers(String spaceCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> listVocabularyTermsForProperty(PropertyType property) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getCVLabelForProperty(PropertyType propertyType, String propertyValue) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SampleType getSampleTypeByString(String sampleType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, SampleType> getSampleTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExperimentType getExperimentTypeByString(String experimentType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String triggerIngestionService(String serviceName, Map<String, Object> parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String generateBarcode(String proj, int number_of_samples_offset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String openBIScodeToString(String entityCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getDataStoreDownloadURL(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getDataStoreDownloadURLLessGeneric(String dataSetCode, String openbisFilename)
      throws MalformedURLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<Sample, List<Sample>> getParentMap(List<Sample> samples) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getProjectTSV(String projectCode, String sampleType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> getChildrenSamples(Sample sample) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean spaceExists(String spaceCode) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean projectExists(String spaceCode, String projectCode) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean expExists(String spaceCode, String projectCode, String experimentCode) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean sampleExists(String sampleCode) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public float computeProjectStatus(Project project) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float computeProjectStatus(List<Experiment> experiments) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Map<String, String> getVocabCodesAndLabelsForVocab(String vocabularyCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Vocabulary getVocabulary(String vocabularyCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getVocabCodesForVocab(String vocabularyCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Experiment> getExperimentsForUser(String userID) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void ingest(String dss, String serviceName, Map<String, Object> params) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<Experiment> listExperimentsOfProjects(List<Project> projectList) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> listDataSetsForExperiments(List<String> experimentIdentifiers) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> listSamplesForProjects(List<String> projectIdentifiers) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DataSet> listDataSetsForSamples(List<String> sampleIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URL getUrlForDataset(String datasetCode, String datasetName) throws MalformedURLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getDatasetStream(String datasetCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getDatasetStream(String datasetCode, String folder) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Sample> searchSampleByCode(String sampleCode) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PropertyType> getPropertiesOfExperimentType(ExperimentType type) {
    return OpenBisClientHelper.getPropertiesOfEntityType(new ArrayList<>(Arrays.asList(type)));
  }

  @Override
  public List<PropertyType> getPropertiesOfSampleType(SampleType type) {
    return OpenBisClientHelper.getPropertiesOfEntityType(new ArrayList<>(Arrays.asList(type)));
  }

  @Override
  public List<PropertyType> getPropertiesOfDataSetType(DataSetType type) {
    return OpenBisClientHelper.getPropertiesOfEntityType(new ArrayList<>(Arrays.asList(type)));
  }

}
