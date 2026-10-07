package br.com.houpper.tenancy.model;

import lombok.Builder;

import java.util.UUID;

/**
 * Representa a identificação de um tenant.
 *
 * @param tenantID   identificador único do tenant.
 * @param tenantCode código numérico do tenant.
 * @param schemaName nome do schema do tenant.
 */
@Builder
public record TenantIdentifier(

        UUID tenantID,

        Integer tenantCode,

        String schemaName
) {
}