package br.com.houpper.tenancy.configuration;

import br.com.houpper.tenancy.properties.TenancyProperties;
import br.com.houpper.tenancy.provider.SchemaMultiTenantConnectionProvider;
import br.com.houpper.tenancy.resolver.CurrentTenantIdentifierResolverImpl;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Configuração automática da infraestrutura de multitenancy.
 */
@AutoConfiguration
@EnableConfigurationProperties(TenancyProperties.class)
@ConditionalOnClass({
        MultiTenantConnectionProvider.class,
        CurrentTenantIdentifierResolver.class,
        HibernatePropertiesCustomizer.class,
        DataSource.class
})
public class TenancyAutoConfiguration {

    /**
     * Cria o resolvedor do tenant atual.
     *
     * @param properties propriedades de multitenancy.
     * @return resolvedor do tenant.
     */
    @Bean
    @ConditionalOnMissingBean
    public CurrentTenantIdentifierResolver<String> tenantIdentifierResolver(TenancyProperties properties) {
        return new CurrentTenantIdentifierResolverImpl(properties);
    }

    /**
     * Cria o provedor de conexões multi-tenant.
     *
     * @param dataSource fonte de dados.
     * @param properties propriedades de multitenancy.
     * @return provedor de conexões.
     */
    @Bean
    @ConditionalOnMissingBean
    public MultiTenantConnectionProvider<String> multiTenantConnectionProvider(
            DataSource dataSource, TenancyProperties properties) {
        return new SchemaMultiTenantConnectionProvider(dataSource, properties);
    }

    /**
     * Configura a integração do multitenancy com o Hibernate.
     *
     * @param connectionProvider provedor de conexões.
     * @param tenantResolver     resolvedor do tenant.
     * @return customizador das propriedades do Hibernate.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            MultiTenantConnectionProvider<String> connectionProvider,
            CurrentTenantIdentifierResolver<String> tenantResolver
    ) {
        return hibernateProperties -> {
            hibernateProperties.put(
                    MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER,
                    connectionProvider
            );

            hibernateProperties.put(
                    MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                    tenantResolver
            );
        };
    }
}