package com.codekeys.journalAction;

import com.codekeys.common.PageReponse;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalActionService {

    private final JournalActionRepository journalActionRepository;
    private final JournalActionMapper journalActionMapper;

    @Transactional
    public JournalAction enregistrer(
            String nom,
            String prenom,
            String role,
            String matricule,
            String adresseIp,
            String client,
            String action,
            String module,
            String details,
            String statut,
            LocalDateTime dateAction
    ) {
        try {
            JournalAction journal = JournalAction.builder()
                    .slug(UUID.randomUUID().toString())
                    .nom(StringUtils.hasText(nom) ? nom.trim() : "Système")
                    .prenom(StringUtils.hasText(prenom) ? prenom.trim() : "")
                    .role(StringUtils.hasText(role) ? role.trim() : "SYSTÈME")
                    .matricule(StringUtils.hasText(matricule) ? matricule.trim() : null)
                    .adresseIp(StringUtils.hasText(adresseIp) ? adresseIp.trim() : "127.0.0.1")
                    .client(StringUtils.hasText(client) ? client.trim() : "Navigateur web")
                    .action(StringUtils.hasText(action) ? action.trim() : "Action système")
                    .module(StringUtils.hasText(module) ? module.trim() : "GÉNÉRAL")
                    .details(details)
                    .statut(StringUtils.hasText(statut) ? statut.trim() : "SUCCES")
                    .dateAction(dateAction != null ? dateAction : LocalDateTime.now())
                    .build();

            return journalActionRepository.save(journal);
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'action dans le journal: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public JournalAction enregistrerDepuisRequete(
            Utilisateur utilisateur,
            HttpServletRequest request,
            String action,
            String module,
            String details,
            String statut
    ) {
        String ip = extraireAdresseIp(request);
        String client = formaterClient(request != null ? request.getHeader("User-Agent") : null);
        String nom = utilisateur != null ? utilisateur.getNom() : "Système";
        String prenom = utilisateur != null ? utilisateur.getPrenom() : "";
        String role = utilisateur != null && utilisateur.getRole() != null ? utilisateur.getRole().getNom() : "ANONYME";
        String matricule = utilisateur != null ? utilisateur.getMatricule() : null;

        return enregistrer(nom, prenom, role, matricule, ip, client, action, module, details, statut, LocalDateTime.now());
    }

    @Transactional
    public PageReponse<JournalActionResponse> lister(
            int page,
            int size,
            String terme,
            String module,
            String statut,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination du journal d'actions sont invalides.");
        }

        LocalDateTime debutDateTime = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDateTime = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;

        Specification<JournalAction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(terme)) {
                String pattern = "%" + terme.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nom")), pattern),
                        cb.like(cb.lower(root.get("prenom")), pattern),
                        cb.like(cb.lower(root.get("role")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("matricule"), "")), pattern),
                        cb.like(cb.lower(root.get("adresseIp")), pattern),
                        cb.like(cb.lower(root.get("client")), pattern),
                        cb.like(cb.lower(root.get("action")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("details"), "")), pattern)
                ));
            }

            if (StringUtils.hasText(module)) {
                predicates.add(cb.equal(cb.upper(root.get("module")), module.trim().toUpperCase()));
            }

            if (StringUtils.hasText(statut)) {
                predicates.add(cb.equal(cb.upper(root.get("statut")), statut.trim().toUpperCase()));
            }

            if (debutDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateAction"), debutDateTime));
            }

            if (finDateTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateAction"), finDateTime));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(page, size, Sort.by("dateAction").descending().and(Sort.by("id").descending()));
        Page<JournalAction> actions = journalActionRepository.findAll(spec, pageable);

        List<JournalActionResponse> contenu = actions.stream()
                .map(journalActionMapper::toResponse)
                .toList();

        return new PageReponse<>(
                contenu,
                actions.getNumber(),
                actions.getSize(),
                actions.getTotalElements(),
                actions.getTotalPages(),
                actions.isFirst(),
                actions.isLast()
        );
    }

    @Transactional
    public JournalActionResponse rechercherParSlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug de l'action est obligatoire.");
        }
        JournalAction journal = journalActionRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucune entrée du journal trouvée pour ce slug."));
        return journalActionMapper.toResponse(journal);
    }

    @Transactional
    public JournalActionStatistiques obtenirStatistiques() {
        LocalDateTime debutAujourdhui = LocalDate.now().atStartOfDay();
        LocalDateTime finAujourdhui = LocalDate.now().atTime(LocalTime.MAX);

        long total = journalActionRepository.count();
        long aujourdhui = journalActionRepository.countByDateActionBetween(debutAujourdhui, finAujourdhui);
        long utilisateursDistincts = journalActionRepository.countDistinctUtilisateurs();
        long succes = journalActionRepository.countByStatutIgnoreCase("SUCCES");
        long echecs = journalActionRepository.countByStatutIgnoreCase("ECHEC");

        return new JournalActionStatistiques(total, aujourdhui, utilisateursDistincts, succes, echecs);
    }

    public String extraireAdresseIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";

        String[] headerCandidates = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR",
                "X-Real-IP"
        };

        for (String header : headerCandidates) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                String ip = ipList.split(",")[0].trim();
                if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                    return "127.0.0.1 (Local)";
                }
                return ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1 (Local)";
        }
        return remoteAddr != null ? remoteAddr : "127.0.0.1";
    }

    public String formaterClient(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Client inconnu (Navigateur web)";
        }

        String ua = userAgent;
        String os = "Système inconnu";
        if (ua.contains("Windows NT 10.0") || ua.contains("Windows NT 11.0")) os = "Windows 10/11";
        else if (ua.contains("Windows")) os = "Windows";
        else if (ua.contains("Android")) os = "Android";
        else if (ua.contains("iPhone") || ua.contains("iPad")) os = "iOS";
        else if (ua.contains("Macintosh") || ua.contains("Mac OS")) os = "macOS";
        else if (ua.contains("Linux")) os = "Linux";

        String browser = "Navigateur";
        if (ua.contains("Edg/")) browser = "Microsoft Edge";
        else if (ua.contains("Chrome/") && !ua.contains("Chromium")) browser = "Google Chrome";
        else if (ua.contains("Firefox/")) browser = "Mozilla Firefox";
        else if (ua.contains("Safari/") && !ua.contains("Chrome")) browser = "Apple Safari";
        else if (ua.contains("PostmanRuntime")) browser = "Postman API Client";
        else if (ua.contains("curl")) browser = "cURL";

        return browser + " (" + os + ")";
    }
}
