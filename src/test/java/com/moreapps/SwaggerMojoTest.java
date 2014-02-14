package com.moreapps;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moreapps.swagger.Service;
import com.moreapps.swagger.ServiceApiDetail;
import com.moreapps.swagger.ServiceModelProperty;
import com.moreapps.swagger.ServiceOperation;
import com.moreapps.swagger.ServiceOperations;

import org.apache.maven.plugin.MojoExecutionException;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SwaggerMojoTest {

    @Test
    public void testGenerate() throws MojoExecutionException, IOException {
        SwaggerMojo swaggerMojo = new SwaggerMojo();
        swaggerMojo.setTitle("Cars API");
        swaggerMojo.setDescription("API for Cars.");
        swaggerMojo.setTermsOfServiceUrl("http://www.morecars.nl");
        swaggerMojo.setContact("test@test.com");
        swaggerMojo.setLicense("Commercial Cars License");
        swaggerMojo.setLicenseUrl("http://www.morecars.nl/cars-license.html");

        swaggerMojo.setBaseControllerPackage("org.example");
        swaggerMojo.setBasePath("/newapidocs");
        swaggerMojo.setApiVersion("v1.0");

        swaggerMojo.setOutputDirectory(new File("target"));

        swaggerMojo.execute();

        assertThat(new File("target/service.json").exists(), is(true));
        assertThat(new File("target/cars.json").exists(), is(true));

        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

        Service service = objectMapper.readValue(new File("target/service.json"), Service.class);
        assertThat(service.getBasePath(), is("/newapidocs"));

        ServiceApiDetail serviceApiDetail = objectMapper.readValue(new File("target/cars.json"), ServiceApiDetail.class);

        assertThat(hasApiWithPathMethodAndResponseClass(serviceApiDetail.getApis(), "/{carId}", "DELETE", "Car"), is(true));

        ServiceModelProperty wheels = serviceApiDetail.getModels().get("Car").getProperties().get("wheels");
        assertThat(wheels.getType(), is("array"));
        assertThat(wheels.getItems().get("$ref"), is("Wheel"));
    }

    private boolean hasApiWithPathMethodAndResponseClass(List<ServiceOperations> apis, String path, String method, String responseClass) {
        for(ServiceOperations serviceOperations  : apis) {
            if (serviceOperations.getPath().equals(path)) {
                for(ServiceOperation operation : serviceOperations.getOperations()) {
                    if (operation.getMethod().equals(method) && operation.getResponseClass().equals(responseClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
