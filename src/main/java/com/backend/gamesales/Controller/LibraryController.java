package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Response.UserGameResponse;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<Page<UserGameResponse>> getLibrary(
            @AuthenticationPrincipal Users user,
            @PageableDefault(size = 10, sort = "purchasedAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(libraryService.getLibrary(user, pageable));
    }

    @GetMapping("/owns/{gameId}")
    public ResponseEntity<Boolean> ownsGame(
            @PathVariable Long gameId,
            @AuthenticationPrincipal Users user
    ) {
        return ResponseEntity.ok(libraryService.ownsGame(user, gameId));
    }
}