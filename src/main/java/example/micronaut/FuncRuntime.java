package example.micronaut;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.micronaut.function.aws.runtime.MicronautLambdaRuntime;

public class FuncRuntime extends MicronautLambdaRuntime {

    public static void main(String[] args) throws Exception {
        new FuncRuntime().run(args);
    }

    @Override
    protected RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> createRequestHandler(
            String... args) {
        FunctionRequestHandler delegate = new FunctionRequestHandler();
        return new FunctionRequestHandlerWrapper(delegate);
    }
}
