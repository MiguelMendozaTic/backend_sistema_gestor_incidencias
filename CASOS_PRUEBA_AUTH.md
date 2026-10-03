# Casos de Prueba de Autenticacion y Alta de Usuarios

Este documento describe los casos de prueba funcionales del modulo de autenticacion, alta de usuarios y perfil.

Reglas del modulo:

- No existe registro publico: **solo el administrador** da de alta usuarios y elige su rol.
- Las contrasenas nuevas deben ser seguras (ej. `@Admin123`).
- Cada usuario solo puede cambiar su **nombre** desde "Mi perfil"; el usuario (login) y el correo los cambia el administrador.
- Un usuario `INACTIVO` no puede iniciar sesion.

Base URL local:

```text
http://localhost:8080
```

Todas las rutas, salvo `POST /login`, requieren el encabezado:

```text
Authorization: Bearer <token>
```

Cada caso indica el test automatizado que lo cubre (`mvnw test`).

---

## Endpoint: Iniciar sesion

Ruta:

```text
POST /login
```

### Caso 1. Login con credenciales validas

- Entrada:

```json
{
  "username": "admin",
  "password": "<contrasena del usuario>"
}
```

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - `token` con el JWT (vigencia de 1 hora) y `message = "<username> ha iniciado sesión con éxito"`.
- Verificacion:
  - Confirmar que el token incluye los roles del usuario en `authorities`.

### Caso 2. Login con contrasena incorrecta o cuenta inactiva

- Entrada: usuario existente con contrasena incorrecta, o usuario con estado `INACTIVO`.
- Salida esperada:
  - Codigo HTTP `401 Unauthorized`.
  - `message = "Error en la autenticación, username o password incorrectos"`.
- Verificacion:
  - Confirmar que el mensaje es el mismo en ambos casos (no revela si la cuenta existe o esta inactiva).
  - Confirmar que una cuenta `INACTIVO` no obtiene token aunque la contrasena sea correcta.

---

## Endpoint: Registrar usuario (solo administrador)

Ruta:

```text
POST /api/usuario
```

Requisitos de la contrasena (`password`):

| Regla | Ejemplo que falla |
|---|---|
| Minimo 6 caracteres (maximo 72) | `Ab1` |
| Al menos una mayuscula | `clave123` |
| Al menos una minuscula | `CLAVE123` |
| Al menos un numero | `ClaveSegura` |
| Al menos un caracter especial (`@ # $ ! .` ...) | `Clave123` |
| Sin espacios | `@Clave 123` |

Valores validos de `rol`: `ROLE_ADMIN`, `ROLE_EMPLEADO`, `ROLE_TECNICO_NIVEL_1`, `ROLE_TECNICO_NIVEL_2`, `ROLE_TECNICO_NIVEL_3`. Si no se envia, se asigna `ROLE_EMPLEADO`.

### Caso 3. Registrar usuario con datos validos

- Usuario en sesion: `ADMIN`.
- Entrada:

```json
{
  "username": "jaime",
  "password": "@Admin123",
  "nombre": "Jaime Suarez",
  "correo": "jaimito@gmail.com",
  "area": "CONTABILIDAD",
  "rol": "ROLE_TECNICO_NIVEL_2"
}
```

- Salida esperada:
  - Codigo HTTP `201 Created`.
  - `success = true`.
  - Mensaje: `Usuario Creado con exito!`.
  - `dato.username = "jaime"`.
  - `dato.roles[0].name` igual al rol elegido.
- Verificacion:
  - Confirmar que el usuario se crea con el rol elegido por el administrador y en estado `ACTIVO`.
  - Confirmar que la respuesta **no** contiene `passwordHash`.
- Test: `UsuarioControllerTest.crearUsuario_retorna201YUsuarioCreado`, `crearUsuario_admin_retorna201ConNombreCompleto`.

### Caso 4. Registrar usuario sin ser administrador

- Usuario en sesion: `EMPLEADO` o `TECNICO_*`.
- Entrada: la del Caso 3 (por ejemplo con `"rol": "ROLE_ADMIN"`).
- Salida esperada:
  - Codigo HTTP `403 Forbidden`.
- Verificacion:
  - Confirmar que no se crea el usuario.
  - Confirmar que un usuario sin rol de administrador no puede crear otro administrador.
- Test: `UsuarioControllerTest.crearUsuario_noAdmin_retorna403`.

### Caso 5. Registrar usuario con nombre invalido

- Entrada: la del Caso 3 con `"nombre": "ja"` (o con mas de 35 caracteres).
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `error = "Bad Request"`.
  - `errors.nombre = "El nombre debe tener entre 3 y 35 caracteres"`.
- Verificacion:
  - Confirmar que se aceptan nombres completos de hasta 35 caracteres (ej. `Ana Lucía Torres Gutiérrez`).
- Test: `UsuarioControllerTest.crearUsuario_retorna400BadRequestNombreInvalido`, `crearUsuario_nombreMuyLargo_retorna400`.

### Caso 6. Registrar usuario con username erroneo

