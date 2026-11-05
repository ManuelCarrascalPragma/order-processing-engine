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

## Ambientes (Profiles)

El perfil activo se resuelve dinamicamente y **cae en `dev`** si no se define
`SPRING_PROFILES_ACTIVE` (`spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}`).

| Perfil | Archivo                  | Base de datos                       | SQL visible | Secretos                                  |
|--------|--------------------------|-------------------------------------|-------------|-------------------------------------------|
| `dev`  | `application-dev.yml`    | H2 en memoria (o Postgres via vars) | si          | valores de prueba por defecto             |
| `prod` | `application-prod.yml`   | PostgreSQL (obligatoria via vars)   | no          | **obligatorios** via variables de entorno |

- `application.yml` contiene solo configuracion **transversal** (nombre, puerto,
  actuator) y el enrutador de perfiles.
- En `prod` **no hay valores quemados**: `url: ${DB_URL}`, `username: ${DB_USERNAME}`,
  `password: ${DB_PASSWORD}`, `secret-key: ${STRIPE_SECRET_KEY}`. Si falta una
  variable obligatoria, la aplicacion **no arranca** (fail-fast).

```bash
# dev (por defecto)
./gradlew bootRun

# prod (exige variables de entorno)
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://host:5432/orderdb \
DB_USERNAME=order_user DB_PASSWORD=*** \
STRIPE_SECRET_KEY=sk_live_*** \
./gradlew bootRun
```

## Logging (Logback)

Configurado en [`logback-spring.xml`](src/main/resources/logback-spring.xml):

- **ConsoleAppender** coloreado: fecha, nivel, hilo, clase de origen y mensaje.
- **RollingFileAppender** a `logs/order-engine.log` con `SizeAndTimeBasedRollingPolicy`:
  rota diariamente **o** al superar **10MB**, comprime a `.gz`, conserva 30 dias (tope 1GB).
- **Granularidad**: `root` en `INFO` (silencia el ruido de Spring/librerias) y el
  paquete de negocio `co.com.pragma.orderengine` en `DEBUG` (rastreo exclusivo de la
  logica propia).

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
