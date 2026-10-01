# Casos de Prueba de Incidente

Este documento describe casos de prueba funcionales(12 casos) para los endpoints implementados en el modulo de incidencia.

Base URL local:

```text
http://localhost:8080
```

## Endpoint: Crear incidencia

Ruta:

```text
POST /api/incidencia
```

### Caso 1. Crear incidencia con datos validos

- Entrada:

```json
{
  "id": 1,
  "titulo": "PC no enciende",
  "descripcion": "El equipo no responde al inicio",
  "estado": "ABIERTO"
}
```

- Salida esperada:
  - Codigo HTTP `201 Created`.
  - Se devuelve un objeto con `id = 1`, `titulo = "PC no enciende"` y `estado = "ABIERTO"`.
- Verificacion:
  - Confirmar que la respuesta contiene el titulo enviado.
  - Confirmar que `id` exista y sea numerico.
  - Confirmar que el estado inicial es `ABIERTO`.
- Rol requerido: `EMPLEADO`.

### Caso 2. Crear incidencia con usuario no encontrado

- Entrada:

```json
{
  "usuario": {
    "id": 99
  }
}
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
  - Mensaje: `El usuario no encontrado`.
- Verificacion:
  - Confirmar que no se crea ninguna incidencia.
  - Confirmar que el cuerpo de respuesta coincide con el mensaje de error esperado.
- Rol requerido: `EMPLEADO`.

## Endpoint: Modificar incidencia

Ruta:

```text
PUT /api/incidencia/{id}
```

### Caso 1. Modificar incidencia existente con datos validos

- Precondicion:
  - Debe existir una incidencia con ID `1`.
  - Deben existir los usuarios referenciados.
- Entrada:

```json
{
  "id": 1,
  "titulo": "PC no enciende",
  "descripcion": "El equipo no responde al inicio",
  "estado": "PENDIENTE"
}
```

- URL de prueba:

```text
PUT /api/incidencia/1
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve la incidencia actualizada con `estado = "PENDIENTE"`.
- Verificacion:
  - Confirmar que `estado` cambia al nuevo valor.
  - Confirmar que los usuarios referenciados siguen siendo validos.
- Rol requerido: `ADMIN`.

### Caso 2. Modificar incidencia con usuario no encontrado

- Entrada:

```json
{
  "id": 1,
  "titulo": "PC no enciende",
  "descripcion": "El equipo prende, pero demora como 5 minutos en iniciar sesion",
  "usuario": {
    "id": 99
  }
}
```

- URL de prueba:

```text
PUT /api/incidencia/1
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
  - Mensaje: `Usuario no encontrado con ese ID`.
- Verificacion:
  - Confirmar que la API no actualiza la incidencia.
  - Confirmar que la validacion de usuario se ejecuta antes de persistir cambios.
- Rol requerido: `ADMIN`.

## Endpoint: Listar todas las incidencias

Ruta:

```text
GET /api/incidencia
```

### Caso 1. Listar incidencias exitosamente

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con al menos una incidencia.
  - El primer elemento tiene `titulo = "PC no enciende"`.
- Verificacion:
  - Confirmar que `$.length() = 1`.
  - Confirmar que `$[0].titulo` tiene el valor esperado.
- Rol requerido: `ADMIN`.

### Caso 2. Listar incidencias sin permisos (403 Forbidden)

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede listar todas las incidencias.

## Endpoint: Mis incidencias

Ruta:

```text
GET /api/incidencia/misIncidencias
```

### Caso 1. Obtener mis incidencias exitosamente

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con 1 elemento.
  - El primer elemento tiene `id = 1` y `titulo = "PC no enciende"`.
- Verificacion:
  - Confirmar que `$.length() = 1`.
  - Confirmar que `$[0].id = 1`.
  - Confirmar que `$[0].titulo = "PC no enciende"`.
- Rol requerido: `EMPLEADO`.

### Caso 2. Obtener mis incidencias sin permisos (403 Forbidden)

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `INVITADO` no puede acceder a sus incidencias.

## Endpoint: Incidencias paginadas

Ruta:

```text
GET /api/incidencia/paginado
```

Parametros:
- `page`: numero de pagina (default 0)
- `size`: cantidad por pagina (default 5)

### Caso 1. Obtener pagina de incidencias exitosamente

- Parametros de prueba:
  - `page = 0`
  - `size = 5`
- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve un objeto con `content` que contiene 1 elemento.
  - `content[0].titulo = "PC no enciende"`.
- Verificacion:
  - Confirmar que `$.content.length() = 1`.
  - Confirmar que el titulo del primer elemento coincide.
- Rol requerido: `ADMIN`.

### Caso 2. Obtener pagina de incidencias sin permisos (403 Forbidden)

- Parametros de prueba:
  - `page = 0`
  - `size = 5`
- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede acceder a incidencias paginadas.

## Endpoint: Obtener incidencia por ID

Ruta:

```text
GET /api/incidencia/{id}
```

### Caso 1. Obtener incidencia existente

- URL de prueba:

```text
GET /api/incidencia/1
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve la incidencia con `id = 1`.
- Verificacion:
  - Confirmar que `$.id = 1`.
- Rol requerido: `ADMIN`.

### Caso 2. Obtener incidencia inexistente

- URL de prueba:

```text
GET /api/incidencia/99
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
  - Mensaje: `Incidencia no encontrada con id: 99`.
- Verificacion:
  - Confirmar que no se devuelve ninguna incidencia.
- Rol requerido: `ADMIN`.
