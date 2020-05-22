package ao.ArcUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;

import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;

public class ArcUtils {

    private static AoInitialize aoInitialize;
    
    /**
     * Bootstrap Arcobject jar file  AND  initialize Engine  AND  AoInitialize.
     * @throws IOException
     * @throws UnknownHostException 
     */
    public static void bootArcEnvironment() {
        // Get the ArcGIS Engine runtime, if it is available
        String arcObjectsHome = System.getenv("AGSENGINEJAVA");

        // If the ArcGIS Engine runtime is not available, then we can try ArcGIS Desktop
        // runtime
        if (arcObjectsHome == null) {
            arcObjectsHome = System.getenv("AGSDESKTOPJAVA");
        }

        // If no runtime is available, exit application gracefully
        if (arcObjectsHome == null) {
            if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
                System.err.println("You must have ArcGIS Engine Runtime or ArcGIS Desktop "
                        + "installed in order to execute this sample.");
                System.err.println("Install one of the products above, then re-run this sample.");
                System.err.println("Exiting execution of this sample...");
                System.exit(0);
            } else {
                System.err.println("You must have ArcGIS Engine Runtime installed " + "in order to execute this sample.");
                System.err.println("Install the product above, then re-run this sample.");
                System.err.println("Exiting execution of this sample...");
                System.exit(0);
            }
        }

        // Obtain the relative path to the arcobjects.jar file
        String jarPath = arcObjectsHome + "java" + File.separator + "lib" + File.separator + "arcobjects.jar";

        // Create a new file
        File jarFile = new File(jarPath);

        // Test for file existence
        if (!jarFile.exists()) {
            System.err.println("The arcobjects.jar was not found in the following location: " + jarFile.getParent());
            System.err.println("Verify that arcobjects.jar can be located in the specified folder.");
            System.err.println("If not present, try uninstalling your ArcGIS software and reinstalling it.");
            System.err.println("Exiting execution of this sample...");
            System.exit(0);
        }

        // Helps load classes and resources from a search path of URLs
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { jarFile.toURI().toURL() });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.err.println("Could not add arcobjects.jar to system classloader");
            System.err.println("Exiting execution of this sample...");
            System.exit(0);
        }
        
        System.out.println( "---- Bootstrapped ArcObject jar ----");

        //Initialize engine console application
        EngineInitializer.initializeEngine();
        
        try {
            aoInitialize = new AoInitialize();
            initLicense(aoInitialize);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("AoInitialize process caught UnknownHostException...");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("AoInitialize process caught IOException...");
            System.exit(0);
        }
        
    }

    /* 
     * Initialize ArcGIS license.
     * Checks to see if an ArcGIS Engine Runtime license or an Basic License is
     * available. If so, then the appropriate ArcGIS License is initialized.
     * 
     * License Order:  Advance --> Engine --> Standard --> Basic
     */
    public static void initLicense(AoInitialize aoInitialize) throws AutomationException, IOException {
        if (aoInitialize.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeAdvanced)
                == esriLicenseStatus.esriLicenseAvailable) {
            aoInitialize.initialize(esriLicenseProductCode.esriLicenseProductCodeAdvanced);
            System.out.println( "**** use Advanced License ****");
        } else if (aoInitialize.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) 
                == esriLicenseStatus.esriLicenseAvailable) {
            aoInitialize.initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
            System.out.println( "**** use Engine License ****");
        } else if (aoInitialize.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeStandard) 
                == esriLicenseStatus.esriLicenseAvailable) {
            aoInitialize.initialize(esriLicenseProductCode.esriLicenseProductCodeStandard);
            System.out.println( "**** use Standard License ****");
        } else if (aoInitialize.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeBasic) 
                == esriLicenseStatus.esriLicenseAvailable) {
            aoInitialize.initialize(esriLicenseProductCode.esriLicenseProductCodeBasic);
            System.out.println( "**** use Basic License ****");
        } else {
            System.err.println("Could not initialize an Engine or Basic License. Exiting application.");
            System.exit(-1);
        }
        
        System.out.println( "---- ArcGIS License initialized ----");
    }

    /**
     * Released by EngineInitializer.
     */
    public static void release() {
        try {
            aoInitialize.shutdown();
        } catch (AutomationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        EngineInitializer.releaseAll();
    }
    
    /**
     * Print Geoprocess tool results.
     * @throws IOException
     * @throws AutomationException 
     */
    public static void printResult(IGeoProcessorResult result) throws AutomationException, IOException {
        if (result.getMessageCount() > 0) {
            for (int i = 0; i < result.getMessageCount(); i++) {
                System.out.println("  -" + result.getMessage(i));
            }
        }
    }
    
    /**
     * Print all fields name.
     * @param fields A set of field.
     * @throws AutomationException
     * @throws IOException
     */
    public static void printFieldsName(IFields fields) throws AutomationException, IOException {
        if ( fields.getFieldCount()>0 ) {
            for (int i = 0; i < fields.getFieldCount(); i++) {
                System.out.println( "  --Field name: " + fields.getField(i).getName() +" - Length: "+ fields.getField(i).getLength() );
            }
        }
    }
    
}
