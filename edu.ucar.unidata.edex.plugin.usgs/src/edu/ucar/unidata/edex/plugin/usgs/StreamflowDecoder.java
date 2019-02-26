package edu.ucar.unidata.edex.plugin.usgs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.ucar.unidata.common.dataplugin.usgs.StreamflowRecord;
import edu.ucar.unidata.common.dataplugin.usgs.StreamflowStation;

public class StreamflowDecoder {
	
    GeometryFactory geomFact = new GeometryFactory();
    
    private StreamflowStationDao streamflowStationDao = new StreamflowStationDao();
    
    private IUFStatusHandler logger = UFStatus.getHandler(StreamflowDecoder.class);

    public PluginDataObject[] decode(byte[] data) throws Exception {
        logger.info("Starting USGS Streamgauge Decoder");

        ArrayList<StreamflowRecord> list = new ArrayList<StreamflowRecord>();

        String input = new String(data);
        String[] lines = input.split("\n");
		String[] filterStrings = {"Rat","Ssn","Ice","Bkw","Eqp","***"};

        for(String line : lines) {
        	
        	if (line.trim().startsWith("#")) {
                continue;
        	} else if (line.trim().startsWith("USGS") && ! lineContains(line, filterStrings)) {
        		
        		String[] values = line.split("\t");
        		StreamflowStation station = getStationByID(values[1]);
        		
        		if (station != null && values.length > 6) {
            		
        			String stationid = values[1];
            		String dateString = values[2];
            		String timeZone = values[3];
        			String cfs = values[4];
            		String status = values[5];
            		String height = values[6];
            		
            		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            		dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            		Date date = dateFormat.parse(dateString);
            		
            		if (! cfs.isEmpty() ) {
            			StreamflowRecord record = new StreamflowRecord();
            			record.setStationID(stationid);
                		record.setCfs(Float.parseFloat(cfs));
                		record.setHeight(Float.parseFloat(height));
                		record.setStatus(status);
                		record.setDataTime(new DataTime(date));
            			record.setElevation(station.getElevation());
            			record.setStationName(station.getStationName());
            			record.setGeometry(station.getGeometry());
            			                		
                		list.add(record);
            		}
        		}
            }
        }
        
        logger.info("Finished USGS Streamgauge Decoder");
        return (list.toArray(new PluginDataObject[list.size()]));
    }
    
    private static boolean lineContains(String inputStr, String[] items) {
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }
    
    /**
     * Retrieve the streamflow station from the dao for the ID given
     * 
     * @param stationid
     * @return
     */
    private StreamflowStation getStationByID(String stationid) {
        StreamflowStation station = null;
        try {
            station = streamflowStationDao.queryByStationId(stationid);
            if (station == null) {
                throw new IOException("No station found for station_id = " + stationid);
            }

        } catch (Exception e) {
            logger.handle(Priority.ERROR, "Unable to query for the streamflow station", e);
        }

        return station;
    }
    
    public StreamflowStationDao getStreamflowStationDao() {
        return streamflowStationDao;
    }

    public void setStreamflowStationDao(StreamflowStationDao streamflowStationDao) {
        this.streamflowStationDao = streamflowStationDao;
    }
}
