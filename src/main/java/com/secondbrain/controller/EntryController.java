package com.secondbrain.controller;

import com.secondbrain.dto.CreateEntryRequest;
import com.secondbrain.dto.EntryResponse;
import com.secondbrain.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @PostMapping
    public ResponseEntity<EntryResponse> createEntry(@Valid @RequestBody CreateEntryRequest request) {
        EntryResponse response = entryService.createEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntryResponse> getEntry(@PathVariable Long id) {
        EntryResponse response = entryService.getEntry(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EntryResponse>> getAllEntries() {
        List<EntryResponse> entries = entryService.getAllEntries();
        return ResponseEntity.ok(entries);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        entryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}
