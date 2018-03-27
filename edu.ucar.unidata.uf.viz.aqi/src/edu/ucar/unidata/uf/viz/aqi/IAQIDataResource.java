/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
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
