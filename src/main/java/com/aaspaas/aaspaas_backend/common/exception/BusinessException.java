// package com.aaspaas.aaspaas_backend.common.exception;

// import lombok.Getter;

// @Getter
// public class BusinessException extends RuntimeException {

//     private final int statusCode;

//     public BusinessException(String message, int statusCode) {
//         super(message);
//         this.statusCode = statusCode;
//     }
//     public BusinessException(String message) {   // ADD THIS
//     this(message, 400);                        // default 400 Bad Request
// }
// }

// package com.aaspaas.aaspaas_backend.common.exception;
// import lombok.Getter;
// @Getter
// public class BusinessException extends RuntimeException {

//     private int status = 400; // default bad request

//     // 1-argument constructor (already exists)
//     public BusinessException(String message) {
//         super(message);
//     }

//     // YEH ADD KAREIN: 2-argument constructor (String, int)
//     public BusinessException(String message, int status) {
//         super(message);
//         this.status = status;
//     }

//     public int getStatus() {
//         return status;
//     }
    
// }

package com.aaspaas.aaspaas_backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private int status = 400;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public HttpStatus getStatusCode() {
        return HttpStatus.resolve(status) != null ? HttpStatus.resolve(status) : HttpStatus.BAD_REQUEST;
    }
}