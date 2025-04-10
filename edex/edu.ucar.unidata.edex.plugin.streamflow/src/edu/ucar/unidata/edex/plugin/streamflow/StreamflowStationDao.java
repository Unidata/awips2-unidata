package edu.ucar.unidata.edex.plugin.streamflow;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataquery.db.QueryResult;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

import edu.ucar.unidata.common.dataplugin.streamflow.StreamflowStation;

public class StreamflowStationDao extends CoreDao {

    public StreamflowStationDao() {
        super(DaoConfig.forClass(StreamflowStation.class));
    }

    public StreamflowStation queryByStationId(String id) throws DataAccessLayerException {
        List<?> stations = queryBySingleCriteria("id", id);
        if (stations.isEmpty()) {
            return null;
        } else {
            return (StreamflowStation) stations.get(0);
        }
    }

    public List<String> getStationIDs() {
        String buf = "select id from " + daoClass.getName();
        QueryResult result = this.executeHQLQuery(buf);
        List<String> stationIds = new ArrayList<String>();
        for (int i = 0; i < result.getResultCount(); i++) {
            stationIds.add((String) result.getRowColumnValue(i, 0));
        }
        return stationIds;
    }
}
