package com.tfg.proyectolibreria.psicologiaAplicada.patients;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@AllArgsConstructor
@NoArgsConstructor
public class PatientsEntity implements Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(name = "birthday")
    private LocalDate birthDay;
    private String cellPhone;

    private Genre genre;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public Genre getGenre() {
        return genre;
    }
}
