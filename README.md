# Proyecto Bancario — Guía completa (Parte I + II + III)

Sistema de microservicios bancarios en Java 17 / Spring Boot / Maven /
MongoDB / Redis / Kafka, con seguridad JWT, arquitectura orientada a
eventos y monedero móvil. Todo corre en Docker.

---

## 0. Mapa de servicios

| Servicio | Puerto | Rol |
|---|---|---|
| `ms-gateway` | 8080 | **Punto de entrada único** (enruta + valida JWT + circuit breaker) |
| `ms-auth` | 8095 | Login, emite JWT |
| `ms-eureka` | 8761 | Registro y descubrimiento de servicios |
| `config-server` | 8888 | Configuración centralizada |
| `ms-customers` | 8090 | Clientes (personal/empresarial, perfiles VIP/PYME) — cachea con Redis |
| `ms-accounts` | 8091 | Cuentas (ahorro/corriente/plazo fijo), transferencias, tarjeta de débito, reportes |
| `ms-credits` | 8092 | Créditos y tarjetas de crédito, deuda vencida |
| `ms-yanki` | 8093 | Monedero móvil (envío de dinero por celular) |
| `ms-notifications` | 8096 | Consume eventos Kafka, simula notificaciones |
| `mongodb` | 27017 | Base de datos (una por microservicio) |
| `mongo-express` | 8081 | UI web para inspeccionar Mongo |
| `redis` | 6379 | Caché de `ms-customers` |
| `kafka` | 9092 | Broker de eventos (modo KRaft, sin Zookeeper) |
| `kafka-ui` | 8082 | UI web para inspeccionar tópicos de Kafka |

---

## 1. Instalación (requisitos previos)

Solo necesitas **Docker** y **Docker Compose** — todo el build de Java/Maven
ocurre dentro de contenedores, no necesitas Java ni Maven instalados
localmente.

```bash
docker --version          # Docker 24+ recomendado
docker compose version    # Compose v2
```

Descomprime el proyecto y ubícate en la carpeta raíz:

```bash
cd banco-parte1
ls
# checkstyle.xml  config-repo/  config-server/  docker-compose.yml
# ms-accounts/  ms-auth/  ms-credits/  ms-customers/  ms-eureka/
# ms-gateway/  ms-notifications/  ms-yanki/  postman/  README.md
```

---

## 2. Ejecución (build + arranque)

```bash
docker compose up --build
```

Esto construye las 9 imágenes (multi-stage: `maven:3.9-eclipse-temurin-17`
para compilar, `eclipse-temurin:17-jre-alpine` para ejecutar) y levanta los
14 contenedores. La primera vez tarda varios minutos (descarga de
dependencias Maven); las siguientes son mucho más rápidas por el caché de
capas de Docker.

Para correr en segundo plano:
```bash
docker compose up --build -d
```

Ver logs de un servicio puntual:
```bash
docker compose logs -f ms-accounts
```

**Orden de arranque:** Compose respeta `depends_on`, pero eso solo
garantiza el orden de *inicio de contenedor*, no que la app dentro ya esté
lista (Eureka y Config Server tardan ~10-15s en aceptar tráfico). Cada
microservicio de negocio tiene configurado `fail-fast` + `retry` (5
intentos cada 2s) para el Config Server, así que normalmente se auto-
recupera. Si alguno queda caído, reinícialo:
```bash
docker compose restart ms-accounts
```

**Verificación de que todo levantó bien:**
```bash
# Eureka debe listar ms-customers, ms-accounts, ms-credits, ms-yanki,
# ms-notifications, ms-gateway, ms-auth como UP
open http://localhost:8761        # o curl http://localhost:8761

# Health checks individuales
curl http://localhost:8090/actuator/health   # ms-customers
curl http://localhost:8091/actuator/health   # ms-accounts
curl http://localhost:8092/actuator/health   # ms-credits
curl http://localhost:8093/actuator/health   # ms-yanki
curl http://localhost:8095/actuator/health   # ms-auth
curl http://localhost:8096/actuator/health   # ms-notifications
```

Para detener todo:
```bash
docker compose down          # detiene y elimina contenedores
docker compose down -v       # ademas borra el volumen de datos de Mongo
```

---

## 3. Despliegue Docker (detalle de la arquitectura de contenedores)

