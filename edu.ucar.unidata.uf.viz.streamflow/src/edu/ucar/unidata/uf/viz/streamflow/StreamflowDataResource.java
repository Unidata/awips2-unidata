package edu.ucar.unidata.uf.viz.streamflow;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.raytheon.uf.viz.core.rsc.interrogation.StringInterrogationKey;

import edu.ucar.unidata.common.dataplugin.streamflow.StreamflowRecord;

/**
 * Interface for USGS Streamflow Data providing {@link AbstractVizResource}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2019            mjames      Initial creation
 * 
 * </pre>
 * 
 * @author mjames
 * @version 1.0
 */

public interface StreamflowDataResource extends Interrogatable {

    public static final InterrogationKey<StreamflowRecord[]> STREAMFLOW_RECORDS_INTERROGATE_KEY = new StringInterrogationKey<>(
            "StreamflowRecords", StreamflowRecord[].class);

    public static final InterrogationKey<StreamflowRecord> CLOSEST_STREAMFLOW_RECORD_INTERROGATE_KEY = new StringInterrogationKey<>(
            "Closest_StreamflowRecord", StreamflowRecord.class);

}
