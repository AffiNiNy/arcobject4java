package ao.shapefile.create;

import java.io.File;
import java.io.IOException;

import com.esri.arcgis.datasourcesfile.ShapefileWorkspaceFactory;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.GeometryDef;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geometry.IGeographicCoordinateSystem;
import com.esri.arcgis.geometry.SpatialReferenceEnvironment;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.geometry.esriSRGeoCSType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;

import ao.ArcUtils.ArcUtils;

public class CreateToShapefile {

    public CreateToShapefile() {}
    
    public static void main(String[] args) throws AutomationException, IOException {
        ArcUtils.bootArcEnvironment();
        AoInitialize aoInit = new AoInitialize();
        ArcUtils.initLicense(aoInit);
        
        // Data output setup
        String divKitHome = System.getenv("AGSDEVKITJAVA");
        String outFolder  = divKitHome + File.separator + "Test_Output Shapefile";
        String shpOutName = "MyPOI.shp";
        
        File shpOutDir = new File(outFolder);
        if ( !shpOutDir.exists() ) shpOutDir.mkdir();
        
        File outShpFile = new File(shpOutDir, shpOutName);
        if (outShpFile.exists()) {
            System.out.println("Output datafile already exists: " + outShpFile.getAbsolutePath());
            System.out.println("Delete it (plus .shx and .dbf files) and rerun");
            System.exit(-1);
        }
        
        System.out.println("--------------");
        
    }
    
    @SuppressWarnings("unused")
    private FeatureClass createPointShapefile(String shapefilePath, String shapefileName) throws IOException {
        // Get a feature workspace from the specified shapefile location
        ShapefileWorkspaceFactory shapefileWorkspaceFactory = new ShapefileWorkspaceFactory();
        Workspace workspace = (Workspace) shapefileWorkspaceFactory.openFromFile(shapefilePath, 0);
        
        // Create a GeometryDef object to hold geometry information
        GeometryDef geometryDef = new GeometryDef();
        geometryDef.setGeometryType(esriGeometryType.esriGeometryPoint);
        geometryDef.setHasM(false);
        geometryDef.setHasZ(false);

        // Create spatial reference information, and add it to the geometry definition
        IGeographicCoordinateSystem geographicCoordinateSystem = new SpatialReferenceEnvironment()
                .createGeographicCoordinateSystem(esriSRGeoCSType.esriSRGeoCS_WGS1984);
        geometryDef.setSpatialReferenceByRef(geographicCoordinateSystem);
        
        // Not finished!!!!!
        return null;
    }
    
}
