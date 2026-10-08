# Policlinico_admin — Panel administrador

Aplicación **Spring Boot (Java 17) + Thymeleaf**. Al abrirla pide **usuario y contraseña**; adentro hay un dashboard
para administrar **doctores, horarios, especialidades/precios y citas** (confirmar pagos de Yape).

```
Web pública (Vercel) ──┐
                       ├──► Backend Java (Render) ──► Neon (base de datos)
Panel admin (Render) ──┘
```
El panel NO toca la base de datos: usa la API del backend que ya existe (con las credenciales de administrador
guardadas como variables de entorno del servidor, nunca en el navegador).

## Variables de entorno

| Variable | Para qué | Ejemplo |
|---|---|---|
| `BACKEND_URL` | URL de su backend en Render | `https://policlinico-backend.onrender.com` |
| `BACKEND_USER` | Usuario admin del **backend** (`ADMIN_USER`) | `admin` |
| `BACKEND_PASSWORD` | Clave admin del **backend** (`ADMIN_PASSWORD`) | *(la misma que puso en el backend)* |
| `PANEL_USER` | Usuario para **entrar a este panel** | `admin` |
| `PANEL_PASSWORD` | Clave para entrar a este panel (larga; distinta a la del backend). **Sin ella nadie entra.** | |
| `COOKIE_SECURE` | `true` cuando esté en Render (HTTPS) | `true` |

## 1) Probar en IntelliJ IDEA
1. `File > Open` y elija la carpeta `Policlinico_admin` (donde está `pom.xml`). Espere a que Maven descargue.
2. `File > Project Structure > SDK`: **Java 17** (o superior).
3. Abra `PoliclinicoAdminApplication` > `Run > Edit Configurations` > *Environment variables* y pegue, por ejemplo:
   `BACKEND_URL=https://TU-BACKEND.onrender.com;BACKEND_PASSWORD=LA_CLAVE_DEL_BACKEND;PANEL_PASSWORD=UnaClaveLarga123`
4. Ejecute (▶) y abra <http://localhost:8082>.

## 2) Subir a GitHub
En Git Bash, dentro de la carpeta:
```bash
git init
git add .
git commit -m "Panel administrador"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/Policlinico_admin.git
git push -u origin main
```
(o en IntelliJ: `Git > GitHub > Share Project on GitHub`). Nunca suba contraseñas: van solo en Render.

## 3) Publicar en Render
1. Render > **New > Web Service** > conecte el repo `Policlinico_admin`.
2. **Language: Docker** (usa el `Dockerfile` incluido). Plan: Free.
3. En **Environment** agregue las variables de la tabla de arriba.
4. **Create Web Service**. Cuando termine, abra la URL que le da Render: verá el login.

> No hace falta tocar CORS en el backend: el panel habla con el backend de servidor a servidor.
> En el plan gratis, ambos servicios se duermen; el primer ingreso puede tardar cerca de 1 minuto.
