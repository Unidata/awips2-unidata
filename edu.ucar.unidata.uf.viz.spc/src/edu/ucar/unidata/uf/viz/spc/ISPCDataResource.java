package edu.ucar.unidata.uf.viz.spc;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.raytheon.uf.viz.core.rsc.interrogation.StringInterrogationKey;

import edu.ucar.unidata.common.dataplugin.spc.SPCRecord;

/**
 * Interface for SPC Data providing {@link AbstractVizResource}
 *
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2018            mjames@ucar Initial creation
 * 
 * @author mjames
 */

public interface ISPCDataResource extends Interrogatable {

    public static final InterrogationKey<SPCRecord[]> SPC_RECORDS_INTERROGATE_KEY = new StringInterrogationKey<>(
            "SPCRecords", SPCRecord[].class);

    public static final InterrogationKey<SPCRecord> CLOSEST_SPC_RECORD_INTERROGATE_KEY = new StringInterrogationKey<>(
            "Closest_SPCRecord", SPCRecord.class);

}
