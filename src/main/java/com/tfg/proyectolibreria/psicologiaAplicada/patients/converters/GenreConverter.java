package com.tfg.proyectolibreria.psicologiaAplicada.patients.converters;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class GenreConverter  implements AttributeConverter<Genre,String> {
    @Override
    public String convertToDatabaseColumn(Genre genre) {
        if(genre == null) return null;
        return genre.getValue();
    }

    @Override
    public Genre convertToEntityAttribute(String s) {
       if(s == null ) return null;

       return Stream.of(Genre.values())
               .filter(g -> g.getValue().equalsIgnoreCase(s))
               .findFirst()
               .orElseThrow(IllegalArgumentException::new);

    }


}
