package com.bufete.backend.utils;

import com.bufete.backend.Dtos.usuario.CreateUsuarioRequest;
import com.bufete.backend.Dtos.usuario.UpdateUsuarioRequest;
import com.bufete.backend.Dtos.usuario.UsuarioDTO;
import com.bufete.backend.model.Rol;
import com.bufete.backend.model.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-24T12:25:37-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public UsuarioDTO toDTO(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setRolNombre( usuarioRolNombre( usuario ) );
        usuarioDTO.setRolId( usuarioRolId( usuario ) );
        usuarioDTO.setId( usuario.getId() );
        usuarioDTO.setNombre( usuario.getNombre() );
        usuarioDTO.setApellido( usuario.getApellido() );
        usuarioDTO.setEmail( usuario.getEmail() );
        usuarioDTO.setIdentificacion( usuario.getIdentificacion() );
        usuarioDTO.setTelefono( usuario.getTelefono() );
        usuarioDTO.setDireccion( usuario.getDireccion() );
        usuarioDTO.setActivo( usuario.getActivo() );
        usuarioDTO.setNuevoUsuario( usuario.getNuevoUsuario() );
        usuarioDTO.setCreatedAt( usuario.getCreatedAt() );
        usuarioDTO.setUpdatedAt( usuario.getUpdatedAt() );
        usuarioDTO.setLastLogin( usuario.getLastLogin() );

        return usuarioDTO;
    }

    @Override
    public List<UsuarioDTO> toDTOList(List<Usuario> usuarios) {
        if ( usuarios == null ) {
            return null;
        }

        List<UsuarioDTO> list = new ArrayList<UsuarioDTO>( usuarios.size() );
        for ( Usuario usuario : usuarios ) {
            list.add( toDTO( usuario ) );
        }

        return list;
    }

    @Override
    public Usuario toEntity(CreateUsuarioRequest request) {
        if ( request == null ) {
            return null;
        }

        Usuario.UsuarioBuilder usuario = Usuario.builder();

        usuario.nombre( request.getNombre() );
        usuario.apellido( request.getApellido() );
        usuario.email( request.getEmail() );
        usuario.identificacion( request.getIdentificacion() );
        usuario.contrasena( request.getContrasena() );
        usuario.telefono( request.getTelefono() );
        usuario.direccion( request.getDireccion() );

        return usuario.build();
    }

    @Override
    public void updateEntity(Usuario usuario, UpdateUsuarioRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getNombre() != null ) {
            usuario.setNombre( request.getNombre() );
        }
        if ( request.getApellido() != null ) {
            usuario.setApellido( request.getApellido() );
        }
        if ( request.getEmail() != null ) {
            usuario.setEmail( request.getEmail() );
        }
        if ( request.getIdentificacion() != null ) {
            usuario.setIdentificacion( request.getIdentificacion() );
        }
        if ( request.getTelefono() != null ) {
            usuario.setTelefono( request.getTelefono() );
        }
        if ( request.getDireccion() != null ) {
            usuario.setDireccion( request.getDireccion() );
        }
        if ( request.getActivo() != null ) {
            usuario.setActivo( request.getActivo() );
        }
    }

    private String usuarioRolNombre(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }
        Rol rol = usuario.getRol();
        if ( rol == null ) {
            return null;
        }
        String nombre = rol.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private Long usuarioRolId(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }
        Rol rol = usuario.getRol();
        if ( rol == null ) {
            return null;
        }
        Long id = rol.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
