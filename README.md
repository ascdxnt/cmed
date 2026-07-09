# CitaMed

Sistema web de gestión de citas médicas desarrollado como proyecto final del curso SC-403 Desarrollo de Aplicaciones Web y Patrones de la Universidad Fidélitas.

## Descripción

CitaMed permite que los pacientes registren su cuenta, consulten disponibilidad y reserven citas. Los doctores administran su agenda, bloquean horarios y registran la atención. Los administradores gestionan pacientes, médicos, especialidades, citas y reportes.

La aplicación utiliza una estructura MVC con persistencia relacional y vistas dinámicas construidas con Thymeleaf y Bootstrap.

## Integrantes

| Nombre | GitHub |
| --- | --- |
| Aguilar Chavarría Hillary Sofía | Hilla-Aguilar15 |
| Calvo Monge Dilan Steef | dilanscm |
| Fuentes García Jeferson Andrew | nzhfire |
| Ramírez Pérez Fernando Esteban | FernandoRPZoy1996 |

## Tecnologías

- Java 21
- Spring Boot
- Thymeleaf
- Bootstrap 5
- Hibernate / JPA
- MySQL
- Maven

## Estructura

```text
src/main/java/com/citamed/
├── controller/
├── domain/
├── repository/
└── service/

src/main/resources/
├── templates/
├── static/css/
├── application.properties
├── messages.properties
└── citamed.sql
```

## Ejecución

Requisitos:

- Java 21
- Maven 3.8 o superior
- MySQL 8 o superior
- NetBeans con soporte para Maven

Pasos:

1. Ejecutar `src/main/resources/citamed.sql` en MySQL.
2. Verificar las credenciales de conexión en `application.properties`.
3. Abrir `cmed` como proyecto Maven en NetBeans.
4. Ejecutar el proyecto.
5. Abrir `http://localhost:91/`.

Usuarios iniciales:

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | `andrew@citamed.com` | `123` |
| Paciente | `hillary@citamed.com` | `123` |
| Doctor | `dilan@citamed.com` | `123` |
| Doctor | `fernando@citamed.com` | `123` |

## Estado del Avance 2

El Avance 2 implementa 15 de las 20 historias de usuario definidas en el Avance 1. Esto representa un 75% del backlog, con prioridad en el ciclo funcional de registro, reserva, gestión y atención de citas.

### Historias implementadas

| ID | Funcionalidad |
| --- | --- |
| HU-01 | Registro de médicos con especialidad y horario. |
| HU-02 | Registro, edición, búsqueda y desactivación de pacientes. |
| HU-03 | Registro público y almacenamiento protegido de contraseña. |
| HU-04 | Inicio de sesión del paciente y restricción por rol. |
| HU-05 | Reserva con especialidad, médico, disponibilidad y confirmación. |
| HU-06 | Agenda diaria y semanal del doctor. |
| HU-07 | Consulta administrativa de citas con filtros. |
| HU-08 | Cancelación de citas propias con más de 24 horas. |
| HU-09 | Confirmación y rechazo de citas con motivo. |
| HU-10 | Inicio de sesión administrativo y restricción de rutas mediante sesión. |
| HU-11 | Cierre de cita y registro de nota médica. |
| HU-14 | Reporte de citas por rango y estado. |
| HU-15 | Bloqueo de horarios del doctor. |
| HU-16 | Médicos y horarios disponibles por especialidad. |
| HU-20 | Activación y desactivación de médicos. |

### Historias parciales

| ID | Estado pendiente |
| --- | --- |
| HU-12 | Se muestran citas y notas; falta separar exclusivamente el historial pasado. |
| HU-13 | Se registran y listan especialidades; faltan edición y eliminación controlada. |
| HU-18 | El doctor consulta el historial seleccionado; falta búsqueda por nombre o cédula. |
| HU-19 | El perfil permite consultar datos y cambiar contraseña; falta editar los datos de contacto. |

### Historia pendiente

| ID | Funcionalidad |
| --- | --- |
| HU-17 | Reprogramación administrativa de citas. |

## Acuerdo de trabajo

- `main` representa la versión estable.
- Cada funcionalidad se desarrolla en una rama `feature/nombre-funcionalidad`.
- Las correcciones se realizan en ramas `fix/descripcion-correccion`.
- Todo cambio hacia `main` requiere un pull request revisado por otro integrante.

## Estado general

- Avance 1: planteamiento, backlog, prototipo y modelo preliminar completados.
- Avance 2: aplicación funcional, persistencia, navegación por roles y vistas Bootstrap implementadas.
