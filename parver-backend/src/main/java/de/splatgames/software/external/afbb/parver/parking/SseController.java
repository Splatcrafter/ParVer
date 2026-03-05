package de.splatgames.software.external.afbb.parver.parking;

import de.splatgames.software.external.afbb.parver.model.ParkingSpace;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final ParkingSpotService parkingSpotService;

    public SseController(
            @NotNull final SseEmitterService sseEmitterService,
            @NotNull final ParkingSpotService parkingSpotService) {
        this.sseEmitterService = sseEmitterService;
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping(value = "/api/parking-spaces/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamParkingUpdates() {
        final SseEmitter emitter = this.sseEmitterService.createEmitter();

        // Send initial data
        try {
            final List<ParkingSpace> spaces = this.parkingSpotService.buildParkingSpacesResponse();
            emitter.send(SseEmitter.event()
                    .name("parking-update")
                    .data(spaces));
        } catch (final IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
