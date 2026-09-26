package com.aaspaas.aaspaas_backend.delivery.websocket;

import com.aaspaas.aaspaas_backend.security.JwtService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketAuthChannelInterceptor
        implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final DeliveryWebSocketAuthorizationService
            authorizationService;

    public WebSocketAuthChannelInterceptor(
            JwtService jwtService,
            UserRepository userRepository,
            DeliveryWebSocketAuthorizationService
                    authorizationService
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authorizationService =
                authorizationService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        StompCommand command =
                accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {

            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {

            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(
            StompHeaderAccessor accessor
    ) {

        String authorization =
                accessor.getFirstNativeHeader(
                        "Authorization"
                );

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "Missing WebSocket Authorization header"
            );
        }

        String token =
                authorization.substring(7);

        if (!jwtService.isTokenValid(token)) {

            throw new IllegalArgumentException(
                    "Invalid WebSocket JWT"
            );
        }

        String phone =
                jwtService.extractPhone(token);

        User user =
                userRepository.findByPhone(phone)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "WebSocket user not found"
                                )
                        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
        );

        accessor.setUser(authentication);
    }

    private void authorizeSubscription(
            StompHeaderAccessor accessor
    ) {

        Principal principal =
                accessor.getUser();

        if (!(principal instanceof Authentication authentication)) {

            throw new IllegalArgumentException(
                    "WebSocket user is not authenticated"
            );
        }

        Object principalObject =
                authentication.getPrincipal();

        if (!(principalObject instanceof User user)) {

            throw new IllegalArgumentException(
                    "Invalid WebSocket principal"
            );
        }

        String destination =
                accessor.getDestination();

        if (destination == null) {

            throw new IllegalArgumentException(
                    "WebSocket destination is required"
            );
        }

        String prefix =
                "/topic/delivery/";

        if (!destination.startsWith(prefix)) {

            return;
        }

        String assignmentIdString =
                destination.substring(
                        prefix.length()
                );

        Long assignmentId;

        try {

            assignmentId =
                    Long.parseLong(
                            assignmentIdString
                    );

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "Invalid delivery assignment ID"
            );
        }

        boolean allowed =
                authorizationService.canAccessDelivery(
                        assignmentId,
                        user.getId()
                );

        if (!allowed) {

            throw new IllegalArgumentException(
                    "You are not authorized to track this delivery"
            );
        }
    }
}