Cada microservicio tiene su propio `Dockerfile` **multi-stage**:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build   # Etapa 1: compila con Maven
...
FROM eclipse-temurin:17-jre-alpine           # Etapa 2: solo el JRE + el jar
```

Esto mantiene las imágenes finales livianas (no cargan Maven ni el
código fuente, solo el `.jar` compilado).

Todos los contenedores comparten la red `banco-net` (bridge de Docker), lo
que les permite resolverse por nombre de servicio (`ms-customers`,
`mongodb`, `kafka`, etc.) sin exponer puertos entre ellos más que los
necesarios para que tú los inspecciones desde el host.

**Persistencia:** solo MongoDB usa un volumen nombrado (`mongo-data`), así
que los datos sobreviven a `docker compose down` (pero no a `down -v`).
Redis y Kafka no persisten en esta configuración de desarrollo.

**Variables de entorno clave** (ya configuradas en `docker-compose.yml`,
documentadas aquí para referencia si despliegas en otro entorno):

| Variable | Usada por | Propósito |
|---|---|---|
| `CONFIG_SERVER_URI` | todos | URL del Config Server |
| `EUREKA_URI` | todos | URL de registro de Eureka |
| `MONGODB_URI` | customers/accounts/credits/yanki | Cadena de conexión Mongo |
| `REDIS_HOST`, `REDIS_PORT` | ms-customers | Conexión a Redis |
| `KAFKA_BROKERS` | accounts/credits/notifications | Bootstrap servers de Kafka |
| `JWT_SECRET` | ms-gateway, ms-auth | Clave HMAC compartida para firmar/validar tokens |
| `CUSTOMERS_URL`, `CREDITS_URL`, `ACCOUNTS_URL` | accounts/credits/yanki | URLs lógicas (balanceadas por Eureka) para llamadas entre microservicios |

**Escalar un microservicio** (Eureka + Gateway ya soportan balanceo):
```bash
docker compose up --build --scale ms-accounts=3
```

---

## 4. Funcionalidad — flujo de prueba end-to-end

Todo el tráfico de negocio pasa por el **Gateway** (`http://localhost:8080`)
y requiere un **JWT** salvo el login. Importa
`postman/parte3.postman_collection.json` o sigue estos pasos con `curl`.

### 4.1 Login (obtener el token)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Respuesta: `{ "token": "eyJhbGciOi...", "tokenType": "Bearer", ... }`.
Guarda ese token; todas las siguientes peticiones necesitan el header
`Authorization: Bearer <token>`.

Usuarios de demostración (definidos en `ms-auth`, en memoria):
- `admin` / `admin123` (rol ADMIN)
- `cliente1` / `cliente123` (rol CLIENT)

### 4.2 Crear cliente, cuenta, y probar el caché (Redis)

```bash
TOKEN="pega-aqui-tu-token"

curl -X POST http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"documentNumber":"45678912","customerType":"PERSONAL","fullName":"Juan Perez","email":"juan@mail.com"}'
# copia el "id" de la respuesta -> CUSTOMER_ID

# Primera consulta: cache MISS (va a Mongo)
curl http://localhost:8080/api/v1/customers/$CUSTOMER_ID -H "Authorization: Bearer $TOKEN"
# Segunda consulta: cache HIT (responde desde Redis, revisa el log de ms-customers)
curl http://localhost:8080/api/v1/customers/$CUSTOMER_ID -H "Authorization: Bearer $TOKEN"
docker compose logs ms-customers | grep -i cache
```

### 4.3 Aperturar cuenta y ver el evento en Kafka

```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"customerId\":\"$CUSTOMER_ID\",\"accountType\":\"SAVINGS\",\"openingAmount\":100}"
# copia el "id" -> ACCOUNT_ID

curl -X POST http://localhost:8080/api/v1/accounts/$ACCOUNT_ID/deposits \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"amount":200,"description":"Deposito inicial"}'

# Verifica que ms-notifications recibio el evento
curl http://localhost:8080/api/v1/notifications -H "Authorization: Bearer $TOKEN"
# o visualmente en Kafka UI: http://localhost:8082 -> topic "account-movements"
```

### 4.4 Tarjeta de débito (cascada de cuentas)

```bash
curl -X POST http://localhost:8080/api/v1/debit-cards \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"customerId\":\"$CUSTOMER_ID\",\"primaryAccountId\":\"$ACCOUNT_ID\",\"associatedAccountIds\":[]}"
```

