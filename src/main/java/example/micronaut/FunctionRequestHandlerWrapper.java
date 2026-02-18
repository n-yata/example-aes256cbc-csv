package example.micronaut;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class FunctionRequestHandlerWrapper
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionRequestHandlerWrapper.class);

    private final FunctionRequestHandler delegate;

    public FunctionRequestHandlerWrapper(FunctionRequestHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Before execute. path={}, body={}", request != null ? request.getPath() : null,
                request != null ? request.getBody() : null);

        SqlSession session = delegate.getApplicationContext().findBean(SqlSession.class).orElse(null);
        if (session != null) {
            session.getConfiguration();
        }

        APIGatewayProxyResponseEvent response = delegate.handleRequest(request, context);

        LOG.info("After execute. statusCode={}, body={}", response != null ? response.getStatusCode() : null,
                response != null ? response.getBody() : null);
        return response;
    }
}
