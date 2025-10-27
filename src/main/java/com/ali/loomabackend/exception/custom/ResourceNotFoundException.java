package com.ali.loomabackend.exception.custom;


import com.ali.loomabackend.model.enums.ErrorCode;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
    
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ResourceNotFoundException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, 
                "User not found with id: " + userId);
    }
    
    public static ResourceNotFoundException post(Long postId) {
        return new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, 
                "Post not found with id: " + postId);
    }
    
    public static ResourceNotFoundException comment(Long commentId) {
        return new ResourceNotFoundException(ErrorCode.COMMENT_NOT_FOUND, 
                "Comment not found with id: " + commentId);
    }
    
    public static ResourceNotFoundException media(Long mediaId) {
        return new ResourceNotFoundException(ErrorCode.MEDIA_NOT_FOUND, 
                "Media not found with id: " + mediaId);
    }
    
    public static ResourceNotFoundException notification(Long notificationId) {
        return new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, 
                "Notification not found with id: " + notificationId);
    }
}

