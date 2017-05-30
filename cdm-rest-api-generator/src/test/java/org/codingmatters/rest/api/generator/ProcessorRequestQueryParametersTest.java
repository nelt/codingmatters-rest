package org.codingmatters.rest.api.generator;

import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by nelt on 5/30/17.
 */
public class ProcessorRequestQueryParametersTest extends AbstractProcessorHttpRequestTest {

    @Before
    public void setUp() throws Exception {
        ProcessorGeneratorTestHelper helper = new ProcessorGeneratorTestHelper(this.dir, this.fileHelper)
                .setUpWithResource("processor/processor-request.raml");
        this.compiled = helper.compiled();
        this.fileHelper.printFile(this.dir.getRoot(), "TestAPIProcessor.java");
    }

    @Test
    public void singleParameter() throws Exception {
        AtomicReference requestHolder = new AtomicReference();
        this.setupProcessorWithHandler(
                "queryParamsGetHandler",
                req -> {
                    requestHolder.set(req);
                    return null;
                });

        Response response = this.client.newCall(new Request.Builder().url(this.undertow.baseUrl() + "/api/query-params?stringParam=val")
                .get()
                .build()).execute();
        Object request = requestHolder.get();

        assertThat(response.code(), is(200));
        assertThat(request, is(notNullValue()));
        assertThat(
                this.compiled.on(request).castedTo("org.generated.api.QueryParamsGetRequest").invoke("stringParam"),
                is("val")
        );
    }

    @Ignore
    @Test
    public void arrayParameter() throws Exception {
        fail("TODO");
    }
}
