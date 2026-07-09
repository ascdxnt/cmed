package com.citamed.controller;

import com.citamed.domain.Rol;
import com.citamed.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/login")
public class AuthController {

    // Atiende el inicio y el cierre de sesión de los tres roles.
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public AuthController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String login() {
        return "/auth/login";
    }

    @PostMapping
    public String autenticar(@RequestParam String correo, @RequestParam String contrasena,
            HttpSession session, RedirectAttributes redirectAttributes) {
        // Las credenciales se validan antes de guardar los datos de sesión.
        var usuarioOpt = usuarioService.autenticar(correo, contrasena);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("login.error", null, Locale.getDefault()));
            return "redirect:/login";
        }

        var usuario = usuarioOpt.get();
        // Estos datos se reutilizan para identificar al usuario en las pantallas.
        session.setAttribute("idUsuario", usuario.getIdUsuario());
        session.setAttribute("rol", usuario.getRol().name());
        session.setAttribute("nombreUsuario", usuario.getNombre());

        // Cada rol ingresa directamente a su pantalla principal.
        if (usuario.getRol() == Rol.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        if (usuario.getRol() == Rol.DOCTOR) {
            return "redirect:/doctor/agenda";
        }
        return "redirect:/paciente";
    }

    @GetMapping("/salir")
    public String salir(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
