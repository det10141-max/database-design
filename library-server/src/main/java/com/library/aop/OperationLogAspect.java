package com.library.aop;
import com.library.entity.OperationLog;
import com.library.mapper.OperationLogMapper;
import com.library.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint jp, com.library.aop.OperationLog opLog) throws Throwable {
        Object result = jp.proceed();
        try {
            OperationLog log = new OperationLog();
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser u) {
                log.setUserId(u.getId());
            }
            log.setAction(opLog.value());
            log.setTargetType(jp.getSignature().getDeclaringTypeName());
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                log.setDetail(req.getMethod() + " " + req.getRequestURI());
                log.setIp(req.getRemoteAddr());
            }
            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("OperationLogAspect error", e);
        }
        return result;
    }
}
