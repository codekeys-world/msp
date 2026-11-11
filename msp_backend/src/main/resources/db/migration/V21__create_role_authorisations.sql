-- Schema initial : _role_authorisations.
-- Correspond aux mappings JPA ; relations ajoutees dans V27.

create table _role_authorisations (
    roles_id bigint not null,
    authorisations_id bigint not null,
    primary key (roles_id, authorisations_id)
);
