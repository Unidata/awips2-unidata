package edu.ucar.unidata.common.dataplugin.usgs;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * USGS Streamflow Record
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 19, 2019             mjames@ucar  Initial creation
 * 
 * @author mjamess
 */

@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "streamflowseq")
//@Table(name = StreamflowRecord.PLUGIN_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "dataURI" }) })
@Table(name = StreamflowRecord.PLUGIN_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
        "station_id", "status", "cfs", "height", "refTime" }) })
@org.hibernate.annotations.Table(appliesTo = StreamflowRecord.PLUGIN_NAME, indexes = {
        @Index(name = "%TABLE%_cfsandheight_index", columnNames = {"cfs", "height"}) })
@DynamicSerialize
public class StreamflowRecord extends PluginDataObject {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "streamflow";

	@Column(name = "station_id")
    @XmlAttribute
    @DynamicSerializeElement
    private String stationId;
    
    @Column(name = "status")
    @DynamicSerializeElement
    private String status;
    
    @Column(name = "cfs")
    @DynamicSerializeElement
    private Float cfs;

    @Column(name = "height")
    @DynamicSerializeElement
    private Float height;

    @Column(name = "location", columnDefinition = "geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Geometry geometry;

    /**
     * Default Constructor
     */
    public StreamflowRecord() {
    }
    
	public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
	public String getStationID() {
		return stationId;
	}
	
    public void setStationID(String stationId) {
        this.stationId = stationId;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Float getCfs() {
        return cfs;
    }

    public void setCfs(Float cfs) {
        this.cfs = cfs;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }
    
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
}
