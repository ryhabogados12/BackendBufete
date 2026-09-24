package com.bufete.backend.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bufete.backend.Dtos.ApiResponse;
import com.bufete.backend.Dtos.juridico.CatalogoDTO;
import com.bufete.backend.service.CatalogoJuridicoService;

@RestController
@RequestMapping("/api")
public class CatalogoJuridicoController {
    private final CatalogoJuridicoService service;
    public CatalogoJuridicoController(CatalogoJuridicoService service) { this.service = service; }

    @GetMapping("/jurisdicciones")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> jurisdicciones() {
        return ResponseEntity.ok(ApiResponse.success(service.jurisdicciones(), "Jurisdicciones obtenidas"));
    }

    @GetMapping("/jurisdicciones/{id}/asuntos")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> asuntos(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.asuntos(id), "Asuntos obtenidos"));
    }

    @GetMapping("/asuntos/{id}/etapas")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> etapas(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.etapas(), "Etapas obtenidas"));
    }

    @GetMapping("/asuntos/{id}/procedimientos")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> procedimientos(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(service.procedimientos(id), "Procedimientos obtenidos"));
    }

    @GetMapping("/delitos")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> delitos() {
        return ResponseEntity.ok(ApiResponse.success(service.delitos(), "Delitos obtenidos"));
    }

    @GetMapping("/bienes-juridicos")
    public ResponseEntity<ApiResponse<List<CatalogoDTO>>> bienesJuridicos() {
        return ResponseEntity.ok(ApiResponse.success(service.bienesJuridicos(), "Bienes jurídicos obtenidos"));
    }
}