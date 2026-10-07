# Houpper Tenancy

Biblioteca responsável por fornecer a infraestrutura de **multitenancy baseada em schema** para aplicações Houpper.

A biblioteca integra o gerenciamento do tenant ao Hibernate, permitindo que cada tenant utilize um schema PostgreSQL independente dentro da mesma base de dados.

## Tecnologias

* Java 25
* Spring Boot 4.1.1
* Hibernate
* PostgreSQL
* Gradle

## Funcionalidades

* Resolução do tenant atual por requisição/thread.
* Isolamento dos dados por schema PostgreSQL.
* Integração automática com Hibernate.
* Configuração do schema padrão.
* Abstração do `DataSource` para conexões multi-tenant.
* Possibilidade de sobrescrever as implementações padrão.

## Instalação

Adicione a biblioteca como dependência da aplicação:

```groovy
dependencies {
    implementation 'br.com.houpper:tenancy-lib:1.0.0-SNAPSHOT'
}
```

Caso a biblioteca esteja publicada no GitHub Packages, configure o repositório:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/houpper/tenancy-lib")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }

    mavenCentral()
}
```

> Ajuste a versão conforme a versão publicada da biblioteca.

## Configuração

A biblioteca possui configuração automática e não exige `@Enable...` ou configuração manual do Hibernate.

Por padrão, o schema utilizado quando nenhum tenant estiver definido é `public`.

Para alterar esse comportamento:

```yaml
houpper:
  tenancy:
    default-schema: public
```

### Propriedades

| Propriedade                      | Padrão   | Descrição                                                                 |
|----------------------------------| -------- | ------------------------------------------------------------------------- |
| `houpper.tenancy.default-schema` | `public` | Schema utilizado quando não existe um tenant associado ao contexto atual. |

> O prefixo deve ser mantido exatamente como definido pela biblioteca.

## Identificação do Tenant

O tenant atual é representado por `TenantIdentifier`:

```java
TenantIdentifier tenant = TenantIdentifier.builder()
        .tenantID(tenantId)
        .tenantCode(tenantCode)
        .schemaName(schemaName)
        .build();
```

A aplicação deve associar o tenant ao contexto atual:

```java
TenantContext.set(tenant);
```

A partir desse momento, as operações Hibernate realizadas pela thread atual utilizarão o schema informado em `schemaName`.

### Exemplo

```java
TenantIdentifier tenant = TenantIdentifier.builder()
        .tenantID(UUID.fromString("7f4d7c8b-2a1f-4f7a-9f5a-123456789abc"))
        .tenantCode(1001)
        .schemaName("tenant_1001")
        .build();

TenantContext.set(tenant);
```

As operações Hibernate realizadas posteriormente utilizarão:

```text
tenant_1001
```

como schema PostgreSQL.

## Limpando o Contexto

O `TenantContext` utiliza `ThreadLocal`. Por isso, o contexto deve sempre ser removido ao final da execução.

A aplicação deve utilizar `try/finally`:

```java
TenantContext.set(tenant);

try {
    // Operações da requisição
} finally {
    TenantContext.clear();
}
```

Isso é especialmente importante em aplicações que utilizam pools de threads, como aplicações web.

## Passando o Tenant nas Requisições

A `tenancy-lib` não define como o tenant deve ser transportado entre as aplicações.

Essa responsabilidade pertence à aplicação ou à camada de autenticação.

Por exemplo, uma aplicação pode receber o identificador do tenant através de:

* JWT;
* Header HTTP;
* Subdomínio;
* Informações obtidas através do usuário autenticado;
* Outro mecanismo definido pela arquitetura.

### Exemplo utilizando Header

Uma aplicação pode receber:

```http
X-Tenant-ID: 7f4d7c8b-2a1f-4f7a-9f5a-123456789abc
```

Após validar o tenant, a aplicação cria o contexto:

```java
TenantIdentifier tenant = tenantService.findTenant(tenantId);

TenantContext.set(tenant);

