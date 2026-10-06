package br.com.alura.screenmatch.service;

import tools.jackson.databind.ObjectMapper;

public class ConverteDados implements IConverteDados{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T obterDados(String json, Class<T> tipoClasse) {
        try {
            return objectMapper.readValue(json, tipoClasse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
