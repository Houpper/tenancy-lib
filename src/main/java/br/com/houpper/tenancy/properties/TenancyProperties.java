package br.com.houpper.tenancy.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração do multitenancy.
 */
@Data
@ConfigurationProperties(
        prefix = "houpper.tenancy"
)
public class TenancyProperties {

    /**
     * Schema padrão utilizado pelo multitenancy.
     */
    private String defaultSchema = "public";
}