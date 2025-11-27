package com.cinema.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class MovieDto {
    private Long id;

    @NotBlank(message = "Назва обов'язкова")
    private String title;

    private String description;

    @Min(1895) @NotNull
    private Integer releaseYear;

    @Min(0) @Max(10)
    private Double rating;

    @NotEmpty(message = "Вкажіть хоча б одну озвучку")
    private List<String> availableVoiceovers;
}