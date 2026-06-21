# dev — Levantar el entorno de desarrollo completo

```bash
# 1. Infraestructura (PostgreSQL persistente)
docker-compose up -d

# 2. Backend — en terminal separada
cd backend
mvn spring-boot:run

# 3. Frontend — en terminal separada
cd frontend
npm install   # solo la primera vez o tras cambios en package.json
npm run dev
```

| Servicio   | URL                          |
| ---------- | ---------------------------- |
| Backend    | http://localhost:8093        |
| Frontend   | http://localhost:5183        |
| WebSocket  | ws://localhost:8093/ws       |
| PostgreSQL | localhost:5443 / db infiltrado |

El frontend tiene proxy configurado en Vite para `/api` y `/ws`, por lo que las peticiones
del navegador van a `localhost:5183` y se redirigen automáticamente al backend.
