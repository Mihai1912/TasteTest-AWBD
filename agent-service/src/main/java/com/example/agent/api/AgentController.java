package com.example.agent.api;

import com.example.agent.tools.RestaurantTools;
import com.example.agent.tools.UserTools;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    private final ChatClient chatClient;
    private final RestaurantTools restaurantTools;
    private final UserTools userTools;

    public AgentController(ChatClient chatClient,
                           RestaurantTools restaurantTools,
                           UserTools userTools) {
        this.chatClient = chatClient;
        this.restaurantTools = restaurantTools;
        this.userTools = userTools;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Agent chat from {}: {}", principal, truncate(request.message(), 200));

        try {
            String reply = chatClient.prompt()
                    .user(request.message())
                    .tools(restaurantTools, userTools)
                    .call()
                    .content();
            return ResponseEntity.ok(new ChatResponse(reply));
        } catch (Exception e) {
            logger.error("Agent invocation failed", e);
            return ResponseEntity.status(502)
                    .body(new ChatResponse("Sorry, I couldn't reach the model right now. (" + e.getMessage() + ")"));
        }
    }

    private String truncate(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n) + "…";
    }
}
