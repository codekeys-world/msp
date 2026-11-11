-- Schema initial : _utilisateur_authorisation.
-- Correspond aux mappings JPA ; relations ajoutees dans V27.

create table _utilisateur_authorisation (
    utilisateur_id bigint not null,
    authorisation_id bigint not null,
    primary key (utilisateur_id, authorisation_id)
);
