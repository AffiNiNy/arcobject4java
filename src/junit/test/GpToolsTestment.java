package junit.test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.ESRILicenseInfo;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriProductCode;

public class GpToolsTestment {

//    @Before
//    public void init() throws AutomationException, IOException {
//    }
    
    @Test
    public void test() throws UnknownHostException, IOException {
        EngineInitializer.initializeEngine();
        
        ESRILicenseInfo licInfo = new ESRILicenseInfo();
        System.out.println( licInfo.getDefaultProduct() );
        
        System.out.println( licInfo.isLicensed(esriProductCode.esriProductCodeAdvanced) );
    }

}
