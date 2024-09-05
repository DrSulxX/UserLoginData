package ottosulaoja.drsulxx.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ApiLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAspect.class);

    // Pointcut to match all methods in classes annotated with @RestController
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {
        // Pointcut for all RestController methods
    }

    // Log before the execution of any API endpoint
    @Before("restControllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Entering in Method: {}", joinPoint.getSignature().getName());
        logger.info("Class Name: {}", joinPoint.getSignature().getDeclaringTypeName());
        logger.info("Arguments: {}", Arrays.toString(joinPoint.getArgs()));
        logger.info("Target class: {}", joinPoint.getTarget().getClass().getName());
    }

    // Log after the successful execution of any API endpoint
    @AfterReturning(pointcut = "restControllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Method Return value: {}", result);
    }

    // Log if there is an exception thrown in any API endpoint
    @AfterThrowing(pointcut = "restControllerMethods()", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        logger.error("Exception in method: {}", joinPoint.getSignature().getName());
        logger.error("Exception is: {}", error.getMessage());
    }
}