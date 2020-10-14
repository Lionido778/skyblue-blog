package cn.codeprobe.blog.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;


/**
 * 错误码处理
 */
@Configuration
public class ErrorCodeConfig implements ErrorPageRegistrar {
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        // 不定参数
        registry.addErrorPages(
                new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
                new ErrorPage(HttpStatus.FORBIDDEN, "/403"),
                new ErrorPage(HttpStatus.GATEWAY_TIMEOUT, "/504"),
                new ErrorPage(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "/505")
        );
    }
}
