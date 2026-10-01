# Casos de Prueba de Seguimiento

Este documento describe casos de prueba funcionales(17 casos) para los endpoints implementados en el modulo de seguimiento.

Base URL local:

```text
http://localhost:8080
```

## Endpoint: Crear seguimiento

Ruta:

```text
POST /api/seguimiento
```

### Caso 1. Crear seguimiento con datos validos

- Entrada:

```json
{
  "idIncidencia": 1,
  "estado": "ACTIVO",
  "comentario": "Revisando el equipo"
}
```

- Salida esperada:
  - Codigo HTTP `201 Created`.
  - Se devuelve un objeto con `id = 1`.
  - El campo `comentario` debe ser `"Revisando el equipo"`.
- Verificacion:
  - Confirmar que la respuesta contiene el comentario enviado.
  - Confirmar que `id` exista y sea numerico.

### Caso 2. Crear seguimiento con usuario no encontrado

- Entrada:

```json
{
  "idIncidencia": 1,
  "estado": "ACTIVO",
  "comentario": "Revisando el equipo"
}
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
  - Mensaje: `El usuario no se ha encontrado`.
- Verificacion:
  - Confirmar que no se crea ningun seguimiento.
  - Confirmar que el cuerpo de respuesta coincide con el mensaje de error esperado.

## Endpoint: Modificar seguimiento

Ruta:

```text
PUT /api/seguimiento/{id}
```

### Caso 1. Modificar seguimiento existente con datos validos

- Precondicion:
  - Debe existir un seguimiento con ID `1`.
  - Debe existir una incidencia con ID `1`.
- Entrada:

```json
{
  "idSeguimiento": 1,
  "comentario": "Equipo reparado",
  "incidencia": {
    "id": 1
  }
}
```

- URL de prueba:

```text
PUT /api/seguimiento/1
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve el seguimiento actualizado con `comentario = "Equipo reparado"`.
- Verificacion:
  - Confirmar que `comentario` cambia al nuevo valor.
  - Confirmar que la referencia de incidencia sigue siendo valida.
- Rol requerido: `ADMIN`.

### Caso 2. Modificar seguimiento sin permisos (403 Forbidden)

- Entrada: Mismo cuerpo que Caso 1.
- URL de prueba:

```text
PUT /api/seguimiento/1
```

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede modificar seguimientos.

### Caso 3. Modificar seguimiento cuando incidencia no existe

- Entrada:

```json
{
  "idSeguimiento": 1,
  "comentario": "Equipo reparado, puede recogerlo.",
  "incidencia": {
    "id": 99
  }
}
```

- URL de prueba:

```text
PUT /api/seguimiento/1
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
  - Mensaje: `Incidencia no encontrado con ese ID`.
- Verificacion:
  - Confirmar que la API no actualiza el seguimiento.
  - Confirmar que la validacion de incidencia se ejecuta antes de persistir cambios.
- Rol requerido: `ADMIN`.

## Endpoint: Listar todos los seguimientos

Ruta:

```text
GET /api/seguimiento
```

### Caso 1. Listar seguimientos exitosamente

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con al menos un seguimiento.
  - El primer elemento tiene `comentario = "Revisando el equipo"`.
- Verificacion:
  - Confirmar que `$.length() = 1`.
  - Confirmar que `$[0].comentario` tiene el valor esperado.
- Rol requerido: `ADMIN`.

### Caso 2. Listar seguimientos sin permisos (403 Forbidden)

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede listar todos los seguimientos.

### Caso 3. Listar seguimientos devuelve lista vacia

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista vacia `[]`.
- Verificacion:
  - Confirmar que `$.length() = 0`.
- Rol requerido: `ADMIN`.

### Caso 4. Listar multiples seguimientos

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con 2 elementos.
- Verificacion:
  - Confirmar que `$.length() = 2`.
- Rol requerido: `ADMIN`.

## Endpoint: Seguimientos paginados

Ruta:

```text
GET /api/seguimiento/paginado
```

Parametros:
- `page`: numero de pagina (default 0)
- `size`: cantidad por pagina (default 5)

### Caso 1. Obtener pagina de seguimientos exitosamente

- Parametros de prueba:
  - `page = 0`
  - `size = 5`
- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve un objeto con `content` que contiene 1 elemento.
  - `content[0].comentario = "Revisando el equipo"`.
- Verificacion:
  - Confirmar que `$.content.length() = 1`.
  - Confirmar que el comentario del primer elemento coincide.
- Rol requerido: `ADMIN`.

### Caso 2. Obtener pagina de seguimientos sin permisos (403 Forbidden)

- Parametros de prueba:
  - `page = 0`
  - `size = 5`
- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede acceder a seguimientos paginados.

## Endpoint: Obtener seguimiento por ID

Ruta:

```text
GET /api/seguimiento/{id}
```

### Caso 1. Obtener seguimiento existente

- URL de prueba:

```text
GET /api/seguimiento/1
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve el seguimiento con `idSeguimiento = 1`.
  - El campo `comentario = "Revisando el equipo"`.
- Verificacion:
  - Confirmar que `$.idSeguimiento = 1`.
  - Confirmar que `$.comentario` tiene el valor esperado.
- Rol requerido: `ADMIN`.

### Caso 2. Obtener seguimiento inexistente

- URL de prueba:

```text
GET /api/seguimiento/99
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
- Verificacion:
  - Confirmar que no se devuelve ningun seguimiento.
- Rol requerido: `ADMIN`.

### Caso 3. Obtener seguimiento con ID invalido

- URL de prueba:

```text
GET /api/seguimiento/-1
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
- Verificacion:
  - Confirmar que un ID negativo retorna 404.
- Rol requerido: `ADMIN`.

## Endpoint: Obtener mis seguimientos por incidencia

Ruta:

```text
GET /api/seguimiento/{id}/seguimientos
```

### Caso 1. Obtener seguimientos de una incidencia exitosamente

- URL de prueba:

```text
GET /api/seguimiento/1/seguimientos
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con 1 elemento.
  - El primer elemento tiene `id = 1` y `comentario = "Revisando el equipo"`.
- Verificacion:
  - Confirmar que `$.length() = 1`.
  - Confirmar que `$[0].id = 1`.
  - Confirmar que `$[0].comentario = "Revisando el equipo"`.
- Rol requerido: `EMPLEADO`.

### Caso 2. Obtener seguimientos de una incidencia sin permisos (403 Forbidden)

- URL de prueba:

```text
GET /api/seguimiento/1/seguimientos
```

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `NUEVO` no puede acceder a los seguimientos de una incidencia.
