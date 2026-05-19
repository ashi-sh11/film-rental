package com.example.backend.service.film;


import com.example.backend.dto.projection.ActorProjection;
import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ActorRepository;
import com.example.backend.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final FilmRepository filmRepository;

    public Page<ActorProjection> getAllActors(Pageable pageable) {
        return actorRepository.findAllProjectedBy(pageable);
    }

    public List<ActorProjection> getAllActorsBasic() {
        return actorRepository.findAllByOrderByFirstNameAscLastNameAsc();
    }

    public Page<ActorProjection> searchActor(String name, Pageable pageable) {
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");

        if (parts.length >= 2) {
            return actorRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                    parts[0], parts[parts.length - 1], pageable);
        }
        return actorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                trimmed, trimmed, pageable);
    }

    public ActorProjection getActorById(Integer id) {
        return actorRepository.findProjectedByActorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));
    }

    public Page<FilmProjection> getActorMovies(Integer actorId, Pageable pageable) {
        if (!actorRepository.existsById(actorId)) {
            throw new ResourceNotFoundException("Actor not found");
        }
        return filmRepository.findDistinctByFilmActors_Actor_ActorId(actorId, pageable);
    }
}
