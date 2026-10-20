# Order Processing Engine

Microservicio de procesamiento de ordenes construido con **Arquitectura Hexagonal** siguiendo
los estandares de ingenieria backend de Pragma. Expone una API REST para el checkout y consulta
de ordenes, delegando el cobro a una pasarela de pago y persistiendo el resultado.

## Stack

- Java 21
- Spring Boot 3.3
- Gradle 9 (wrapper incluido)
- MapStruct (mapeo en compile-time)
- H2 (por defecto, en memoria) / preparado para PostgreSQL via variables de entorno

## Estructura (Hexagonal - estandar Pragma)

```
co.com.pragma.orderengine
├── OrderEngineApplication          # Punto de entrada Spring Boot
├── domain                          # Logica de negocio pura (sin frameworks)
│   ├── model                       # Entidades y value objects (Order, Money, OrderStatus)
│   │   └── exception               # Excepciones de dominio
│   ├── ports
│   │   ├── in                      # Puertos de entrada (ICheckoutOrderUseCase)
│   │   └── out                     # Puertos de salida (IOrderRepositoryPort, IChargeablePaymentGateway, ...)
│   └── usecases                    # Implementacion de casos de uso (CheckoutOrderUseCaseImpl)
├── infrastructure
│   ├── entry_points                # Adaptadores de entrada
│   │   └── web                     # Controller REST, DTOs, mapper web, manejo de errores (RFC 7807)
│   └── driven_adapters             # Adaptadores de salida
│       ├── payment                 # StripePaymentAdapter
│       └── persistence             # Adapter JPA, entidad, Spring Data repo, mapper de persistencia
└── application
    └── config                      # Cableado de beans (inyeccion de dependencias)
```

**Reglas de dependencia** (verificadas con ArchUnit):
- El dominio no depende de Spring ni de infraestructura ni de JPA.
- Los casos de uso son POJOs limpios, cableados como beans en `application/config`.
- La direccion de dependencias es siempre hacia adentro.

## Ejecucion

```bash
# Compilar y ejecutar todas las pruebas (unitarias + ArchUnit)
./gradlew test

# Levantar la aplicacion
./gradlew bootRun
```

Con Docker (build multi-stage, usuario non-root):

```bash
docker build -t order-processing-engine .
docker run -p 8080:8080 --env-file .env order-processing-engine
```

Las variables de entorno estan documentadas en [`.env.example`](.env.example).

## Endpoints

| Metodo | Ruta                          | Descripcion                         |
|--------|-------------------------------|-------------------------------------|
| POST   | `/api/v1/orders/checkout`     | Crea y procesa (cobra) una orden    |
| GET    | `/api/v1/orders/{id}`         | Consulta una orden por su id        |
| GET    | `/actuator/health`            | Health check                        |

Ejemplo de checkout:

```bash
curl -X POST http://localhost:8080/api/v1/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{"email":"client@example.com","amount":150.0}'
```

Los errores se devuelven en formato **RFC 7807 (Problem Details)**.

## Quality Gates

Ejecutados en el pipeline de CI (GitHub Actions) y disponibles localmente:

| Gate                | Comando                                      | Umbral                              |
|---------------------|----------------------------------------------|-------------------------------------|
| Pruebas             | `./gradlew test`                             | Todas en verde                      |
| Cobertura (JaCoCo)  | `./gradlew jacocoTestCoverageVerification`   | 70% global, 80% en `domain`         |
| Mutation (Pitest)   | `./gradlew pitest`                           | 60% mutation score sobre `domain`   |
| OWASP Dependency    | `./gradlew dependencyCheckAnalyze`           | Falla con CVSS >= 7                 |
| Arquitectura        | `./gradlew test` (ArchUnit)                  | Aislamiento de dominio              |

Verificacion completa:

```bash
./gradlew check
```
