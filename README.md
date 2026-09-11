# DigitalFix - Microservicio Workorders

Microservicio encargado de la gestión de órdenes de trabajo del proyecto **DigitalFix**.

## Integrantes

- Sebastián Ahumada
- Benjamín Gutiérrez

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- Jakarta Validation
- Maven
- Oracle Database
- Oracle JDBC
- Docker

## Funcionalidades implementadas

El microservicio permite:

- Crear órdenes de trabajo.
- Listar órdenes.
- Consultar una orden por su identificador.
- Filtrar órdenes por estado.
- Consultar órdenes por rango de fechas.
- Actualizar el estado de una orden.
- Validar transiciones de estado.
- Persistir información en Oracle Database.

## Estados de una orden

```text
CREADA
ASIGNADA
EN_DESPLAZAMIENTO
EN_EJECUCION
CERRADA
CANCELADA
````

## Transiciones permitidas

Flujo principal:

```text
CREADA
  ↓
ASIGNADA
  ↓
EN_DESPLAZAMIENTO
  ↓
EN_EJECUCION
  ↓
CERRADA
```

Una orden también puede pasar a:

```text
CANCELADA
```

desde un estado no finalizado.

Para pasar de:

```text
CREADA → ASIGNADA
```

la orden debe tener un técnico asignado.

## Puerto

El microservicio utiliza el puerto:

```text
8080
```

## Persistencia

El microservicio utiliza Oracle Database para almacenar las órdenes de trabajo.

La conexión se configura mediante propiedades y variables de entorno, evitando almacenar credenciales directamente en el repositorio.

El esquema utilizado corresponde al dominio Workorders de DigitalFix.

## Endpoints principales

```text
GET    /workorders
GET    /workorders/{id}
POST   /workorders
PUT    /workorders/{id}/status
```

Las solicitudes externas no llegan directamente a este microservicio.

El flujo utilizado es:

```text
Frontend
→ AWS API Gateway
→ BFF
→ Workorders
→ Oracle Database
```

## Arquitectura

```text
Angular
   |
   v
AWS API Gateway
   |
   v
DigitalFix BFF
   |
   v
Workorders :8080
   |
   v
Oracle Database
```

## Docker

El proyecto incluye un:

```text
Dockerfile
```

El microservicio es ejecutado como contenedor Docker dentro de la infraestructura de DigitalFix.

Nombre del contenedor utilizado:

```text
digitalfix-workorders
```

## Compilación

En Windows:

```powershell
.\mvnw.cmd clean package
```

También puede compilarse mediante:

```powershell
.\mvnw.cmd compile
```

## Ejecución local

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux:

```bash
./mvnw spring-boot:run
```

## Flujo de trabajo Git

El proyecto utiliza las ramas:

```text
main
development
feature/*
```

Flujo utilizado:

```text
feature/*
   ↓
Pull Request
   ↓
development
   ↓
Pull Request de release
   ↓
main
```

Cada funcionalidad se desarrolla en una rama independiente y luego se integra mediante Pull Request.

## Estado del proyecto

Actualmente se encuentran implementados y validados:

* Creación de órdenes.
* Consulta de órdenes.
* Listado de órdenes.
* Actualización de estados.
* Validación de transiciones.
* Persistencia en Oracle Database.
* Comunicación con el BFF.
* Ejecución mediante Docker.
* Despliegue en AWS EC2.
