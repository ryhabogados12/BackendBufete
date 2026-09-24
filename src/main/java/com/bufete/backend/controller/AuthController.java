package com.bufete.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bufete.backend.Dtos.auth.JwtAuthResponseDTO;
import com.bufete.backend.Dtos.auth.LoginDTO;
import com.bufete.backend.Dtos.usuario.UsuarioDTO;
import com.bufete.backend.config.jwt.JwtTokenProvider;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    
    private final JwtTokenProvider jwtTokenProvider;

    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioService = usuarioService;
    }

    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDto){
        try {              
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDto.getEmail(), loginDto.getPassword()
                )
            );            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Generar JWT token
            String token = jwtTokenProvider.generateToken(authentication);            
            // Obtener datos del usuario
            Usuario user = usuarioService.getUsuarioByEmail(loginDto.getEmail());

            UsuarioDTO updatedLastLoginUsuario = usuarioService.updateLastLogin(user.getId());
            
            JwtAuthResponseDTO response = new JwtAuthResponseDTO(
                token, 
                updatedLastLoginUsuario.getEmail(), 
                updatedLastLoginUsuario.getRolNombre(),
                updatedLastLoginUsuario.getNombre() +" "+updatedLastLoginUsuario.getApellido()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Credenciales incorrectas", HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Error durante la autenticación", HttpStatus.UNAUTHORIZED);
        }
    }
}
