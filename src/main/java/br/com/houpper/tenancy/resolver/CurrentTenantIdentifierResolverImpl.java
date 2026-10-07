package br.com.houpper.tenancy.resolver;

import br.com.houpper.tenancy.context.TenantContext;
import br.com.houpper.tenancy.properties.TenancyProperties;
import lombok.RequiredArgsConstructor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Resolve o identificador do tenant utilizado pelo Hibernate.
 */
@RequiredArgsConstructor
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

    /**
     * Propriedades de configuração do multitenancy.
     */
    private final TenancyProperties properties;

    /**
     * Obtém o schema do tenant associado ao contexto atual.
     *
     * @return nome do schema do tenant ou {@code null} quando nenhum tenant estiver definido.
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.hasTenant()
                ? TenantContext.getSchemaName()
                : properties.getDefaultSchema();
    }

    /**
     * Indica se as sessões existentes devem ser validadas quando o tenant atual for alterado.
     *
     * @return {@code true} para validar o tenant associado às sessões existentes.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}