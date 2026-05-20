package com.example.backend.service.film;


import com.example.backend.dto.cache.CacheDtos.ActorDto;
import com.example.backend.dto.cache.CacheDtos.FilmDto;
import com.example.backend.dto.projection.ActorProjection;
import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ActorRepository;
import com.example.backend.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final FilmRepository filmRepository;

    @Cacheable(value = "actors", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ActorProjection> getAllActors(Pageable pageable) {
        return actorRepository.findAllProjectedBy(pageable)
                .map(p -> (ActorProjection) ActorDto.from(p));
    }

    @Cacheable(value = "actorsBasic")
    public List<ActorProjection> getAllActorsBasic() {
        return actorRepository.findAllByOrderByFirstNameAscLastNameAsc().stream()
                .<ActorProjection>map(ActorDto::from)
                .toList();
    }

    @Cacheable(value = "actorSearch",
            key = "#name + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ActorProjection> searchActor(String name, Pageable pageable) {
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");

        Page<ActorProjection> raw = parts.length >= 2
                ? actorRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                        parts[0], parts[parts.length - 1], pageable)
                : actorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        trimmed, trimmed, pageable);
        return raw.map(p -> (ActorProjection) ActorDto.from(p));
    }

    @Cacheable(value = "actorById", key = "#id")
    public ActorProjection getActorById(Integer id) {
        return actorRepository.findProjectedByActorId(id)
                .map(ActorDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));
    }

    @Cacheable(value = "actorMovies",
            key = "#actorId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<FilmProjection> getActorMovies(Integer actorId, Pageable pageable) {
        if (!actorRepository.existsById(actorId)) {
            throw new ResourceNotFoundException("Actor not found");
        }
        return filmRepository.findDistinctByFilmActors_Actor_ActorId(actorId, pageable)
                .map(p -> (FilmProjection) FilmDto.from(p));
    }
}