- Entrada: la del Caso 3 con `"username": "ch"`.
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `errors.username = "El username debe tener entre 3 y 20 caracteres"`.
- Test: `UsuarioControllerTest.crearUsuario_retorna400BadRequestUsernameErroneo`.

### Caso 7. Registrar usuario con correo y contrasena invalidos

- Entrada: la del Caso 3 con `"correo": "jaimito.com.pe"` y `"password": "1234"`.
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `errors.correo = "El formato del correo electrónico no es válido"`.
  - `errors.password = "La contraseña debe tener mínimo 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial"`.
- Verificacion:
  - Confirmar que ambos errores se reportan simultaneamente.
- Test: `UsuarioControllerTest.crearUsuario_retorna400BadRequestCorreoYPassword`.

### Caso 8. Registrar usuario con contrasena debil

- Entrada: la del Caso 3 con cada una de las contrasenas de la tabla de requisitos (`Ab1`, `clave123`, `CLAVE123`, `ClaveSegura`, `Clave123`, `@Clave 123`).
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `errors.password` con el mensaje de la politica de contrasenas.
- Test: `UsuarioControllerTest.crearUsuario_passwordDebil_retorna400`.

### Caso 9. Registrar usuario con correo ya existente

- Entrada: la del Caso 3 con un correo ya registrado.
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `message = "El correo electrónico ya se encuentra registrado."`.
- Verificacion:
  - Confirmar que no se crea un usuario duplicado.
- Test: `UsuarioControllerTest.crearUsuario_retorna400BadRequestCorreoExiste`.

---

## Endpoint: Registro publico (eliminado)

Ruta:

```text
POST /api/auth/register
```

### Caso 10. El registro publico ya no esta disponible

- Usuario en sesion: ninguno.
- Salida esperada:
  - Codigo HTTP `401` o `403`.
- Verificacion:
  - Confirmar que nadie puede crear cuentas sin un administrador.
  - En el frontend, `/auth/register` redirige al login.
- Test: `AuthControllerTest.registroPublico_yaNoEstaDisponible`.

---

## Endpoints: Comprobar disponibilidad de usuario y correo (solo administrador)

Rutas (las usa el formulario de alta del panel admin):

```text
GET /api/auth/{username}
GET /api/auth/{correo}/validacion
```

### Caso 11. Consultar disponibilidad como administrador

- Salida esperada:
  - Codigo HTTP `200 OK`.
  - `{ "exists": true }` o `{ "exists": false }`.
- Test: `AuthControllerTest.existeUsername_admin_retorna200`, `existeCorreo_admin_retorna200`.

### Caso 12. Consultar disponibilidad sin ser administrador

- Usuario en sesion: ninguno, o `EMPLEADO`.
- Salida esperada:
  - Sin sesion: `401` o `403`. Con `EMPLEADO`: `403 Forbidden`.
- Verificacion:
  - Confirmar que no se puede averiguar desde fuera que usuarios o correos existen.
- Test: `AuthControllerTest.existeUsername_sinSesion_denegado`, `existeUsername_empleado_retorna403`.

---

## Endpoints: Perfil propio

Rutas:

```text
GET  /api/usuario/{username}/username
POST /api/usuario/updatePerfil
```

### Caso 13. Consultar el perfil propio

- Usuario en sesion: el mismo `{username}`.
- Salida esperada:
  - Codigo HTTP `200 OK` con los datos del usuario, **sin** `passwordHash`.
- Test: `UsuarioControllerTest.perfilPorUsername_propio_retorna200SinPasswordHash`.

### Caso 14. Consultar el perfil de otro usuario

- Usuario en sesion: un `EMPLEADO` distinto de `{username}` (o sin sesion).
- Salida esperada:
  - Con sesion: `403 Forbidden`. Sin sesion: `401` o `403`.
- Verificacion:
  - El administrador si puede consultar cualquier perfil.
- Test: `UsuarioControllerTest.perfilPorUsername_deOtroUsuario_retorna403`, `perfilPorUsername_sinSesion_retorna401o403`.

### Caso 15. Actualizar el nombre en "Mi perfil"

- Entrada (usuario y correo sin cambios):

```json
{
  "username": "ana",
  "correo": "ana@empresa.com",
  "nombre": "Ana Lucía Torres"
}
```

- Salida esperada:
  - Codigo HTTP `200 OK` con el nombre actualizado.
- Test: `UsuarioServiceTest.perfil_cambiaSoloElNombre`.

### Caso 16. Intentar cambiar usuario o correo desde "Mi perfil"

- Entrada: la del Caso 15 con un `username` o `correo` distinto al actual.
- Salida esperada:
  - Codigo HTTP `400 Bad Request`.
  - `message = "El usuario y el correo solo los puede cambiar el administrador."`.
- Verificacion:
  - Confirmar que no se guarda ningun cambio.
  - En el frontend ambos campos son de solo lectura.
- Test: `UsuarioServiceTest.perfil_noPuedeCambiarSuUsuario`, `perfil_noPuedeCambiarSuCorreo`.
