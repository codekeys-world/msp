# Gestion des assemblées

Le menu **Assemblées** est accessible au Secrétaire administratif, à l’Administrateur et au Sysadmin. Ces trois rôles peuvent rechercher, créer, modifier et supprimer une assemblée. La suppression demande une confirmation.

Chaque assemblée contient trois champs obligatoires : le nom (200 caractères maximum), la date et le lieu (255 caractères maximum). Les dates passées et futures sont acceptées.

Le tableau de bord utilise le total réel des assemblées enregistrées, accessible à tous les utilisateurs connectés. Pour les trois rôles de gestion, la carte ouvre le module. Un échec de chargement affiche un bouton de nouvelle tentative et ne remplace pas le total par zéro.

## Installation

La migration Flyway `V31__create_assemblee.sql` crée la table `_assemblee` au démarrage du backend. Elle ne crée aucune réunion fictive. Les accès utilisent les rôles existants et ne nécessitent aucune nouvelle permission à attribuer.

## API

Toutes les routes nécessitent une authentification. Les opérations de gestion sont limitées aux trois rôles ci-dessus.

| Méthode | Route | Usage |
| --- | --- | --- |
| GET | `/assemblees?page=0&size=8&q=` | Liste paginée, recherche par nom ou lieu |
| GET | `/assemblees/{slug}` | Consultation |
| POST | `/assemblees` | Création |
| PUT | `/assemblees/{slug}` | Modification |
| DELETE | `/assemblees/{slug}` | Suppression |
| GET | `/assemblees/statistiques` | Total pour le tableau de bord de tout utilisateur connecté |

Exemple de contenu JSON pour la création ou la modification :

```json
{"nom":"Assemblée générale","date":"2026-12-01","lieu":"Bamako"}
```

Les réponses suivent le format commun de l’application : `donnees.resultat`. Le total est renvoyé sous la forme `{"total":0}`. La liste est triée par date décroissante, puis par identifiant décroissant.
