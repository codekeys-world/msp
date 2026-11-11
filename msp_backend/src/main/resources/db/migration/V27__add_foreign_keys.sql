-- Toutes les tables existent avant la creation des relations circulaires.

ALTER TABLE _adjoint
    ADD CONSTRAINT fk_adjoint_adjoint_id
    FOREIGN KEY (adjoint_id) REFERENCES _utilisateur (id);

ALTER TABLE _adjoint
    ADD CONSTRAINT fk_adjoint_delegant_id
    FOREIGN KEY (delegant_id) REFERENCES _utilisateur (id);

ALTER TABLE _cas_social
    ADD CONSTRAINT fk_cas_social_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _cas_social
    ADD CONSTRAINT fk_cas_social_adherent_id
    FOREIGN KEY (adherent_id) REFERENCES _utilisateur (id);

ALTER TABLE _cas_social
    ADD CONSTRAINT fk_cas_social_justificatif_id
    FOREIGN KEY (justificatif_id) REFERENCES _document (id);

ALTER TABLE _conjoint
    ADD CONSTRAINT fk_conjoint_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _conjoint
    ADD CONSTRAINT fk_conjoint_acte_mariage_id
    FOREIGN KEY (acte_mariage_id) REFERENCES _document (id);

ALTER TABLE _conjoint
    ADD CONSTRAINT fk_conjoint_adherent_id
    FOREIGN KEY (adherent_id) REFERENCES _utilisateur (id);

ALTER TABLE _convocation
    ADD CONSTRAINT fk_convocation_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _convocation
    ADD CONSTRAINT fk_convocation_convocation_id
    FOREIGN KEY (convocation_id) REFERENCES _document (id);

ALTER TABLE _courrier
    ADD CONSTRAINT fk_courrier_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _courrier
    ADD CONSTRAINT fk_courrier_courrier_id
    FOREIGN KEY (courrier_id) REFERENCES _document (id);

ALTER TABLE _depense
    ADD CONSTRAINT fk_depense_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _depense
    ADD CONSTRAINT fk_depense_frais_de_gestion_id
    FOREIGN KEY (frais_de_gestion_id) REFERENCES _frais_de_gestion (id);

ALTER TABLE _depense
    ADD CONSTRAINT fk_depense_justificatif_id
    FOREIGN KEY (justificatif_id) REFERENCES _document (id);

ALTER TABLE _encaissement
    ADD CONSTRAINT fk_encaissement_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _encaissement
    ADD CONSTRAINT fk_encaissement_justificatif_id
    FOREIGN KEY (justificatif_id) REFERENCES _document (id);

ALTER TABLE _enfant
    ADD CONSTRAINT fk_enfant_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _enfant
    ADD CONSTRAINT fk_enfant_acte_naissance_id
    FOREIGN KEY (acte_naissance_id) REFERENCES _document (id);

ALTER TABLE _enfant
    ADD CONSTRAINT fk_enfant_certificat_vie_collectif_id
    FOREIGN KEY (certificat_vie_collectif_id) REFERENCES _document (id);

ALTER TABLE _enfant
    ADD CONSTRAINT fk_enfant_adherent_id
    FOREIGN KEY (adherent_id) REFERENCES _utilisateur (id);

ALTER TABLE _evenement
    ADD CONSTRAINT fk_evenement_utilisateur_id
    FOREIGN KEY (utilisateur_id) REFERENCES _utilisateur (id);

ALTER TABLE _frais_de_gestion
    ADD CONSTRAINT fk_frais_de_gestion_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _frais_de_gestion
    ADD CONSTRAINT fk_frais_de_gestion_justificatif_id
    FOREIGN KEY (justificatif_id) REFERENCES _document (id);

ALTER TABLE _materiel
    ADD CONSTRAINT fk_materiel_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _ordre_mission
    ADD CONSTRAINT fk_ordre_mission_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _ordre_mission
    ADD CONSTRAINT fk_ordre_mission_ordre_mission_id
    FOREIGN KEY (ordre_mission_id) REFERENCES _document (id);

ALTER TABLE _password_reset_token
    ADD CONSTRAINT fk_password_reset_token_utilisateur_id
    FOREIGN KEY (utilisateur_id) REFERENCES _utilisateur (id);

ALTER TABLE _plainte
    ADD CONSTRAINT fk_plainte_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _plainte
    ADD CONSTRAINT fk_plainte_plainte_id
    FOREIGN KEY (plainte_id) REFERENCES _document (id);

ALTER TABLE _prestation
    ADD CONSTRAINT fk_prestation_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _prestation
    ADD CONSTRAINT fk_prestation_adherent_id
    FOREIGN KEY (adherent_id) REFERENCES _utilisateur (id);

ALTER TABLE _prestation
    ADD CONSTRAINT fk_prestation_justificatif_id
    FOREIGN KEY (justificatif_id) REFERENCES _document (id);

ALTER TABLE _pv
    ADD CONSTRAINT fk_pv_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _pv
    ADD CONSTRAINT fk_pv_document_pv_id
    FOREIGN KEY (document_pv_id) REFERENCES _document (id);

ALTER TABLE _role_authorisations
    ADD CONSTRAINT fk_role_authorisations_authorisations_id
    FOREIGN KEY (authorisations_id) REFERENCES _authorisation (id);

ALTER TABLE _role_authorisations
    ADD CONSTRAINT fk_role_authorisations_roles_id
    FOREIGN KEY (roles_id) REFERENCES _role (id);

ALTER TABLE _token
    ADD CONSTRAINT fk_token_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _token
    ADD CONSTRAINT fk_token_utilisateur_id
    FOREIGN KEY (utilisateur_id) REFERENCES _utilisateur (id);

ALTER TABLE _utilisateur
    ADD CONSTRAINT fk_utilisateur_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _utilisateur
    ADD CONSTRAINT fk_utilisateur_profile_id
    FOREIGN KEY (profile_id) REFERENCES _document (id);

ALTER TABLE _utilisateur
    ADD CONSTRAINT fk_utilisateur_role_id
    FOREIGN KEY (role_id) REFERENCES _role (id);

ALTER TABLE _utilisateur_authorisation
    ADD CONSTRAINT fk_utilisateur_authorisation_authorisation_id
    FOREIGN KEY (authorisation_id) REFERENCES _authorisation (id);

ALTER TABLE _utilisateur_authorisation
    ADD CONSTRAINT fk_utilisateur_authorisation_utilisateur_id
    FOREIGN KEY (utilisateur_id) REFERENCES _utilisateur (id);

ALTER TABLE _validation
    ADD CONSTRAINT fk_validation_valider_par
    FOREIGN KEY (valider_par) REFERENCES _validation_entite (id);

ALTER TABLE _validation_entite
    ADD CONSTRAINT fk_validation_entite_valider_par
    FOREIGN KEY (valider_par) REFERENCES _utilisateur (id);
