package ao.shapefile.gp;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.geoprocessing.tools.analysistools.Near;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;
import com.esri.arcgis.system.AoInitialize;

import ao.ArcUtils.ArcUtils;
import ao.ArcUtils.DemoData;

/**
 * Check ArcGIS Help Document about licensing information of the tools.
 * Near tool(Analysis - proximity) needs advance license.
 * @author YunquNet_233
 *
 */
public class GeoProcess_Near {

    public static void main(String[] args) {
        ArcUtils.bootArcEnvironment();
        AoInitialize aoInit = null;
        
        String inFGDB = DemoData.usaFGDB;
        
        try {
            aoInit = new AoInitialize();
            ArcUtils.initLicense(aoInit);
            
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
//            FeatureClass windClass = new FeatureClass(workspace.openFeatureClass("wind"));
            FeatureClass highwayClass = new FeatureClass(workspace.openFeatureClass("ushigh"));
            String windClassStr = inFGDB + File.separator + "wind";
            
            GeoProcessor gp = new GeoProcessor();
            MakeFeatureLayer inFeatureLayer = new MakeFeatureLayer(highwayClass, "high_lyr");
            gp.execute(inFeatureLayer, null);
            
            // 1
//            Near near = new Near();
//            near.setInFeatures(windClassStr);
//            String highwayClassStr = inFGDB + File.separator + "ushigh";
//            near.setNearFeatures(highwayClassStr);
            // 2 
            // Using FeatureLayer or path string to shapefile in GDB is all OK.
            Near near = new Near(windClassStr, "high_lyr");
            IGeoProcessorResult result = gp.execute(near, null);
            
            ArcUtils.printResult(result);
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ArcUtils.release();
        }
        
        
    }

}
