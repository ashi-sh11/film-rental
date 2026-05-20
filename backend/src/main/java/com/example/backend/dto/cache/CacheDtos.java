package com.example.backend.dto.cache;

import com.example.backend.dto.projection.ActorProjection;
import com.example.backend.dto.projection.CategoryProjection;
import com.example.backend.dto.projection.CustomerProjection;
import com.example.backend.dto.projection.FilmProjection;
import com.example.backend.dto.projection.LanguageProjection;
import com.example.backend.dto.projection.RecentRentalProjection;
import com.example.backend.dto.projection.RentalProjection;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


public final class CacheDtos {

    private CacheDtos() {}

    public record ActorDto(Integer actorId, String actorName, Long totalMovies)
            implements ActorProjection, Serializable {
        @Override public Integer getActorId() { return actorId; }
        @Override public String  getActorName() { return actorName; }
        @Override public Long    getTotalMovies() { return totalMovies; }

        public static ActorDto from(ActorProjection p) {
            return new ActorDto(p.getActorId(), p.getActorName(), p.getTotalMovies());
        }
    }

    public record FilmDto(Integer filmId, String title, String releaseYear,
                          String language, BigDecimal rentalRate, String rating, Integer length)
            implements FilmProjection, Serializable {
        @Override public Integer    getFilmId() { return filmId; }
        @Override public String     getTitle() { return title; }
        @Override public String     getReleaseYear() { return releaseYear; }
        @Override public String     getLanguage() { return language; }
        @Override public BigDecimal getRentalRate() { return rentalRate; }
        @Override public String     getRating() { return rating; }
        @Override public Integer    getLength() { return length; }

        public static FilmDto from(FilmProjection p) {
            return new FilmDto(p.getFilmId(), p.getTitle(), p.getReleaseYear(),
                    p.getLanguage(), p.getRentalRate(), p.getRating(), p.getLength());
        }
    }

    public record CustomerDto(Integer customerId, String fullName, String email,
                              Integer storeId, Boolean active)
            implements CustomerProjection, Serializable {
        @Override public Integer getCustomerId() { return customerId; }
        @Override public String  getFullName() { return fullName; }
        @Override public String  getEmail() { return email; }
        @Override public Integer getStoreId() { return storeId; }
        @Override public Boolean getActive() { return active; }

        public static CustomerDto from(CustomerProjection p) {
            return new CustomerDto(p.getCustomerId(), p.getFullName(), p.getEmail(),
                    p.getStoreId(), p.getActive());
        }
    }

    public record RentalDto(Integer rentalId, String movieTitle, String customerName,
                            LocalDateTime rentalDate, LocalDateTime returnDate, Boolean returned)
            implements RentalProjection, Serializable {
        @Override public Integer       getRentalId() { return rentalId; }
        @Override public String        getMovieTitle() { return movieTitle; }
        @Override public String        getCustomerName() { return customerName; }
        @Override public LocalDateTime getRentalDate() { return rentalDate; }
        @Override public LocalDateTime getReturnDate() { return returnDate; }
        @Override public Boolean       getReturned() { return returned; }

        public static RentalDto from(RentalProjection p) {
            return new RentalDto(p.getRentalId(), p.getMovieTitle(), p.getCustomerName(),
                    p.getRentalDate(), p.getReturnDate(), p.getReturned());
        }
    }

    public record CategoryDto(Integer categoryId, String name)
            implements CategoryProjection, Serializable {
        @Override public Integer getCategoryId() { return categoryId; }
        @Override public String  getName() { return name; }

        public static CategoryDto from(CategoryProjection p) {
            return new CategoryDto(p.getCategoryId(), p.getName());
        }
    }

    public record LanguageDto(Integer languageId, String name)
            implements LanguageProjection, Serializable {
        @Override public Integer getLanguageId() { return languageId; }
        @Override public String  getName() { return name; }

        public static LanguageDto from(LanguageProjection p) {
            return new LanguageDto(p.getLanguageId(), p.getName());
        }
    }

    public record RecentRentalDto(Integer rentalId, String movieTitle, String customerName,
                                  LocalDateTime rentalDate, Boolean returned)
            implements RecentRentalProjection, Serializable {
        @Override public Integer       getRentalId() { return rentalId; }
        @Override public String        getMovieTitle() { return movieTitle; }
        @Override public String        getCustomerName() { return customerName; }
        @Override public LocalDateTime getRentalDate() { return rentalDate; }
        @Override public Boolean       getReturned() { return returned; }

        public static RecentRentalDto from(RecentRentalProjection p) {
            return new RecentRentalDto(p.getRentalId(), p.getMovieTitle(), p.getCustomerName(),
                    p.getRentalDate(), p.getReturned());
        }
    }
}
