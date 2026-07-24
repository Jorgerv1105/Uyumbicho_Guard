package com.udla.uyumbichoguard.dto.response;

import com.udla.uyumbichoguard.model.Residente;
import com.udla.uyumbichoguard.model.enums.EstadoResidente;

import java.util.List;

public record ResidenteResponse(
        Long id,
        Long usuarioId,
        String nombresCompletos,
        String cedula,
        String telefonoContacto,
        String manzana,
        String numeroCasa,
        String direccionReferencia,
        EstadoResidente estado,
        List<VehiculoResponse> vehiculos
) {
    public static ResidenteResponse desde(Residente residente) {
        return new ResidenteResponse(
                residente.getId(),
                residente.getUsuario() != null ? residente.getUsuario().getId() : null,
                residente.getNombresCompletos(),
                residente.getCedula(),
                residente.getTelefonoContacto(),
                residente.getManzana(),
                residente.getNumeroCasa(),
                residente.getDireccionReferencia(),
                residente.getEstado(),
                residente.getVehiculos() != null
                        ? residente.getVehiculos().stream().map(VehiculoResponse::desde).toList()
                        : List.of()
        );
    }

    /** Versión sin la lista de vehículos, para evitar N+1 al listar
     * muchos residentes donde no se necesita el detalle de vehículos. */
    public static ResidenteResponse desdeSinVehiculos(Residente residente) {
        return new ResidenteResponse(
                residente.getId(),
                residente.getUsuario() != null ? residente.getUsuario().getId() : null,
                residente.getNombresCompletos(),
                residente.getCedula(),
                residente.getTelefonoContacto(),
                residente.getManzana(),
                residente.getNumeroCasa(),
                residente.getDireccionReferencia(),
                residente.getEstado(),
                List.of()
        );
    }
}