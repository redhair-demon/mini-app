package com.example.miniapp;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MiniAppService {

    private final String baseURL = "http://193.19.100.32:7000/api";
    private final Map<String, Integer> testes;
    private final WebClient webClient;

    public MiniAppService() {
        this.testes = new HashMap<>();
        WebClient.Builder webClientBuilder = WebClient.builder();
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .baseUrl(baseURL)
                .build();
    }

    public TaskResponse doTask(String name) {
        testes.putIfAbsent(name, 0);

        String first_name = name.split("-")[0];
        String last_name = name.split("-")[1];
        String email = String.format("%s-%d@example.ru", name, testes.get(name));

        List<String> roles = getRoles();
        String myRole = roles.get(1); // backend Java

        String signUpResponse = signUp(last_name, first_name, email, myRole);

        String code = getCode(email).replaceAll("\"", "");

        String token = getToken(email, code);
        String setStatusResponse = setStatus(token, "increased");

        testes.put(name, testes.get(name) + 1);

        return new TaskResponse(last_name, first_name, email, code, token, String.format("%s\n%s", signUpResponse, setStatusResponse));
    }

    private List<String> getRoles() {
        Roles response = webClient
                .get()
                .uri("/get-roles")
                .retrieve()
                .bodyToMono(Roles.class)
                .block();
        assert response != null;
        return response.roles;
    }

    private String signUp(String last_name, String first_name, String email, String role) {
        Candidate candidate = new Candidate(last_name, first_name, email, role);
        return webClient
                .post()
                .uri("/sign-up")
                .bodyValue(candidate)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getCode(String email) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/get-code")
                        .queryParam("email", email)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String setStatus(String token, String status) {
        Status body = new Status(token, status);
        return webClient
                .post()
                .uri("/set-status")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private static String getToken(String email, String code) {
        return Base64.getEncoder().encodeToString(String.format("%s:%s", email, code).getBytes());
    }

    public static class Roles {
        public List<String> roles;
    }

    public static class Candidate {
        public String last_name;
        public String first_name;
        public String email;
        public String role;

        public Candidate(String last_name, String first_name, String email, String role) {
            this.last_name = last_name;
            this.first_name = first_name;
            this.email = email;
            this.role = role;
        }
    }

    public static class Status {
        public String token;
        public String status;

        public Status(String token, String status) {
            this.token = token;
            this.status = status;
        }
    }

    public static class TaskResponse {
        public String last_name;
        public String first_name;
        public String email;
        public String code;
        public String token;
        public String log;

        public TaskResponse(String last_name, String first_name, String email, String code, String token, String log) {
            this.last_name = last_name;
            this.first_name = first_name;
            this.email = email;
            this.code = code;
            this.token = token;
            this.log = log;
        }
    }

}
