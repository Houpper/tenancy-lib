package br.com.houpper.tenancy.provider;

import br.com.houpper.tenancy.properties.TenancyProperties;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fornece conexões configuradas para o schema do tenant associado à requisição atual.
 */
@RequiredArgsConstructor
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    /**
     * Fonte de conexões da aplicação.
     */
    private final DataSource dataSource;

    /**
     * Propriedades de configuração do multitenancy.
     */
    private final TenancyProperties properties;

    /**
     * Obtém uma conexão sem associação a um tenant específico.
     *
     * @return conexão aberta.
     * @throws SQLException caso ocorra erro ao obter a conexão.
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Libera uma conexão sem associação a um tenant específico.
     *
     * @param connection conexão a ser liberada.
     * @throws SQLException caso ocorra erro ao fechar a conexão.
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Obtém uma conexão configurada para o schema do tenant informado.
     *
     * @param tenantIdentifier nome do schema do tenant.
     * @return conexão configurada para o schema informado.
     * @throws SQLException caso ocorra erro ao configurar a conexão.
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {

        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            throw new IllegalArgumentException("Tenant identifier was not provided.");
        }

        Connection connection = getAnyConnection();

        try {
            connection.setSchema(tenantIdentifier);
            return connection;

        } catch (SQLException ex) {

            try {
                connection.close();

            } catch (SQLException closeException) {
                ex.addSuppressed(closeException);
            }

            throw ex;
        }
    }

    /**
     * Libera a conexão utilizada pelo tenant.
     *
     * @param tenantIdentifier identificador do tenant.
     * @param connection       conexão a ser liberada.
     * @throws SQLException caso ocorra erro ao liberar a conexão.
     */
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {

        try (connection) {
            connection.setSchema(properties.getDefaultSchema());

        }
    }

    /**
     * Indica se o provedor suporta liberação agressiva de conexões.
     *
     * @return {@code false}.
     */
    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    /**
     * Indica se o provedor pode ser convertido para o tipo informado.
     *
     * @param unwrapType tipo solicitado.
     * @return {@code false}.
     */
    @Override
    public boolean isUnwrappableAs(@NonNull Class<?> unwrapType) {
        return false;
    }

    /**
     * Não suporta conversão para outros tipos.
     *
     * @param unwrapType tipo solicitado.
     * @param <T>        tipo esperado.
     * @return nunca retorna.
     * @throws UnsupportedOperationException quando invocado.
     */
    @Override
    public <T> T unwrap(@NonNull Class<T> unwrapType) {
        throw new UnsupportedOperationException("Unsupported unwrap type: " + unwrapType.getName());
    }
}