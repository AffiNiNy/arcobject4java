package ao.shapefile.gp;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.Cursor;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.Fields;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CopyFeatures;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;

import ao.ArcUtils.ArcUtils;

/**
 * 东莞市政 POI 图层. 对相同道路名称的 POI 组做邻近分析.
 * @author YunquNet_233
 *
 */
public class GeoProcess_Near_Q1 {

    public static void main(String[] args) {
        ArcUtils.bootArcEnvironment();
        
        // GDB Path
        String inFGDB = "Z:\\PROJECT_DATA\\10_DONGGUAN\\DG_GOV_ADDR_NP.gdb";
        // FeatureClass name
        String fcName = "DongChengStreet_GOVPOI";
        
        try {
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
            
            // Open POI by table
            Table poiTable = new Table(workspace.openTable(fcName));
            
            String roadFieldName = "所属街路巷代码";
            int roadIdx = poiTable.findField(roadFieldName);
            String clause = "\"%s\" = '%s' ";
            
            // Iterate unique roadname r1. 
            QueryFilter queryFilter = new QueryFilter();
            queryFilter.setPrefixClause("DISTINCT");
            queryFilter.setSubFields(roadFieldName);
            queryFilter.setWhereClause(roadFieldName + " IS NOT NULL ");
            
            ICursor iCursor = poiTable.ITable_search(queryFilter, true);
            IRow nextRow = iCursor.nextRow();
            String roadName = nextRow.getValue(9).toString();
            System.out.println("The first road name :" + roadName);
            
            // Make specific feature layer based on r1
            MakeFeatureLayer roadFeatureLayer = null;
            roadFeatureLayer = new MakeFeatureLayer(inFGDB + File.separator + fcName, "oneRoadLayer");
            // -- Feature layer set where clause
            roadFeatureLayer.setWhereClause( String.format(clause, roadFieldName, roadName) );
            
            // GeoProcessor to execute 
            GeoProcessor gpor = new GeoProcessor();
            gpor.execute(roadFeatureLayer, null);
            
            // -- Feature layer gets out layer
//            CopyFeatures cf = new CopyFeatures(roadFeatureLayer.getOutLayer(), inFGDB + File.separator + "MakeFeatureLayer_Output");
//            IGeoProcessorResult result = gpor.execute(cf, null);
//            System.out.println( result.getMessage(2) );
            
            FeatureClass featureClass = new FeatureClass( "oneRoadLayer" ); // Exception in thread "main" java.lang.IllegalArgumentException: Argument is not a remote object: oneRoadLayer
            System.out.println( featureClass.featureCount(null) );
            
            // Check FeatureLayer.getOutLayer's fields whether contains NEAR_* fields or not..
            
            
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ArcUtils.release();
        }
        
        System.out.println("---- Application exit ----");
    }

}
