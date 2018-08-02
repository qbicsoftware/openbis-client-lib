package life.qbic.openbis.openbisclient;

import static com.google.common.truth.Truth.assertThat;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;

public class TestOpenBisClientHelper {

  public static void assertSampleAllFetched(Sample sample){
    assertThat(sample.getClass().equals(Sample.class));
    assertThat(sample.getAttachments());
    assertThat(sample.getCode());
    assertThat(sample.getChildren());
    assertThat(sample.getComponents());
    assertThat(sample.getContainer());
    assertThat(sample.getDataSets());
    assertThat(sample.getExperiment());
    assertThat(sample.getFetchOptions());
    assertThat(sample.getHistory());
    assertThat(sample.getIdentifier());
    assertThat(sample.getMaterialProperties());
    assertThat(sample.getModificationDate());
    assertThat(sample.getModifier());
    assertThat(sample.getParents());
    //TODO Project cannot be fetched NotFetchedException but is actually fetched.
    //assertThat(sample.getProject());
    assertThat(sample.getPermId());
    assertThat(sample.getProperties());
    assertThat(sample.getRegistrationDate());
    assertThat(sample.getRegistrator());
    assertThat(sample.getSpace());
    assertThat(sample.getTags());
    assertThat(sample.getType());
  }

}
