# build — Compilar y empaquetar el proyecto

```bash
# Backend
cd backend
mvn clean verify -DskipTests

# Frontend
cd frontend
npm run build
```

El backend genera un JAR ejecutable en `backend/target/`. El frontend genera los estáticos en `frontend/dist/`.
