package ua.edu.chdtu.deanoffice.api.student.synchronization.diploma.number;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.edu.chdtu.deanoffice.api.general.ExceptionHandlerAdvice;
import ua.edu.chdtu.deanoffice.api.general.ExceptionToHttpCodeMapUtil;
import ua.edu.chdtu.deanoffice.api.student.synchronization.diploma.number.dto.DiplomaAndStudentSynchronizedDataDTO;
import ua.edu.chdtu.deanoffice.api.student.synchronization.diploma.number.dto.DiplomaNumberDataForSaveDTO;
import ua.edu.chdtu.deanoffice.api.student.synchronization.diploma.number.dto.FormedListsWithDiplomaDataDTO;
import ua.edu.chdtu.deanoffice.api.student.synchronization.diploma.number.dto.MissingDataRedDTO;
import ua.edu.chdtu.deanoffice.api.student.synchronization.edebo.SyncronizationController;
import ua.edu.chdtu.deanoffice.entity.ApplicationUser;
import ua.edu.chdtu.deanoffice.exception.OperationCannotBePerformedException;
import ua.edu.chdtu.deanoffice.service.StudentDegreeService;
import ua.edu.chdtu.deanoffice.service.datasync.edebo.diploma.number.EdeboDiplomaNumberSynchronizationReport;
import ua.edu.chdtu.deanoffice.service.datasync.edebo.diploma.number.EdeboDiplomaNumberSynchronizationService;
import ua.edu.chdtu.deanoffice.webstarter.security.CurrentUser;

import java.util.*;

import static ua.edu.chdtu.deanoffice.api.general.mapper.Mapper.*;

@RestController
@RequestMapping("/students")
public class DiplomaNumberController {
    private EdeboDiplomaNumberSynchronizationService edeboDiplomaNumberSynchronizationService;
    private final StudentDegreeService studentDegreeService;

    @Autowired
    public DiplomaNumberController(EdeboDiplomaNumberSynchronizationService edeboDiplomaNumberSynchronizationService, StudentDegreeService studentDegreeService) {
        this.edeboDiplomaNumberSynchronizationService = edeboDiplomaNumberSynchronizationService;
        this.studentDegreeService = studentDegreeService;
    }

    @PostMapping("/edebo-diploma-number-synchronization")
    public ResponseEntity diplomaNumberSynchronization(@RequestParam("file") MultipartFile uploadFile,
                                                       @CurrentUser ApplicationUser user) {
        try {
            validateInputDataForFileWithTheses(uploadFile);
            FormedListsWithDiplomaDataDTO listsWithDiplomaData = new FormedListsWithDiplomaDataDTO();
            EdeboDiplomaNumberSynchronizationReport edeboDiplomaNumberSynchronizationReport = edeboDiplomaNumberSynchronizationService.getEdeboDiplomaNumberSynchronizationReport(
                    uploadFile.getInputStream(),
                    user.getFaculty().getName()
            );

            List<DiplomaAndStudentSynchronizedDataDTO> diplomaAndStudentSynchronizedDataDTOs = map(
                    edeboDiplomaNumberSynchronizationReport.getDiplomaAndStudentSynchronizedDataGreen(),
                    DiplomaAndStudentSynchronizedDataDTO.class
            );
            List<MissingDataRedDTO> missingDataRedDTOs = map(
                    edeboDiplomaNumberSynchronizationReport.getMissingDataRed(),
                    MissingDataRedDTO.class
            );
            listsWithDiplomaData.setDiplomaAndStudentSynchronizedDataDTOs(diplomaAndStudentSynchronizedDataDTOs);
            listsWithDiplomaData.setMissingDataRedDTOs(missingDataRedDTOs);
            return ResponseEntity.ok(listsWithDiplomaData);
        } catch (Exception exception) {
            return handleException(exception);
        }

    }

    @PutMapping("/edebo-diploma-number-synchronization")
    public ResponseEntity diplomaNumberSaveChanges(@RequestBody DiplomaNumberDataForSaveDTO[] diplomaNumberDataForSaveDTOS,
                                                   @CurrentUser ApplicationUser user,
                                                   @RequestParam(required = false) Date diplomaDate,
                                                   @RequestParam(required = false) Date supplementDate) {
        try {
            Map<String, Object> savedDiploma = savedDiplomaData(diplomaNumberDataForSaveDTOS, diplomaDate, supplementDate);
            return ResponseEntity.ok(savedDiploma);
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private Map<String, Object> savedDiplomaData(DiplomaNumberDataForSaveDTO[] diplomaNumberDataForSaveDTOS,
                                                 @JsonFormat(pattern="yyyy-MM-dd", locale = "uk_UA", timezone = "EET") Date diplomaDate,
                                                 @JsonFormat(pattern="yyyy-MM-dd", locale = "uk_UA", timezone = "EET")  Date supplementDate) {
        int count = 0;
        List<String> notSavedDiplomaData = new ArrayList<>();
        for (DiplomaNumberDataForSaveDTO diplomaData : diplomaNumberDataForSaveDTOS) {
            try {
                studentDegreeService.updateDiplomaNumber(diplomaData.getId(), diplomaData.getDiplomaSeriesAndNumber(), diplomaData.isHonor(), diplomaDate, supplementDate);
                count++;
            } catch (Exception exception) {
                notSavedDiplomaData.add(diplomaData.getSurname() + " " + diplomaData.getName() + " " + diplomaData.getPatronimic());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("updatedDiplomaData", count);
        result.put("notUpdatedDiplomaData", notSavedDiplomaData);
        return result;
    }

    private void validateInputDataForFileWithTheses(MultipartFile uploadFile) throws OperationCannotBePerformedException {
        if (uploadFile.isEmpty()) {
            String message = "Файл не було надіслано";
            throw new OperationCannotBePerformedException(message);
        }
    }

    private ResponseEntity handleException(Exception exception) {
        return ExceptionHandlerAdvice.handleException(exception, SyncronizationController.class, ExceptionToHttpCodeMapUtil.map(exception));
    }
}
