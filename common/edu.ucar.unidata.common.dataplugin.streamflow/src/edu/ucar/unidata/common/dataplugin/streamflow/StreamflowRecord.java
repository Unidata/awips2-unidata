package edu.ucar.unidata.common.dataplugin.streamflow;

import javax.measure.quantity.Length;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import si.uom.NonSI;
import si.uom.SI;
import systems.uom.common.USCustomary;
import javax.measure.Unit;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Index;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.dataplugin.persist.PersistablePluginDataObject;
import com.raytheon.uf.common.geospatial.ISpatialEnabled;
import com.raytheon.uf.common.pointdata.IPointData;
import com.raytheon.uf.common.pointdata.PointDataView;
import com.raytheon.uf.common.pointdata.spatial.SurfaceObsLocation;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import org.locationtech.jts.geom.Geometry;

/**
 * 
 * USGS Streamflow Record
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 19, 2019             mjames@ucar  Initial creation
 * 
 * @author mjames
 */

@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "streamflowseq")
@Table(name = StreamflowRecord.PLUGIN_NAME, uniqueConstraints = { @UniqueConstraint( name = "uk_streamflow_datauri_fields", columnNames = { "dataURI" }) })
/*
 * Both refTime and forecastTime are included in the refTimeIndex since
 * forecastTime is unlikely to be used.
 */
@org.hibernate.annotations.Table( appliesTo = StreamflowRecord.PLUGIN_NAME, indexes = { @Index( name = "sf_refTimeIndex", columnNames = { 
		"refTime", "forecastTime" })})
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class StreamflowRecord extends PersistablePluginDataObject implements 
ISpatialEnabled, IPointData {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "streamflow";
    
    public static final Unit<Length> HEIGHT_UNIT = USCustomary.FOOT;
    
    public static final Unit<VolumetricFlowRate> FLOW_UNIT = ((USCustomary.FOOT).multiply(USCustomary.FOOT).multiply(USCustomary.FOOT)).divide(SI.SECOND).asType(VolumetricFlowRate.class);
//    public static final Unit<VolumetricFlowRate> FLOW_UNIT = ((USCustomary.FOOT));
    @Embedded
    @DataURI(position = 1, embedded = true)
    @DynamicSerializeElement
    private SurfaceObsLocation location;
    
    @Column(name="name")
    @DynamicSerializeElement
    private String name;
    
    @Column(name="status")
    @DynamicSerializeElement
    private String status;
    
    @Column(name="cfs")
    @DynamicSerializeElement
    private Float cfs;

    @Column(name="height")
    @DynamicSerializeElement
    private Float height;

    @Embedded
    @DynamicSerializeElement
    private PointDataView pointDataView;
    
    public StreamflowRecord() {
    }
    
    public StreamflowRecord(String datauri) {
    	super(datauri);
    }
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public SurfaceObsLocation getSpatialObject() {
		return location;
	}
	
	public SurfaceObsLocation getLocation() {
        return location;
    }
    
	public void setLocation(SurfaceObsLocation location) {
        this.location = location;
    }
    
    public Geometry getGeometry() {
        return location.getGeometry();
    }
	
	public double getLatitude() {
        return location.getLatitude();
    }
    
    public double getLongitude() {
        return location.getLongitude();
    }
	
	public String getStationid() {
		return location.getStationId();
	}
	
	public Integer getElevation() {
        return location.getElevation();
    }
	
	public Boolean getLocationDefined() {
        return location.getLocationDefined();
    }

	@Override
	public void setPointDataView(PointDataView pointDataView) {
		this.pointDataView = pointDataView;
	}

	@Override
	public PointDataView getPointDataView() {
		return pointDataView;
	}

	@Override
    @Column
    @Access(AccessType.PROPERTY)
    public String getDataURI() {
        return super.getDataURI();
    }
	
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
}
