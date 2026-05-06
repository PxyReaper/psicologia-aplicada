package com.tfg.proyectolibreria.psicologiaAplicada.patients.enums;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.converters.GenreConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenreConverterTest {

    private final GenreConverter converter = new GenreConverter();

    @Test
    void convertToDatabaseColumn_shouldReturnStringValue() {
        String result = converter.convertToDatabaseColumn(Genre.MASCULINO);
        assertThat(result).isEqualTo("masculino");
    }

    @Test
    void convertToDatabaseColumn_shouldReturnNullForNullInput() {
        String result = converter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_shouldReturnGenre() {
        Genre result = converter.convertToEntityAttribute("masculino");
        assertThat(result).isEqualTo(Genre.MASCULINO);
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullForNullInput() {
        Genre result = converter.convertToEntityAttribute(null);
        assertThat(result).isNull();
    }

    @Test
    void fromValue_shouldReturnGenreForValidInput() {
        Genre result = Genre.fromValue("femenino");
        assertThat(result).isEqualTo(Genre.FEMENINO);
    }

    @Test
    void fromValue_shouldBeCaseInsensitive() {
        Genre result = Genre.fromValue("MASCULINO");
        assertThat(result).isEqualTo(Genre.MASCULINO);
    }
}
