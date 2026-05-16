package com.backend.gamesales.Controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Tags;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.AdminService;
import com.backend.gamesales.Infrastructure.Storage.Storage;
import com.backend.gamesales.Services.TagService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMINISTRATOR')")
public class AdminController {

    private final AdminService adminService;
    private final Storage storage;
    private final TagService tagService;

    @GetMapping("/sellers/pending")
    public ResponseEntity<Page<Seller>> getPendingSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getPendingSellers(PageRequest.of(page, size)));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<Users>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(PageRequest.of(page, size)));
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<Map<String, String>> deleteGame(@PathVariable Long id) {
        adminService.deleteGameByAdmin(id, storage);
        return ResponseEntity.ok(Map.of("message", "Juego eliminado por administrador"));
    }

    @PostMapping("/tags")
    public ResponseEntity<Tags> createTag(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tagService.createTag(body.get("name")));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(Map.of("message", "Tag eliminado"));
    }

    @PatchMapping("/sellers/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateSeller(@PathVariable Long id) {
        adminService.deactivateSeller(id);
        return ResponseEntity.ok(Map.of("message", "Vendedor desactivado correctamente"));
    }
}
