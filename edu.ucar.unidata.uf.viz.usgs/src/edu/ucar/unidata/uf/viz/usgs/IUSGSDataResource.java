package edu.ucar.unidata.uf.viz.usgs;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.raytheon.uf.viz.core.rsc.interrogation.StringInterrogationKey;

import edu.ucar.unidata.common.dataplugin.usgs.USGSRecord;

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

public interface IUSGSDataResource extends Interrogatable {

    public static final InterrogationKey<USGSRecord[]> USGS_RECORDS_INTERROGATE_KEY = new StringInterrogationKey<>(
            "USGSRecords", USGSRecord[].class);

    public static final InterrogationKey<USGSRecord> CLOSEST_USGS_RECORD_INTERROGATE_KEY = new StringInterrogationKey<>(
            "Closest_USGSRecord", USGSRecord.class);

}
