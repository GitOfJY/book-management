package com.ex.bookmanagement.config;

import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.exception.ErrorExamples;
import com.ex.bookmanagement.exception.ErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class OpenApiErrorExamplesConfig {

    /** 1) ErrorResponse 스키마를 components.schemas에 선등록 */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OpenApiCustomizer registerErrorResponseSchema() {
        return openApi -> {
            if (openApi.getComponents() == null) return;

            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            if (schemas != null && schemas.containsKey("ErrorResponse")) return;

            var resolved = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class));

            if (resolved != null && resolved.schema != null) {
                openApi.getComponents().addSchemas("ErrorResponse", resolved.schema);
                if (resolved.referencedSchemas != null) {
                    resolved.referencedSchemas.forEach(openApi.getComponents()::addSchemas);
                }
            }
        };
    }

    /** 2) 엔드포인트별로 지정한 ErrorCode만 예시로 주입 */
    @Bean
    public OperationCustomizer perOperationErrorExamples() {
        return (operation, handlerMethod) -> {
            ErrorExamples ann = getAnnotation(handlerMethod, ErrorExamples.class);
            if (ann == null || ann.value().length == 0) return operation;

            // ErrorResponse 스키마 참조
            Schema<?> errorRef = new Schema<>().$ref("#/components/schemas/ErrorResponse");

            // 상태코드별로 ErrorCode 묶기 (예: 400/404/409)
            Map<Integer, List<ErrorCode>> byHttp = Arrays.stream(ann.value())
                    .collect(Collectors.groupingBy(ec -> ec.status().value(), LinkedHashMap::new, Collectors.toList()));

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            // 람다 대신 일반 for문으로 캡처 이슈 방지
            for (Map.Entry<Integer, List<ErrorCode>> entry : byHttp.entrySet()) {
                int http = entry.getKey();
                List<ErrorCode> codes = entry.getValue();
                String codeStr = String.valueOf(http);

                ApiResponse resp = new ApiResponse().description(desc(http));

                Content content = new Content();
                MediaType json = new MediaType().schema(errorRef);

                // 이 엔드포인트에서 지정한 에러코드들만 예시 등록
                for (ErrorCode ec : codes) {
                    json.addExamples(ec.name(), toExample(ec, operation.getOperationId()));
                }

                content.addMediaType("application/json", json);
                responses.addApiResponse(codeStr, resp.content(content)); // 덮어쓰기
            }

            return operation;
        };
    }

    private Example toExample(ErrorCode ec, String opId) {
        Example ex = new Example();
        ex.setSummary(ec.name());
        ex.setValue(Map.of(
                "code", ec.name(),
                "message", ec.defaultMessage(),
                "status", ec.status().value(),
                "path", "/" + (opId == null ? "sample/path" : opId),
                "timestamp", "2025-11-11T10:00:00Z",
                "args", Map.of("sample", "value")
        ));
        return ex;
    }

    private String desc(int http) {
        return switch (http) {
            case 400 -> "잘못된 요청";
            case 404 -> "리소스를 찾을 수 없음";
            case 409 -> "비즈니스 충돌";
            default -> "오류";
        };
    }

    private <A extends Annotation> A getAnnotation(HandlerMethod hm, Class<A> type) {
        A a = hm.getMethodAnnotation(type);
        if (a != null) return a;
        return hm.getBeanType().getAnnotation(type); // (필요하면 클래스 레벨도 지원)
    }
}