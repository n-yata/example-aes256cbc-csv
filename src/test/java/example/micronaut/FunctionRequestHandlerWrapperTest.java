package example.micronaut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

class FunctionRequestHandlerWrapperTest {

    @Test
    void wrapperDelegatesAndReturnsResponse() {
        StubFunctionRequestHandler delegate = new StubFunctionRequestHandler();
        try {
            FunctionRequestHandlerWrapper wrapper = new FunctionRequestHandlerWrapper(delegate);

            APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
            request.setPath("/health");
            request.setBody("{}");

            APIGatewayProxyResponseEvent response = wrapper.handleRequest(request, null);

            assertEquals(204, response.getStatusCode());
            assertEquals("wrapped", response.getBody());
        } finally {
            delegate.getApplicationContext().close();
        }
    }

    @Test
    void funcRuntimeCreatesWrapper() {
        TestableFuncRuntime runtime = new TestableFuncRuntime();
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> handler = runtime
                .exposeCreateRequestHandler();

        assertInstanceOf(FunctionRequestHandlerWrapper.class, handler);
    }

    static class StubFunctionRequestHandler extends FunctionRequestHandler {
        @Override
        public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent request) {
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(204);
            response.setBody("wrapped");
            return response;
        }
    }

    static class TestableFuncRuntime extends FuncRuntime {
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> exposeCreateRequestHandler() {
            return createRequestHandler();
        }
    }
}
