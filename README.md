# DigitalFix — Microservicio Workorders

Microservicio de gestión de órdenes de trabajo del proyecto DigitalFix.

## Integrantes

- Sebastián Ahumada
- Benjamín Gutiérrez

## Responsabilidades de este microservicio

- Estructura inicial e implementación: Sebastián Ahumada.
- Revisión de pull requests: Benjamín Gutiérrez.

## Funcionalidades implementadas

- Crear órdenes de trabajo.
- Listar órdenes con filtros por estado y rango de fechas.
- Consultar una orden por su identificador.
- Actualizar el estado de una orden validando las transiciones permitidas.

## Tecnologías

- Java 21
- Spring Boot
- Maven
- Spring Data JPA
- Jakarta Validation
- Driver JDBC de Oracle

## Compilación en Windows

Desde la carpeta principal del proyecto:

```powershell
.\mvnw.cmd compile
```

## Estado actual

La compilación fue comprobada desde IntelliJ.
La conexión a Oracle y las pruebas de funcionamiento de los endpoints
están pendientes de configurar y verificar.

## Flujo de trabajo

Cada tarea se registra en un issue y se desarrolla en una rama propia
creada desde development. Los cambios se integran mediante pull request
con revisión del otro integrante.