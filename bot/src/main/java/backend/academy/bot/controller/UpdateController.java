package backend.academy.bot.controller;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/updates")
public class UpdateController {
    private final UpdateService updateService;

    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody @Valid LinkUpdate update) {
        updateService.sendUpdateToChats(update);
        return ResponseEntity.ok().build();
    }
}
