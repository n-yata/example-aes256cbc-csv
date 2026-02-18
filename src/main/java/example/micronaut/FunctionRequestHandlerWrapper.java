package example.micronaut;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class FunctionRequestHandlerWrapper
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionRequestHandlerWrapper.class);
    private static final Tracer TRACER = GlobalOpenTelemetry
            .getTracer("example.micronaut.FunctionRequestHandlerWrapper");

    private final FunctionRequestHandler delegate;

    public FunctionRequestHandlerWrapper(FunctionRequestHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        Span span = TRACER.spanBuilder("FunctionRequestHandlerWrapper.handleRequest").startSpan();
        if (request != null && request.getPath() != null) {
            span.setAttribute("http.target", request.getPath());
        }
        if (context != null && context.getAwsRequestId() != null) {
            span.setAttribute("aws.request_id", context.getAwsRequestId());
        }

        try (Scope ignored = span.makeCurrent()) {
            LOG.info("Before execute. path={}, body={}", request != null ? request.getPath() : null,
                    request != null ? request.getBody() : null);

            SqlSession session = delegate.getApplicationContext().findBean(SqlSession.class).orElse(null);
            if (session != null) {
                session.getConfiguration();
            }

            APIGatewayProxyResponseEvent response = delegate.handleRequest(request, context);
            if (response != null && response.getStatusCode() != null) {
                span.setAttribute("http.status_code", response.getStatusCode());
            }
            span.setStatus(StatusCode.OK);

            LOG.info("After execute. statusCode={}, body={}", response != null ? response.getStatusCode() : null,
                    response != null ? response.getBody() : null);
            return response;
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            throw ex;
        } finally {
            span.end();
        }
    }
}
