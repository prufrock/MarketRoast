package com.dkanen.MarketRoast;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.MarketplaceWebServiceClient;
import com.amazonaws.mws.MarketplaceWebServiceConfig;
import com.amazonaws.mws.MarketplaceWebServiceException;
import com.amazonaws.mws.model.GetReportRequest;
import com.amazonaws.mws.model.GetReportResponse;
import com.amazonaws.mws.model.ResponseMetadata;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * MarketRoast interfaces with Amazon's MWS to gather information for Kanen Coffee's sales on amazon.
 *
 * @author David Kanenwisher
 */
public class MarketRoast {

    private String pathToConfigFile;
    private Configuration config;

    /**
     * Creates a new object.
     *
     * @param pathToConfigFile The path to the configuration file for MarketRoast.
     */
    public MarketRoast(String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
    }

    /**
     * Loads the configuration from file.
     *
     * @return Whether or not the file was able to be loaded.
     */
    private boolean loadConfigurationFromFile() {
        try {
            config = new PropertiesConfiguration(pathToConfigFile);
        } catch (ConfigurationException e) {
            return false;
        }
        return true;
    }

    /**
     * Retrieve the report from MWS.
     * @param service The service to get the report from.
     * @param merchantId The ID of the merchant to make the request to.
     */
    private void retrieveReportFrom(MarketplaceWebService service, String merchantId) {
        GetReportRequest request = new GetReportRequest();
        request.setMerchant(merchantId);

        request.setReportId(config.getString("marketRoast.aws.reportId"));

        try {
            OutputStream report = new FileOutputStream(config.getString("marketRoast.aws.reportOutputFile"));
            request.setReportOutputStream(report);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        invokeGetReport(service, request);
    }

    /**
     * Get Report  request sample
     * The GetReport operation returns the contents of a report. Reports can potentially be
     * very large (>100MB) which is why we only return one report at a time, and in a
     * streaming fashion.
     *
     * @param service instance of MarketplaceWebService service
     * @param request Action to invoke
     */
    private void invokeGetReport(MarketplaceWebService service, GetReportRequest request) {
        try {

            GetReportResponse response = service.getReport(request);

            System.out.println("GetReport Action Response");
            System.out.println("=============================================================================");
            System.out.println();

            System.out.print("    GetReportResponse");
            System.out.println();
            System.out.print("    GetReportResult");
            System.out.println();
            System.out.print("            MD5Checksum");
            System.out.println();
            System.out.print("                " + response.getGetReportResult().getMD5Checksum());
            System.out.println();
            if (response.isSetResponseMetadata()) {
                System.out.print("        ResponseMetadata");
                System.out.println();
                ResponseMetadata responseMetadata = response.getResponseMetadata();
                if (responseMetadata.isSetRequestId()) {
                    System.out.print("            RequestId");
                    System.out.println();
                    System.out.print("                " + responseMetadata.getRequestId());
                    System.out.println();
                }
            }
            System.out.println();

            System.out.println("Report");
            System.out.println("=============================================================================");
            System.out.println();
            System.out.println(request.getReportOutputStream().toString());
            System.out.println();

            System.out.println(response.getResponseHeaderMetadata());
            System.out.println();


        } catch (MarketplaceWebServiceException ex) {

            System.out.println("Caught Exception: " + ex.getMessage());
            System.out.println("Response Status Code: " + ex.getStatusCode());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("Error Type: " + ex.getErrorType());
            System.out.println("Request ID: " + ex.getRequestId());
            System.out.print("XML: " + ex.getXML());
            System.out.println("ResponseHeaderMetadata: " + ex.getResponseHeaderMetadata());
        }
    }

    /**
     * Runs MarketRoast.
     */
    public void run() {
        if (!this.loadConfigurationFromFile()) {
            System.out.println("Unable to load configuration file from [" + pathToConfigFile + "]. Check that the " +
                    "file exists and that file and directory permissions are set correctly.");
        }

        final String appName = "MarketRoast";
        final String appVersion = "0.01";

        final String accessKeyId = config.getString("marketRoast.aws.acccessKeyId");
        final String secretAccessKey = config.getString("marketRoast.aws.secretAccessKey");

        MarketplaceWebServiceConfig mwsConfig = new MarketplaceWebServiceConfig();

        mwsConfig.setServiceURL(config.getString("marketRoast.aws.serviceUrl"));

        MarketplaceWebService service = new MarketplaceWebServiceClient(
                accessKeyId, secretAccessKey, appName, appVersion, mwsConfig);

        final String merchantId = config.getString("marketRoast.aws.sellerID");

        retrieveReportFrom(service, merchantId);
    }
}
