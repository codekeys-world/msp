-- Remplace CasSocialSchemaUpdater : cette evolution s'execute une seule fois.
-- Conserve les anciennes valeurs autorisees pour ne pas invalider les donnees historiques.
DO $$
DECLARE
    obsolete_constraint record;
BEGIN
    FOR obsolete_constraint IN
        SELECT DISTINCT constraint_definition.conname
        FROM pg_constraint constraint_definition
        JOIN pg_class table_definition ON table_definition.oid = constraint_definition.conrelid
        JOIN pg_namespace schema_definition ON schema_definition.oid = table_definition.relnamespace
        JOIN unnest(constraint_definition.conkey) column_number(attnum) ON true
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = table_definition.oid
         AND column_definition.attnum = column_number.attnum
        WHERE constraint_definition.contype = 'c'
          AND schema_definition.nspname = current_schema()
          AND table_definition.relname = '_cas_social'
          AND column_definition.attname IN ('type_cas', 'lien_defunt')
    LOOP
        EXECUTE format('ALTER TABLE _cas_social DROP CONSTRAINT %I', obsolete_constraint.conname);
    END LOOP;
END $$;

ALTER TABLE _cas_social
    ADD CONSTRAINT ck_cas_social_type_cas CHECK (type_cas IN (
        'MALADIE', 'ACCIDENT_CIRCULATION', 'RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE',
        'DECES', 'REFORME_INVALIDITE', 'DECES_ADHERENT', 'RETRAITE'
    ));

ALTER TABLE _cas_social
    ADD CONSTRAINT ck_cas_social_lien_defunt CHECK (
        lien_defunt IS NULL OR lien_defunt IN ('ADHERENT', 'PERE', 'MERE', 'EPOUX', 'EPOUSE', 'ENFANT')
    );
