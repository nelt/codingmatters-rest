package org.codingmatters.rest.php.api.client;

import org.codingmatters.rest.api.generator.exception.RamlSpecException;
import org.codingmatters.rest.php.api.client.generator.PhpClassGenerator;
import org.codingmatters.rest.php.api.client.model.HttpMethodDescriptor;
import org.codingmatters.rest.php.api.client.model.ResourceClientDescriptor;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhpClientRequesterGenerator {

    private final String clientPackage;
    private final String apiPackage;
    private final String typesPackage;
    private final File rootDir;
    private final Utils utils;
    private final PhpClassGenerator phpClassGenerator;

    public PhpClientRequesterGenerator( String clientPackage, String apiPackage, String typesPackage, File rootDir ) {
        this.clientPackage = clientPackage;
        this.apiPackage = apiPackage;
        this.typesPackage = typesPackage;
        this.rootDir = rootDir;
        this.utils = new Utils();
        this.phpClassGenerator = new PhpClassGenerator( rootDir.getPath(), clientPackage );
    }

    public void generate( RamlModelResult model ) throws RamlSpecException, IOException {
        Api api = model.getApiV10();
        if( api != null ) {
            List<ResourceClientDescriptor> clientDescriptors = processApi( api );
            for( ResourceClientDescriptor clientDescriptor : clientDescriptors ) {
                processGeneration( clientDescriptor );
            }
            System.out.println( "Got it" );
        } else {
            throw new RamlSpecException( "Cannot parse th raml spec v10" );
        }
    }


    private void processGeneration( ResourceClientDescriptor clientDescriptor ) throws IOException {
        phpClassGenerator.generateInterface( clientDescriptor );
        phpClassGenerator.generateImplementationClass( clientDescriptor );
        for( ResourceClientDescriptor subResourceClientDescriptor : clientDescriptor.nextFloorResourceClientGetters() ) {
            processGeneration( subResourceClientDescriptor );
        }
    }

    private List<ResourceClientDescriptor> processApi( Api api ) {
        List<ResourceClientDescriptor> clientDescriptors = new ArrayList<>();
        for( Resource resource : api.resources() ) {
            clientDescriptors.add( this.processResource( resource ) );
        }
        return clientDescriptors;
    }

    private ResourceClientDescriptor processResource( Resource resource ) {
        String resourceName = utils.getJoinedName( resource.displayName().value() );
        ResourceClientDescriptor resourceDesc = new ResourceClientDescriptor( resourceName, clientPackage );

        for( Method method : resource.methods() ) {
            HttpMethodDescriptor httpMethod = new HttpMethodDescriptor( utils.firstLetterLowerCase( resourceName ) )
                    .withRequestType( resourceName + utils.firstLetterUpperCase( method.method() ) + "Request", apiPackage )
                    .withResponseType( resourceName + utils.firstLetterUpperCase( method.method() ) + "Response", apiPackage )
                    .withPath( resource.resourcePath() )
                    .withMethod( method );


            resourceDesc.addMethodDescriptor( httpMethod );

        }

        for( Resource subResource : resource.resources() ) {
            ResourceClientDescriptor subResourceDesc = processResource( subResource );
            resourceDesc.addNextFloorResource( subResourceDesc );
        }
        return resourceDesc;
    }

}
