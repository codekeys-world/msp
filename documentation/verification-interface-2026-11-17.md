# Vérification de l’interface — 17 novembre 2026

## Changements

- Bandeau de préférences cookies, refus et acceptation mémorisés six mois, choix modifiable, page d’information. Aucun traceur facultatif n’est installé ni chargé.
- Titres et descriptions par route, métadonnées Open Graph, pages privées et 404 exclues de l’indexation.
- Page 404 personnalisée, remplacement du lien de rapport inexistant et du bouton de contact sans action.
- Textes alternatifs, dimensions des images, décodage asynchrone et chargement différé des photos.
- Logos WebP : 684 728 → 13 534 octets et 72 737 → 12 816 octets. Les PNG sources sont conservés pour régénérer les versions optimisées.
- Couleurs de texte et états sélectionnés assombris, navigation au clavier visible, adaptation des formulaires, actions et conteneurs aux petites largeurs.
- Gzip Nginx, cache long pour les ressources avec empreinte, revalidation des images et absence de cache du document principal.
- Limitation côté serveur des écritures : 180/minute/adresse ; connexion : 20/minute/adresse ; demandes et réinitialisations de mot de passe : 5/15 minutes/adresse. Réponse 429 et en-tête `Retry-After`.

## Profil personnel

Les champs date de naissance, lieu de naissance, père, mère et adresse sont transmis par l’API et chargés dans le formulaire. Les réponses de sauvegarde réinitialisent désormais le formulaire avec les valeurs normalisées du serveur. La date est explicitement sérialisée au format `yyyy-MM-dd`. Les réponses du profil portent `Cache-Control: no-store`.

Le compte administrateur créé par la migration V30 ne possède initialement ni date, ni lieu de naissance, ni adresse ; les parents sont enregistrés comme chaînes vides. Le formulaire indique les informations non renseignées et permet de les compléter. Aucune information d’état civil n’est inventée et aucune migration historique n’a été modifiée.

Les tests couvrent le chargement des cinq champs, les valeurs absentes, l’enregistrement et la relecture, la normalisation, la récupération des données actuelles plutôt que celles de l’ancien principal de sécurité, ainsi que le rejet des dates futures et champs obligatoires vides.

## Résultats

- Build Angular de production réussi.
- 36 tests frontend réussis.
- 13 tests backend ciblés réussis : profil, anti-spam et assemblées.
- Nginx 1.27 : `nginx -t` réussi dans un conteneur temporaire sans exposition de port.
- 57 parcours × 3 largeurs (360, 768, 1440 px), soit 171 contrôles navigateur : aucune erreur JavaScript, image cassée, route inattendue, lien interne invalide ni débordement horizontal détecté.
- Audit axe WCAG A/AA des 57 parcours à 360 px : aucune violation détectée dans les états rendus.
- Refus du consentement conservé après rechargement, réouverture des préférences et menu mobile vérifiés.

Lighthouse mobile sur serveur local, fichiers de production compressés :

| Page | Performance | Accessibilité | Premier affichage | LCP | CLS |
| --- | ---: | ---: | ---: | ---: | ---: |
| Accueil | 100 | 100 | 0,6 s | 0,6 s | 0 |
| Connexion | 100 | 100 | 0,6 s | 0,6 s | 0 |
| Confidentialité | 100 | 100 | 0,6 s | 0,6 s | 0 |

## Portée des mesures

Les parcours privés utilisent une session et des réponses API de démonstration ; ce ne sont pas des essais avec des données réelles ni une vérification de toutes les combinaisons de droits et d’états métier. Les mesures Lighthouse n’incluent pas la latence du backend de production. Son absence provoque l’erreur de statistiques publiques relevée dans la console de l’accueil. Le score SEO réduit de la connexion correspond à son exclusion volontaire de l’indexation.

Le limiteur anti-spam utilise des compteurs en mémoire par instance et l’adresse fournie par le serveur HTTP. Derrière un reverse proxy, l’extraction de l’adresse cliente doit être configurée uniquement pour les proxys de confiance ; sans cela, leurs clients partagent le quota de l’adresse du proxy. Pour plusieurs instances, les quotas doivent être centralisés à l’entrée ou dans un stockage partagé. Aucun en-tête d’adresse envoyé directement par le client n’est accepté par le filtre.

## Reproduire

Depuis `msp_frontend`, avec Chrome installé :

```text
npm ci
npm run images:optimize
npm test -- --watch=false
npm run build
npm run audit:ui
npm run audit:performance
```

`CHROME_PATH` permet de choisir l’exécutable utilisé par Lighthouse. Les rapports détaillés et captures sont générés dans `msp_frontend/reports/`, exclu du dépôt.

Depuis `msp_backend` sous PowerShell :

```powershell
.\mvnw.cmd -q '-Dtest=ProfilUtilisateurTest,AntiSpamFilterTest,AssembleeSecurityTest,AssembleeServiceTest' test
```
