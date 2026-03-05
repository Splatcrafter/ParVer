package de.splatgames.software.external.afbb.parver.parking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {

    private static final Logger LOG = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L; // 5 minutes

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError(e -> this.emitters.remove(emitter));

        this.emitters.add(emitter);
        return emitter;
    }

    public void broadcast(final Object data) {
        for (final SseEmitter emitter : this.emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("parking-update")
                        .data(data));
            } catch (final IOException e) {
                LOG.debug("Failed to send SSE event, removing emitter: {}", e.getMessage());
                this.emitters.remove(emitter);
            }
        }
    }
}
