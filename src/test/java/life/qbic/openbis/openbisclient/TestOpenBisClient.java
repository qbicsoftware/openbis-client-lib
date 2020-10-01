package life.qbic.openbis.openbisclient;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import life.qbic.openbis.openbisclient.helper.OpenBisClientHelper;

public class TestOpenBisClient {

  IOpenBisClient openbis;
  OpenBisClientHelper helper;

  

  @Before
  public void setUp() {
    openbis = new OpenBisClientMock();
  }


  @Test
  public void testRelativesOfSamplesFetched() {
    Sample orphan = new Sample();

    SampleFetchOptions options = helper.fetchSamplesCompletely();

    orphan.setFetchOptions(options);

    // no parents or children, but right fetch options, no exception thrown
    assert (orphan.getParents() == null);
    assert (orphan.getChildren() == null);

    Sample sample = new Sample();
    sample.setParents(Collections.singletonList(orphan));

    Sample descendant = new Sample();
    sample.setChildren(Collections.singletonList(descendant));

    // parents and children, but wrong fetch options, assert exceptions
    try {
      sample.getParents();
    } catch (NotFetchedException e) {
      assert (true);
    }

    try {
      sample.getChildren();
    } catch (NotFetchedException e) {
      assert (true);
    }
    // now they should be fetched
    sample.setFetchOptions(options);
    assertEquals(1, sample.getChildren().size());
    assertEquals(1, sample.getParents().size());
  }

  @Test
  public void testFetchProperties() throws Exception {
    ExperimentType eType = new ExperimentType();
    List<PropertyAssignment> assignments = new ArrayList<>();

    PropertyAssignment pa1 = new PropertyAssignment();

    PropertyAssignmentFetchOptions paFo = new PropertyAssignmentFetchOptions();
    paFo.withPropertyType();
    pa1.setFetchOptions(paFo);

    PropertyType pt1 = new PropertyType();
    pt1.setCode("Test_Property1");
    pt1.setDataType(DataType.VARCHAR);
    pa1.setPropertyType(pt1);

    PropertyAssignment pa2 = new PropertyAssignment();

    pa2.setFetchOptions(paFo);
    PropertyType pt2 = new PropertyType();
    pt2.setCode("Test_Property2");
    pt2.setDataType(DataType.BOOLEAN);
    pa2.setPropertyType(pt2);

    assignments.add(pa1);
    assignments.add(pa2);
    eType.setPropertyAssignments(assignments);
    ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
    fo.withPropertyAssignments();
    eType.setFetchOptions(fo);

    List<PropertyType> propTypes = openbis.getPropertiesOfEntityType(eType);
    assertEquals(2, propTypes.size());
    for (PropertyType p : propTypes) {
      switch (p.getDataType()) {
        case BOOLEAN:
          assertEquals("Test_Property2", p.getCode());
          break;
        case VARCHAR:
          assertEquals("Test_Property1", p.getCode());
          break;
        default:
          throw new Exception("unexpected property type data type");
      }
    }
  }
}
