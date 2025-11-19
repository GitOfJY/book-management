package com.ex.bookmanagement.exception;

import java.util.Map;

public class BusinessException extends RuntimeException{
    private final ErrorCode code;
    private final Map<String, Object> args;

    public BusinessException(ErrorCode code) {
        super(code.defaultMessage());
        this.code = code;
        this.args = Map.of();
    }

    public BusinessException(ErrorCode code, Map<String, Object> args) {
        super(apply(code.defaultMessage(), args));
        this.code = code;
        this.args = args;
    }

    public ErrorCode getCode() { return code; }
    public Map<String, Object> getArgs() { return args; }

    private static String apply(String template, Map<String, Object> args) {
        String result = template;
        for (var e : args.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return result;
    }
}
