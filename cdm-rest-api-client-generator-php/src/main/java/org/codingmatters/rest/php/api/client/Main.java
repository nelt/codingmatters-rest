package org.codingmatters.rest.php.api.client;

import org.codingmatters.rest.php.api.client.model.ApiGeneratorPhp;
import org.codingmatters.rest.php.api.client.model.ApiTypesPhpGenerator;
import org.codingmatters.value.objects.php.generator.SpecPhpGenerator;
import org.codingmatters.value.objects.spec.Spec;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;

import java.io.File;

public class Main {

    public static void main( String[] args ) {
        try {
//            File rootDir = Files.createTempDirectory( "cdmRest" ).toFile();
            File rootDir = new File( "/home/nico/workspace/codingmatters-rest/cdm-rest-api-client-generator-php/target/test-classes" );

            String clientPackage = "org.generated.client";
            String apiPackage = "org.generated.api";
            String typesPackage = "org.generated.types";

            PhpClientRequesterGenerator requesterGenerator = new PhpClientRequesterGenerator( clientPackage, apiPackage, typesPackage, rootDir );

            String ramlLocation = "/home/nico/workspace/codingmatters-rest/cdm-rest-api-client-generator-php/src/main/resources/test.raml";
            RamlModelResult model = new RamlModelBuilder().buildApi( ramlLocation );

            System.out.println( "Generating api in " + rootDir );

            Spec spec = new ApiTypesPhpGenerator( typesPackage ).generate( model );
            new SpecPhpGenerator( spec, typesPackage, rootDir ).generate();

            spec = new ApiGeneratorPhp( typesPackage ).generate( model );
            new SpecPhpGenerator( spec, apiPackage, rootDir ).generate();

            requesterGenerator.generate( model );


        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

}
