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
    date = "2025-11-14T16:57:51-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
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
        usuarioDTO.setActivo( usuario.getActivo() );
        usuarioDTO.setApellido( usuario.getApellido() );
        usuarioDTO.setCreatedAt( usuario.getCreatedAt() );
        usuarioDTO.setDireccion( usuario.getDireccion() );
        usuarioDTO.setEmail( usuario.getEmail() );
        usuarioDTO.setId( usuario.getId() );
        usuarioDTO.setIdentificacion( usuario.getIdentificacion() );
        usuarioDTO.setLastLogin( usuario.getLastLogin() );
        usuarioDTO.setNombre( usuario.getNombre() );
        usuarioDTO.setNuevoUsuario( usuario.getNuevoUsuario() );
        usuarioDTO.setTelefono( usuario.getTelefono() );
        usuarioDTO.setUpdatedAt( usuario.getUpdatedAt() );

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

        usuario.apellido( request.getApellido() );
        usuario.direccion( request.getDireccion() );
        usuario.email( request.getEmail() );
        usuario.identificacion( request.getIdentificacion() );
        usuario.nombre( request.getNombre() );
        usuario.telefono( request.getTelefono() );

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
