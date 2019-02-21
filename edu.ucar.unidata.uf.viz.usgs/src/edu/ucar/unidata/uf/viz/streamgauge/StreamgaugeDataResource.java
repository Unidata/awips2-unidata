package edu.ucar.unidata.uf.viz.streamgauge;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.raytheon.uf.viz.core.rsc.interrogation.StringInterrogationKey;

import edu.ucar.unidata.common.dataplugin.usgs.StreamflowRecord;

/**
 * Interface for USGS Data providing {@link AbstractVizResource}
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

public interface StreamgaugeDataResource extends Interrogatable {

    public static final InterrogationKey<StreamflowRecord[]> STREAMGAUGE_RECORDS_INTERROGATE_KEY = new StringInterrogationKey<>(
            "StreamgaugeRecords", StreamflowRecord[].class);

    public static final InterrogationKey<StreamflowRecord> CLOSEST_STREAMGAUGE_RECORD_INTERROGATE_KEY = new StringInterrogationKey<>(
            "Closest_StreamgaugeRecord", StreamflowRecord.class);

}
