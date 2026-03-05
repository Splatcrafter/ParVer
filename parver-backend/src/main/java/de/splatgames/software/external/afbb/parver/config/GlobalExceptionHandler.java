package de.splatgames.software.external.afbb.parver.config;

import de.splatgames.software.external.afbb.parver.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<String, String> FIELD_NAMES = Map.of(
            "username", "Benutzername",
            "displayName", "Anzeigename",
            "password", "Passwort",
            "role", "Rolle"
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException ex) {
        final String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    final String fieldName = FIELD_NAMES.getOrDefault(fe.getField(), fe.getField());
                    final String code = fe.getCode();
                    if ("Size".equals(code) || "Length".equals(code)) {
                        final Object min = fe.getArguments()[2];
                        final Object max = fe.getArguments()[1];
                        if (max instanceof Integer && (Integer) max >= Integer.MAX_VALUE) {
                            return fieldName + " muss mindestens " + min + " Zeichen lang sein.";
                        }
                        return fieldName + " muss zwischen "
                                + min + " und " + max + " Zeichen lang sein.";
                    }
                    if ("MinLength".equals(code) || "Min".equals(code)) {
                        return fieldName + " muss mindestens "
                                + fe.getArguments()[1] + " Zeichen lang sein.";
                    }
                    if ("NotBlank".equals(code) || "NotNull".equals(code) || "NotEmpty".equals(code)) {
                        return fieldName + " darf nicht leer sein.";
                    }
                    return fieldName + ": " + fe.getDefaultMessage();
                })
                .reduce((a, b) -> a + " " + b)
                .orElse("Validierung fehlgeschlagen.");
        LOG.debug("Validation error: {}", detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Ungültige Eingabe",
                        detail,
                        OffsetDateTime.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(final HttpMessageNotReadableException ex) {
        LOG.debug("Unreadable request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Ungültige Anfrage",
                        "Die Anfrage konnte nicht verarbeitet werden. Bitte überprüfen Sie Ihre Eingaben.",
                        OffsetDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(final Exception ex) {
        LOG.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Serverfehler",
                        "Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.",
                        OffsetDateTime.now()));
    }
}
