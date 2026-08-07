package ftn.iis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //dodati user not found ??
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .toList();

        return ResponseEntity
                .badRequest()
                .body(Map.of("errors", errors));
    }

    @ExceptionHandler(NonManagerCreatingSupplierException.class)
    public ResponseEntity<String> handleNonMenagerCreatingSupplierException(NonManagerCreatingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierPibAlreadyExists.class)
    public ResponseEntity<String> handleSupplierPibAlreadyExists(SupplierPibAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierEmailAlreadyExists.class)
    public ResponseEntity<String> handleSupplierEmailAlreadyExists(SupplierEmailAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierPhoneAlreadyExists.class)
    public ResponseEntity<String> handleSupplierPhoneAlreadyExists(SupplierPhoneAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierNameAlreadyExists.class)
    public ResponseEntity<String> handleSupplierNameAlreadyExists(SupplierNameAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerViewingSupplierException.class)
    public ResponseEntity<String> handleNonManagerViewingSupplierException(NonManagerViewingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NoSupplierFound.class)
    public ResponseEntity<String> handleNoSupplierFound(NoSupplierFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerUpdatingSupplierException.class)
    public ResponseEntity<String> handleNonMenagerUpdatingSupplierException(NonManagerUpdatingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerDeletingSupplierException.class)
    public ResponseEntity<String> handleNonMenagerDeletingSupplierException(NonManagerDeletingSupplierException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerCreatingContractException.class)
    public ResponseEntity<String> handleNonManagerCreatingContractException(NonManagerCreatingContractException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(SupplierNotActiveException.class)
    public ResponseEntity<String> handleSupplierNotActiveException(SupplierNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(ActiveContractAlreadyExistsException.class)
    public ResponseEntity<String> handleActiveContractAlreadyExistsException(ActiveContractAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidContractDateException.class)
    public ResponseEntity<String> handleInvalidContractDateException(InvalidContractDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NoContractFoundException.class)
    public ResponseEntity<String> handleNoContractFoundException(NoContractFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ContractNotActiveException.class)
    public ResponseEntity<String> handleContractNotActiveException(ContractNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidContractSaleException.class)
    public ResponseEntity<String> handleInvalidContractSaleException(InvalidContractSaleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NegativeDeliveryException.class)
    public ResponseEntity<String> handleNegativeDeliveryException(NegativeDeliveryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NonClanGivingSuggestions.class)
    public ResponseEntity<String> handleNonClanGivingSuggestions(NonClanGivingSuggestions ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonClanViewingSuggestions.class)
    public ResponseEntity<String> handleNonClanViewingSuggestions(NonClanViewingSuggestions ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerViewingSuggestionsException.class)
    public ResponseEntity<String> handleNonManagerViewingSuggestionsException(NonManagerViewingSuggestionsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonBiblotekarViewingSuggestionsException.class)
    public ResponseEntity<String> handleNonBiblotekarViewingSuggestionsException(NonBiblotekarViewingSuggestionsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonClanRecievingNotification.class)
    public ResponseEntity<String> handleNonClanRecievingNotification(NonClanRecievingNotification ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(NonManagerStartingAnalysisException.class)
    public ResponseEntity<String> handleNonManagerStartingAnalysisException(NonManagerStartingAnalysisException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(MaksimalnaDubinaKomentaraException.class)
    public ResponseEntity<String> handleMaksimalnaDubinaKomentaraException(MaksimalnaDubinaKomentaraException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ZanrNotFoundException.class)
    public ResponseEntity<String> handleZanrNotFoundException(ZanrNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
