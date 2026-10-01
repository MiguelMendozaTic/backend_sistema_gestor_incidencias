# Peticiones de la API en Local

Este archivo reúne las peticiones que actualmente soporta el proyecto `backend_sistema_gestor_incidencias`, usando `curl` contra una ejecucion local de Spring Boot.

Base URL local:

```text
http://localhost:8080
```

## Consideraciones previas

- Primero debes levantar la aplicacion en local.
- Las incidencias dependen de usuarios ya registrados.
- El proyecto trabaja en memoria, por lo que al reiniciar la aplicacion se pierden los datos.
- Los endpoints de seguimiento disponibles actualmente son crear y modificar.

## 1. Crear usuario

Endpoint:

```text
POST /api/usuario
```

`curl`:

```bash
curl --location 'http://localhost:8080/api/usuario' \
--header 'Content-Type: application/json' \
--data-raw '{
  "nombre": "Juan Perez",
  "correo": "juan@empresa.com",
  "estado": "activo",
  "rol": "empleado",
  "area": "Administracion"
}'
```

Ejemplo alterno para crear un tecnico:

```bash
curl --location 'http://localhost:8080/api/usuario' \
--header 'Content-Type: application/json' \
--data-raw '{
  "nombre": "Ana Torres",
  "correo": "ana@empresa.com",
  "estado": "activo",
  "rol": "tecnicoNivel1",
  "area": "Logistica"
}'
```

Uso en Postman:

- Metodo: `POST`
- URL: `http://localhost:8080/api/usuario`
- Body: `raw`
- Formato: `JSON`

## 2. Obtener usuario por ID

Endpoint:

```text
GET /api/usuario/{id}
```

`curl`:

```bash
curl --location 'http://localhost:8080/api/usuario/1'
```

Uso en Postman:

- Metodo: `GET`
- URL: `http://localhost:8080/api/usuario/1`

## 3. Crear incidencia

Endpoint:

```text
POST /api/incidencia
```

Antes de ejecutar esta peticion:

- Debe existir el usuario reportante.
- Debe existir el tecnico asignado.

Ejemplo: si ya creaste al usuario con ID `1` y al tecnico con ID `2`, puedes registrar la incidencia asi:

```bash
curl --location 'http://localhost:8080/api/incidencia' \
--header 'Content-Type: application/json' \
--data-raw '{
  "titulo": "Error de acceso al sistema",
  "descripcion": "El usuario no puede iniciar sesion en la plataforma",
  "estado": "abierto",
  "usuario": {
    "id": 1
  },
  "tecnico": {
    "id": 2
  }
}'
```

Uso en Postman:

- Metodo: `POST`
- URL: `http://localhost:8080/api/incidencia`
- Body: `raw`
- Formato: `JSON`

## 4. Modificar incidencia por ID

Endpoint:

```text
POST /api/incidencia/{id}
```

Esta peticion reemplaza la incidencia existente por una nueva version del objeto enviado.

Ejemplo para modificar la incidencia con ID `1`:

```bash
curl --location 'http://localhost:8080/api/incidencia/1' \
--header 'Content-Type: application/json' \
--data-raw '{
  "titulo": "Error de acceso al sistema - actualizado",
  "descripcion": "Se valida que el problema ocurre solo en horario de oficina",
  "estado": "pendiente",
  "usuario": {
    "id": 1
  },
  "tecnico": {
    "id": 2
  }
}'
```

Uso en Postman:

- Metodo: `POST`
- URL: `http://localhost:8080/api/incidencia/1`
- Body: `raw`
- Formato: `JSON`

## 5. Crear seguimiento

Endpoint:

```text
POST /api/seguimiento
```

Antes de ejecutar esta peticion:

- Debe existir la incidencia asociada.

Ejemplo: si ya creaste una incidencia con ID `1`, puedes registrar un seguimiento asi:

```bash
curl --location 'http://localhost:8080/api/seguimiento' \
--header 'Content-Type: application/json' \
--data-raw '{
  "comentario": "Se inicia la revision del caso",
  "fecha": "2026-04-17T10:00:00.000+00:00",
  "estado": "activo",
  "incidencia": {
    "id": 1
  }
}'
```

Uso en Postman:

- Metodo: `POST`
- URL: `http://localhost:8080/api/seguimiento`
- Body: `raw`
- Formato: `JSON`

