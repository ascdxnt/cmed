package com.citamed.service;

import com.citamed.domain.Rol;
import com.citamed.domain.Usuario;
import com.citamed.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    // Reglas relacionadas con cuentas, acceso y contraseñas.
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        // Permite listar todos los usuarios o solamente los activos.
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String correo, String contrasena) {
        // Para ingresar, el correo debe existir y el usuario debe estar activo.
        var usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            var usuario = usuarioOpt.get();
            if (usuario.isActivo() && usuario.getContrasena().equals(protegerContrasena(contrasena))) {
                return usuarioOpt;
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuariosPorRol(Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuario.getIdUsuario() == null && usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalStateException("El correo electrónico ya está registrado.");
        }
        if (usuario.getIdUsuario() == null) {
            usuario.setContrasena(protegerContrasena(usuario.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public boolean cambiarContrasena(Integer idUsuario, String actual, String nueva, String confirmar) {
        // Se valida la contraseña actual y la confirmación antes de guardar.
        var usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        var usuario = usuarioOpt.get();
        if (!usuario.getContrasena().equals(protegerContrasena(actual)) || !nueva.equals(confirmar)) {
            return false;
        }
        usuario.setContrasena(protegerContrasena(nueva));
        usuarioRepository.save(usuario);
        return true;
    }

    private String protegerContrasena(String contrasena) {
        // La contraseña se guarda como un resumen y no como texto visible.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] resumen = digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", e);
        }
    }
}
