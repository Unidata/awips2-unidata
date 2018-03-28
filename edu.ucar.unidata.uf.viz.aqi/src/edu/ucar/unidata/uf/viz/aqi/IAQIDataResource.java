package edu.ucar.unidata.uf.viz.aqi;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.raytheon.uf.viz.core.rsc.interrogation.StringInterrogationKey;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;

/**
 * Interface for AQI Data providing {@link AbstractVizResource}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 10, 2014            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public interface IAQIDataResource extends Interrogatable {

    public static final InterrogationKey<AQIRecord[]> AQI_RECORDS_INTERROGATE_KEY = new StringInterrogationKey<>(
            "AQIRecords", AQIRecord[].class);

    public static final InterrogationKey<AQIRecord> CLOSEST_AQI_RECORD_INTERROGATE_KEY = new StringInterrogationKey<>(
            "Closest_AQIRecord", AQIRecord.class);

}
