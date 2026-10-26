# Sistema Académico Inteligente con alertas automatizadas
I.E. N° 86135 — Aija · Curso Integrador I: Sistemas Software · **Versión Java con Spring Boot**

Misma interfaz y mismas funciones que la versión PHP, pero ahora **toda la lógica del negocio está en archivos `.java`**,
organizada con **SOLID** y el **patrón DAO** para la base de datos (MySQL de XAMPP hoy, otra base mañana).

---

## 1. Cómo ejecutarlo (XAMPP + MySQL)

Necesitas: **JDK 25 o superior** y un IDE (IntelliJ, Eclipse o VS Code con Java). Maven viene dentro del IDE.

1. Abre XAMPP y enciende **MySQL** (Apache ya no hace falta).
2. En phpMyAdmin → pestaña **Importar** → elige `database/mysql.sql`. (Crea la base `colegio_rural` con datos de ejemplo.)
3. Abre la carpeta del proyecto en el IDE como **proyecto Maven** (File → Open → `pom.xml`). Espera a que descargue las librerías.
4. Ejecuta la clase `com.colegiorural.ColegioRuralApplication` (botón Run). También sirve `mvn spring-boot:run` en la terminal.
5. Entra a **http://localhost:8080**

| Usuario | Clave | Rol |
|---|---|---|
| admin | admin123 | Administrador |
| coord | coord123 | Coordinador |
| docente | docente123 | Docente |
| alumno | alumno123 | Alumno |

Si tu MySQL tiene contraseña, cámbiala en `src/main/resources/application-mysql.properties`.

## 2. Qué ve el profesor en el código (todo en `src/main/java/com/colegiorural`)

```
web/        Controladores: reciben la petición, llaman al servicio y eligen la pantalla (sin reglas de negocio)
servicio/   LÓGICA DEL NEGOCIO: validar notas, crear alertas, pase de lista, reglas, copias de seguridad...
regla/      Reglas de alerta (una clase por regla)
dao/        INTERFACES de acceso a datos (qué se puede guardar/consultar, sin decir cómo)
dao/jdbc/   Implementación con JdbcTemplate para SQL (MySQL o PostgreSQL)
dominio/    Datos puros: Estudiante, Calificacion, Alerta...
config/     Valores de application.properties
```
Pantallas HTML: `src/main/resources/templates` (Thymeleaf) · Estilos y JS: `src/main/resources/static/assets`.

### Recorrido de una petición: "registrar una nota"
`calificaciones.html` (formulario) → `CalificacionController.accion()` → `CalificacionService.registrar()` → `CalificacionDao` → `JdbcCalificacionDao` → MySQL

### "Ingresar nota si es correcto" (validación)
`CalificacionService.registrar()` rechaza con `ValidacionException` y **no guarda nada** si: la nota no está entre 0 y 20,
la asistencia no está entre 0 y 100, el curso está vacío o el estudiante no existe / está inactivo.
Lo escrito que no es número (`abc`) lo rechaza `P.decimal()` en la capa web. El mensaje sale en rojo dentro del mismo formulario
y los datos escritos no se pierden. Si es correcto, se guarda, se evalúan las reglas y se crean las alertas.

## 3. Patrones SOLID (dónde se ve cada uno)

| Principio | Dónde está |
|---|---|
| **S** Responsabilidad única | Cada servicio hace una sola cosa: `CalificacionService` (notas), `AlertaService` (alertas), `AsistenciaService` (pase de lista), `RespaldoService` (copias)... Cada DAO maneja una sola tabla. Los controladores solo traducen web ↔ servicio. |
| **O** Abierto/cerrado | `regla/ReglaAlerta` es una interfaz. `ReglaNotaBaja`, `ReglaAsistenciaBaja` y `ReglaFaltasSeguidas` la implementan y llevan `@Component`. `AlertaService` recibe `List<ReglaAlerta>`: **una regla nueva = una clase nueva**, sin tocar el servicio. (La prueba lo demuestra.) |
| **L** Sustitución de Liskov | `JdbcEstudianteDao` y `MemoriaEstudianteDao` (en `src/test`) cumplen el mismo contrato `EstudianteDao`; los servicios funcionan igual con cualquiera. |
| **I** Segregación de interfaces | Una interfaz DAO pequeña por tabla (`EstudianteDao`, `AlertaDao`...) en vez de una gigante. `UsuarioActual` y `Hasher` tienen un solo método cada una. |
| **D** Inversión de dependencias | Los servicios piden **interfaces** por constructor (`CalificacionDao`, `UsuarioActual`, `Hasher`...), nunca clases concretas ni `HttpSession`. Spring conecta cada interfaz con su implementación. |

## 4. Patrón DAO: "la escuela quiere cambiar de base de datos"

El servicio nunca escribe SQL ni conoce MySQL: solo llama métodos como `estudianteDao.buscarPorDni(dni)`.
Todo el SQL vive en `dao/jdbc` y está escrito en SQL estándar (`LIMIT ? OFFSET ?`, `LIKE`, ids generados con `getGeneratedKeys`).