### 4.5 Tarjeta de crédito y regla de deuda vencida

```bash
curl -X POST http://localhost:8080/api/v1/cards \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"customerId\":\"$CUSTOMER_ID\",\"cardType\":\"PERSONAL\",\"creditLimit\":1000}"
# copia el "id" -> CARD_ID

# Simula que un proceso batch marca la deuda como vencida
curl -X PATCH "http://localhost:8080/api/v1/cards/$CARD_ID/overdue?overdue=true" \
  -H "Authorization: Bearer $TOKEN"

# Intenta aperturar OTRA cuenta: debe fallar con 409 por deuda vencida
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"customerId\":\"$CUSTOMER_ID\",\"accountType\":\"CHECKING\",\"openingAmount\":0}"
# -> 409 Conflict "El cliente tiene deuda vencida..."

# Revierte para seguir probando
curl -X PATCH "http://localhost:8080/api/v1/cards/$CARD_ID/overdue?overdue=false" \
  -H "Authorization: Bearer $TOKEN"
```

### 4.6 Yanki: enviar dinero por número de celular

```bash
# Registra el wallet del cliente (requiere una cuenta de debito ya creada)
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"phoneNumber\":\"987654321\",\"customerId\":\"$CUSTOMER_ID\",\"documentNumber\":\"45678912\",\"linkedAccountId\":\"$ACCOUNT_ID\"}"

# (Registra un segundo wallet de otro cliente/cuenta de la misma forma)

# Envia dinero solo con los numeros de celular
curl -X POST http://localhost:8080/api/v1/wallets/send \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"fromPhoneNumber":"987654321","toPhoneNumber":"912345678","amount":50}'
```

### 4.7 Sin token: debe rechazar con 401

```bash
curl -i http://localhost:8080/api/v1/customers
# -> HTTP/1.1 401 Unauthorized
```

---

## 5. Pruebas automatizadas

```bash
cd ms-customers && mvn test && cd ..
cd ms-accounts && mvn test && cd ..
cd ms-credits && mvn test && cd ..
cd ms-yanki && mvn test && cd ..

# Cobertura (JaCoCo)
open ms-accounts/target/site/jacoco/index.html

# Estilo de codigo (Checkstyle)
cd ms-accounts && mvn checkstyle:check
```

---

## 6. Resumen de qué implementa cada Parte

| Parte | Contenido |
|---|---|
| **I** | `ms-customers`, `ms-accounts`, `ms-credits` — CRUD de clientes, cuentas y créditos, arquitectura *database per service*, Config Server |
| **II** | `ms-eureka`, `ms-gateway`, circuit breaker (Resilience4j, timeout 2s), Checkstyle, JaCoCo, perfiles VIP/PYME, comisiones, transferencias, reportes con Streams, patrones Strategy/Factory |
| **III** | `ms-auth` (JWT), filtro de seguridad en el Gateway, Redis (caché en ms-customers), Kafka + `ms-notifications` (eventos asíncronos), tarjeta de débito con cascada, `ms-yanki` (monedero móvil), regla de deuda vencida |

## 7. Decisiones de diseño relevantes de la Parte III

- **JWT validado en el Gateway, no en cada microservicio:** todos comparten
  la misma clave HMAC (`JWT_SECRET`); el Gateway valida la firma una sola
  vez y propaga la identidad vía headers (`X-User-Name`, `X-User-Role`,
  `X-Customer-Id`), evitando que cada microservicio reimplemente la
  validación o dependa de llamar a `ms-auth` en cada petición.
- **Redis solo en `ms-customers`:** es el catálogo más consultado por los
  demás microservicios (cada apertura de cuenta/crédito lo consulta), por
  lo que es el punto de mayor beneficio para cachear.
- **Kafka es best-effort, no transaccional:** si Kafka está caído, el
  movimiento en `ms-accounts`/`ms-credits` igual se persiste; solo se
  pierde la notificación asíncrona, que es un efecto secundario.
- **Yanki no guarda saldo propio:** delega el movimiento real a la cuenta
  de débito vinculada en `ms-accounts`, reusando toda su lógica de
  comisiones/validaciones/eventos en vez de duplicarla.
- **Deuda vencida marcada manualmente vía PATCH:** en producción esto lo
  haría un job batch nocturno revisando fechas de vencimiento; para esta
  entrega se expone el endpoint directamente para poder probar la regla
  sin construir un scheduler completo.
