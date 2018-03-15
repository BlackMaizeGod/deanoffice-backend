package ua.edu.chdtu.deanoffice.api.document.diplomasupplement;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.chdtu.deanoffice.api.document.DocumentResponseController;
import ua.edu.chdtu.deanoffice.service.document.diploma.supplement.DiplomaSupplementService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//TODO дуже велика адреса ресурсу. її краще скоротити, наприклад до "/students/degrees/{id}/diploma"
@RestController
@RequestMapping("/documents/diplomas/supplements")
public class DiplomaSupplementController extends DocumentResponseController {

    //TODO Потрібно прибирати код, який не використувається (якщо він буде потрібен, то система контролю версій тобі
    // його збереже)
    private static Logger log = LoggerFactory.getLogger(DiplomaSupplementController.class);

    private DiplomaSupplementService diplomaSupplementService;

    public DiplomaSupplementController(DiplomaSupplementService diplomaSupplementService) {
        this.diplomaSupplementService = diplomaSupplementService;
    }

    //TODO Краще уникати в адресах ресурсів таких моментів "studentdegrees", таке краще розділяти на "/student/degrees/"
    @GetMapping(path = "/studentdegrees/{studentDegreeId}")
    public ResponseEntity<Resource> generateForStudent(@PathVariable Integer studentDegreeId) throws IOException, Docx4JException {
        File studentDiplomaSupplement = diplomaSupplementService.formDiplomaSupplementForStudent(studentDegreeId);
        return buildDocumentResponseEntity(studentDiplomaSupplement, studentDiplomaSupplement.getName());
    }
}
