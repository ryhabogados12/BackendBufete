package com.bufete.backend.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.PageResponse;
import com.bufete.backend.Dtos.usuario.CreateUsuarioRequest;
import com.bufete.backend.Dtos.usuario.EditUsuarioDTO;
import com.bufete.backend.Dtos.usuario.UpdateUsuarioRequest;
import com.bufete.backend.Dtos.usuario.UsuarioDTO;
import com.bufete.backend.model.Rol;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.RolRepository;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.utils.UsuarioMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UsuarioService implements UserDetailsService {
    
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    
    public UsuarioService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         UsuarioMapper usuarioMapper,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional(readOnly = true)
    public PageResponse<UsuarioDTO> getAllUsuarios(int page, int size, String sortBy, String sortDir,
                                                  String nombre, String apellido, String email, Boolean activo) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Usuario> usuariosPage = usuarioRepository.findAllActive(pageable);
        List<UsuarioDTO> usuariosDTOs = usuarioMapper.toDTOList(usuariosPage.getContent());
        
        return PageResponse.<UsuarioDTO>builder()
                .content(usuariosDTOs)
                .page(usuariosPage.getNumber())
                .size(usuariosPage.getSize())
                .totalElements(usuariosPage.getTotalElements())
                .totalPages(usuariosPage.getTotalPages())
                .first(usuariosPage.isFirst())
                .last(usuariosPage.isLast())
                .empty(usuariosPage.isEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> allUsuarios(){
        List<UsuarioDTO> usuarioDTOs = usuarioRepository.findAllActive();
        
        return usuarioDTOs;

    } 
    
    @Transactional(readOnly = true)
    public EditUsuarioDTO getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        EditUsuarioDTO edituser = new EditUsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getApellido(), usuario.getEmail(), usuario.getRol().getId(), usuario.getContrasena());
        return edituser;
    }
    
    public UsuarioDTO createUsuario(CreateUsuarioRequest request) {
        // Validaciones
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Ya existe un usuario con el email: " + request.getEmail());
        }
        
        if (request.getIdentificacion() != null && usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new ValidationException("Ya existe un usuario con la identificación: " + request.getIdentificacion());
        }
        
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + request.getRolId()));
        
        // Crear usuario
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setRol(rol);
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario creado: {} (ID: {})", savedUsuario.getEmail(), savedUsuario.getId());
        
        return usuarioMapper.toDTO(savedUsuario);
    }
    
    public UsuarioDTO updateUsuario(Long id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        
        // Validaciones
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Ya existe un usuario con el email: " + request.getEmail());
            }
        }
        
        if (request.getIdentificacion() != null && !request.getIdentificacion().equals(usuario.getIdentificacion())) {
            if (usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
                throw new ValidationException("Ya existe un usuario con la identificación: " + request.getIdentificacion());
            }
        }
        
        if (request.getRolId() != null && !request.getRolId().equals(usuario.getRol().getId())) {
            Rol rol = rolRepository.findById(request.getRolId())
                    .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + request.getRolId()));
            usuario.setRol(rol);
        }
        
        usuarioMapper.updateEntity(usuario, request);
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: {} (ID: {})", updatedUsuario.getEmail(), updatedUsuario.getId());
        
        return usuarioMapper.toDTO(updatedUsuario);
    }
    
    public void deleteUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        
        // Soft delete
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("Usuario desactivado: {} (ID: {})", usuario.getEmail(), usuario.getId());
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioDTO> getAbogados() {
        List<Usuario> abogados = usuarioRepository.findByRolNombreAndActivoTrue("ABOGADO");
        return usuarioMapper.toDTOList(abogados);
    }
    
    public UsuarioDTO changePassword(Long id, String oldPassword, String newPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (!passwordEncoder.matches(oldPassword, usuario.getContrasena())) {
            throw new ValidationException("La contrasena actual es incorrecta");
        }

        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuario.setNuevoUsuario(false);
        Usuario updatedUsuario = usuarioRepository.save(usuario);

        return usuarioMapper.toDTO(updatedUsuario);
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con Email: " + email));
        return usuario;
    }

    public UsuarioDTO updateLastLogin(Long id) {

        // Hora actual (UTC)
        Instant now = Instant.now();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setLastLogin(now);
        
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        log.info("Usuario loggeado: {} (ID: {})", updatedUsuario.getEmail(), updatedUsuario.getId());
        
        return usuarioMapper.toDTO(updatedUsuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRol().getNombre()));

        // Permisos del rol
        user.getRol().getPermisos()
                .forEach(permiso -> authorities.add(new SimpleGrantedAuthority(permiso.getNombre())));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getContrasena(),
                authorities);
    }
}