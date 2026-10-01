# Casos de Prueba de Autenticacion

Este documento describe casos de prueba funcionales(5 casos) para los endpoints implementados en el modulo de autenticacion y registro de usuarios.

Base URL local:

```text
http://localhost:8080
```

## Endpoint: Registrar usuario

Ruta:

```text
POST /api/auth/register
```

### Caso 1. Registrar usuario con datos validos

- Entrada:

```json
{
  "username": "jaime",
  "password": "123456",
  "nombre": "Jaime Suarez",
  "correo": "jaimito@gmail.com",
  "area": "CONTABILIDAD"
}
```

- Salida esperada:
  - Codigo HTTP `201 Created`.
  - Se devuelve un objeto con `success = true`.
  - Mensaje: `Usuario Creado con exito!`.
  - `dato.username = "jaime"`.
  - `dato.roles[0].name = "ROLE_EMPLEADO"`.
- Verificacion:
  - Confirmar que la respuesta indica exito en la creacion.
  - Confirmar que el username del usuario creado coincide con el enviado.
  - Confirmar que el rol asignado por defecto es `ROLE_EMPLEADO`.

### Caso 2. Registrar usuario con nombre invalido

- Entrada:

```json
{
  "username": "jaime",
  "password": "123456",
  "nombre": "ja",
  "correo": "jaimito@gmail.com",
  "area": "CONTABILIDAD"
}
```

- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `error = "Bad Request"`.
  - `errors.nombre = "El nombre debe tener entre 3 y 35 caracteres"`.
- Verificacion:
  - Confirmar que la validacion de longitud del nombre se ejecuta correctamente.
  - Confirmar que el error se reporta en el campo `nombre`.

### Caso 3. Registrar usuario con username erroneo

- Entrada:

```json
{
  "username": "ch",
  "password": "123456",
  "nombre": "Jaime Suarez",
  "correo": "jaimito@gmail.com",
  "area": "CONTABILIDAD"
}
```

- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `error = "Bad Request"`.
  - `errors.username = "El username debe tener entre 3 y 20 caracteres"`.
- Verificacion:
  - Confirmar que la validacion de longitud del username se ejecuta correctamente.
  - Confirmar que el error se reporta en el campo `username`.

### Caso 4. Registrar usuario con correo y password invalidos

- Entrada:

```json
{
  "username": "jaime",
  "password": "1234",
  "nombre": "Jaime Suarez",
  "correo": "jaimito.com.pe",
  "area": "CONTABILIDAD"
}
```

- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `error = "Bad Request"`.
  - `errors.correo = "El formato del correo electronico no es valido"`.
  - `errors.password = "La contrasena debe tener al menos 5 caracteres"`.
- Verificacion:
  - Confirmar que la validacion de formato del correo se ejecuta correctamente.
  - Confirmar que la validacion de longitud de la contrasena se ejecuta correctamente.
  - Confirmar que ambos errores se reportan simultaneamente.

### Caso 5. Registrar usuario con correo ya existente

- Entrada:

```json
{
  "username": "jaime",
  "password": "123456",
  "nombre": "Jaime Suarez",
  "correo": "jaimito@gmail.com",
  "area": "CONTABILIDAD"
}
```

- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `error = "Bad Request"`.
  - `message = "El correo electronico ya se encuentra registrado."`.
- Verificacion:
  - Confirmar que la validacion de unicidad del correo se ejecuta correctamente.
  - Confirmar que no se crea un usuario duplicado.
