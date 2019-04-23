package edu.ucar.unidata.edex.plugin.streamflow;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.pointdata.PointDataContainer;
import com.raytheon.uf.common.pointdata.PointDataView;
import com.raytheon.uf.common.pointdata.spatial.SurfaceObsLocation;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.ucar.unidata.common.dataplugin.streamflow.StreamflowRecord;
import edu.ucar.unidata.common.dataplugin.streamflow.StreamflowStation;

public class StreamflowDecoder {
	
    GeometryFactory geomFact = new GeometryFactory();
    
    private IUFStatusHandler logger = UFStatus.getHandler(StreamflowDecoder.class);
    
    private StreamflowStationDao streamflowStationDao = new StreamflowStationDao();
    
    private final StreamflowDao dao;

    public StreamflowDecoder() throws PluginException {
    	this("streamflow");
    }
    
    public StreamflowDecoder(String pluginName) throws PluginException {
    	dao = new StreamflowDao(pluginName);
    }
    
    public PluginDataObject[] decode(byte[] data) throws Exception {
        logger.info("Starting USGS Streamflow Decoder");

        ArrayList<StreamflowRecord> list = new ArrayList<StreamflowRecord>();

        String input = new String(data);
        String[] lines = input.split("\n");
		String[] filterStrings = {"Rat","Ssn","Ice","Bkw","Eqp","Dis","***"};

        for(String line : lines) {
        	
        	if (line.trim().startsWith("#")) {
                continue;
        	} else if (line.trim().startsWith("USGS") && ! lineContains(line, filterStrings)) {
        		
        		String[] values = line.split("\t");
        		String stationID = values[1];
        		StreamflowStation station = getStationByID(stationID);
        		SurfaceObsLocation location = new SurfaceObsLocation();
        		
        		if (station != null && values.length > 6) {
            		
        			location.setElevation(Math.round(station.getElevation()));
        			location.setGeometry(station.getGeometry());
        			location.setStationId(stationID);
        			
            		String dateString = values[2];
            		String timeZone = values[3];
        			String cfs = values[4];
            		String status = values[5];
            		String height = values[6];
            		
            		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            		dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            		Date date = dateFormat.parse(dateString);
            		
            		if (! cfs.isEmpty() ) {
            			//StreamflowRecord record = new StreamflowRecord();
            			StreamflowRecord record = (StreamflowRecord) dao.newObject();
            			record.setName(station.getStationName());
                		record.setCfs(Float.parseFloat(cfs));
                		record.setHeight(Float.parseFloat(height));
                		record.setStatus(status);
                		record.setDataTime(new DataTime(date));
                		record.setLocation(location);
                		Map<File, PointDataContainer> containerMap = new HashMap<>();
                        File file = dao.getFullFilePath(record);
                        PointDataContainer container = containerMap.get(file);
                        if (container == null) {
                            container = PointDataContainer.build(dao.getPointDataDescription(null));
                            containerMap.put(file, container);
                        }
                        PointDataView view = container.append();
                        record.setPointDataView(view);
                		list.add(record);
            		}
        		}
            }
        }
        
        logger.info("Finished USGS Streamflow Decoder");
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
                throw new IOException("No station found for stationid = " + stationid);
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
