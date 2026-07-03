package com.bufete.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bufete.backend.Dtos.PageResponse;
import com.bufete.backend.Dtos.cliente.ClienteConsultarDTO;
import com.bufete.backend.Dtos.cliente.ClienteDTO;
import com.bufete.backend.Dtos.cliente.CreateClienteRequest;
import com.bufete.backend.model.Cliente;
import com.bufete.backend.model.Usuario;
import com.bufete.backend.repository.ClienteRepository;
import com.bufete.backend.repository.UsuarioRepository;
import com.bufete.backend.utils.ClienteMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ClienteService {
    
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;
    
    public ClienteService(ClienteRepository clienteRepository,
                         UsuarioRepository usuarioRepository,
                         ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteMapper = clienteMapper;
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
        return usuario.getId();
    }
    
    @Transactional(readOnly = true)
    public PageResponse<ClienteDTO> getAllClientes(int page, int size, String sortBy, String sortDir,
                                                  String nombre, String identificacion, 
                                                  Cliente.TipoCliente tipoCliente, Boolean activo) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Cliente> clientesPage = clienteRepository.findAllActive(pageable);
        List<ClienteDTO> clientesDTOs = clienteMapper.toDTOList(clientesPage.getContent());
        
        return PageResponse.<ClienteDTO>builder()
                .content(clientesDTOs)
                .page(clientesPage.getNumber())
                .size(clientesPage.getSize())
                .totalElements(clientesPage.getTotalElements())
                .totalPages(clientesPage.getTotalPages())
                .first(clientesPage.isFirst())
                .last(clientesPage.isLast())
                .empty(clientesPage.isEmpty())
                .build();
    }
    
    @Transactional(readOnly = true)
    public ClienteDTO getClienteById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
        return clienteMapper.toDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteConsultarDTO getClienteByIdentificacion(String identificacion) {
        Cliente cliente = clienteRepository.findByIdentificacion(identificacion)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con identificación: " + identificacion));

        
        ClienteConsultarDTO clienteDTO = new ClienteConsultarDTO();
        clienteDTO.setNombre(cliente.getNombre());
        clienteDTO.setApellido(cliente.getApellido());
        clienteDTO.setIdentificacion(cliente.getIdentificacion());
        clienteDTO.setTipoDocumento(cliente.getTipoDocumento());

        return clienteDTO;
    }
    
    public ClienteDTO createCliente(CreateClienteRequest request, Long createdById) {
        // Validaciones
        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new ValidationException("Ya existe un cliente con la identificación: " + request.getIdentificacion());
        }
        
        Usuario createdBy = usuarioRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + createdById));
        
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setCreatedBy(createdBy);
        
        Cliente savedCliente = clienteRepository.save(cliente);
        log.info("Cliente creado: {} (ID: {})", savedCliente.getNombre(), savedCliente.getId());
        
        return clienteMapper.toDTO(savedCliente);
    }
    
    public ClienteDTO updateCliente(Long id, CreateClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
        
        // Validar identificación si cambió
        if (!request.getIdentificacion().equals(cliente.getIdentificacion())) {
            if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
                throw new ValidationException("Ya existe un cliente con la identificación: " + request.getIdentificacion());
            }
        }
        
        clienteMapper.updateEntity(cliente, request);
        Cliente updatedCliente = clienteRepository.save(cliente);
        log.info("Cliente actualizado: {} (ID: {})", updatedCliente.getNombre(), updatedCliente.getId());
        
        return clienteMapper.toDTO(updatedCliente);
    }
    
    public void deleteCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
        
        // Soft delete
        cliente.setActivo(false);
        clienteRepository.save(cliente);
        log.info("Cliente desactivado: {} (ID: {})", cliente.getNombre(), cliente.getId());
    }
}