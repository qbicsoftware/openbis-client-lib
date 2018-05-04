package life.qbic.openbis.openbisclient;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sven1103 on 5/04/17.
 */
public class HelperMethods {

    /**
     * Returns a string containing a specific property of multiple samples, separated by commas
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
     * @param samples Set of openbis Samples
     * @param sampleTypeCode Code of the sample type by which the ancestors should be filtered
     * @return Filtered set of all ancestor samples of a specific type
     */
    protected static Set<Sample> fetchAncestorsOfType(ArrayList<Sample> samples, String sampleTypeCode) {
        Set<Sample> targets = new HashSet<Sample>();
        while (!samples.isEmpty()) {
            Set<Sample> store = new HashSet<Sample>();
            for (Sample sample : samples) {
                if (!sample.getSampleTypeCode().equals(sampleTypeCode)) {
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
     * @param samples List of openbis samples
     * @param taxTerms List of openbis vocabulary terms of the NCBI taxonomy vocabulary
     * @return String representation of the root source(s) of thse samples
     */
    protected static String fetchSource(List<Sample> samples, List<VocabularyTerm> taxTerms) {
        List<String> res = new ArrayList<String>();
        boolean isCellLine = false;
        Set<Sample> roots = new HashSet<Sample>();
        while (!samples.isEmpty()) {
            Set<Sample> store = new HashSet<Sample>();
            for (Sample sample : samples) {
                if (!sample.getSampleTypeCode().equals("Q_BIOLOGICAL_ENTITY")) {
                    store.addAll(sample.getParents());
                    if (sample.getSampleTypeCode().equals("Q_BIOLOGICAL_SAMPLE"))
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
                for (VocabularyTerm term : taxTerms) {
                    if (organism.equals(term.getCode()))
                        desc = term.getLabel();
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
}
