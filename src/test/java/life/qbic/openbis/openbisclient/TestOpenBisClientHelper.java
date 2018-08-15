package life.qbic.openbis.openbisclient;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;

public class TestOpenBisClientHelper {

  public static void assertSampleCompletetlyFetched(Sample sample) throws NotFetchedException {
    sample.getAttachments();
    sample.getCode();
    sample.getChildren();
    sample.getComponents();
    sample.getContainer();
    sample.getDataSets();
    sample.getExperiment();
    sample.getFetchOptions();
    sample.getHistory();
    sample.getIdentifier();
    sample.getMaterialProperties();
    sample.getModificationDate();
    sample.getModifier();
    sample.getParents();
    //TODO Project could not be fetched
    //sample.getProject();
    sample.getPermId();
    sample.getProperties();
    sample.getRegistrationDate();
    sample.getRegistrator();
    sample.getSpace();
    sample.getTags();
    sample.getType();
  }

  public static void assertProjectCompletelyFetched(Project project) throws NotFetchedException {
    project.getAttachments();
    project.getCode();
    project.getFetchOptions();
    project.getHistory();
    project.getIdentifier();
    project.getModificationDate();
    project.getModifier();
    project.getPermId();
    project.getRegistrationDate();
    project.getRegistrator();
    project.getSpace();
    project.getDescription();
    project.getExperiments();
    project.getLeader();
    //TODO Samples could not be fetched
    //project.getSamples();
    project.getSpace();
  }

  public static void assertExperimentCompletetlyFetched(Experiment experiment)
      throws NotFetchedException {
    experiment.getAttachments();
    experiment.getCode();
    experiment.getDataSets();
    experiment.getFetchOptions();
    experiment.getHistory();
    experiment.getIdentifier();
    experiment.getMaterialProperties();
    experiment.getModificationDate();
    experiment.getModifier();
    experiment.getProject();
    experiment.getPermId();
    experiment.getProperties();
    experiment.getRegistrationDate();
    experiment.getRegistrator();
    experiment.getTags();
    experiment.getType();
    experiment.getSamples();
  }
}
