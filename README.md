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

## 📊 Diagrama

![Logo GitHub](https://github.com/DWS-2025/project-grupo-10/blob/main/src/main/resources/static/images/swapsoundsEnBlanco.png)

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
1. Securizing api requests by filtering by role and checking the session user with principal. <a href="https://github.com/DWS-2025/project-grupo-10/commit/e076255b5636240f261eaa2b59f5fcd781750fd0">e076255</a>

2. Modifying the old logic that allowed users to login and signup, now using spring security settings and password encrypting for saving in the database.<a href="https://github.com/DWS-2025/project-grupo-10/commit/d034df446b981575ca8508ba705259569a97f1de">d034df4</a> 

3. Implementing some sanitization on all the files and Strings submitted by users with owasp sanitization and file validation. <a href="https://github.com/DWS-2025/project-grupo-10/commit/33acbf7c8335d2074c6427ae5208d8d59e2fe322">33acbf7</a>

4. New admin panel where users with role Admin are able to check all the sounds and users currently in the database and registered, the panel includes the deletion of the elements on a direct link. <a href="https://github.com/DWS-2025/project-grupo-10/commit/b20dff474d86c79ad15f6f94d4c9eed80551e8cf">b20dff4</a>

5. Adding the local disc file inclusion to the web page, users logged are able to upload to the web a sound which won't be visible for the rest, it will be keeped locally and it's available for download. Security features included on it's addition. <a href="https://github.com/DWS-2025/project-grupo-10/commit/5a0c2df3ce0aaa8862158b7a3b0f0994ccca2d0e">5a0c2df</a>

<p>The file I have worked more on has been the SecurityConfig.java one, adding rules to access different resources.</p>

--------------------
@4lexxxx
--------------------
1. Adding all the functions for deleting sounds from the admin panel **4ce9d39a67099e22caba5c934cd7610f665f017b** && **eb82becc499957ba9c62a89ed1ffd8b78c477efd**

2. Adding the download funcionality for the Sounds **a43fe4d21fed99d0e4b6911aefec1824851ff366**

3. First steps for the DB **20e0b5507370f4519754e42ded29aade0490a7a0**

4. Changing all the stuff to Blob, (the new audioBlob and imageBlob are the old FilePath and imagePath) **6b5240d5d91247d0d6e852cc1687628739d8c849**

5. Changing the Data Loader for the second fase type **bb66468fadec6f09e0d3648f054ec20b20f66b36**

--------------------
@myyykyy
--------------------

My task during the practice was mainly focused on the comments and categories sections. Throughout the semester, I have been modifying the files related to these two entities as we progressed through the final part. Finally, towards the end of the practice, I was responsible for implementing dynamic queries for the Sound entity, allowing filtering by title, exact duration, category, and userId (the user who may have uploaded sounds). I also took care of enabling the editing and deleting of categories through the REST API.

1. Creating the Dynamic queries for the entity sound modifiying this files:SoundControler, SoundRepository y SoundService. 
 **ee74fdfbfc01b508263ed4d140badf7f51f25b19**

2. Creating the CommentRepository and CommentService files. **29d50a8ec96bbbfff0682329ad85f25206d909ac**

3. Crating and configuring CategoryService.  **7d299a112403a039d5df870301dcb2c28a62eaf0**

4. Creating the editing, creation and deletion functions of the comment entity. **2a073ed23c4b5594ae94ee3c9406807115ba6e36**

5. Making the transition from the CommentRepository to the CommentService. **e8dda06104b851f71f38e12e07e63bf7fdf70530**

--------------------
@carlagmezc
--------------------
My work on this project has been divided primarily between the User and Sound categories. Despite joining this working group later, we structured the tasks and worked as a team to learn from each other. I've been in charge of adding functionality, helping with security, adding pagination, structuring the database, working with the API, among other areas. In this final phase, I've worked with Java, HTML, and JavaScript to provide a connection throughout the project. Finally, I've also been in charge of creating the visual diagrams that appear in this document. 

1. Adding different policies, sanitizers and security, as well as fixing some bugs we had previously. **45c39ceccf621e998b68f15bf2f9b2d483e59f71** 

2. Improve security in "SecurityConfig" because the previously created code caused problems with sounds when compiling; and improve functionality. **e01894cbafd01c94efd1316bbeb9e8420ab8c031** 

3. Add pagination functionality, with reload button and other settings **e586b33f84b3e5986cdd50a4df14f951f0e97ab2** 

4. Include web pagination, with javascript, html and the Services **6bf7c3e41acbf233979e3994081ec80341d0c344** (165 commits)

5. Add functionality to the UserService and AuthService **9952324bd2e105e1baebce3c015a006e413bd6fc**

--------------------
## 🚀 Cómo ejecutar el proyecto localmente

```bash
# Clonar el repositorio
git clone https://github.com/DWS-2025/project-grupo-10.git

# Ejecutar con Maven
./mvnw spring-boot:run
