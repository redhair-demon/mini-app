package com.example.miniapp;

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
    public MiniAppService.TaskResponse doTask(@RequestParam(defaultValue = "na-egorov") String name) {
        return service.doTask(name);
    }
}
