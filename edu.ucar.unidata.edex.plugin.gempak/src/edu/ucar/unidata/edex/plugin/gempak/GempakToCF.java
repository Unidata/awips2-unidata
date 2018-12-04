package edu.ucar.unidata.edex.plugin.gempak;

import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.CFGridWriter2;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

import java.util.List;
import java.util.ArrayList;

public class GempakToCF {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("You must supply an input and output file name");
            System.exit(1);
        }
        String input = args[0];
        String output = args[1];
        
        GridDataset gds = GridDataset.open(input);
        List<GridDatatype> grids = gds.getGrids();
        List<String> gridList = new ArrayList<String>(grids.size());
        
        grids.forEach(grid -> {
        	gridList.add(((GeoGrid) grid).getName());
        });
        
        int stride_time = 1;
        int horizStride = 1;
        NetcdfFileWriter ncwriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, output);
        CFGridWriter2.writeFile(gds, gridList, null, null, horizStride, null, null, stride_time, false, ncwriter);

    }
}
