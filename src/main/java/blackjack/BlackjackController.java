package blackjack;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blackjack")
public class BlackjackController {

    private final SessionManager manager;
    private final AccountManager accountManager;

    public BlackjackController(SessionManager manager, AccountManager accountManager) 
    {
        this.manager = manager;
        this.accountManager = accountManager;
    }

    // ✅ Start a new session
    @Operation(summary = "Start", description = "Start a new game")
    @PostMapping("/start")
    public GameStateDTO start(@RequestParam String username, @RequestParam String password) 
    {
        System.out.println("Starting session for user: " + username);
        authenticate(username, password);
        System.out.println("Authenticated user: " + username);
        BlackjackSession session = manager.createNewSession(username);
        GameStateDTO dto = GameStateDTO.from(session);
        System.out.println(dto);
        return dto;
    }

    // ✅ Place a bet and deal
    @Operation(summary = "Bet", description = "Bet the given amount. Must be a multiple of 10")
    @PostMapping("/{id}/bet/{amount}")
    public GameStateDTO bet(@PathVariable UUID id, @PathVariable int amount,
                            @RequestParam String username, @RequestParam String password) {
        authenticate(id, username, password);
        BlackjackSession session = manager.getSession(id).orElseThrow();
        session.betAndDeal(amount);
        return GameStateDTO.from(session);
    }

    // ✅ Player hits
    @Operation(summary = "Hit", description = "Player hits and receives a new card")
    @PostMapping("/{id}/hit")
    public GameStateDTO hit(@PathVariable UUID id,
                            @RequestParam String username, 
                            @RequestParam String password) 
    {
        authenticate(id, username, password);
        BlackjackSession session = manager.getSession(id).orElseThrow();
        session.playerHit();
        return GameStateDTO.from(session);
    }

    // ✅ Player stands
    @Operation(summary = "Stand", description = "Player stands and dealer plays")
    @PostMapping("/{id}/stand")
    public GameStateDTO stand(@PathVariable UUID id,
                              @RequestParam String username, 
                              @RequestParam String password) 
    {
        authenticate(id, username, password);
        BlackjackSession session = manager.getSession(id).orElseThrow();
        session.playerStand();
        session.dealerPlay();
        return GameStateDTO.from(session);
    }

    // ✅ Reset session state (same balance, new cards)
    @Operation(summary = "Reset", description = "Reset session state (same balance, new cards)")
    @PostMapping("/{id}/reset")
    public GameStateDTO reset(@PathVariable UUID id,
                              @RequestParam String username, 
                              @RequestParam String password) 
    {
        authenticate(id, username, password);
        BlackjackSession session = manager.getSession(id).orElseThrow();
        session.reset();
        return GameStateDTO.from(session);
    }

    // ✅ Check current state of a session
    @Operation(summary = "State", description = "Check current state of a session")
    @GetMapping("/{id}/state")
    public GameStateDTO state(@PathVariable UUID id,
                              @RequestParam String username, 
                              @RequestParam String password) 
    {
        authenticate(id, username, password);
        return GameStateDTO.from(manager.getSession(id).orElseThrow());
    }

    // ✅ Finish session (archive it to H2)
    @Operation(summary = "Finish", description = "Finish session (archive it to H2)")
    @PostMapping("/{id}/finish")
    public ResponseEntity<Void> finish(@PathVariable UUID id,
                                       @RequestParam String username, 
                                       @RequestParam String password) 
    {
        authenticate(id, username, password);
        manager.archiveSession(id);
        return ResponseEntity.ok().build();
    }

    // ✅ Look up your most recent session
    @Operation(summary = "My session", description = "Look up your most recent session")
    @GetMapping("/my-session")
    public UUID getMySession(@RequestParam String username, @RequestParam String password) 
    {
        authenticate(username, password);
        return manager.getActiveSessionIdForUser(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found"));
    }

    // ✅ View all archived sessions for a user
    @Operation(summary = "Archived sessions", description = "View all archived sessions for a user")
    @GetMapping("/sessions/{username}")
    public List<SessionSummaryDTO> getArchivedSessions(@PathVariable String username,
                                                       @RequestParam String password)
    {
        authenticate(username, password);
        return manager.getArchivedSessions(username).stream()
            .map(SessionSummaryDTO::from)
            .toList();
    }

    @PostMapping("/resume/{id}")
    @Operation(summary = "Resume session", description = "Resume a session by ID")
    public GameStateDTO resumeSession(@PathVariable UUID id,
                    @RequestParam String username,
                    @RequestParam String password) 
    {
        authenticate(username, password);

        UserSessionEntity archived = manager.getArchivedSession(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Archived session not found"));

        if (!archived.getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session does not belong to user");
        }

        BlackjackSession session = BlackjackSession.fromEntity(archived); // reconstructs in-memory session
        manager.resumeSession(session); // puts it into active map
        if (session.getPhase() == GamePhase.RESOLVED) {
            session.reset();
        }

        return GameStateDTO.from(session);
    }

    

    // 🔐 Auth helpers

    private void authenticate(String username, String password) {
        if (!accountManager.isValid(username, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    private void authenticate(UUID id, String username, String password) {
        authenticate(username, password);
        if (!manager.sessionBelongsTo(id, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Session does not belong to user: " + id + " user " + username);
        }
    }
}
