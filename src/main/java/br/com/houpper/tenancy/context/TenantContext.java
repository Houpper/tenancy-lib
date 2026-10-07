package br.com.houpper.tenancy.context;

import br.com.houpper.tenancy.model.TenantIdentifier;

/**
 * Mantém o tenant associado à thread atual.
 */
public final class TenantContext {

    /**
     * Armazena o identificador do tenant associado à thread atual.
     */
    private static final ThreadLocal<TenantIdentifier> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Construtor privado para impedir a instanciação da classe utilitária.
     */
    private TenantContext() {
    }

    /**
     * Define o tenant associado à thread atual.
     *
     * @param tenant identificador do tenant a ser associado ao contexto
     */
    public static void set(TenantIdentifier tenant) {

        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null.");
        }

        CURRENT_TENANT.set(tenant);
    }

    /**
     * Obtém o tenant associado à thread atual.
     *
     * @return identificador do tenant ou {@code null} quando nenhum tenant estiver definido.
     */
    public static TenantIdentifier get() {
        return CURRENT_TENANT.get();
    }

    /**
     * Obtém o código do tenant associado à thread atual.
     *
     * @return código do tenant ou {@code null} quando nenhum tenant estiver definido.
     */
    public static Integer getTenantCode() {

        TenantIdentifier tenant = get();

        if (tenant == null) {
            return null;
        }

        return tenant.tenantCode();
    }

    /**
     * Obtém o nome do schema do tenant associado à thread atual.
     *
     * @return nome do schema ou {@code null} quando nenhum tenant estiver definido.
     */
    public static String getSchemaName() {

        TenantIdentifier tenant = get();

        if (tenant == null) {
            return null;
        }

        return tenant.schemaName();
    }

    /**
     * Verifica se existe um tenant associado à thread atual.
     *
     * @return {@code true} quando houver um tenant definido, {@code false} caso contrário.
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Remove o tenant associado à thread atual.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}