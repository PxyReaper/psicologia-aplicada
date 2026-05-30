package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.calendar.CalendarAsyncService;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionWithPatientDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final PatientAccess patientAccess;
    private final CalendarAsyncService calendarAsyncService;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              PatientAccess patientAccess,
                              CalendarAsyncService calendarAsyncService) {
        this.sessionRepository = sessionRepository;
        this.patientAccess = patientAccess;
        this.calendarAsyncService = calendarAsyncService;
    }

    @Override
    public void save(SessionRequestDTO requestDTO) {
        var patient = patientAccess.findById(requestDTO.idPatient())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        SessionEntity session = new SessionEntity(
                null,
                requestDTO.dateSession(),
                requestDTO.dateSessionEnd(),
                requestDTO.observatory(),
                requestDTO.observatorySummary(),
                requestDTO.pay() != null ? requestDTO.pay() : false,
                requestDTO.idPatient()
        );

        SessionEntity saved = sessionRepository.save(session);

        String patientFullName = patient.getName() + " " + patient.getSurname();
        calendarAsyncService.createAndStoreEvent(
                saved.getId(),
                patientFullName,
                requestDTO.dateSession(),
                requestDTO.dateSessionEnd()
        );
    }

    @Override
    public void update(Long id, SessionRequestDTO requestDTO) {
        SessionEntity existing = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        var patient = patientAccess.findById(requestDTO.idPatient())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        var oldPatient = patientAccess.findById(existing.getPatientId());

        SessionEntity updated = new SessionEntity(
                id,
                requestDTO.dateSession(),
                requestDTO.dateSessionEnd(),
                requestDTO.observatory(),
                requestDTO.observatorySummary(),
                requestDTO.pay() != null ? requestDTO.pay() : existing.isPay(),
                requestDTO.idPatient()
        );
        updated.setGoogleEventId(existing.getGoogleEventId());

        sessionRepository.save(updated);

        String patientFullName = patient.getName() + " " + patient.getSurname();
        String oldPatientFullName = oldPatient.map(p -> p.getName() + " " + p.getSurname()).orElse(patientFullName);

        calendarAsyncService.updateAndStoreEvent(
                id,
                patientFullName,
                requestDTO.dateSession(),
                requestDTO.dateSessionEnd(),
                existing.getGoogleEventId(),
                oldPatientFullName,
                existing.getSessionDate(),
                existing.getSessionDateEnd()
        );
    }

    @Override
    public void delete(Long id) {
        SessionEntity session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        String eventId = session.getGoogleEventId();

        sessionRepository.deleteById(id);

        calendarAsyncService.deleteEvent(eventId);
    }

    @Override
    public void deleteUpcomingSessionsByPatientId(Long patientId) {
        List<SessionEntity> upcoming = sessionRepository.findByPatientIdAndSessionDateGreaterThanEqual(
                patientId, LocalDateTime.now());
        for (SessionEntity session : upcoming) {
            if (session.getGoogleEventId() != null) {
                calendarAsyncService.deleteEvent(session.getGoogleEventId());
            }
            sessionRepository.delete(session);
        }
    }

    @Override
    public List<SessionWithPatientDTO> findByDate(LocalDate date) {
        List<SessionEntity> sessions = sessionRepository.findBySessionDateBetween(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );

        List<Long> patientIds = sessions.stream()
                .map(SessionEntity::getPatientId)
                .distinct()
                .toList();

        List<Patient> patients = patientAccess.findByIdIn(patientIds);
        var patientMap = patients.stream()
                .collect(Collectors.toMap(Patient::getId, p -> p));

        return sessions.stream()
                .map(session -> {
                    Patient patient = patientMap.get(session.getPatientId());
                    return new SessionWithPatientDTO(
                            session.getId(),
                            session.getSessionDate(),
                            session.getSessionDateEnd(),
                            session.getObservation(),
                            session.getObservationSummary(),
                            session.isPay(),
                            session.getPatientId(),
                            patient != null ? patient.getName() : null,
                            patient != null ? patient.getSurname() : null
                    );
                })
                .toList();
    }
}
