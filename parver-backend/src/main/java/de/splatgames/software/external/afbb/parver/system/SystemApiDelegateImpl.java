package de.splatgames.software.external.afbb.parver.system;

import de.splatgames.software.external.afbb.parver.api.SystemApiDelegate;
import de.splatgames.software.external.afbb.parver.model.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SystemApiDelegateImpl implements SystemApiDelegate {

    @Override
    public ResponseEntity<HealthResponse> getHealth() {
        return ResponseEntity.ok(new HealthResponse()
                .status("UP")
                .timestamp(OffsetDateTime.now()));
    }
}
