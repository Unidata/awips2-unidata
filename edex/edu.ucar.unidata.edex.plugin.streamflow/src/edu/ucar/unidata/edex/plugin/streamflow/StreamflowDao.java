package edu.ucar.unidata.edex.plugin.streamflow;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.edex.pointdata.PointDataPluginDao;

import edu.ucar.unidata.common.dataplugin.streamflow.StreamflowRecord;


public class StreamflowDao extends PointDataPluginDao<StreamflowRecord> {
	
	private String[] fileNameKeys = new String[] { "dataTime.refTime" };

	public StreamflowDao(String pluginName) throws PluginException {
		super(pluginName);
	}

	public void setFileNameKeys(String[] fileNameKeys) {
        this.fileNameKeys = fileNameKeys;
    }
	
	@Override
	public String[] getKeysRequiredForFileName() {
		return fileNameKeys;
	}

	@Override
	public StreamflowRecord newObject() {
		return new StreamflowRecord();
	}

	@Override
	public String getPointDataFileName(StreamflowRecord p) {
		return "streamflow.h5";
	}
	
}