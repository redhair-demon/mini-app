package com.example.miniapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class MiniAppController {

    private final MiniAppService service;

    public MiniAppController(MiniAppService service) {
        this.service = service;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<MiniAppService.TaskResponse> doTask(@RequestParam(defaultValue = "na-egorov") String name) {
        return isValid(name) ? ResponseEntity.ok(service.doTask(name)) : ResponseEntity.badRequest().build();
    }

    private Boolean isValid(String name) {
        return name.matches("(\\w|\\d)+-(\\w|\\d)+");
    }
}
