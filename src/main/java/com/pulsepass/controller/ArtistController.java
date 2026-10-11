package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.service.ArtistService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<ArtistResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.findById(id));
    }

    @GetMapping("/artists/by-stage-name")
    public ResponseEntity<ArtistResponse> findByStageName(@RequestParam String stageName) {
        return ResponseEntity.ok(artistService.findByStageName(stageName));
    }

    @GetMapping("/artists/active")
    public ResponseEntity<List<ArtistResponse>> findActiveArtists() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }
}
