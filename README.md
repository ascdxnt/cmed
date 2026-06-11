# cmed — CitaMed

Sistema web de gestión de citas médicas desarrollado con Java, Spring Boot, Thymeleaf y Bootstrap como proyecto final del curso SC-403 Desarrollo de Aplicaciones Web y Patrones, Universidad Fidélitas.

## Descripción

CitaMed permite a pacientes reservar citas médicas en línea, a doctores gestionar su agenda y a administradores controlar el flujo completo de atención de una clínica privada. El sistema implementa autenticación por roles, persistencia con JPA/Hibernate, internacionalización y componentes transaccionales.

## Integrantes

| Nombre | GitHub |
|---|---|
| Aguilar Chavarría Hillary Sofía | Hilla-Aguilar15 |
| Calvo Monge Dilan Steef | dilanscm |
| Fuentes García Jeferson Andrew | n4-zh |
| Ramírez Pérez Fernando Esteban | FernandoRPZoy1996 |

## Tecnologías

- Java 21
- Spring Boot 3
- Spring Security
- Thymeleaf
- Bootstrap 5
- Hibernate / JPA
- MySQL
- Maven

## Estructura del proyecto
```tree
src/
├── main/
│   ├── java/
│   │   └── com/citamed/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── domain/
│   │       └── config/
│   └── resources/
│       ├── templates/
│       ├── static/
│       └── messages/
```
## Requisitos para ejecutar

- Java 17 o superior (jdk 21 establecido para el proyecto)
- Maven 3.8 o superior
- MySQL 8 o superior
- Crear la base de datos `citamed_db` antes de ejecutar
- Configurar credenciales en `application.properties`

## Acuerdo de trabajo por ramas

- `main` es la rama estable. No se permiten commits directos sobre ella.
- Cada funcionalidad se desarrolla en una rama propia: `feature/nombre-funcionalidad`
- Las correcciones se trabajan en ramas: `fix/descripcion-correccion`
- Todo cambio hacia `main` se realiza mediante un pull request revisado por al menos otro integrante del equipo antes de aceptarse.

### Ejemplos de nombres de ramas
- feature/autenticacion-roles
- feature/reserva-citas
- feature/crud-medicos
- feature/panel-admin
- fix/validacion-formulario

## Estado del proyecto

Avance 1 - Semana 5: historias de usuario, prototipo y modelo de datos completados.
