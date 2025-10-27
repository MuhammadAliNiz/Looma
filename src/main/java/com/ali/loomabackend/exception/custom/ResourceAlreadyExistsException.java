package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown when attempting to create a resource that already exists
 */
public class ResourceAlreadyExistsException extends BaseException {
    
    public ResourceAlreadyExistsException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }
    
    public ResourceAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ResourceAlreadyExistsException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public static ResourceAlreadyExistsException user(String username) {
        return new ResourceAlreadyExistsException(ErrorCode.USER_ALREADY_EXISTS, 
                "User already exists with username: " + username);
    }
    
    public static ResourceAlreadyExistsException email(String email) {
        return new ResourceAlreadyExistsException(ErrorCode.USER_ALREADY_EXISTS, 
                "User already exists with email: " + email);
    }
    
    public static ResourceAlreadyExistsException reaction(Long userId, Long targetId) {
        return new ResourceAlreadyExistsException(ErrorCode.REACTION_ALREADY_EXISTS, 
                "User already reacted to this content");
    }
    
    public static ResourceAlreadyExistsException follow(Long followerId, Long followingId) {
        return new ResourceAlreadyExistsException(ErrorCode.ALREADY_FOLLOWING, 
                "User is already following this user");
    }
}

