# Manuel utilisateur — Administrateur système

Mutuelle des Surveillants de Prison du Mali (MSP). Édition novembre 2026.

## 1. Votre rôle et les repères de ce guide

Superviser les accès et diagnostiquer les difficultés d’utilisation à partir des écrans d’administration et du journal.

### Ce que vous devez préparer

- L’adresse de l’application communiquée par votre organisation et vos identifiants personnels.
- Pour une saisie : les références, dates, montants et pièces du dossier. Vérifiez leur lisibilité avant de commencer.
- Pour une consultation : un critère de recherche précis, par exemple un matricule, une référence ou une période.

### Comprendre vos accès

Les menus sont calculés à partir du rôle et des autorisations de votre compte. Une fonction décrite dans ce guide peut être absente si votre compte n’est pas habilité. Demandez au responsable des accès de vérifier la configuration ; ne cherchez pas à contourner le menu.





![Repère 1 : menu à gauche, espace de travail au centre, identité et rôle en haut à droite — écran de démonstration](captures/accueil-sysadmin.png)


### Lire les illustrations et les commandes

Les images sont de véritables captures de l’interface, réalisées localement avec une API de démonstration et des données fictives. Les écrans de modules sont mutualisés et montrent un compte Administrateur habilité ; les captures du tableau de bord sont propres à chaque rôle. Les listes vides illustrent un écran sans dossier, et non une panne. Les valeurs visibles ne sont pas des données d’exploitation.


| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Vert MSP | Actions principales et repères de navigation ; couleur de référence #0F7D4B. |
| Étoile près d’un champ | Information obligatoire ; compléter également les champs conditionnels affichés. |
| Message rouge | Erreur à lire et corriger avant de recommencer. |
| Bouton désactivé | Action indisponible tant que le formulaire est incomplet, qu’un chargement est en cours ou qu’aucune donnée n’est disponible. |
| Chargement | Attendre le résultat avant de cliquer à nouveau. |


## 2. Se connecter et retrouver son espace


![Connexion : identité MSP et formulaire d’accès — écran de démonstration](captures/connexion.png)


### Connexion pas à pas

1. Ouvrez l’adresse de l’application dans votre navigateur, puis accédez à la page de connexion.
2. Dans Identifiant, saisissez le téléphone, le matricule ou l’adresse e-mail de votre compte, comme indiqué dans le champ. Renseignez ensuite votre mot de passe.
3. Vérifiez la saisie, puis utilisez le bouton de connexion. Attendez la redirection.
4. Sur le tableau de bord, contrôlez le nom et le rôle en haut à droite. Si l’identité ne correspond pas à votre compte, déconnectez-vous.


### Mot de passe oublié ou accès refusé

Utilisez le lien de mot de passe oublié présent sur la page de connexion. Renseignez l’adresse e-mail demandée, puis suivez le lien de réinitialisation reçu. Sur la page de réinitialisation, choisissez et confirmez le nouveau mot de passe. Si le lien n’est plus valable, recommencez la demande ; si vous n’avez pas accès à l’adresse enregistrée, contactez le responsable des comptes.

Si vos identifiants sont refusés, vérifiez la saisie et le verrouillage des majuscules. Si l’échec persiste, faites vérifier l’état du compte. Une erreur de chargement après connexion doit être distinguée d’un mot de passe incorrect.

## 3. Utiliser le tableau de bord et télécharger son manuel


![Accueil de votre rôle : le bloc Manuels d’utilisation permet de télécharger le PDF — écran de démonstration](captures/accueil-sysadmin.png)


1. Repérez le menu latéral. Le bouton à trois traits permet de réduire ou développer ce menu.
2. Lisez les indicateurs affichés, puis ouvrez un module autorisé si vous devez examiner les dossiers correspondants.
3. Dans Manuels d’utilisation, cliquez sur le bouton du guide associé à votre rôle.
4. Attendez la fin du téléchargement, puis ouvrez le PDF depuis les téléchargements du navigateur.
5. Utilisez le sommaire cliquable ou les signets du lecteur PDF pour retrouver une procédure.


> Si les manuels ne se chargent pas, utilisez Réessayer. Si un téléchargement échoue, vérifiez votre connexion et recommencez après avoir relu le message. Un manuel téléchargé est une copie : téléchargez-le à nouveau après une mise à jour de l’application.

Les statistiques sont des repères de suivi. Le contenu et les raccourcis varient selon les permissions ; un total affiché ne donne pas automatiquement accès à toutes les fiches.

## 4. Activer ou désactiver un compte utilisateur

**Accès :** Menu Utilisateurs, réservé aux fonctions d’administration des accès.

**Objectif :** Contrôler le statut d’accès d’une personne sans confondre cette opération avec la suppression de son dossier.


