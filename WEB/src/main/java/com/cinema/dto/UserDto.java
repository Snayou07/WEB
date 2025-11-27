package com.cinema.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;

    @NotBlank
    private String username;

    @Email @NotBlank
    private String email;

    private LocalDate registeredAt;
}