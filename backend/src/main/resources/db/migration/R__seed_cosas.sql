-- Seed repeatable: ~30 palabras para el banco de cosas
-- Idempotente: salta las que ya existan (mismo nombre activo)
INSERT INTO cosas (id, nombre, tipo, activo)
SELECT gen_random_uuid(), v.nombre, 'PALABRA', true
FROM (VALUES
    ('silla'),
    ('perro'),
    ('cafe'),
    ('libro'),
    ('piano'),
    ('luna'),
    ('arbol'),
    ('llave'),
    ('camara'),
    ('pelota'),
    ('sombrero'),
    ('guitarra'),
    ('reloj'),
    ('bicicleta'),
    ('montana'),
    ('espejo'),
    ('telefono'),
    ('zapato'),
    ('ventana'),
    ('manzana'),
    ('dinosaurio'),
    ('astronauta'),
    ('submarino'),
    ('castillo'),
    ('paraguas'),
    ('pijama'),
    ('tijera'),
    ('volcan'),
    ('mariposa'),
    ('elefante')
) AS v(nombre)
WHERE NOT EXISTS (
    SELECT 1
    FROM cosas c
    WHERE lower(c.nombre) = lower(v.nombre)
      AND c.deleted_at IS NULL
);
