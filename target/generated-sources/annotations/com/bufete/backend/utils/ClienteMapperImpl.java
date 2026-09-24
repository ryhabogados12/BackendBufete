package com.bufete.backend.utils;

import com.bufete.backend.Dtos.cliente.ClienteDTO;
import com.bufete.backend.Dtos.cliente.CreateClienteRequest;
import com.bufete.backend.model.Cliente;
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
public class ClienteMapperImpl implements ClienteMapper {

    @Override
    public ClienteDTO toDTO(Cliente cliente) {
        if ( cliente == null ) {
            return null;
        }

        ClienteDTO.ClienteDTOBuilder clienteDTO = ClienteDTO.builder();

        clienteDTO.createdByNombre( clienteCreatedByNombre( cliente ) );
        clienteDTO.id( cliente.getId() );
        clienteDTO.tipoCliente( tipoClienteToTipoCliente( cliente.getTipoCliente() ) );
        clienteDTO.nombre( cliente.getNombre() );
        clienteDTO.apellido( cliente.getApellido() );
        clienteDTO.razonSocial( cliente.getRazonSocial() );
        clienteDTO.identificacion( cliente.getIdentificacion() );
        clienteDTO.tipoDocumento( cliente.getTipoDocumento() );
        clienteDTO.email( cliente.getEmail() );
        clienteDTO.telefono( cliente.getTelefono() );
        clienteDTO.direccion( cliente.getDireccion() );
        clienteDTO.ciudad( cliente.getCiudad() );
        clienteDTO.departamento( cliente.getDepartamento() );
        clienteDTO.pais( cliente.getPais() );
        clienteDTO.fechaNacimiento( cliente.getFechaNacimiento() );
        clienteDTO.representanteLegal( cliente.getRepresentanteLegal() );
        clienteDTO.activo( cliente.getActivo() );
        clienteDTO.observaciones( cliente.getObservaciones() );
        clienteDTO.createdAt( cliente.getCreatedAt() );
        clienteDTO.updatedAt( cliente.getUpdatedAt() );

        return clienteDTO.build();
    }

    @Override
    public List<ClienteDTO> toDTOList(List<Cliente> clientes) {
        if ( clientes == null ) {
            return null;
        }

        List<ClienteDTO> list = new ArrayList<ClienteDTO>( clientes.size() );
        for ( Cliente cliente : clientes ) {
            list.add( toDTO( cliente ) );
        }

        return list;
    }

    @Override
    public Cliente toEntity(CreateClienteRequest request) {
        if ( request == null ) {
            return null;
        }

        Cliente.ClienteBuilder cliente = Cliente.builder();

        cliente.tipoCliente( tipoClienteToTipoCliente1( request.getTipoCliente() ) );
        cliente.nombre( request.getNombre() );
        cliente.apellido( request.getApellido() );
        cliente.razonSocial( request.getRazonSocial() );
        cliente.identificacion( request.getIdentificacion() );
        cliente.tipoDocumento( request.getTipoDocumento() );
        cliente.email( request.getEmail() );
        cliente.telefono( request.getTelefono() );
        cliente.direccion( request.getDireccion() );
        cliente.ciudad( request.getCiudad() );
        cliente.departamento( request.getDepartamento() );
        cliente.pais( request.getPais() );
        cliente.fechaNacimiento( request.getFechaNacimiento() );
        cliente.representanteLegal( request.getRepresentanteLegal() );
        cliente.observaciones( request.getObservaciones() );

        return cliente.build();
    }

    @Override
    public void updateEntity(Cliente cliente, CreateClienteRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getTipoCliente() != null ) {
            cliente.setTipoCliente( tipoClienteToTipoCliente1( request.getTipoCliente() ) );
        }
        if ( request.getNombre() != null ) {
            cliente.setNombre( request.getNombre() );
        }
        if ( request.getApellido() != null ) {
            cliente.setApellido( request.getApellido() );
        }
        if ( request.getRazonSocial() != null ) {
            cliente.setRazonSocial( request.getRazonSocial() );
        }
        if ( request.getIdentificacion() != null ) {
            cliente.setIdentificacion( request.getIdentificacion() );
        }
        if ( request.getTipoDocumento() != null ) {
            cliente.setTipoDocumento( request.getTipoDocumento() );
        }
        if ( request.getEmail() != null ) {
            cliente.setEmail( request.getEmail() );
        }
        if ( request.getTelefono() != null ) {
            cliente.setTelefono( request.getTelefono() );
        }
        if ( request.getDireccion() != null ) {
            cliente.setDireccion( request.getDireccion() );
        }
        if ( request.getCiudad() != null ) {
            cliente.setCiudad( request.getCiudad() );
        }
        if ( request.getDepartamento() != null ) {
            cliente.setDepartamento( request.getDepartamento() );
        }
        if ( request.getPais() != null ) {
            cliente.setPais( request.getPais() );
        }
        if ( request.getFechaNacimiento() != null ) {
            cliente.setFechaNacimiento( request.getFechaNacimiento() );
        }
        if ( request.getRepresentanteLegal() != null ) {
            cliente.setRepresentanteLegal( request.getRepresentanteLegal() );
        }
        if ( request.getObservaciones() != null ) {
            cliente.setObservaciones( request.getObservaciones() );
        }
    }

    private String clienteCreatedByNombre(Cliente cliente) {
        if ( cliente == null ) {
            return null;
        }
        Usuario createdBy = cliente.getCreatedBy();
        if ( createdBy == null ) {
            return null;
        }
        String nombre = createdBy.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    protected ClienteDTO.TipoCliente tipoClienteToTipoCliente(Cliente.TipoCliente tipoCliente) {
        if ( tipoCliente == null ) {
            return null;
        }

        ClienteDTO.TipoCliente tipoCliente1;

        switch ( tipoCliente ) {
            case NATURAL: tipoCliente1 = ClienteDTO.TipoCliente.NATURAL;
            break;
            case JURIDICA: tipoCliente1 = ClienteDTO.TipoCliente.JURIDICA;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + tipoCliente );
        }

        return tipoCliente1;
    }

    protected Cliente.TipoCliente tipoClienteToTipoCliente1(ClienteDTO.TipoCliente tipoCliente) {
        if ( tipoCliente == null ) {
            return null;
        }

        Cliente.TipoCliente tipoCliente1;

        switch ( tipoCliente ) {
            case NATURAL: tipoCliente1 = Cliente.TipoCliente.NATURAL;
            break;
            case JURIDICA: tipoCliente1 = Cliente.TipoCliente.JURIDICA;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + tipoCliente );
        }

        return tipoCliente1;
    }
}
