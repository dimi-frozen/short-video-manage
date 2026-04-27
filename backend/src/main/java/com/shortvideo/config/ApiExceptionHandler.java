package com.shortvideo.config;

import com.shortvideo.utils.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

/**
 * 统一异常处理：
 * - 参数校验失败：400
 * - 资源不存在：404
 * - 乐观锁/并发冲突：409
 * - 其他异常：500
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // @RequestBody 参数校验失败（Bean Validation）
        String msg = e.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.error(400, msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        // @RequestParam/@PathVariable 参数校验失败
        return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException e) {
        // 业务侧主动抛出的参数错误
        return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> handleIllegalState(IllegalStateException e) {
        // 并发冲突、状态不一致等（例如乐观锁冲突后的提示）
        return ResponseEntity.status(409).body(Result.error(409, e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoSuchElementException e) {
        // 统一返回 404（不直接暴露内部异常信息）
        return ResponseEntity.status(404).body(Result.error(404, "资源不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        // 兜底异常，便于前端统一提示
        return ResponseEntity.status(500).body(Result.error(500, e.getMessage()));
    }
}
