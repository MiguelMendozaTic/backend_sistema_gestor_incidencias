# Documentacion Detallada del Proyecto

## 1. Descripcion general

`backend_sistema_gestor_incidencias` es un backend REST construido con Spring Boot que modela un sistema basico de gestion de incidencias. Su finalidad es permitir el registro de usuarios, la creacion de incidencias y la asignacion de esas incidencias a tecnicos responsables de su atencion.

Este proyecto representa una base academica o inicial para una mesa de ayuda. Actualmente se enfoca en la logica minima de negocio y en la exposicion de endpoints REST simples, sin integrar todavia una capa de persistencia real ni un esquema de seguridad.

## 2. Problema que busca resolver

En muchas organizaciones se presentan incidentes tecnicos relacionados con acceso a sistemas, hardware, software o procesos internos. Sin una plataforma centralizada, el seguimiento de estos casos suele hacerse de forma manual, desordenada o dispersa.

El proyecto propone una estructura basica para:

- Registrar usuarios dentro de distintas areas.
- Identificar roles como empleado, administrador o tecnico.
- Crear incidencias reportadas por usuarios.
- Asignar cada incidencia a un tecnico.
- Mantener un modelo preparado para incorporar seguimiento del caso.

## 3. Enfoque tecnico actual

La aplicacion utiliza Spring Boot como framework principal y sigue una organizacion por capas:

- `controller`: expone endpoints HTTP.
- `service`: contiene la logica basica de negocio y almacenamiento temporal.
- `model`: define las clases del dominio y enums del sistema.

El almacenamiento es temporal y se realiza con `ArrayList` en memoria. Los IDs se generan con `AtomicLong`, lo que permite simular identificadores autoincrementales sin base de datos.

## 4. Tecnologias y herramientas

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## 5. Arquitectura del proyecto

La estructura principal del codigo fuente es la siguiente:

```text
src/main/java/utp/edu/sistema_gestor_incidencias
|-- controller
|   |-- IncidenciaController.java
|   |-- SeguimientoController.java
|   `-- UsuarioController.java
|-- model
|   |-- Area.java
|   |-- Estado.java
|   |-- EstadoIncidencia.java
|   |-- Incidencia.java
|   |-- Rol.java
|   |-- Seguimiento.java
|   `-- Usuario.java
|-- service
|   |-- IncidenciaService.java
|   `-- UsuarioService.java
`-- SistemaGestorIncidenciasApplication.java
```

### 5.1 Capa de entrada

La clase `SistemaGestorIncidenciasApplication` es el punto de arranque de la aplicacion. Spring Boot se encarga de levantar el servidor embebido y detectar automaticamente controladores y servicios.

### 5.2 Capa de controladores

Los controladores reciben solicitudes HTTP, deserializan JSON a objetos Java y delegan la logica a los servicios.

Controladores presentes:

- `UsuarioController`
- `IncidenciaController`
- `SeguimientoController`

### 5.3 Capa de servicios

Los servicios almacenan y manipulan la informacion del sistema.

- `UsuarioService`
  - Crea usuarios.
  - Busca usuarios por ID.
  - Usa una lista en memoria y un generador incremental de IDs.

- `IncidenciaService`
  - Registra incidencias.
  - Permite modificar incidencias existentes.
  - Tambien trabaja con almacenamiento en memoria.

### 5.4 Capa de modelos

Los modelos representan la informacion del dominio:

- `Usuario`
- `Incidencia`
- `Seguimiento`
- `Rol`
- `Area`
- `Estado`
- `EstadoIncidencia`

## 6. Explicacion de las entidades

### 6.1 Usuario

Representa a una persona dentro del sistema. Puede ser quien reporta una incidencia o quien la atiende.

Campos:

- `id`: identificador unico.
- `nombre`: nombre del usuario.
- `correo`: correo electronico.
- `estado`: estado general del usuario (`activo`, `inactivo`).
- `rol`: rol funcional dentro del sistema.
- `area`: area organizacional a la que pertenece.

### 6.2 Incidencia

Representa un problema o caso reportado para ser atendido.

Campos:

- `id`: identificador unico.
- `titulo`: resumen breve de la incidencia.
- `descripcion`: detalle del problema.
- `estado`: estado funcional de la incidencia.
- `usuario`: usuario que registra o reporta el caso.
- `tecnico`: usuario asignado para atender el caso.

### 6.3 Seguimiento

Representa una anotacion o avance asociado a una incidencia. Aunque ya existe como clase, su uso aun no ha sido implementado completamente en la API.

Campos:

- `idSeguimiento`: identificador del seguimiento.
- `incidencia`: incidencia relacionada.
- `comentario`: observacion o avance.
- `fecha`: fecha del registro.
- `estado`: estado del seguimiento.

## 7. Enumeraciones del dominio

### 7.1 Rol

Define el tipo de usuario dentro del sistema:

- `admin`
- `empleado`
- `tecnicoNivel1`
- `tecnicoNivel2`
- `tecnicoNivel3`

### 7.2 Area

Define el area organizacional:

- `RecursosHumanos`
- `Administracion`
- `Contabilidad`
- `Gerencia`
- `Logistica`

### 7.3 Estado

Estado general aplicado actualmente a usuarios y seguimientos:

- `activo`
- `inactivo`

### 7.4 EstadoIncidencia

Define el estado operativo de la incidencia:

- `abierto`
- `cerrado`
- `pendiente`

## 8. Funcionamiento de la API

### 8.1 Gestion de usuarios

#### Crear usuario

Endpoint:

```text
POST /api/usuario
```

Funcion:

- Recibe un objeto `Usuario` en formato JSON.
- Genera un ID automaticamente.
- Guarda el usuario en memoria.
- Devuelve el usuario creado.

