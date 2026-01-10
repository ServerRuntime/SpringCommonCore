package io.commoncore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponse<T> {
    private int statusCode;
    
    private T data;
    
    private String message;

    public CustomResponse(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
    }

    public static <T> CustomResponse<T> success(T data) {
        return new CustomResponse<>(200, data, "Success");
    }

    public static <T> CustomResponse<T> success(T data, String message) {
        return new CustomResponse<>(200, data, message);
    }

    public static <T> CustomResponse<T> created(T data) {
        return new CustomResponse<>(201, data, "Created successfully");
    }

    public static <T> CustomResponse<T> created(T data, String message) {
        return new CustomResponse<>(201, data, message);
    }

    public static <T> CustomResponse<T> error(int statusCode, String message) {
        return new CustomResponse<>(statusCode, null, message);
    }
}