try {
    // Processamento da requisição
} finally {
    TenantContext.clear();
}
```

A biblioteca então utiliza o `schemaName` desse tenant para configurar a conexão Hibernate.

## Integração com Autenticação

A biblioteca é independente da infraestrutura de autenticação.

Por exemplo, caso a aplicação utilize um token contendo o tenant:

```text
JWT
 └── tenantId
       ↓
Authentication / Filter
       ↓
TenantService
       ↓
TenantIdentifier
       ↓
TenantContext
       ↓
Hibernate
       ↓
Schema do tenant
```

A `tenancy-lib` não é responsável por validar o token ou determinar se o usuário possui acesso ao tenant.

Essa responsabilidade deve permanecer na camada de autenticação/autorização da aplicação.

## Schema Padrão

Quando não existe um tenant associado ao contexto:

```java
TenantContext.hasTenant()
```

retorna `false`.

Nesse cenário, o Hibernate utiliza o schema configurado em:

```yaml
houpper:
  tenancy:
    default-schema: public
```

Por padrão:

```text
public
```

Isso permite que operações que não pertencem a um tenant específico continuem utilizando o banco normalmente.

## Sobrescrevendo Implementações

Os componentes principais da biblioteca são registrados como beans condicionais.

Isso permite que uma aplicação forneça sua própria implementação quando necessário.

Por exemplo:

```java
@Bean
public CurrentTenantIdentifierResolver<String> tenantIdentifierResolver(...) {
    // implementação customizada
}
```

A implementação fornecida pela aplicação terá prioridade sobre a implementação padrão da biblioteca.

## Responsabilidades

### tenancy-lib

A biblioteca é responsável por:

* manter o tenant atual;
* resolver o tenant para o Hibernate;
* configurar a conexão para o schema correto;
* integrar o multitenancy ao Hibernate;
* fornecer configurações padrão.

### Aplicação

A aplicação é responsável por:

* identificar o tenant da requisição;
* validar o acesso ao tenant;
* construir o `TenantIdentifier`;
* definir o `TenantContext`;
* limpar o contexto ao final da requisição.

### Auth/Security

A camada de autenticação e autorização é responsável por:

* autenticar o usuário;
* validar o token;
* identificar o usuário;
* determinar quais tenants o usuário pode acessar.

## Arquitetura

```text
┌──────────────────────────────┐
│          Requisição          │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│      Authentication/Security │
│                              │
│      Identifica Tenant       │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│       TenantContext          │
│                              │
│       TenantIdentifier       │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ CurrentTenantIdentifier      │
│ Resolver                     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│           Hibernate          │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│ SchemaMultiTenantConnection  │
│ Provider                     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│         PostgreSQL           │
│                              │
│  public                     │
│  tenant_1001                │
│  tenant_1002                │
│  tenant_1003                │
└──────────────────────────────┘
```

## Boas Práticas

### Sempre limpar o contexto

Nunca mantenha o tenant definido após o processamento da requisição:

```java
try {
    TenantContext.set(tenant);

    // processamento
} finally {
    TenantContext.clear();
}
```

### Não confiar diretamente em dados externos

O `schemaName` não deve ser utilizado diretamente a partir de um header ou outro dado externo sem validação.

O tenant deve ser identificado e validado pela aplicação antes de ser colocado no `TenantContext`.

### Não misturar responsabilidades

A `tenancy-lib` não deve conter regras relacionadas a:

* usuários;
* permissões;
* autenticação;
* autorização;
* JWT;
* clientes;
* regras específicas do negócio.

Essas responsabilidades pertencem às respectivas aplicações ou bibliotecas.

## Estrutura

```text
src/
└── main/
    └── java/
        └── br.com.houpper.tenancy/
            ├── configuration/
            │   └── TenancyAutoConfiguration
            │
            ├── context/
            │   └── TenantContext
            │
            ├── model/
            │   └── TenantIdentifier
            │
            ├── properties/
            │   └── TenancyProperties
            │
            ├── provider/
            │   └── SchemaMultiTenantConnectionProvider
            │
            └── resolver/
                └── CurrentTenantIdentifierResolverImpl
```

## Licença

Projeto privado da plataforma Houpper.
