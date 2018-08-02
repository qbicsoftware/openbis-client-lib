package life.qbic.openbis.openbisclient.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;

public class OpenBisClientHelper {

  public static SampleFetchOptions fetchSamplesCompletely(){
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
    //sampleFetchOptions.withProject();
    sampleFetchOptions.withProperties();
    sampleFetchOptions.withRegistrator();
    sampleFetchOptions.withSpace();
    sampleFetchOptions.withTags();
    sampleFetchOptions.withType();
    sampleFetchOptions.withParents();

    return sampleFetchOptions;
  }

  public static ProjectFetchOptions fetchProjectsCompletely(){
    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
    projectFetchOptions.withAttachments();
    projectFetchOptions.withHistory();
    projectFetchOptions.withModifier();
    projectFetchOptions.withRegistrator();
    projectFetchOptions.withSpace();
    projectFetchOptions.withExperiments();
    projectFetchOptions.withLeader();
    //TODO Samples could not be fetched
    //projectFetchOptions.withSamples();
    projectFetchOptions.withSpace();

    return projectFetchOptions;
  }

  public static ExperimentFetchOptions fetchExperimentsCompletely(){
    ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
    experimentFetchOptions.withAttachments();
    experimentFetchOptions.withHistory();
    experimentFetchOptions.withModifier();
    experimentFetchOptions.withRegistrator();
    experimentFetchOptions.withSamples();
    experimentFetchOptions.withDataSets();
    experimentFetchOptions.withMaterialProperties();
    experimentFetchOptions.withProject();
    experimentFetchOptions.withProperties();
    experimentFetchOptions.withSamples();
    experimentFetchOptions.withTags();
    experimentFetchOptions.withType();

    return experimentFetchOptions;
  }

}