Ejemplo:

```json
{
  "nombre": "Maria Lopez",
  "correo": "maria@empresa.com",
  "estado": "activo",
  "rol": "empleado",
  "area": "RecursosHumanos"
}
```

#### Obtener usuario por ID

Endpoint:

```text
GET /api/usuario/{id}
```

Funcion:

- Busca un usuario dentro de la lista en memoria.
- Si lo encuentra, devuelve el objeto usuario.
- Si no existe, responde con estado `404`.

### 8.2 Gestion de incidencias

#### Crear incidencia

Endpoint:

```text
POST /api/incidencia
```

Funcion:

- Recibe una incidencia en formato JSON.
- Extrae el ID del usuario reportante y del tecnico.
- Verifica que ambos existan en `UsuarioService`.
- Reemplaza las referencias parciales por los objetos completos encontrados.
- Genera un ID para la incidencia.
- Guarda la incidencia en memoria.

Ejemplo:

```json
{
  "titulo": "No funciona la impresora",
  "descripcion": "La impresora del area de Contabilidad no responde",
  "estado": "abierto",
  "usuario": {
    "id": 1
  },
  "tecnico": {
    "id": 2
  }
}
```

#### Modificar incidencia

Endpoint:

```text
POST /api/incidencia/{id}
```

Funcion:

- Recibe una nueva version de la incidencia.
- Verifica nuevamente la existencia del usuario y el tecnico.
- Busca la incidencia existente por ID.
- Si la encuentra, la reemplaza dentro de la lista.

Observacion importante:

- Si la incidencia no existe, el servicio devuelve `null`.
- El controlador no transforma ese caso en una respuesta HTTP clara, por lo que la salida actual puede ser ambigua desde el cliente.

### 8.3 Seguimiento

Existe un `SeguimientoController` con la ruta base:

```text
/api/seguimiento
```

Sin embargo:

- No tiene endpoints implementados.
- Solo contiene una lista interna vacia.
- Actualmente no ofrece funcionalidad utilizable desde la API.

## 9. Logica de negocio implementada

La logica actual es simple pero funcional para pruebas iniciales:

- Los usuarios se crean con IDs autogenerados.
- Las incidencias se relacionan con usuarios existentes.
- Un tecnico tambien es tratado como un usuario del sistema.
- La modificacion de incidencias reemplaza el objeto completo almacenado por una nueva version.

No se han implementado aun:

- validaciones de campos obligatorios,
- restricciones por rol,
- historial de cambios,
- persistencia real,
- autenticacion de clientes,
- filtros o listados.

## 10. Persistencia y manejo de datos

El proyecto no usa base de datos ni repositorios JPA. Toda la informacion vive en memoria mientras la aplicacion esta encendida.

Esto implica que:

- al reiniciar la aplicacion, se pierde toda la informacion registrada;
- no existe integridad relacional real;
- no hay consultas avanzadas ni almacenamiento permanente;
- el sistema esta orientado a practica, prototipo o demostracion.

## 11. Configuracion actual

En `src/main/resources/application.properties` solo se define:

```properties
spring.application.name=sistema_gestor_incidencias
```

No existen configuraciones de:

- puerto personalizado,
- conexion a base de datos,
- perfiles de entorno,
- logs personalizados,
- seguridad.

## 12. Fortalezas del proyecto

- Tiene una estructura limpia y entendible para aprender Spring Boot.
- Separa responsabilidades basicas entre controlador, servicio y modelo.
- Modela un caso de uso real y comun en organizaciones.
- Es un punto de partida adecuado para evolucionar hacia una API mas completa.

## 13. Limitaciones tecnicas actuales

Estas son las principales restricciones observadas:

- No hay base de datos.
- No existe persistencia de datos.
- No se aplican validaciones con anotaciones como `@NotNull` o `@Email`.
- No hay manejo global de excepciones.
- No existen DTOs ni mapeos separados del modelo de dominio.
- No hay endpoints para listar, eliminar o filtrar recursos.
- El modulo de seguimiento no esta implementado funcionalmente.
- La comparacion de IDs en `UsuarioService` se hace con `==`, lo que no es la forma mas segura para objetos `Long`.
- La actualizacion de incidencias usa `POST` en vez de un metodo como `PUT` o `PATCH`.

## 14. Posibles mejoras

Para convertir este proyecto en una solucion mas robusta, convendria incorporar:

- Spring Data JPA y una base de datos como MySQL o PostgreSQL.
- Validaciones de entrada con Jakarta Validation.
- DTOs para requests y responses.
- Manejo global de errores con `@ControllerAdvice`.
- Endpoints REST mas completos: listar, eliminar, buscar y filtrar.
- Seguimiento real de incidencias con historial.
- Seguridad con Spring Security y control por roles.
- Documentacion de API con Swagger/OpenAPI.
- Pruebas unitarias y de integracion mas completas.

## 15. Casos de uso que cubre hoy

En su estado actual, el proyecto permite simular este flujo:

1. Registrar usuarios de distintas areas y roles.
2. Consultar un usuario por ID.
3. Registrar una incidencia asociando un usuario reportante y un tecnico.
4. Modificar una incidencia ya creada.

## 16. Conclusion

Este proyecto es un backend introductorio para un sistema gestor de incidencias. Su alcance actual es pequeno, pero la estructura ya refleja conceptos importantes del desarrollo backend con Spring Boot: controladores REST, servicios, modelos de dominio y manejo basico de datos.

Es especialmente util como base academica, prototipo o ejercicio de arquitectura en capas. Para uso real en produccion, aun requiere incorporar persistencia, validaciones, seguridad, pruebas y una API mas completa.
