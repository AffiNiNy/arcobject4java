package ao.shapefile.gp;

import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Workspace;

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
        String fcName = "DG_GOV_ADDR_NP";
        
        try {
            
            FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
            Workspace workspace = new Workspace(factory.openFromFile(inFGDB, 0));
            
            ITable poiTable = workspace.openTable(fcName);
            
            // query filter
            QueryFilter queryFilter = new QueryFilter();
            queryFilter.setPrefixClause("DISTINCT");
            queryFilter.setSubFields("所属街路巷代码");
            queryFilter.setWhereClause("\"所属街路巷代码\" IS NOT NULL");
            
            ICursor table_search = poiTable.ITable_search(queryFilter, false);
            System.out.println( table_search );
            
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
