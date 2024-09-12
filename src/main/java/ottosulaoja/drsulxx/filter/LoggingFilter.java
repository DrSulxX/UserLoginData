package ottosulaoja.drsulxx.filter;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.web.server.WebFilter;
// import org.springframework.web.server.WebFilterChain;
// import reactor.core.publisher.Mono;

// @Component
// public class LoggingFilter implements WebFilter {

//     private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

//     @Override
//     public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//         String uri = exchange.getRequest().getURI().toString();
//         String queryString = exchange.getRequest().getURI().getQuery();

//         // Log the request URI and sanitized query string
//         logger.info("Request URI: {}", uri);
//         if (queryString != null && !queryString.isEmpty()) {
//             logger.info("Query string: {}", sanitizeQueryString(queryString));
//         }

//         return chain.filter(exchange);
//     }

//     private String sanitizeQueryString(String queryString) {
//         // Add logic to sanitize the query string, e.g., remove sensitive data
//         return queryString.replaceAll("([?&])email=[^&]*", "$1email=***")
//                           .replaceAll("([?&])username=[^&]*", "$1username=***")
//                           .replaceAll("([?&])name=[^&]*", "$1name=***")
//                           .replaceAll("([?&])familyName=[^&]*", "$1familyName=***");
//     }
// }