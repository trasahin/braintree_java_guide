package com.braintree.guide;

import static spark.Spark.get;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import spark.Response;
import spark.Route;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Customer;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;

public class App {
    private static BraintreeGateway gateway = new BraintreeGateway(
            Environment.SANDBOX,
            "your_merchant_id",
            "your_public_key",
            "your_private_key"
            );

    static String renderHtml(String templateFname, HashMap<String, String> valuesMap) {
        try {
            File formFile = new File(templateFname);
            String formTemplate = FileUtils.readFileToString(formFile);
            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            return sub.replace(formTemplate);
        } catch (IOException e) {
            return "";
        }
    }

    public static void main(String[] args) {
        get(new Route("/") {
            @Override
            public Object handle(spark.Request request, Response response) {
                // set the response type
                response.type("text/html");

                String braintreeUrl = gateway.transparentRedirect().url();
                CustomerRequest trParams = new CustomerRequest();

                String trData = gateway.transparentRedirect().trData(trParams, "http://localhost:4567/braintree");

                // return HTML with braintreeUrl and trData interpolated
                HashMap<String, String> valuesMap = new HashMap<String, String>();
                valuesMap.put("braintreeUrl", braintreeUrl);
                valuesMap.put("trData", trData);
                return renderHtml("views/form.html", valuesMap);
            }
        });

        get(new Route("/braintree") {
            @Override
            public Object handle(spark.Request request, Response response) {
                response.type("text/html");
                Result<Customer> result = gateway.transparentRedirect().confirmCustomer(request.queryString());
                String message = "";
                if (result.isSuccess()) {
                    message = result.getTarget().getEmail().toString();
                } else {
                    message = result.getMessage();
                }

                HashMap<String, String> valuesMap = new HashMap<String, String>();
                valuesMap.put("message", message);
                return renderHtml("views/response.html", valuesMap);
            }
        });
    }
}
