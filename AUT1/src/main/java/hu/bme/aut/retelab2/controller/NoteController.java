package hu.bme.aut.retelab2.controller;

import hu.bme.aut.retelab2.domain.Note;
import hu.bme.aut.retelab2.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @GetMapping
    public List<Note> getAll(@RequestParam(required = false, defaultValue = "") String keyword) {
        return keyword.equals("") ? noteRepository.findAll() : noteRepository.findByKeyword(keyword);
    }

    @GetMapping("{id}")
    public ResponseEntity<Note> getById(@PathVariable long id) {
        Note note = noteRepository.findById(id);
        if (note == null)
            return ResponseEntity.notFound().build();
        else
            return ResponseEntity.ok(note);
    }

    @PostMapping
    public Note create(@RequestBody Note note) {
        note.setId(null);
        return noteRepository.save(note);
    }

    @PutMapping
    public ResponseEntity<Note> update(@RequestBody Note note) {
        Note n = noteRepository.findById(note.getId());
        if (n == null)
            return ResponseEntity.notFound().build();
        n = noteRepository.save(note);
        return ResponseEntity.ok(n);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        Note note = noteRepository.findById(id);
        if (note == null)
            return ResponseEntity.notFound().build();
        else {
            noteRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
    }
}