package com.codekeys.manuel;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ManuelControleurTest {
    private final ManuelControleur controller = new ManuelControleur();

    @Test
    void refusesAnonymousAndOtherRoles() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.telechargerManuel("PRESIDENT", null).getStatusCode());
        var user = new UsernamePasswordAuthenticationToken("user", "", List.of(new SimpleGrantedAuthority("ROLE_ADHERENT")));
        assertEquals(HttpStatus.FORBIDDEN, controller.telechargerManuel("PRESIDENT", user).getStatusCode());
    }

    @Test
    void generatesReadablePdfForEverySupportedRole() throws Exception {
        for (String role : List.of("SYSADMIN", "ADMINISTRATEUR", "PRESIDENT", "TRESORIER", "ADHERENT",
                "SECRETAIRE_ADMINISTRATIF", "SECRETAIRE_ORGANISATION", "SECRETAIRE_AFFAIRE_SOCIALE", "COMMISSION_CONTROLE")) {
            var user = new UsernamePasswordAuthenticationToken("user", "", List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            var response = controller.telechargerManuel(role, user);
            assertEquals(HttpStatus.OK, response.getStatusCode(), role);
            assertNotNull(response.getBody());
            var reader = new PdfReader(response.getBody());
            assertTrue(reader.getNumberOfPages() >= 7, role);
            assertNotNull(reader.getCatalog().getAsDict(PdfName.OUTLINES), role);
            var extractor = new PdfTextExtractor(reader);
            StringBuilder text = new StringBuilder();
            int images = 0;
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page));
                PdfDictionary resources = reader.getPageN(page).getAsDict(PdfName.RESOURCES);
                if (resources != null) {
                    PdfDictionary objects = resources.getAsDict(PdfName.XOBJECT);
                    if (objects != null) for (PdfName key : objects.getKeys()) {
                        PdfDictionary object = (PdfDictionary) PdfReader.getPdfObject(objects.get(key));
                        if (PdfName.IMAGE.equals(object.getAsName(PdfName.SUBTYPE))) images++;
                    }
                }
            }
            assertTrue(images >= 6, role + " : logo et captures intégrés");
            assertTrue(text.toString().contains("Surveillants de Prison du Mali"), role);
            assertTrue(text.toString().contains("Sommaire"), role);
            assertTrue(text.toString().contains("Mot de passe"), role);
            assertTrue(text.toString().contains("Champ / repère"), role + " : tableaux conservés");
            assertFalse(text.toString().contains("---"), role + " : séparateurs Markdown masqués");
            String output = System.getProperty("msp.manual.output");
            if (output != null) {
                Files.createDirectories(Path.of(output));
                Files.write(Path.of(output, "manuel_" + role.toLowerCase(java.util.Locale.ROOT) + ".pdf"), response.getBody());
            }
            reader.close();
        }
    }
}
