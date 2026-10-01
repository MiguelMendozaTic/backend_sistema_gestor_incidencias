# Casos de Prueba de Usuario

Este documento describe casos de prueba funcionales(13 casos) para los endpoints implementados en el modulo de usuario.

Base URL local:

```text
http://localhost:8080
```

## Endpoint: Modificar usuario

Ruta:

```text
PUT /api/usuario/{id}
```

### Caso 1. Modificar usuario existente con datos validos

- Precondicion:
  - Debe existir un usuario con ID `1`.
- Entrada:

```json
{
  "id": 1,
  "nombre": "Abel Modificado"
}
```

- URL de prueba:

```text
PUT /api/usuario/1
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve el usuario actualizado con `nombre = "Abel Modificado"`.
- Verificacion:
  - Confirmar que `nombre` cambia al nuevo valor.
- Rol requerido: `ADMIN`.

### Caso 2. Modificar usuario inexistente

- Entrada:

```json
{
  "id": 99,
  "nombre": "Abel Modificado"
}
```

- URL de prueba:

```text
PUT /api/usuario/99
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
  - Mensaje: `Usuario no encontrado con id: 99`.
- Verificacion:
  - Confirmar que la API no actualiza ningun usuario.
- Rol requerido: `ADMIN`.

## Endpoint: Listar todos los usuarios

Ruta:

```text
GET /api/usuario
```

### Caso 1. Listar usuarios exitosamente

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con al menos un usuario.
  - El primer elemento tiene `nombre = "Abel Torres"`.
- Verificacion:
  - Confirmar que `$.length() = 1`.
  - Confirmar que `$[0].nombre` tiene el valor esperado.
- Rol requerido: `ADMIN`.

### Caso 2. Listar usuarios sin permisos (403 Forbidden)

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede listar todos los usuarios.

## Endpoint: Usuarios paginados

Ruta:

```text
GET /api/usuario/paginado
```

Parametros:
- `page`: numero de pagina (default 0)
- `size`: cantidad por pagina (default 2)

### Caso 1. Obtener pagina de usuarios exitosamente

- Parametros de prueba:
  - `page = 0`
  - `size = 2`
- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve un objeto con `content` que contiene 1 elemento.
  - `content[0].nombre = "Abel Torres"`.
  - `page.totalElements = 1`.
  - `page.totalPages = 1`.
- Verificacion:
  - Confirmar que `$.page.totalElements = 1`.
  - Confirmar que `$.page.totalPages = 1`.
  - Confirmar que `$.content[0].nombre` tiene el valor esperado.
- Rol requerido: `ADMIN`.

### Caso 2. Obtener pagina de usuarios con parametro page erroneo

- Parametros de prueba:
  - `page = -1`
  - `size = 2`
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - Mensaje: `El numero de pagina no puede ser menor a 0`.
- Verificacion:
  - Confirmar que `$.message` contiene el mensaje de validacion esperado.
- Rol requerido: `ADMIN`.

## Endpoint: Listar tecnicos disponibles

Ruta:

```text
GET /api/usuario/tecnicos
```
### Caso 1. Listar tecnicos disponibles

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve una lista con al menos un usuario envuelto bajo la estructura ApiResponse.
  - El primer elemento tiene `nombre = "Johan Gonzales"`.
- Verificacion:
  - Confirmar que `$.success = true`.
  - Confirmar que `$.message = ` tiene una respuesta: "Busqueda eficiente" .
  - Confirmar que `$.status = 200`.
  - Confirmar que `$.dato[0].nombre` tiene el valor esperado.
- Rol requerido: `ADMIN`.

### Caso 2.  Listar tecnicos disponibles sin permisos (403 Forbidden)

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede listar todos los usuarios.
  
## Endpoint: Obtener usuario por ID

Ruta:

```text
GET /api/usuario/{id}
```

### Caso 1. Obtener usuario existente

- URL de prueba:

```text
GET /api/usuario/1
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - Se devuelve el usuario con `id = 1`.
- Verificacion:
  - Confirmar que `$.id = 1`.
- Rol requerido: `ADMIN`.

### Caso 2. Obtener usuario inexistente

- URL de prueba:

```text
GET /api/usuario/99
```

- Salida esperada:
  - Codigo HTTP `404 Not Found`.
- Verificacion:
  - Confirmar que no se devuelve ningun usuario.
- Rol requerido: `ADMIN`.

### Caso 3. Obtener usuario sin permisos (403 Forbidden)

- URL de prueba:

```text
GET /api/usuario/1
```

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `EMPLEADO` no puede consultar usuarios individuales.

## Endpoint: Actualizar rol de usuario

Ruta:

```text
POST /api/usuario/{id}/role
```

### Caso 1. Actualizar rol exitosamente

- Entrada:

```json
{
  "role": "TECNICO_NIVEL_1"
}
```

- URL de prueba:

```text
POST /api/usuario/1/role
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
- Verificacion:
  - Confirmar que el rol del usuario se actualiza correctamente.
- Rol requerido: `ADMIN`.

### Caso 2. Actualizar rol sin permisos (403 Forbidden)

- Entrada:

```json
{
  "role": "TECNICO_NIVEL_1"
}
```

- URL de prueba:

```text
POST /api/usuario/1/role
```

- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que un usuario con rol `TECNICO` no puede actualizar roles de otros usuarios.
