package edu.ucar.unidata.edex.plugin.usgs;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.edex.pointdata.PointDataPluginDao;
import edu.ucar.unidata.common.dataplugin.usgs.StreamflowRecord;


public class StreamflowDAO extends PointDataPluginDao<StreamflowRecord> {

	public StreamflowDAO(String pluginName) throws PluginException {
		super(pluginName);
	}

	@Override
	public String[] getKeysRequiredForFileName() {
		return null;
	}

	@Override
	public StreamflowRecord newObject() {
		return new StreamflowRecord();
	}

	@Override
	public String getPointDataFileName(StreamflowRecord p) {
		return null;
	}
	
}