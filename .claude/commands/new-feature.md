# new-feature — Flujo para agregar una funcionalidad

Seguir este orden para respetar la Arquitectura Hexagonal y mantener la separación de capas.

## 1. Dominio (`[modulo]/domain/`)

- Definir/extender entidades y value objects (sin anotaciones Spring ni JPA).
- Declarar el puerto (interfaz) que necesita el caso de uso.
- Agregar reglas de negocio como métodos del dominio o como validadores internos.
- Escribir pruebas unitarias del dominio.

## 2. Aplicación (`[modulo]/application/`)

- Implementar el caso de uso que orquesta dominio + puertos.
- El caso de uso recibe DTOs de entrada (Records) y devuelve DTOs de salida (Records).
- No accede directamente a JPA ni a HTTP.

## 3. Infraestructura — Persistencia (`[modulo]/infrastructure/`)

- Implementar el adaptador JPA (la interfaz del puerto del dominio).
- Agregar entidad JPA y repositorio Spring Data si es necesario.
- Crear migración Flyway si el schema cambia (`Vn__descripcion.sql`).

## 4. Infraestructura — Web / WS (`[modulo]/infrastructure/`)

- Implementar el controller REST o handler STOMP que invoca el caso de uso.
- Validar inputs con Jakarta Validation en la firma del método.
- Manejar errores con `@RestControllerAdvice` (ya configurado en `shared`).
- **Seguridad:** verificar que el endpoint no filtre roles, códigos ni datos sensibles.

## 5. Frontend (`frontend/src/features/<feature>/`)

- Crear/extender el hook de React Query para el endpoint nuevo.
- Si hay estado persistente, actualizar el Zustand store correspondiente.
- Implementar la vista/componente.
- Conectar al WS si la feature emite/recibe eventos STOMP.

## 6. Pruebas

- Prueba unitaria: lógica de dominio y caso de uso (mocks de puertos).
- Prueba de integración: controller → caso de uso → BD real (`test_infiltrado`).
- Verificar que ningún log ni respuesta filtre datos sensibles.

## Checklist de seguridad antes de hacer commit

- [ ] El rol/carta del jugador no aparece en respuestas de listado.
- [ ] El `codigo_4_digitos` no se registra en ningún log.
- [ ] Los eventos WS de difusión no incluyen roles antes de `REVELACION`.
- [ ] Inputs externos validados con Jakarta Validation.
- [ ] Errores devueltos con respuesta saneada (sin stack trace).
