package com.citamed.controller;

import com.citamed.domain.Doctor;
import com.citamed.domain.EstadoCita;
import com.citamed.domain.Especialidad;
import com.citamed.domain.Usuario;
import com.citamed.service.CitaService;
import com.citamed.service.DoctorService;
import com.citamed.service.EspecialidadService;
import com.citamed.service.PacienteService;
import com.citamed.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Locale;
import java.time.LocalTime;
import com.citamed.domain.DiaSemana;
import com.citamed.domain.Paciente;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Atiende los módulos de gestión disponibles para administración.
    private final DoctorService doctorService;
    private final EspecialidadService especialidadService;
    private final PacienteService pacienteService;
    private final CitaService citaService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public AdminController(DoctorService doctorService, EspecialidadService especialidadService,
            PacienteService pacienteService, CitaService citaService, UsuarioService usuarioService,
            MessageSource messageSource) {
        this.doctorService = doctorService;
        this.especialidadService = especialidadService;
        this.pacienteService = pacienteService;
        this.citaService = citaService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        // Los datos del dashboard se calculan con la información guardada.
        var citas = citaService.getCitas();
        model.addAttribute("totalCitas", citas.size());
        model.addAttribute("totalMedicos", doctorService.getDoctores(true).size());
        model.addAttribute("totalPacientes", pacienteService.getPacientes(true).size());
        model.addAttribute("citasHoy", citas.stream().filter(c -> LocalDate.now().equals(c.getFecha())).count());
        model.addAttribute("citasPendientes", citas.stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count());
        model.addAttribute("citas", citas);
        return "/admin/dashboard";
    }

    @GetMapping("/medicos/listado")
    public String medicos(Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        // Se prepara el objeto que utiliza el formulario de registro.
        var doctor = new Doctor();
        doctor.setUsuario(new Usuario());
        doctor.setEspecialidad(new Especialidad());
        doctor.setActivo(true);
        model.addAttribute("doctor", doctor);
        model.addAttribute("doctores", doctorService.getDoctores(false));
        model.addAttribute("especialidades", especialidadService.getEspecialidades(true));
        model.addAttribute("diasSemana", DiaSemana.values());
        return "/admin/medicos/listado";
    }

    @GetMapping("/pacientes/listado")
    public String pacientes(@RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer editar, Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        var paciente = new Paciente();
        paciente.setUsuario(new Usuario());
        if (editar != null) {
            paciente = pacienteService.getPaciente(editar).orElse(paciente);
        }
        model.addAttribute("paciente", paciente);
        model.addAttribute("pacientes", pacienteService.buscarPacientes(busqueda));
        model.addAttribute("busqueda", busqueda);
        return "/admin/pacientes/listado";
    }

    @PostMapping("/pacientes/guardar")
    public String guardarPaciente(@Valid Paciente paciente, HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        try {
            pacienteService.save(paciente);
            redirectAttributes.addFlashAttribute("todoOk", "Los datos del paciente fueron actualizados.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/pacientes/listado";
    }

    @PostMapping("/pacientes/estado")
    public String estadoPaciente(@RequestParam Integer idPaciente, HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        pacienteService.cambiarEstado(idPaciente);
        redirectAttributes.addFlashAttribute("todoOk", "El estado del paciente fue actualizado.");
        return "redirect:/admin/pacientes/listado";
    }

    @PostMapping("/medicos/guardar")
    public String guardarMedico(@Valid Doctor doctor, @RequestParam DiaSemana diaSemana,
            @RequestParam LocalTime horaInicio, @RequestParam LocalTime horaFin,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        try {
            doctorService.guardarConHorario(doctor, diaSemana, horaInicio, horaFin);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("medico.guardado", null, Locale.getDefault()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/medicos/listado";
    }

    @PostMapping("/medicos/estado")
    public String estadoMedico(@RequestParam Integer idDoctor, HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        doctorService.cambiarEstado(idDoctor);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("medico.estado", null, Locale.getDefault()));
        return "redirect:/admin/medicos/listado";
    }

    @PostMapping("/especialidades/guardar")
    public String guardarEspecialidad(@Valid Especialidad especialidad, HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        especialidad.setActivo(true);
        especialidadService.save(especialidad);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("especialidad.guardada", null, Locale.getDefault()));
        return "redirect:/admin/especialidades/listado";
    }

    @GetMapping("/especialidades/listado")
    public String especialidades(Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("especialidades", especialidadService.getEspecialidades(false));
        return "/admin/especialidades/listado";
    }

    @GetMapping("/reportes")
    public String reportes(@RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin, Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        LocalDate inicio = (fechaInicio == null || fechaInicio.isBlank())
                ? LocalDate.now().minusDays(30) : LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFin == null || fechaFin.isBlank())
                ? LocalDate.now().plusDays(30) : LocalDate.parse(fechaFin);
        // El reporte resume las citas que están dentro del rango escogido.
        var citas = citaService.getCitasPorRango(inicio, fin);
        model.addAttribute("citas", citas);
        model.addAttribute("totalCitas", citas.size());
        model.addAttribute("fechaInicio", inicio);
        model.addAttribute("fechaFin", fin);
        model.addAttribute("pendientes", citas.stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count());
        model.addAttribute("confirmadas", citas.stream().filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count());
        model.addAttribute("completadas", citas.stream().filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count());
        model.addAttribute("canceladas", citas.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count());
        return "/admin/reportes";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombreUsuario"));
        model.addAttribute("rol", session.getAttribute("rol"));
        return "/admin/perfil";
    }

    @PostMapping("/perfil/contrasena")
    public String cambiarContrasena(@RequestParam String actual, @RequestParam String nueva,
            @RequestParam String confirmar, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        Integer idUsuario = (Integer) session.getAttribute("idUsuario");
        if (idUsuario != null && usuarioService.cambiarContrasena(idUsuario, actual, nueva, confirmar)) {
            redirectAttributes.addFlashAttribute("todoOk", "La contraseña fue actualizada.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar la contraseña.");
        }
        return "redirect:/admin/perfil";
    }

    @GetMapping("/citas/listado")
    public String citas(@RequestParam(required = false) Integer idDoctor,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) EstadoCita estado,
            Model model, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        // Los filtros se aplican sobre la lista completa de citas.
        var citasFiltradas = new java.util.ArrayList<com.citamed.domain.Cita>();
        for (var cita : citaService.getCitas()) {
            boolean coincide = true;
            if (idDoctor != null && !cita.getDoctor().getIdDoctor().equals(idDoctor)) {
                coincide = false;
            }
            if (fecha != null && !fecha.isBlank() && !cita.getFecha().equals(LocalDate.parse(fecha))) {
                coincide = false;
            }
            if (estado != null && cita.getEstado() != estado) {
                coincide = false;
            }
            if (coincide) {
                citasFiltradas.add(cita);
            }
        }
        model.addAttribute("citas", citasFiltradas);
        model.addAttribute("doctores", doctorService.getDoctores(false));
        model.addAttribute("estados", EstadoCita.values());
        model.addAttribute("idDoctor", idDoctor);
        model.addAttribute("fecha", fecha);
        model.addAttribute("estado", estado);
        return "/admin/citas/listado";
    }

    @PostMapping("/citas/confirmar")
    public String confirmar(@RequestParam Integer idCita, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        citaService.confirmar(idCita);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("cita.confirmada", null, Locale.getDefault()));
        return "redirect:/admin/citas/listado";
    }

    @PostMapping("/citas/rechazar")
    public String rechazar(@RequestParam Integer idCita, @RequestParam String motivo,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        try {
            citaService.rechazar(idCita, motivo);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("cita.rechazada", null, Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/citas/listado";
    }

    private boolean esAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("rol"));
    }
}
