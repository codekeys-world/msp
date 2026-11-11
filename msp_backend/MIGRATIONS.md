# Migrations PostgreSQL avec Flyway

Spring Boot 4 charge Flyway avec `spring-boot-starter-flyway` et le module `flyway-database-postgresql`. Le parent Spring Boot gère leurs versions.

Les scripts sont dans `src/main/resources/db/migration` :

- **V1 à V26** : une migration par table, soit 24 entités et 2 tables d'association. Colonnes héritées, identités, unicité, contraintes d'énumération et index correspondent aux mappings Hibernate.
- **V27** : les 46 clés étrangères, ajoutées après la création des tables pour gérer les références circulaires.
- **V28** : contraintes des cas sociaux auparavant gérées à chaque démarrage par `CasSocialSchemaUpdater`. Les valeurs historiques restent autorisées, comme avant cette intégration.

Hibernate utilise désormais `ddl-auto=validate` en développement et en production. Flyway applique les migrations avant la validation Hibernate et conserve leur historique et leur checksum dans `flyway_schema_history`.

## Nouvelle base vide

Configurer les variables habituelles (`ACTIVE_PROFILE=prod`, `DB_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, ainsi que les variables de sécurité, de fichiers et de mail), puis démarrer le backend. Les 28 migrations s'exécutent automatiquement. L'utilisateur PostgreSQL doit pouvoir créer les tables, les index et les contraintes dans le schéma cible.

Ne pas activer `baseline-on-migrate` pour une nouvelle base.

## Base existante créée par Hibernate

Une base non vide sans historique Flyway est refusée par défaut. Il faut établir explicitement son point de départ ; ne pas lancer les migrations de création dessus.

1. Sauvegarder la base et travailler d'abord sur une copie de restauration.
2. Arrêter les instances du backend pendant cette transition.
3. Comparer le schéma existant aux migrations V1 à V27 : les 26 tables, les colonnes, leurs types, les contraintes et les index doivent déjà exister. Les noms des clés étrangères peuvent différer. Une baseline ne crée ni ne répare les objets manquants. Hibernate `validate` ne vérifie pas tous les index et toutes les contraintes.
4. Sur une base confirmée équivalente à V27, définir **uniquement pour le premier démarrage** :

   ```text
   SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
   SPRING_FLYWAY_BASELINE_VERSION=27
   ```

   Flyway inscrit la baseline 27 sans rejouer V1 à V27, puis applique V28. Cette opération n'a pas été exécutée sur la base de l'application par l'agent.

5. Retirer ces deux variables après ce premier démarrage réussi, puis redémarrer normalement. `baseline-on-migrate=false` reste la configuration par défaut.

Si le schéma existant ne correspond pas à V27, préparer une migration de mise à niveau adaptée aux écarts constatés avant de définir la baseline. Ne pas utiliser `ddl-auto=update` pour masquer les différences.

## Évolutions suivantes

Créer `V29__description_du_changement.sql`, puis V30, etc. Ne jamais modifier une migration déjà appliquée. Tester chaque évolution sur une base vide et sur une copie de la base existante. `clean` est désactivé dans l'application.

## Vérification reproductible

Les tests nécessitent une **base PostgreSQL de test dédiée**. Ils créent et suppriment uniquement des schémas aléatoires préfixés `msp_flyway_test_`. La base de l'application ne doit pas être utilisée.

Exemple PowerShell, après création d'une base de test :

```powershell
$env:MSP_MIGRATION_TEST_URL = 'jdbc:postgresql://localhost:5432/msp_migration_test'
$env:MSP_MIGRATION_TEST_USER = 'postgres'
$env:MSP_MIGRATION_TEST_PASSWORD = '<mot-de-passe-de-test>'
./mvnw.cmd '-Dtest=FlywayMigrationTest,EncaissementPerformanceTest,DepensePerformanceTest' test
```

Les tests couvrent la migration d'une base vide, la validation de toutes les entités, les clés étrangères et index, un second passage sans modification, la reprise avec baseline et conservation des données, et l'ordre Flyway puis Hibernate dans Spring Boot. Sans `MSP_MIGRATION_TEST_URL`, les tests PostgreSQL sont ignorés explicitement.

Pour exporter le schéma Hibernate courant dans `target/hibernate-schema.sql` et le comparer aux migrations, sans connexion à une base :

```powershell
./mvnw.cmd '-Dtest=HibernateSchemaExportTest' '-Dmsp.export-schema=true' test
```

Cet export sert à la revue ; il ne remplace pas automatiquement les migrations versionnées.
