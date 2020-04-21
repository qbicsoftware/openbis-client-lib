package life.qbic.openbis.openbisclient.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;

public class OpenBisClientHelper {

  public static SampleFetchOptions fetchSamplesCompletely() {
    SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
    sampleFetchOptions.withChildrenUsing(sampleFetchOptions);
    sampleFetchOptions.withExperiment();
    sampleFetchOptions.withAttachments();
    sampleFetchOptions.withComponents();
    sampleFetchOptions.withContainer();
    sampleFetchOptions.withDataSets();
    sampleFetchOptions.withHistory();
    sampleFetchOptions.withMaterialProperties();
    sampleFetchOptions.withModifier();
    //TODO Project could not be fetched
    sampleFetchOptions.withProperties();
    sampleFetchOptions.withRegistrator();
    sampleFetchOptions.withSpace();
    sampleFetchOptions.withTags();
    sampleFetchOptions.withType();
    sampleFetchOptions.withParents();

    return sampleFetchOptions;
  }

  public static ProjectFetchOptions fetchProjectsCompletely() {
    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
    projectFetchOptions.withAttachments();
    projectFetchOptions.withHistory();
    projectFetchOptions.withModifier();
    projectFetchOptions.withRegistrator();
    projectFetchOptions.withSpace();
    projectFetchOptions.withExperiments();
    projectFetchOptions.withLeader();
    //TODO Samples could not be fetched
    projectFetchOptions.withSpace();

    return projectFetchOptions;
  }

  public static ExperimentFetchOptions fetchExperimentsCompletely() {
    ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
    experimentFetchOptions.withAttachments();
    experimentFetchOptions.withHistory();
    experimentFetchOptions.withModifier();
    experimentFetchOptions.withRegistrator();
    experimentFetchOptions.withSamples();
    experimentFetchOptions.withDataSets();
    experimentFetchOptions.withMaterialProperties();
    experimentFetchOptions.withProjectUsing(fetchProjectsCompletely());
    experimentFetchOptions.withProperties();
    experimentFetchOptions.withTags();
    experimentFetchOptions.withType();

    return experimentFetchOptions;
  }

  public static DataSetFetchOptions fetchDataSetsCompletely() {
    DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
    dataSetFetchOptions.withProperties();
    dataSetFetchOptions.withChildren();
    dataSetFetchOptions.withComponents();
    dataSetFetchOptions.withContainers();
    dataSetFetchOptions.withDataStore();
    dataSetFetchOptions.withExperiment();
    dataSetFetchOptions.withHistory();
    dataSetFetchOptions.withLinkedData();
    dataSetFetchOptions.withMaterialProperties();
    dataSetFetchOptions.withModifier();
    dataSetFetchOptions.withParents();
    dataSetFetchOptions.withPhysicalData();
    dataSetFetchOptions.withRegistrator();
    dataSetFetchOptions.withSample();
    dataSetFetchOptions.withTags();
    dataSetFetchOptions.withType();

    return dataSetFetchOptions;
  }

  public static SampleTypeFetchOptions fetchSampleTypesCompletely() {
    SampleTypeFetchOptions sampleTypeFetchOption = new SampleTypeFetchOptions();
    sampleTypeFetchOption.withPropertyAssignments();

    return sampleTypeFetchOption;
  }

  public static ExperimentTypeFetchOptions fetchExperimentTypesCompletely() {
    ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
    experimentTypeFetchOptions.withPropertyAssignments();

    return experimentTypeFetchOptions;
  }

}
