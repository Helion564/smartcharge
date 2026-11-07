package com.smartcharge.user.event;

import com.smartcharge.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private String eventType;
    private UUID userId;
    private String email;
    private User.Role role;
    private String firstName;
    private String lastName;
    private Instant timestamp;

    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String USER_LOGGED_IN   = "USER_LOGGED_IN";
    public static final String USER_DEACTIVATED = "USER_DEACTIVATED";
    public static final String USER_UPDATED     = "USER_UPDATED";
}
