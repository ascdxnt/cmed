package com.citamed.controller;

import com.citamed.domain.Cita;
import com.citamed.domain.Doctor;
import com.citamed.domain.Paciente;
import com.citamed.service.CitaService;
import com.citamed.service.DoctorService;
import com.citamed.service.EspecialidadService;
import com.citamed.service.PacienteService;
import com.citamed.service.UsuarioService;
import java.util.Locale;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paciente")
public class PacienteController {

    // Atiende el panel, las reservas, las citas y el perfil del paciente.
    private final PacienteService pacienteService;
    private final EspecialidadService especialidadService;
    private final DoctorService doctorService;
    private final CitaService citaService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public PacienteController(PacienteService pacienteService, EspecialidadService especialidadService,
            DoctorService doctorService, CitaService citaService, UsuarioService usuarioService,
            MessageSource messageSource) {
        this.pacienteService = pacienteService;
        this.especialidadService = especialidadService;
        this.doctorService = doctorService;
        this.citaService = citaService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        var pacienteOpt = getPacienteActual(session);
        if (pacienteOpt.isPresent()) {
            var paciente = pacienteOpt.get();
            model.addAttribute("nombrePaciente", paciente.getUsuario().getNombre());
            model.addAttribute("proximaCita", citaService.getProximaCita(paciente).orElse(null));
        }
        return "/paciente/dashboard";
    }

    @GetMapping("/citas/reservar")
    public String reservar(@RequestParam(required = false) Integer idEspecialidad,
            @RequestParam(required = false) Integer idDoctor,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String hora,
            @RequestParam(required = false) String motivo,
            Model model, HttpSession session) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        var pacienteOpt = getPacienteActual(session);
        model.addAttribute("especialidades", especialidadService.getEspecialidades(true));
        pacienteOpt.ifPresent(paciente -> model.addAttribute("pacienteActual", paciente));
        var cita = new Cita();
        cita.setPaciente(new Paciente());
        cita.setDoctor(new Doctor());
        model.addAttribute("cita", cita);
        // El wizard carga los doctores después de escoger una especialidad.
        if (idEspecialidad != null) {
            var especialidadOpt = especialidadService.getEspecialidad(idEspecialidad);
            especialidadOpt.ifPresent(especialidad
                    -> model.addAttribute("doctores", doctorService.getDoctoresPorEspecialidad(especialidad)));
            model.addAttribute("idEspecialidad", idEspecialidad);
            model.addAttribute("especialidadSeleccionada", especialidadOpt.orElse(null));
        }
        if (idDoctor != null) {
            doctorService.getDoctor(idDoctor).ifPresent(doctor -> {
                model.addAttribute("doctorSeleccionado", doctor);
                if (fecha != null && !fecha.isBlank()) {
                    model.addAttribute("horasDisponibles",
                            citaService.getHorasDisponibles(doctor, LocalDate.parse(fecha)));
                }
            });
            model.addAttribute("idDoctor", idDoctor);
            model.addAttribute("fechaSeleccionada", fecha);
            model.addAttribute("horaSeleccionada", hora);
            model.addAttribute("motivoSeleccionado", motivo);
        }
        model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
        return "/paciente/citas/reservar";
    }

    @PostMapping("/citas/guardar")
    public String guardarCita(Cita cita, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        try {
            // El paciente y el doctor se recuperan de la base antes de reservar.
            var paciente = getPacienteActual(session)
                    .orElseThrow(() -> new IllegalStateException("No se encontró el paciente en sesión."));
            var doctor = doctorService.getDoctor(cita.getDoctor().getIdDoctor())
                    .orElseThrow(() -> new IllegalStateException("El médico seleccionado no existe."));
            var horas = citaService.getHorasDisponibles(doctor, cita.getFecha());
            if (!horas.contains(cita.getHora())) {
                throw new IllegalStateException("El horario seleccionado ya no está disponible.");
            }
            cita.setPaciente(paciente);
            cita.setDoctor(doctor);
            citaService.reservar(cita);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("cita.reservada", null, Locale.getDefault()));
            return "redirect:/paciente/citas/listado";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/paciente/citas/reservar";
        }
    }

    @GetMapping("/citas/listado")
    public String misCitas(Model model, HttpSession session) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        getPacienteActual(session).ifPresent(paciente
                -> model.addAttribute("citas", citaService.getCitasPorPaciente(paciente)));
        return "/paciente/citas/listado";
    }

    @PostMapping("/citas/cancelar")
    public String cancelar(@RequestParam Integer idCita, HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        try {
            // La regla de 24 horas se valida dentro del servicio.
            var paciente = getPacienteActual(session)
                    .orElseThrow(() -> new IllegalStateException("No se encontró el paciente en sesión."));
            citaService.cancelar(idCita, paciente);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("cita.cancelada", null, Locale.getDefault()));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/paciente/citas/listado";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        getPacienteActual(session).ifPresent(paciente -> model.addAttribute("paciente", paciente));
        return "/paciente/perfil";
    }

    @PostMapping("/perfil/contrasena")
    public String cambiarContrasena(@RequestParam String actual, @RequestParam String nueva,
            @RequestParam String confirmar, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esPaciente(session)) {
            return "redirect:/login";
        }
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null && usuarioService.cambiarContrasena(idUsuario, actual, nueva, confirmar)) {
            redirectAttributes.addFlashAttribute("todoOk", "La contraseña fue actualizada.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la contraseña.");
        }
        return "redirect:/paciente/perfil";
    }

    private java.util.Optional<com.citamed.domain.Paciente> getPacienteActual(HttpSession session) {
        // El id guardado en sesión permite recuperar el perfil del paciente.
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario == null) {
            return java.util.Optional.empty();
        }
        return pacienteService.getPacientePorUsuario(idUsuario);
    }

    private boolean esPaciente(HttpSession session) {
        return "PACIENTE".equals(session.getAttribute("rol"));
    }
}