**Pasar de MySQL a PostgreSQL:**
1. Crea la base `colegio_rural` en PostgreSQL y ejecuta `database/postgres.sql`.
2. En `application.properties` cambia `spring.profiles.active=mysql` por `spring.profiles.active=postgres`.
3. Listo. **No se modifica ninguna clase Java.** (Los dos drivers ya están en el `pom.xml`.)

Para una base distinta (Oracle, SQL Server...) se agrega su driver al `pom.xml` y un `application-xxx.properties`.
Si algún motor exigiera SQL diferente, se escribe otra implementación (`OracleEstudianteDao implements EstudianteDao`) y los servicios ni se enteran.

## 5. Cómo la librería/framework hace el código más simple

**Sin librería** (JDBC a mano) buscar un estudiante por DNI:
```java
Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
try {
    con = DriverManager.getConnection(url, user, pass);
    ps = con.prepareStatement("SELECT * FROM estudiantes WHERE dni = ?");
    ps.setString(1, dni);
    rs = ps.executeQuery();
    if (rs.next()) { return Optional.of(new Estudiante(rs.getLong("id"), ...)); }
    return Optional.empty();
} catch (SQLException e) { throw new RuntimeException(e); }
finally { /* cerrar rs, ps y con, cada uno con su propio try/catch */ }
```
**Con Spring `JdbcTemplate`** (así está en `JdbcEstudianteDao`):
```java
return una("SELECT * FROM estudiantes WHERE dni = ?", MAPA, dni);
```
La librería abre y cierra la conexión, usa un pool (HikariCP), traduce errores y evita la inyección SQL. Queda **una línea** en vez de unas 15.

| Antes (PHP / Java manual) | Con Spring Boot |
|---|---|
| Crear a mano cada objeto y pasarlo: `new Servicio(new Dao(new Conexion()))` | `@Service` / `@Repository` + constructor: Spring crea y conecta todo (**inyección de dependencias**) |
| Registrar cada regla de alerta en una lista | `List<ReglaAlerta>` se llena sola con todas las clases `@Component` |
| Leer `$_POST`, `$_SESSION` y escribir `header('Location')` | `@GetMapping`, `@PostMapping`, `@RequestParam`, `return "redirect:..."` |
| Mezclar HTML con código | Thymeleaf: la pantalla solo muestra datos (`th:text`, `th:each`), la lógica queda en `.java` |
| Abrir/cerrar conexiones y transacciones a mano | `JdbcTemplate` + `@Transactional` |
| Leer un archivo de configuración con código propio | `application.properties` + `@ConfigurationProperties` |
| Servidor aparte (Apache) | Tomcat incluido: se ejecuta como un programa normal |

Resultado: las clases son más cortas, cada una hace una sola cosa y se lee de arriba abajo sin código "de fontanería".

## 6. Seguridad incluida
- Contraseñas con **BCrypt** (los hash de la versión PHP siguen funcionando).
- Bloqueo del login tras 5 intentos fallidos (60 s).
- Control por rol en un solo lugar (`web/AccesoInterceptor`); el alumno solo ve su propio boletín.
- Token **CSRF** en todos los formularios.
- Consultas con parámetros (`?`): sin inyección SQL. Restaurar copias solo acepta archivos propios y valida línea por línea.
- Bitácora de actividad (quién, qué, cuándo).

## 7. Pruebas de la lógica (sin base de datos y sin Spring)
`src/test/java/com/colegiorural/memoria/PruebaLogica.java` arma los servicios con DAO falsos en memoria y comprueba
validación de notas, alertas, regla nueva sin tocar el servicio, faltas seguidas, reglas editables y copias de seguridad
(38 comprobaciones). Se ejecuta con el método `main` desde el IDE.
Que funcione sin MySQL demuestra que los servicios dependen de interfaces (D) y los DAO son intercambiables (L).

## 8. Funciones (igual que la versión PHP)
Inicio con indicadores y gráficos · Estudiantes (buscar, filtrar, crear, editar, activar, eliminar, importar/exportar CSV, ficha con
calendario de asistencia verde/amarillo/rojo) · Notas · Pase de lista diario · Alertas automáticas y manuales · Docentes · Cursos y
asignaciones · Usuarios y roles · Comunicados · Reportes (CSV / imprimir) · Boletín imprimible · Reglas editables · Copia de seguridad
y restauración · Registro de actividad · Perfil · Modo claro/oscuro · Portal del alumno.

## Documentación para el Avance 3

- **Diagramas**: `docs/mvc.png` (MVC), `docs/dao.png` (capas DAO), `docs/solid.png` (SOLID).
- **Javadoc**: abrir `docs/javadoc/index.html` en el navegador (se regenera con `mvn javadoc:javadoc`).
- **Pruebas (TDD)**: `mvn test` ejecuta las pruebas JUnit 5 de `src/test/java/.../servicio` (notas, alertas, asistencia, estudiantes/CSV, reglas, respaldo, bitácora). Usan DAO en memoria, sin base de datos.
- **Librerías**: Guava, Apache Commons (lang3, csv, io), Apache POI (exportar `/reportes/excel`) y Logback (`logs/colegio-rural.log`).
