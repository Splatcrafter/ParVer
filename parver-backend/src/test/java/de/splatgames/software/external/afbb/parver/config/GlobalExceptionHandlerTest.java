package de.splatgames.software.external.afbb.parver.config;

import de.splatgames.software.external.afbb.parver.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleValidation")
    class HandleValidation {

        @Test
        @DisplayName("Size constraint produces German message with min/max range")
        void sizeConstraint() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            // Size constraint: arguments[0]=codes, arguments[1]=max, arguments[2]=min
            bindingResult.addError(new FieldError("request", "username", null, false,
                    new String[]{"Size"}, new Object[]{"codes", 50, 3}, "size"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("Ungültige Eingabe");
            assertThat(response.getBody().getMessage())
                    .contains("Benutzername")
                    .contains("zwischen")
                    .contains("3")
                    .contains("50");
        }

        @Test
        @DisplayName("Size constraint with max >= MAX_VALUE produces 'mindestens' message")
        void sizeConstraintMinOnly() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "password", null, false,
                    new String[]{"Size"}, new Object[]{"codes", Integer.MAX_VALUE, 8}, "size"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage())
                    .contains("Passwort")
                    .contains("mindestens")
                    .contains("8");
        }

        @Test
        @DisplayName("NotBlank constraint produces 'darf nicht leer sein' message")
        void notBlank() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "displayName", null, false,
                    new String[]{"NotBlank"}, null, "must not be blank"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage())
                    .contains("Anzeigename")
                    .contains("darf nicht leer sein");
        }

        @Test
        @DisplayName("translates field names to German (username → Benutzername)")
        void fieldNameTranslation() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "username", null, false,
                    new String[]{"NotBlank"}, null, "must not be blank"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).startsWith("Benutzername");
        }

        @Test
        @DisplayName("unknown field name passes through unchanged")
        void unknownField() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "customField", null, false,
                    new String[]{"NotBlank"}, null, "must not be blank"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("customField");
        }

        @Test
        @DisplayName("unknown constraint code falls back to default message")
        void unknownConstraint() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "role", null, false,
                    new String[]{"CustomConstraint"}, null, "invalid value"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("Rolle").contains("invalid value");
        }

        @Test
        @DisplayName("multiple field errors are combined into single detail string")
        void multipleErrors() {
            final BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "username", null, false,
                    new String[]{"NotBlank"}, null, "must not be blank"));
            bindingResult.addError(new FieldError("request", "password", null, false,
                    new String[]{"NotBlank"}, null, "must not be blank"));

            final MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(null, bindingResult);
            final ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage())
                    .contains("Benutzername")
                    .contains("Passwort");
        }
    }

    @Nested
    @DisplayName("handleUnreadable")
    class HandleUnreadable {

        @Test
        @DisplayName("returns 400 with German error message")
        void returns400() {
            final HttpMessageNotReadableException ex =
                    new HttpMessageNotReadableException("Could not read JSON");

            final ResponseEntity<ErrorResponse> response = handler.handleUnreadable(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("Ungültige Anfrage");
            assertThat(response.getBody().getMessage())
                    .contains("konnte nicht verarbeitet werden");
            assertThat(response.getBody().getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("handleGeneral")
    class HandleGeneral {

        @Test
        @DisplayName("returns 500 with German error message")
        void returns500() {
            final Exception ex = new RuntimeException("Unexpected error");

            final ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("Serverfehler");
            assertThat(response.getBody().getMessage())
                    .contains("unerwarteter Fehler");
            assertThat(response.getBody().getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("handles NullPointerException correctly")
        void npe() {
            final ResponseEntity<ErrorResponse> response =
                    handler.handleGeneral(new NullPointerException());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
        }
    }
}