![Activer ou désactiver un compte utilisateur : zones à repérer dans le module — écran de démonstration](captures/utilisateurs.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Utilisateur / matricule | Identité et référence à comparer avant toute action. |
| Rôle | Fonction affichée pour le compte. |
| Statut | Actif ou Désactivé. |
| Actions | Bouton d’activation ou de désactivation selon l’état actuel. |

### Procédure pas à pas

1. Ouvrez Utilisateurs et retrouvez le compte dans les pages de résultats.
2. Comparez prénom, nom, matricule et adresse e-mail.
3. Lisez le statut actuel et vérifiez l’effet du bouton proposé.
4. Déclenchez l’action voulue et confirmez si une confirmation est demandée.
5. Attendez la réponse puis contrôlez le badge de statut.
6. En cas de problème d’accès, vérifiez également le rôle et les autorisations avec le responsable de l’administration.

### Résultat attendu et contrôles

Désactiver l’accès ne supprime pas le dossier d’adhérent. Cette page présente la gestion du statut ; elle ne propose pas, dans cette version, un formulaire complet d’affectation des rôles et permissions.



## 5. Contrôler le journal des actions

**Accès :** Menu Commission de Contrôle ou Logs Utilisateurs pour les comptes habilités au journal.

**Objectif :** Examiner les opérations enregistrées, leur auteur, leur date et leur résultat.


![Contrôler le journal des actions : zones à repérer dans le module — écran de démonstration](captures/controle.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Recherche | Rechercher un acteur ou un terme de l’action. |
| Module | Limiter les résultats à un domaine de l’application. |
| Statut | Distinguer les opérations réussies et les échecs. |
| Période | Toutes les dates, aujourd’hui ou les périodes proposées. |
| Détails | Ouvrir la ligne pour lire le contexte conservé. |

### Procédure pas à pas

1. Ouvrez le journal et commencez par une période correspondant au contrôle.
2. Choisissez le module puis le statut à examiner.
3. Saisissez un terme précis et parcourez les résultats avec la pagination.
4. Ouvrez le détail d’une action pour comparer auteur, date, module, résultat et description.
5. Recoupez avec la pièce ou le dossier métier si votre compte y a accès.
6. Conservez les références utiles pour demander une explication au responsable concerné.

### Résultat attendu et contrôles

Un journal vide sous un filtre ne prouve pas l’absence d’activité. Élargissez la période et retirez les filtres. Une trace technique ne remplace pas le justificatif métier.



## 6. Rechercher et consulter un adhérent

**Accès :** Menu Adhérents. Cette liste est accessible aux fonctions habilitées ; le simple rôle Adhérent ne fait pas apparaître ce menu dans la version actuelle.

**Objectif :** Retrouver un membre sans créer de doublon et consulter les informations utiles à votre travail.


![Rechercher et consulter un adhérent : zones à repérer dans le module — écran de démonstration](captures/adherents.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Recherche | Utiliser les critères indiqués dans la zone de recherche, par exemple le nom ou le matricule. |
| Pagination | Changer de page lorsque le résultat ne tient pas sur un seul écran. |
| Actions | Ouvrir la fiche lorsque votre fonction l’autorise ; les commandes de modification et de carte dépendent de vos droits. |

### Procédure pas à pas

1. Ouvrez Adhérents dans le menu latéral.
2. Saisissez un critère précis. Vérifiez l’orthographe et retirez les filtres trop restrictifs si aucun résultat ne s’affiche.
3. Comparez le prénom, le nom et le matricule avant de sélectionner la personne. Deux personnes peuvent porter le même nom.
4. Ouvrez la fiche si l’action est proposée. Consultez les coordonnées, la famille et les pièces jointes nécessaires.
5. Revenez à la liste pour poursuivre votre recherche.

### Résultat attendu et contrôles

Le matricule et l’identité doivent correspondre au dossier demandé. Une liste vide ne signifie pas nécessairement qu’un membre n’existe pas : contrôlez la recherche et la page sélectionnée.

### Export de la liste
Lorsque le bouton d’export est présent, cliquez dessus puis ouvrez le fichier téléchargé dans votre tableur. Vérifiez les colonnes et le nombre de lignes. L’export peut contenir des informations personnelles et familiales ; conservez-le dans l’emplacement de travail prévu. Un message « Aucun adhérent à exporter » indique que l’export ne contient aucune ligne.

## 7. Créer et mettre à jour un dossier d’adhérent

**Accès :** Menu Nouvel adhérent ; modification depuis la fiche ou la liste. Gestion réservée aux fonctions habilitées.

**Objectif :** Constituer un dossier complet comprenant l’identité, les coordonnées et, si nécessaire, la famille.


![Créer et mettre à jour un dossier d’adhérent : zones à repérer dans le module — écran de démonstration](captures/nouvel-adherent.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Identité obligatoire | Nom, prénom, date et lieu de naissance, genre, matricule. Utiliser les informations des pièces justificatives. |
| Coordonnées | Adresse e-mail et téléphone principal ; téléphone secondaire et adresse si disponibles. |
| Références et parents | Numéro d’adhésion lorsqu’il est connu, nom complet du père et de la mère. |
| Photo | Choisir un fichier image net. La photo pourra être utilisée pour le profil et la carte. |
| Conjoint | Renseigner les champs marqués d’une étoile, dont identité, téléphone, adresse et certificat de mariage. |
| Descendants | Ajouter chaque enfant séparément. Compléter l’état civil et joindre les documents disponibles. |

### Procédure pas à pas

1. Recherchez d’abord le membre dans Adhérents pour éviter un doublon. Préparez les pièces et les coordonnées.
2. Ouvrez Nouvel adhérent. Remplissez les champs d’identité puis les coordonnées.
3. Ajoutez une photo si elle est disponible et vérifiez l’aperçu.
4. Ajoutez uniquement les conjoints et descendants concernés. Une ligne familiale ajoutée doit être complétée avant l’enregistrement.
5. Sélectionnez les pièces demandées dans les champs de la personne concernée. Un fichier choisi pour un conjoint ne remplace pas celui d’un autre.
6. Cliquez sur le bouton d’enregistrement et attendez le message de résultat. Corrigez les champs signalés si le formulaire est refusé.
7. Retrouvez la personne dans la liste et ouvrez sa fiche pour contrôler les informations enregistrées.

### Résultat attendu et contrôles

Le dossier doit apparaître une seule fois, avec le bon matricule, les bons noms et les bons liens familiaux. En modification, relisez les informations existantes avant de les remplacer.

### Pièces familiales
Dans la fiche détaillée, utilisez les zones prévues pour joindre ou consulter les actes et certificats. Si une pièce ne s’ouvre pas, vérifiez son format et sa lisibilité avant de la sélectionner à nouveau. Ne remplacez pas un document d’une autre personne pour contourner un champ incomplet.

## 8. Préparer et imprimer une carte de membre

**Accès :** Menu Cartes de membre, pour les comptes habilités à confectionner une carte.

**Objectif :** Produire une carte à partir d’un dossier vérifié, avec le thème et les éléments graphiques proposés par l’application.


![Préparer et imprimer une carte de membre : zones à repérer dans le module — écran de démonstration](captures/cartes.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Adhérent | Rechercher le membre et vérifier son identité. |
| Thème | Vert Officiel MSP, Bleu Sécurité ou Élégance Or & Vert. |
| Délivrance | Contrôler la date de délivrance avant impression. |
| Visuel | Vérifier la photo, les informations du recto et du verso ainsi que le QR code. |

### Procédure pas à pas

1. Ouvrez Cartes de membre. Recherchez puis sélectionnez la bonne personne.
2. Choisissez le thème souhaité. Vérifiez le rendu du recto et du verso.
3. Renseignez la date et les options disponibles. Si un visuel complémentaire est proposé, utilisez un fichier lisible.
4. Relisez le nom, les références et la photo. Corrigez le dossier d’adhérent si une donnée de base est inexacte.
5. Utilisez la commande d’impression. Dans la fenêtre du navigateur, choisissez l’imprimante ou Enregistrer au format PDF.
6. Contrôlez le résultat avant de distribuer la carte.

### Résultat attendu et contrôles

La carte doit rester lisible et correspondre au membre sélectionné. Le fichier d’impression est distinct du manuel PDF téléchargé sur le tableau de bord.



## 9. Enregistrer et suivre le courrier

**Accès :** Menu Courriers pour la consultation ; Nouveau courrier et les actions de modification nécessitent les droits correspondants.

**Objectif :** Conserver une référence, un objet, un correspondant et le document du courrier entrant ou sortant.


![Enregistrer et suivre le courrier : zones à repérer dans le module — écran de démonstration](captures/courrier.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Référence | Identifiant permettant de retrouver le courrier. |
| Type de courrier | Courrier entrant ou courrier sortant. |
| Objet | Résumé précis de la demande ou de la réponse. |
| Expéditeur / destinataire | Auteur du courrier reçu ou destinataire du courrier envoyé. |
| Date | Date de réception pour un entrant, date d’envoi pour un sortant. |
| Document | Obligatoire à la création ; en modification, laisser le fichier inchangé pour conserver la pièce actuelle. |

### Procédure pas à pas

1. Ouvrez la liste Courriers et vérifiez qu’une référence identique n’est pas déjà enregistrée.
2. Si vous devez créer un courrier et que le bouton est disponible, ouvrez Nouveau courrier.
3. Choisissez entrant ou sortant avant de renseigner la date correspondante.
4. Complétez la référence, l’objet et le correspondant. Joignez le document lisible.
5. Enregistrez et vérifiez le retour dans la liste.
6. Pour consulter, ouvrez les détails ou le document de la ligne concernée. Utilisez uniquement les actions proposées par votre compte.

### Résultat attendu et contrôles

Le sens entrant/sortant, la date et le correspondant doivent correspondre au document. En cas de remplacement de fichier, contrôlez la nouvelle pièce après enregistrement.



## 10. Classer un procès-verbal

**Accès :** Menu Procès-verbaux ; Nouveau procès-verbal pour la création, selon les autorisations.

**Objectif :** Retrouver le compte rendu officiel d’une réunion à partir de son numéro, de son type, de son objet et de sa date.


![Classer un procès-verbal : zones à repérer dans le module — écran de démonstration](captures/pv.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Numéro et type | Reprendre les références du procès-verbal. |
| Objet | Indiquer le sujet de la réunion ou de la décision. |
| Date du procès-verbal | Utiliser la date figurant sur le document. |
| Observation | Ajouter les précisions utiles sans remplacer le contenu du document. |
| Document | Obligatoire à la création. Le champ accepte un PDF ou une image selon le formulaire. |

### Procédure pas à pas

1. Consultez la liste pour éviter de saisir deux fois le même numéro.
2. Ouvrez Nouveau procès-verbal si vous êtes chargé de l’enregistrement.
3. Renseignez numéro, type, objet et date.
4. Joignez le document complet, y compris les pages de signature si elles font partie du procès-verbal.
5. Enregistrez puis recherchez la référence dans la liste.
6. Ouvrez le document pour vérifier la lisibilité et le nombre de pages.

### Résultat attendu et contrôles

La référence doit mener au bon document. Les lecteurs habilités peuvent consulter les pièces sans disposer des boutons de création ou de suppression.



## 11. Enregistrer un ordre de mission

**Accès :** Menu Ordres de mission ; Nouveau ordre de mission pour les comptes autorisés à créer.

**Objectif :** Centraliser les informations de déplacement et le document officiel associé.


![Enregistrer un ordre de mission : zones à repérer dans le module — écran de démonstration](captures/mission.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Libellé et objet | Nom court de la mission et objectif détaillé. |
| Itinéraire | Trajet prévu, avec les étapes nécessaires. |
| Dates de départ et de retour | Vérifier leur cohérence et la période figurant sur l’ordre. |
| Imputation budgétaire | Référence à utiliser pour cette mission. |
| Moyen de déplacement | Moyen effectivement prévu. |
| Ordre de mission | Document PDF ou image obligatoire lors de la création. |
| Observations | Précisions complémentaires. |

### Procédure pas à pas

1. Préparez l’ordre signé ou la pièce officielle à classer.
2. Ouvrez le formulaire depuis le menu de mission.
3. Remplissez libellé, objet et itinéraire avec des indications permettant de distinguer cette mission des autres.
4. Renseignez les dates, l’imputation et le moyen de déplacement.
5. Sélectionnez le document ; relisez tous les champs avant d’enregistrer.
6. Retrouvez la mission dans la liste et consultez ses détails pour contrôler le document.

### Résultat attendu et contrôles

Le retour ne doit pas précéder le départ. Un formulaire enregistré ne remplace pas la validation administrative du document officiel.



## 12. Préparer et consulter une convocation

**Accès :** Menu Liste des convocations ; Nouvelle convocation si votre compte possède le droit de création.

**Objectif :** Conserver une convocation datée et sa description, avec une pièce jointe si nécessaire.


![Préparer et consulter une convocation : zones à repérer dans le module — écran de démonstration](captures/convocation.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Date de la convocation | Date renseignée sur la convocation. |
| Description | Préciser le contexte, l’objet et les informations utiles aux destinataires. |
| Document justificatif | Facultatif ; PDF ou image selon le champ proposé. |

### Procédure pas à pas

1. Consultez la liste afin de repérer une convocation déjà enregistrée.
2. Ouvrez Nouvelle convocation si vous devez en créer une.
3. Renseignez la date puis une description explicite. Vérifiez que le lieu, l’horaire et les destinataires figurent dans la description ou dans la pièce lorsque ces indications sont nécessaires.
4. Joignez le document disponible, puis enregistrez.
5. Retrouvez la convocation dans la liste et ouvrez la pièce pour vérifier son contenu.

### Résultat attendu et contrôles

La convocation enregistrée doit correspondre à la bonne réunion. L’enregistrement dans l’application ne prouve pas son envoi aux destinataires ; appliquez le circuit de diffusion de votre organisation.



## 13. Enregistrer et consulter une plainte

**Accès :** Menu Liste des plaintes ; Nouvelle plainte et actions de gestion selon les permissions de votre compte.

**Objectif :** Conserver la date, la description et les pièces d’un dossier de plainte pour permettre son suivi.


![Enregistrer et consulter une plainte : zones à repérer dans le module — écran de démonstration](captures/plainte.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Date de la plainte | Date du dépôt ou de l’enregistrement à retenir pour le dossier. |
| Description de la plainte | Présenter les faits, le contexte et les références utiles de manière factuelle. |
| Document justificatif | Facultatif ; choisir la pièce correspondant au dossier concerné. |

### Procédure pas à pas

1. Ouvrez la liste et recherchez un éventuel dossier déjà enregistré.
2. Cliquez sur Nouvelle plainte si vous possédez le droit de création.
3. Renseignez la date et décrivez les faits utiles à la compréhension.
4. Joignez une pièce lisible si elle est disponible. Évitez les éléments sans rapport avec la plainte.
5. Enregistrez puis ouvrez les détails du dossier dans la liste.
6. Pour une correction, utilisez l’action de modification lorsqu’elle est proposée. En cas de suppression, contrôlez soigneusement le dossier avant de confirmer.

### Résultat attendu et contrôles

La bonne date et le bon justificatif doivent être associés au dossier. Le suivi visible dans l’application ne dispense pas d’appliquer les procédures internes de traitement.



## 14. Constituer un dossier de cas social

**Accès :** Menus Cas sociaux et Nouveau cas social uniquement si les permissions LISTER_CAS_SOCIAUX et AJOUTER_CAS_SOCIAUX ont été accordées.

**Objectif :** Saisir un événement social, identifier le bénéficiaire et conserver les éléments nécessaires à l’évaluation du dossier.


![Constituer un dossier de cas social : zones à repérer dans le module — écran de démonstration](captures/cas-social.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Adhérent concerné | Rechercher le membre et sélectionner le résultat proposé. Un nom saisi sans sélection ne suffit pas. |
| Type de cas | Maladie, accident de la circulation, ou radiation / révocation / retraite disciplinaire. |
| Date du cas | Date de l’événement. |
| Hospitalisation | Pour une maladie, renseigner la durée en heures demandée par le formulaire. Le libellé rappelle le seuil de 72 heures. |
| Cotisations versées | Pour le cas disciplinaire, renseigner le total demandé en FCFA à partir des justificatifs. |
| Bénéficiaire | Adhérent ou personne désignée ; compléter l’identité et le téléphone de la personne désignée. |
| Justificatif et observation | Joindre la pièce disponible et expliquer les éléments utiles au dossier. |

### Procédure pas à pas

1. Préparez l’identité du membre, les dates et les éléments justificatifs.
2. Ouvrez Nouveau cas social et sélectionnez l’adhérent dans les résultats de recherche.
3. Choisissez le type. Les champs supplémentaires varient avec ce choix ; renseignez uniquement ceux affichés.
4. Sélectionnez le bénéficiaire et complétez ses coordonnées si une autre personne doit recevoir la prestation.
5. Ajoutez le justificatif, relisez les informations et enregistrez.
6. Consultez les détails pour lire le statut et le montant calculés par l’application.

### Résultat attendu et contrôles

Ne déduisez pas un droit au versement à partir du seul type choisi. Vérifiez le résultat renvoyé, les règles de l’organisation et les pièces. Si le menu est absent, faites vérifier vos permissions.



## 15. Enregistrer une prestation

**Accès :** Menu Prestations ; Nouvelle prestation pour les fonctions autorisées, notamment la trésorerie.

**Objectif :** Documenter la prestation liée à un événement et identifier précisément la personne qui la reçoit.


![Enregistrer une prestation : zones à repérer dans le module — écran de démonstration](captures/prestation.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Adhérent concerné | Saisir un critère puis sélectionner le membre dans les résultats. |
| Type de prestation | Retraite, décès d’un proche ou de l’adhérent, prestation décès de l’adhérent, réforme pour invalidité. |
| Date de la prestation | Date associée au dossier. |
| Lien avec l’adhérent | Pour les cas de décès, choisir le lien demandé : adhérent, père, mère, époux, épouse ou enfant. |
| Bénéficiaire | Choisir le destinataire proposé et compléter prénom, nom, téléphone lorsque ces champs apparaissent. |
| Justificatif et observation | Pièce facultative et informations complémentaires utiles à la vérification. |

### Procédure pas à pas

1. Vérifiez que le dossier de l’adhérent est correct et préparez les pièces de l’événement.
2. Ouvrez Nouvelle prestation. Recherchez puis sélectionnez l’adhérent.
3. Choisissez le type de prestation ; renseignez les champs conditionnels qui apparaissent, notamment le lien en cas de décès.
4. Choisissez le bénéficiaire et vérifiez son identité et son téléphone.
5. Renseignez la date et ajoutez les documents disponibles.
6. Enregistrez puis consultez le dossier. Lisez le résultat et le montant présentés par l’application au lieu de supposer un montant libre.

### Résultat attendu et contrôles

Le bénéficiaire peut être différent de l’adhérent concerné. Contrôlez ces deux identités avant de poursuivre le circuit de règlement. Une prestation enregistrée n’est pas une preuve de paiement.



## 16. Enregistrer un encaissement

**Accès :** Menus Encaissements et Nouvel encaissement pour les comptes habilités.

**Objectif :** Tracer une recette avec son type, sa date, son montant et son rattachement budgétaire éventuel.


![Enregistrer un encaissement : zones à repérer dans le module — écran de démonstration](captures/encaissement.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Type d’encaissement | Versement ou Dons, legs, intérêts des placements. |
| Date d’encaissement | Date de la recette. |
| Début / fin de période | Champs affichés pour un versement ; vérifier leur cohérence. |
| Montant (FCFA) | Renseigner le montant à partir de la pièce. Ne pas saisir le symbole monétaire dans le nombre. |
| Région / type de budget | Sélectionner le budget régional concerné lorsque nécessaire. |
| Observation et justificatif | Expliquer la recette et joindre la pièce disponible ; formats proposés : PDF, JPG, JPEG, PNG, DOC, DOCX. |

### Procédure pas à pas

1. Comparez la recette à la liste des encaissements pour prévenir un double enregistrement.
2. Ouvrez Nouvel encaissement et choisissez le type.
3. Renseignez la date et, pour un versement, la période concernée si elle est connue.
4. Saisissez le montant en FCFA et sélectionnez le budget régional s’il est applicable.
5. Ajoutez une observation et le justificatif. Cliquez sur Enregistrer une seule fois puis attendez le résultat.
6. Retrouvez la recette dans la liste. Vérifiez type, date et montant par rapport à la pièce.

### Résultat attendu et contrôles

La recette doit apparaître une seule fois. En modification, vérifiez le nom du fichier existant ; sélectionner une nouvelle pièce remplace la pièce actuelle.



## 17. Préparer les frais de gestion annuels

**Accès :** Menu Frais de gestion, puis l’action de création si elle est disponible.

**Objectif :** Renseigner l’enveloppe annuelle à laquelle seront rattachées les dépenses.


![Préparer les frais de gestion annuels : zones à repérer dans le module — écran de démonstration](captures/frais-gestion.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Année | Exercice budgétaire correspondant à l’enveloppe. |
| Montant total (FCFA) | Montant de l’enveloppe de gestion. |
| Observation | Référence ou précision sur l’enveloppe. |
| Justificatif | Pièce disponible permettant d’expliquer le montant. |

### Procédure pas à pas

1. Vérifiez dans la liste si une enveloppe existe déjà pour l’exercice.
2. Ouvrez le formulaire de création. Renseignez l’année et le montant total.
3. Ajoutez les précisions et le justificatif disponibles.
4. Enregistrez puis vérifiez la ligne créée.
5. Lors de l’engagement d’une dépense, sélectionnez l’enveloppe de l’année concernée.

### Résultat attendu et contrôles

L’année choisie doit correspondre à celle de la dépense. Si aucune enveloppe n’est proposée dans le formulaire de dépense, faites vérifier l’existence des frais de gestion et vos droits.



## 18. Engager et corriger une dépense

**Accès :** Menu Dépenses, puis Nouvelle dépense pour les fonctions autorisées à engager une dépense.

**Objectif :** Créer une demande de dépense rattachée à une enveloppe de frais de gestion et la soumettre au circuit de décision.


![Engager et corriger une dépense : zones à repérer dans le module — écran de démonstration](captures/depense.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Libellé | Décrire précisément la dépense. |
| Date de dépense | Date correspondant à la demande ou à la pièce. |
| Montant (FCFA) | Montant de la dépense à vérifier sur le justificatif. |
| Frais de gestion | Sélection obligatoire de l’enveloppe annuelle. |
| Observation | Précisions permettant au décideur de comprendre la demande. |
| Justificatif | Facture, devis ou pièce utile ; formats indiqués par le formulaire. |

### Procédure pas à pas

1. Vérifiez l’existence de l’enveloppe et rassemblez la pièce justificative.
2. Ouvrez Nouvelle dépense. Saisissez le libellé et la date.
3. Renseignez le montant et sélectionnez les frais de gestion correspondant à l’exercice.
4. Ajoutez l’observation et la pièce, puis cliquez sur Engager la dépense.
5. Retrouvez la ligne dans Dépenses et vérifiez son statut.
6. Pour corriger une dépense En attente, utilisez Modifier si l’action apparaît ; pour la retirer, utilisez Supprimer puis vérifiez la confirmation.

### Résultat attendu et contrôles

Les commandes de modification, suppression et décision sont proposées pour les dépenses En attente selon les droits. Les statuts de cette version sont En attente, Validée et Rejetée ; aucun statut « Payée » ne doit être déduit de la validation.



## 19. Valider ou rejeter une dépense

**Accès :** Menu Dépenses, pour les comptes disposant du droit de validation des décaissements.

**Objectif :** Prendre une décision traçable après vérification du montant, du rattachement et du justificatif.


![Valider ou rejeter une dépense : zones à repérer dans le module — écran de démonstration](captures/depenses.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| En attente | Dossier pouvant recevoir une décision si votre compte est habilité. |
| Valider | Ouvre une fenêtre de confirmation ; observation facultative. |
| Rejeter | Ouvre une fenêtre exigeant un motif de rejet. |
| Validée / Rejetée | Résultat de la décision visible dans la liste. |

### Procédure pas à pas

1. Recherchez la dépense et vérifiez son libellé, son année, son montant et sa pièce jointe.
2. Si le dossier est En attente, cliquez sur Valider ou Rejeter.
3. Pour un rejet, rédigez un motif explicite permettant au demandeur de comprendre le problème. Pour une validation, ajoutez une observation si nécessaire.
4. Relisez la décision puis cliquez sur Confirmer la validation ou Confirmer le rejet.
5. Attendez le résultat et contrôlez le nouveau statut dans la liste.
6. Si la demande échoue, relisez le message. Rechargez la liste avant de tenter une nouvelle décision afin de vérifier qu’un autre utilisateur n’a pas déjà traité le dossier.

### Résultat attendu et contrôles

Le motif de rejet est obligatoire. Une dépense Validée signifie que la décision a été enregistrée ; ce statut n’atteste pas à lui seul d’un paiement.



![Fenêtre de décision : le motif est obligatoire pour rejeter une dépense — écran de démonstration](captures/decision.png)

## 20. Lire la situation des encaissements

**Accès :** Menu Situation des encaissements si votre compte peut consulter les encaissements.

**Objectif :** Contrôler les recettes d’un exercice et exporter le rapport affiché.


![Lire la situation des encaissements : zones à repérer dans le module — écran de démonstration](captures/situation-encaissements.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Année | Sélectionner l’exercice voulu ; le rapport est rechargé. |
| Lignes et totaux | Lire les regroupements et montants présentés par le rapport. |
| Télécharger CSV | Télécharger le rapport lorsque des lignes sont disponibles. |

### Procédure pas à pas

1. Ouvrez Situation des encaissements.
2. Choisissez l’année et attendez la fin du chargement.
3. Comparez les regroupements et le total avec les recettes attendues.
4. Si un montant semble incorrect, revenez aux encaissements pour vérifier les enregistrements sources.
5. Utilisez Télécharger CSV si le bouton est actif. Ouvrez le fichier dans un tableur et vérifiez l’année.

### Résultat attendu et contrôles

Un écran sans ligne peut correspondre à un exercice vide. Le bouton de téléchargement reste inactif pendant le chargement ou lorsqu’aucune donnée n’est disponible.



## 21. Lire la situation des décaissements

**Accès :** Menu Situation des décaissements selon vos droits de consultation des dépenses.

**Objectif :** Comparer les montants mensuels en distinguant les statuts des dépenses.


![Lire la situation des décaissements : zones à repérer dans le module — écran de démonstration](captures/situation-depenses.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Année | Exercice utilisé pour le rapport. |
| Mois et libellé | Période et description du regroupement. |
| Statut | En attente, Validée ou Rejetée ; lire ce statut avant d’interpréter le montant. |
| Nombre / montant total | Nombre d’enregistrements et somme pour la ligne. |
| Total général | Somme du rapport sélectionné ; elle ne doit pas être interprétée automatiquement comme une somme payée. |

### Procédure pas à pas

1. Ouvrez le rapport et sélectionnez l’année.
2. Repérez les lignes du mois et du libellé recherchés.
3. Lisez séparément les statuts pour éviter de confondre demandes, validations et rejets.
4. Comparez le nombre de dossiers et les montants aux dépenses sources.
5. Téléchargez le CSV si nécessaire et vérifiez le contenu dans votre tableur.

### Résultat attendu et contrôles

Une dépense rejetée peut figurer dans un regroupement portant ce statut. Le rapport présente des enregistrements de dépenses ; vérifiez le périmètre avant toute utilisation comptable.



## 22. Gérer l’inventaire du matériel

**Accès :** Menu Gestion du matériel, puis Nouveau matériel pour les comptes autorisés.

**Objectif :** Identifier les biens, leur valeur, leur emplacement et leur état.


![Gérer l’inventaire du matériel : zones à repérer dans le module — écran de démonstration](captures/materiel.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Nom / catégorie | Désignation précise permettant de distinguer le bien. |
| Fournisseur | Provenance du matériel. |
| Quantité / prix unitaire | Nombre d’unités et prix par unité en FCFA. |
| Date d’acquisition | Date figurant sur le justificatif d’achat. |
| Emplacement / numéro de série | Localisation physique et identifiant du bien s’il existe. |
| Statut | Actif, En maintenance ou Hors service. |
| Observation | Précisions sur l’état, le suivi ou l’affectation. |

### Procédure pas à pas

1. Consultez l’inventaire pour éviter un doublon, notamment sur le numéro de série.
2. Ouvrez Nouveau matériel et renseignez la désignation, la catégorie et le fournisseur.
3. Complétez quantité, prix unitaire et date. Ne confondez pas prix unitaire et prix du lot.
4. Renseignez emplacement, numéro de série et statut réel.
5. Enregistrez, retrouvez le bien puis contrôlez sa fiche.
6. Lors d’un changement d’emplacement ou d’état, utilisez la modification si elle est autorisée et précisez l’observation.

### Résultat attendu et contrôles

Le statut doit correspondre à la situation physique du bien. Une mise Hors service n’est pas une suppression : conservez les informations nécessaires à la traçabilité.



## 23. Rechercher et ouvrir les archives

**Accès :** Menu Archives pour les comptes disposant des droits de liste et de consultation.

**Objectif :** Retrouver les documents classés par catégorie et consulter la pièce associée.


![Rechercher et ouvrir les archives : zones à repérer dans le module — écran de démonstration](captures/archives.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Recherche | Saisir un terme identifiable dans la référence ou le libellé. |
| Catégorie | Choisir la famille de documents si le filtre est proposé. |
| Document | Ouvrir l’aperçu ou l’action de consultation proposée sur la ligne. |

### Procédure pas à pas

1. Ouvrez Archives et identifiez la catégorie du document recherché.
2. Saisissez un critère, puis retirez les filtres trop restrictifs si la liste est vide.
3. Comparez la référence et les informations de la ligne au dossier attendu.
4. Ouvrez l’aperçu. Vérifiez qu’il s’agit de la bonne pièce et qu’elle est lisible.
5. Utilisez les actions proposées par la visionneuse si vous devez conserver une copie. Refermez l’aperçu pour revenir aux résultats.

### Résultat attendu et contrôles

Une archive consultable ne donne pas automatiquement le droit de modifier le dossier d’origine. Si le fichier est absent ou illisible, signalez sa référence au responsable du module.



## 24. Désigner et suivre un adjoint

**Accès :** Menu Désignation des adjoints uniquement lorsqu’il est proposé et que votre compte est habilité à déléguer.

**Objectif :** Définir une délégation sur une période précise et en suivre l’historique.


![Désigner et suivre un adjoint : zones à repérer dans le module — écran de démonstration](captures/adjoints.png)

### Champs et informations à vérifier

| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Fonction à déléguer | Choisir la carte de fonction proposée par votre compte. |
| Adhérent adjoint | Rechercher la personne puis la sélectionner dans les résultats. |
| Début / fin | Dates définissant la période de délégation. |
| Motif | Explication utile du remplacement. |
| Historique | Consulter les délégations et les actions de suivi disponibles. |

### Procédure pas à pas

1. Vérifiez la fonction concernée et la période d’absence avant d’ouvrir le formulaire.
2. Dans Désignation des adjoints, cliquez sur la commande de désignation de la fonction autorisée.
3. Recherchez le remplaçant et sélectionnez son identité.
4. Complétez début, fin et motif. Vérifiez que la fin ne précède pas le début.
5. Enregistrez puis contrôlez la présence de la délégation dans l’historique.
6. Si la délégation doit être interrompue ou corrigée, utilisez uniquement l’action proposée sur la ligne et vérifiez son résultat.

### Résultat attendu et contrôles

Le rôle seul ne garantit pas l’apparition du menu : la permission de délégation doit être configurée. La capture présente l’écran d’un compte de démonstration habilité.



## 25. Mettre à jour son profil et son mot de passe


![Profil : résumé personnel, photo et formulaire de coordonnées ; faire défiler pour la sécurité — écran de démonstration](captures/profil.png)


### Consulter et modifier ses informations

1. Ouvrez Profil dans le menu latéral ou utilisez l’accès proposé dans la barre supérieure.
2. Vérifiez l’identité affichée, le rôle et le matricule. Cliquez sur Actualiser si vous devez recharger les informations.
3. Dans Informations personnelles, corrigez les coordonnées ou informations nécessaires. Les champs marqués d’une étoile doivent rester renseignés.
4. Cliquez sur Enregistrer les modifications et attendez la confirmation.
5. Rechargez le profil pour vérifier que les informations enregistrées sont correctes.


### Changer sa photo

Choisissez une image dans la zone Photo de profil, vérifiez l’aperçu, puis cliquez sur Mettre à jour la photo. Cette action possède son propre bouton : sélectionner un fichier ne signifie pas que la photo est déjà enregistrée. Si le fichier est refusé, choisissez un format image accepté et recommencez.

### Changer son mot de passe


![Sécurité : mot de passe actuel, nouveau mot de passe et confirmation — écran de démonstration](captures/securite.png)


1. Faites défiler le profil jusqu’à Mot de passe.
2. Saisissez le mot de passe actuel.
3. Saisissez le nouveau mot de passe, d’au moins huit caractères, puis la même valeur dans la confirmation.
4. Cliquez sur Mettre à jour le mot de passe et attendez le résultat.
5. Si la saisie est refusée, relisez le message : contrôlez le mot de passe actuel et l’égalité des deux nouvelles valeurs.


> Les informations du profil et le mot de passe s’enregistrent séparément. Le bouton du profil n’enregistre pas automatiquement un changement de mot de passe.

## 26. Terminer sa session et résoudre les difficultés


![Repère : le menu du compte se trouve en haut à droite de l’écran — écran de démonstration](captures/accueil-sysadmin.png)


### Déconnexion

1. Terminez l’enregistrement en cours et vérifiez le message de résultat.
2. Ouvrez le menu de votre compte en haut à droite.
3. Choisissez Déconnexion et vérifiez le retour à l’écran public ou de connexion.
4. Sur un poste partagé, fermez également les documents téléchargés et la fenêtre de navigation.


### Aide au diagnostic


| Champ / repère | Comment le renseigner ou le lire |
| --- | --- |
| Menu absent | Vérifier le rôle affiché et demander le contrôle des permissions. Une illustration du manuel peut montrer un compte plus habilité. |
| Liste vide | Supprimer les filtres, vérifier la période et la pagination. |
| Champs refusés | Relire le message, compléter les champs obligatoires et vérifier les dates, montants et sélections. |
| Bouton Enregistrer grisé | Vérifier les champs obligatoires et attendre la fin du chargement. |
| Fichier non accepté | Vérifier l’extension demandée et la lisibilité. Sélectionner à nouveau la bonne pièce. |
| Erreur réseau ou session expirée | Vérifier la connexion, rouvrir la session puis contrôler si la dernière opération a été enregistrée avant de la répéter. |
| PDF introuvable | Consulter les téléchargements du navigateur ; vérifier le rôle et réessayer le téléchargement sur le tableau de bord. |


### Signaler une difficulté utilement

Transmettez au responsable le module concerné, la référence du dossier, l’action effectuée, l’heure et le message exact. Une capture limitée à la zone en erreur aide à comprendre le problème ; masquez les informations personnelles inutiles. Ne transmettez jamais votre mot de passe.

### Vérification de fin de travail

- Les opérations enregistrées ont été vérifiées dans leur liste ou leur fiche.
- Les pièces sont lisibles et rattachées au bon dossier.
- Les exports ont été conservés dans l’emplacement de travail prévu.
- La session est fermée lorsque le poste est laissé à une autre personne.
