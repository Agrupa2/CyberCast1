[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/Jd7ILUgB)


## Swapsounds
## 👋 Hola!! 

# 🎧SwapSounds - Comparte y Descarga Sonidos Libres

Bienvenido a *SwapSounds, una plataforma intuitiva y colaborativa donde los usuarios pueden **subir, compartir* y *descargar* sonidos en múltiples formatos. Diseñada con pasión por un equipo de cuatro desarrolladores comprometidos.

---
## 🗂️ Entidades

| <center>Entidad</center> | <center>Descripción</center>                                                                                                     |
| ------------------------ | -------------------------------------------------------------------------------------------------------------------------------- |
| *User*👤                 | Representa a cada usuario de la plataforma. Contiene datos como nombre, email, contraseña y roles.                               |
| *Sound* 🔊               | Archivo de audio subido por un usuario. Incluye título, descripción, fecha de subida, URL y duración.                            |
| *Category* 🏷️           | Clasificación temática o tipo de sonido (por ejemplo: naturaleza, efectos, música, etc.). Cada sonido pertenece a una categoría. |
| *Comment*💬              | Comentarios realizados por los usuarios en cada sonido. Incluyen contenido, autor fecha.                                         |

## 🔗 Relaciones

| <center>Relación</center> | <center>Descripción</center>                         |
| ------------------------- | ---------------------------------------------------- |
| `@OneToMany`              | Un usuario puede tener múltiples comentarios.        |
| `@OneToMany`              | Un usuario puede tener múltiples sonidos.            |
| `@OneToMany`              | Un sonido puede tener múltiples comentarios.         |
| `@ManyToMany`             | Múltiples sonidos pertenecen a múltiples categorías. |
| `@ManyToOne`              | Múltiples comentarios pertenecen a un usuario.       |
| `@ManyToOne`              | Múltiples comentarios pertenecen a un sonido.        |
| `@ManyToMany`             | Múltiples categorías pertenecen a múltiples sonidos. |

## 🛡️ Roles 

| <center>Tipo de usuario</center> | <center>Permisos</center>                                                                                                                                                                                                                                                                                                                                                    |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Anonimous                        | Ver y reproducir el contenido de la página web, acceder al panel de login.                                                                                                                                                                                                                                                                                                   |
| User                             | Ver y reproducir el contenido de la página web, acceder al panel de login. Subir sonidos, descargar sonidos, realizar comentarios y editarlos junto con sus sonidos, además de poder editar su perfil (nombre y foto). También pueden borrar su propia cuenta. Los usuarios pueden ver el número de sonidos y comentarios que tienen .                                       |
| Admin                            | Ver y reproducir el contenido de la página web, acceder al panel de login. Subir sonidos, descargar sonidos, realizar comentarios y editarlos junto con sus sonidos, además de poder editar su perfil (nombre y foto). Puede ver el número de sonidos y comentarios que tienen los usuarios. Dispone de un panel "Admin Panel" que puede borrar tanto usuarios como sonidos. |

## 🖼️ Imágenes





## 📊 Diagrama

![[swapsoundsEnBlanco.png]]

## 🌟 Características principales

- 📤 *Subida rápida* de archivos de audio (.mp3, .wav, etc.).
- 📥 *Descarga sin límites* para todos los usuarios.
- 🔍 Filtros y buscador para encontrar sonidos por nombre, categoría o duración.
- 🔗 *API REST* para integraciones externas.
- 🎨 Interfaz simple y rápida gracias a *Mustache* como motor de plantillas.

---

## 💻 Lenguajes de programación

| <center>Lenguaje</center> | <center>Uso principal</center> |
| ------------------------- | ------------------------------ |
| ☕ *Java*                  | Backend con Spring Boot.       |
| 🌐 *HTML*                 | Estructura del frontend.       |
| 🎨 *CSS*                  | Estilos visuales del frontend. |
| ⚙️ *JavaScript*           | Interactividad en la interfaz. |

---

## 🛠️ Tecnologías utilizadas

| <center>Categoría</center> | <center>Herramientas / Frameworks</center> |
| -------------------------- | ------------------------------------------ |
| 🔧 Backend                 | Spring Boot (Java)                         |
| 🖼️ Frontend               | Mustache como motor de plantillas          |
| 🔗 API                     | API REST con controladores Spring          |
| 💾 Base de datos           | MySQL y/o H2                               |
| ☁️ Almacenamiento          | Almacenamiento local                       |
| 🧪 Control de versiones    | Git + GitHub                               |

---

## 👥 Equipo de desarrollo


| <center>Nombre</center> | <center>Apellidos</center> | <center>Correo</center>              | <center>GitHub</center> |
| ----------------------- | -------------------------- | ------------------------------------ | ----------------------- |
| Sergio                  | Fernández-Dávila Marcos    | se.fernandezdav.2023@alumnos.urjc.es | sefernandezdav2023      |
| Alejandro               | González Martínez          | a.gonzalezmart.2019@alumnos.urjc.es  | 4lexxxx                 |
| Miguel Eduardo          | del Pino Sánchez           | me.delpino.2023@alumnos.urjc.es      | myyykyy                 |
| Carla                   | Gómez Cabanillas           | c.gomez.2023@alumnos.urjc.es         | carlagmezc              |


---

##  🏆 Top commits

--------------------
@sefernandezdav2023 
--------------------
1. 

2.

3.

4.

5.
--------------------
@4lexxxx
--------------------
1. 

2.

3.

4.

5.

--------------------
@myyykyy
--------------------
1. 

2.

3.

4.

5.

--------------------
@carlagmezc
--------------------
1. 

2.

3.

4.

5.

--------------------
## 🚀 Cómo ejecutar el proyecto localmente

```bash
# Clonar el repositorio
git clone https://github.com/DWS-2025/project-grupo-10.git

# Ejecutar con Maven
./mvnw spring-boot:run
