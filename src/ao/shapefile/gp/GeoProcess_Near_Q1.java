package ao.shapefile.gp;

import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabase.esriSchemaLock;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.tools.analysistools.Near;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.CalculateField;
import com.esri.arcgis.geoprocessing.tools.datamanagementtools.MakeFeatureLayer;
import com.esri.arcgis.interop.AutomationException;

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
        String inFGDB = "...\\DG_GOV_ADDR_NP.gdb";
        // FeatureClass name
        String fcName = "DongChengStreet_GOVPOI";
        
        String roadFieldName = "所属街路巷代码";
        String oneRoadLayer  = "oneRoadLayer";
        String clause = " \"%s\" = '%s' ";
        
        /* GeoProcess tools */
        GeoProcessor gp = null;
        Near near = new Near();
        MakeFeatureLayer makeFeatureLayer = new MakeFeatureLayer();
        CalculateField calField = new CalculateField();
        
        
        try {
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));

            /* new GeoProcessor and set property */
            gp = new GeoProcessor();
            gp.setOverwriteOutput(true);
            
            /* Open POI by FeatureClass */
            FeatureClass poiFC = new FeatureClass(workspace.openFeatureClass(fcName));
            makeFeatureLayer.setInFeatures(poiFC);
            makeFeatureLayer.setOutLayer(oneRoadLayer);
            
            /* Add fields for the first time */
            addFields(poiFC);
            
            /* Summarize road name and corresponding amount */
            QueryFilter queryFilter = new QueryFilter();
            queryFilter.setSubFields(roadFieldName + ", COUNT(*)");
            queryFilter.setWhereClause(roadFieldName + " IS NOT NULL ");
            queryFilter.setPostfixClause(" GROUP BY " + roadFieldName);
            
            ICursor iCursor = poiFC.ITable_search(queryFilter, true);
            IRow nextRow;
            String roadName;
            int roadCount;
            int timeCount = 0;
            
            while ((nextRow = iCursor.nextRow()) != null) {
                roadName = nextRow.getValue(0).toString();
                timeCount++;
                roadCount = (Integer) nextRow.getValue(1);

                if ((timeCount%500) == 0) System.out.println( "  -- "+timeCount+" handled.");
                
                /* Make one road feature layer */
                makeFeatureLayer.setWhereClause( String.format(clause, roadFieldName, roadName) );
                gp.execute(makeFeatureLayer, null);
                
                if (roadCount == 1) {
                    calField.setInTable(oneRoadLayer);
                    calField.setField("NEAR_FEATUREID");
                    calField.setExpression("\"NULL\"");
                    gp.execute(calField, null);
                    calField.setField("NEAR_DISTANCE");
                    calField.setExpression(-1);
                    gp.execute(calField, null);
                    continue;
                }

                /* Near  */
                near.setInFeatures(oneRoadLayer);
                near.setNearFeatures(oneRoadLayer);
                gp.execute(near, null);

                /* save values of NEAR_FID and NEAR_DIST */
                calField.setInTable(oneRoadLayer);
                calField.setField("NEAR_FEATUREID");
                calField.setExpression("[NEAR_FID]");
                gp.execute(calField, null);
                calField.setField("NEAR_DISTANCE");
                calField.setExpression("[NEAR_DIST]");
                gp.execute(calField, null);
                
            }
            
            System.out.println( "Finished near on each road group.." );
            
            /* Update the fields FOR CHECKING */
            queryFilter = new QueryFilter();
            queryFilter.setWhereClause(" \"NEAR_FEATUREID\" <> 'NULL' ");
            ICursor updateCursor = poiFC.update(queryFilter, false);
            IRow iRow;

            int guidIdx = poiFC.findField("GUID");
            int roadNameIdx = poiFC.findField(roadFieldName);
            int doornumIdx = poiFC.findField("门牌含小区名称");
            int nearFeatureIdIdx = poiFC.findField("NEAR_FEATUREID");
            int nearGUIDIdx = poiFC.findField("NEAR_GUID");
            int nearRoadnameIdx = poiFC.findField("NEAR_ROADNAME");
            int nearDoornumIdx = poiFC.findField("NEAR_DOORNUM");
            
            while ((iRow=updateCursor.nextRow()) != null) {
                // Get near feature's field info.
                IRow recordRow = poiFC.getRow(Integer.parseInt(iRow.getValue(nearFeatureIdIdx).toString()));
                String nearGUID = recordRow.getValue(guidIdx).toString();
                String nearRoadname = recordRow.getValue(roadNameIdx).toString();
                String nearDoornum = recordRow.getValue(doornumIdx).toString();
                // Update current row's fields.
                iRow.setValue(nearGUIDIdx, nearGUID);
                iRow.setValue(nearRoadnameIdx, nearRoadname);
                iRow.setValue(nearDoornumIdx, nearDoornum);
                updateCursor.updateRow(iRow);
            }
            
            System.out.println( "Finished updated the new added field..." );
            
        } catch (AutomationException e) {
            e.printStackTrace();
            System.out.println( ArcUtils.handleException(e) );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ArcUtils.release();
        }
        
        System.out.println("---- Application exit ----");
    }

    
    private static void addFields(FeatureClass featureClass) throws UnknownHostException, IOException {
        featureClass.changeSchemaLock(esriSchemaLock.esriExclusiveSchemaLock);

        Field nearFeatureID = new Field();
        nearFeatureID.setName("NEAR_FEATUREID");
        nearFeatureID.setType(esriFieldType.esriFieldTypeString);
        nearFeatureID.setLength(32);
        featureClass.addField(nearFeatureID);
        
        Field nearDisField = new Field();
        nearDisField.setName("NEAR_DISTANCE");
        nearDisField.setType(esriFieldType.esriFieldTypeDouble);
        featureClass.addField(nearDisField);
        
        Field nearGUIDField = new Field();
        nearGUIDField.setName("NEAR_GUID");
        nearGUIDField.setType(esriFieldType.esriFieldTypeString);
        nearGUIDField.setLength(12);
        featureClass.addField(nearGUIDField);
        
        Field nearRoadname = new Field();
        nearRoadname.setName("NEAR_ROADNAME");
        nearRoadname.setType(esriFieldType.esriFieldTypeString);
        nearRoadname.setLength(128);
        featureClass.addField(nearRoadname);
        
        Field nearDoornum = new Field();
        nearDoornum.setName("NEAR_DOORNUM");
        nearDoornum.setType(esriFieldType.esriFieldTypeString);
        nearDoornum.setLength(128);
        featureClass.addField(nearDoornum);
        
        featureClass.changeSchemaLock(esriSchemaLock.esriSharedSchemaLock);
        
    }
    
    

}