## 6. Modificar seguimiento por ID

Endpoint:

```text
POST /api/seguimiento/{id}
```

Ejemplo para modificar el seguimiento con ID `1`:

```bash
curl --location 'http://localhost:8080/api/seguimiento/1' \
--header 'Content-Type: application/json' \
--data-raw '{
  "comentario": "Se actualiza el seguimiento luego de la revision",
  "fecha": "2026-04-17T11:00:00.000+00:00",
  "estado": "inactivo",
  "incidencia": {
    "id": 1
  }
}'
```

Uso en Postman:

- Metodo: `POST`
- URL: `http://localhost:8080/api/seguimiento/1`
- Body: `raw`
- Formato: `JSON`

## 7. Flujo recomendado de prueba

Si quieres probar todo desde cero en local, este es el orden correcto:

1. Crear un usuario reportante.
2. Crear un usuario tecnico.
3. Consultar uno de los usuarios por ID.
4. Crear una incidencia usando los IDs de ambos usuarios.
5. Modificar la incidencia creada.
6. Crear un seguimiento para la incidencia.
7. Modificar el seguimiento creado.

## 8. Coleccion rapida de curls

Puedes copiar estas peticiones una por una en Postman o ejecutarlas en terminal:

### Crear usuario reportante

```bash
curl --location 'http://localhost:8080/api/usuario' \
--header 'Content-Type: application/json' \
--data-raw '{
  "nombre": "Carlos Ramos",
  "correo": "carlos@empresa.com",
  "estado": "activo",
  "rol": "empleado",
  "area": "Contabilidad"
}'
```

### Crear usuario tecnico

```bash
curl --location 'http://localhost:8080/api/usuario' \
--header 'Content-Type: application/json' \
--data-raw '{
  "nombre": "Lucia Vega",
  "correo": "lucia@empresa.com",
  "estado": "activo",
  "rol": "tecnicoNivel2",
  "area": "Logistica"
}'
```

### Consultar usuario 1

```bash
curl --location 'http://localhost:8080/api/usuario/1'
```

### Crear incidencia con usuario 1 y tecnico 2

```bash
curl --location 'http://localhost:8080/api/incidencia' \
--header 'Content-Type: application/json' \
--data-raw '{
  "titulo": "Falla de impresora",
  "descripcion": "La impresora no responde desde el area de Contabilidad",
  "estado": "abierto",
  "usuario": {
    "id": 1
  },
  "tecnico": {
    "id": 2
  }
}'
```

### Modificar incidencia 1

```bash
curl --location 'http://localhost:8080/api/incidencia/1' \
--header 'Content-Type: application/json' \
--data-raw '{
  "titulo": "Falla de impresora - revision en curso",
  "descripcion": "Se detecta posible problema de conexion USB",
  "estado": "pendiente",
  "usuario": {
    "id": 1
  },
  "tecnico": {
    "id": 2
  }
}'
```

### Crear seguimiento para incidencia 1

```bash
curl --location 'http://localhost:8080/api/seguimiento' \
--header 'Content-Type: application/json' \
--data-raw '{
  "comentario": "El tecnico inicia diagnostico",
  "fecha": "2026-04-17T12:00:00.000+00:00",
  "estado": "activo",
  "incidencia": {
    "id": 1
  }
}'
```

### Modificar seguimiento 1

```bash
curl --location 'http://localhost:8080/api/seguimiento/1' \
--header 'Content-Type: application/json' \
--data-raw '{
  "comentario": "Se actualiza diagnostico y se solicita validacion",
  "fecha": "2026-04-17T13:00:00.000+00:00",
  "estado": "inactivo",
  "incidencia": {
    "id": 1
  }
}'
```

## 9. Endpoints que aun no existen

Actualmente no estan implementadas estas operaciones:

- Listar usuarios.
- Listar incidencias.
- Obtener incidencia por ID.
- Eliminar usuarios.
- Eliminar incidencias.
- Listar seguimientos.

## 10. Nota para Postman

Si deseas importarlos manualmente en Postman:

- Crea una nueva request.
- Copia la URL local.
- Selecciona el metodo HTTP correspondiente.
- En peticiones `POST`, pega el JSON en `Body > raw > JSON`.
- Agrega el header `Content-Type: application/json`.

Si quieres, en el siguiente paso puedo convertir este archivo en una coleccion Postman `JSON` lista para importar.
